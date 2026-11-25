package com.planetbiru.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class SQLiteDataStore {
	private static Connection connection;
	private static Path storageRoot;

	private SQLiteDataStore()
	{
	}

	public static synchronized void initialize(File root) throws IOException, SQLException {
		storageRoot = root.getCanonicalFile().toPath();
		Files.createDirectories(storageRoot);
		File database = storageRoot.resolve("otp-pi.sqlite").toFile();
		try
		{
			Class.forName("org.sqlite.JDBC");
		}
		catch(ClassNotFoundException e)
		{
			throw new SQLException("SQLite JDBC driver is unavailable", e);
		}
		connection = DriverManager.getConnection("jdbc:sqlite:" + database.getAbsolutePath());
		try(Statement statement = connection.createStatement())
		{
			statement.execute("PRAGMA busy_timeout = 5000");
			statement.execute("PRAGMA journal_mode = WAL");
			statement.execute("CREATE TABLE IF NOT EXISTS file_data ("
					+ "path TEXT PRIMARY KEY, content BLOB, updated_at INTEGER NOT NULL, "
					+ "deleted INTEGER NOT NULL DEFAULT 0)");
			statement.execute("CREATE TABLE IF NOT EXISTS metadata ("
					+ "key TEXT PRIMARY KEY, value TEXT NOT NULL)");
			statement.execute("CREATE TABLE IF NOT EXISTS otp_log ("
					+ "id INTEGER PRIMARY KEY AUTOINCREMENT, otp_id TEXT NOT NULL, receiver TEXT NOT NULL, "
					+ "otp_hash TEXT NOT NULL, event TEXT NOT NULL, outcome TEXT NOT NULL, "
					+ "created_at INTEGER NOT NULL, expires_at INTEGER NOT NULL)");
			statement.execute("CREATE TABLE IF NOT EXISTS sms_log ("
					+ "id INTEGER PRIMARY KEY AUTOINCREMENT, created_at INTEGER NOT NULL, modem_id TEXT NOT NULL, "
					+ "sender TEXT NOT NULL, receiver TEXT NOT NULL, message_length INTEGER NOT NULL, "
					+ "status TEXT NOT NULL, error_code TEXT NOT NULL)");
			statement.execute("CREATE TABLE IF NOT EXISTS email_log ("
					+ "id INTEGER PRIMARY KEY AUTOINCREMENT, created_at INTEGER NOT NULL, account_id TEXT NOT NULL, "
					+ "sender TEXT NOT NULL, receiver TEXT NOT NULL, subject_length INTEGER NOT NULL, "
					+ "message_length INTEGER NOT NULL, status TEXT NOT NULL, error_type TEXT NOT NULL)");
		}
		migrateLegacyFiles();
		migrateLegacySmsLogs();
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				close();
			}
		}));
	}

	public static synchronized String getStorageKey(String fileName) {
		if(connection == null)
		{
			return null;
		}
		try
		{
			Path filePath = new File(fileName).getCanonicalFile().toPath();
			if(!filePath.startsWith(storageRoot) || filePath.equals(storageRoot))
			{
				return null;
			}
			String key = storageRoot.relativize(filePath).toString().replace(File.separatorChar, '/');
			String normalizedKey = key.toLowerCase(Locale.ROOT);
			if(!(normalizedKey.startsWith("resource/data/")
					|| normalizedKey.startsWith("resource/session/")
					|| normalizedKey.startsWith("resource/config/")))
			{
				return null;
			}
			return key;
		}
		catch(IOException e)
		{
			throw new IllegalStateException("Unable to resolve application data path: " + fileName, e);
		}
	}

	public static synchronized boolean contains(String path) throws SQLException {
		try(PreparedStatement statement = connection.prepareStatement(
				"SELECT 1 FROM file_data WHERE path = ?"))
		{
			statement.setString(1, path);
			try(ResultSet result = statement.executeQuery())
			{
				return result.next();
			}
		}
	}

	public static synchronized byte[] read(String path) throws SQLException {
		try(PreparedStatement statement = connection.prepareStatement(
				"SELECT content, deleted FROM file_data WHERE path = ?"))
		{
			statement.setString(1, path);
			try(ResultSet result = statement.executeQuery())
			{
				if(result.next() && result.getInt("deleted") == 0)
				{
					return result.getBytes("content");
				}
			}
		}
		return null;
	}

	public static synchronized void write(String path, byte[] content) throws SQLException {
		write(path, content, System.currentTimeMillis());
	}

	private static synchronized void write(String path, byte[] content, long modified) throws SQLException {
		try(PreparedStatement statement = connection.prepareStatement(
				"INSERT INTO file_data(path, content, updated_at, deleted) VALUES(?, ?, ?, 0) "
						+ "ON CONFLICT(path) DO UPDATE SET content = excluded.content, "
						+ "updated_at = excluded.updated_at, deleted = 0"))
		{
			statement.setString(1, path);
			statement.setBytes(2, content);
			statement.setLong(3, modified);
			statement.executeUpdate();
		}
	}

	public static synchronized void append(String path, byte[] content) throws SQLException {
		byte[] existing = read(path);
		byte[] combined = new byte[(existing == null ? 0 : existing.length) + content.length];
		if(existing != null)
		{
			System.arraycopy(existing, 0, combined, 0, existing.length);
		}
		System.arraycopy(content, 0, combined, existing == null ? 0 : existing.length, content.length);
		write(path, combined);
	}

	public static synchronized void delete(String path) throws SQLException {
		try(PreparedStatement statement = connection.prepareStatement(
				"INSERT INTO file_data(path, content, updated_at, deleted) VALUES(?, NULL, ?, 1) "
						+ "ON CONFLICT(path) DO UPDATE SET content = NULL, "
						+ "updated_at = excluded.updated_at, deleted = 1"))
		{
			statement.setString(1, path);
			statement.setLong(2, System.currentTimeMillis());
			statement.executeUpdate();
		}
	}

	public static synchronized void logOtp(String otpID, String receiver, String otpHash, String event,
			String outcome, long createdAt, long expiresAt) throws SQLException {
		ensureConnected();
		try(PreparedStatement statement = connection.prepareStatement(
				"INSERT INTO otp_log(otp_id, receiver, otp_hash, event, outcome, created_at, expires_at) "
						+ "VALUES(?, ?, ?, ?, ?, ?, ?)"))
		{
			statement.setString(1, otpID);
			statement.setString(2, receiver);
			statement.setString(3, otpHash);
			statement.setString(4, event);
			statement.setString(5, outcome);
			statement.setLong(6, createdAt);
			statement.setLong(7, expiresAt);
			statement.executeUpdate();
		}
	}

	public static synchronized void logSms(long createdAt, String modemID, String sender, String receiver,
			int messageLength, String status, String errorCode) throws SQLException {
		ensureConnected();
		try(PreparedStatement statement = connection.prepareStatement(
				"INSERT INTO sms_log(created_at, modem_id, sender, receiver, message_length, status, error_code) "
						+ "VALUES(?, ?, ?, ?, ?, ?, ?)"))
		{
			statement.setLong(1, createdAt);
			statement.setString(2, modemID);
			statement.setString(3, sender);
			statement.setString(4, receiver);
			statement.setInt(5, messageLength);
			statement.setString(6, status);
			statement.setString(7, errorCode);
			statement.executeUpdate();
		}
	}

	public static synchronized void logEmail(long createdAt, String accountID, String sender, String receiver,
			int subjectLength, int messageLength, String status, String errorType) throws SQLException {
		ensureConnected();
		try(PreparedStatement statement = connection.prepareStatement(
				"INSERT INTO email_log(created_at, account_id, sender, receiver, subject_length, "
						+ "message_length, status, error_type) VALUES(?, ?, ?, ?, ?, ?, ?, ?)"))
		{
			statement.setLong(1, createdAt);
			statement.setString(2, accountID);
			statement.setString(3, sender);
			statement.setString(4, receiver);
			statement.setInt(5, subjectLength);
			statement.setInt(6, messageLength);
			statement.setString(7, status);
			statement.setString(8, errorType);
			statement.executeUpdate();
		}
	}

	public static synchronized JSONArray listFiles(String directory) throws SQLException, IOException {
		Path directoryPath = new File(directory).getCanonicalFile().toPath();
		if(!directoryPath.startsWith(storageRoot))
		{
			return new JSONArray();
		}
		String key = storageRoot.relativize(directoryPath).toString().replace(File.separatorChar, '/');
		if(!key.isEmpty())
		{
			key += "/";
		}
		migrateDirectory(directoryPath);
		Map<String, DirectoryEntry> entries = new HashMap<>();
		try(PreparedStatement statement = connection.prepareStatement(
				"SELECT path, length(content) AS size, updated_at FROM file_data "
						+ "WHERE deleted = 0 AND substr(path, 1, ?) = ?"))
		{
			statement.setInt(1, key.length());
			statement.setString(2, key);
			try(ResultSet result = statement.executeQuery())
			{
				while(result.next())
				{
					String remaining = result.getString("path").substring(key.length());
					if(remaining.isEmpty())
					{
						continue;
					}
					String[] parts = remaining.split("/");
					String entryKey = parts[0];
					DirectoryEntry entry = entries.get(entryKey);
					if(entry == null)
					{
						entry = new DirectoryEntry(entryKey);
						entries.put(entryKey, entry);
					}
					if(parts.length == 1)
					{
						entry.directory = false;
						entry.size = result.getLong("size");
						entry.modified = result.getLong("updated_at");
					}
					else
					{
						entry.directory = true;
						entry.modified = Math.max(entry.modified, result.getLong("updated_at"));
					}
				}
			}
		}
		JSONArray files = new JSONArray();
		for(DirectoryEntry entry : entries.values())
		{
			JSONObject item = new JSONObject();
			item.put("name", entry.name);
			item.put("type", entry.directory ? "dir" : "file");
			item.put("modified", entry.modified);
			if(entry.directory)
			{
				item.put("child", listFiles(new File(directory, entry.name).getPath()));
			}
			else
			{
				item.put("size", entry.size);
			}
			files.put(item);
		}
		return files;
	}

	private static synchronized void migrateLegacyFiles() throws IOException, SQLException {
		if(containsMetadata("legacy_files_migrated"))
		{
			return;
		}
		String[] directories = {"resource/data", "resource/session", "resource/config"};
		for(String directory : directories)
		{
			migrateDirectory(storageRoot.resolve(directory));
		}
		try(PreparedStatement statement = connection.prepareStatement(
				"INSERT INTO metadata(key, value) VALUES('legacy_files_migrated', '1')"))
		{
			statement.executeUpdate();
		}
	}

	private static synchronized void migrateLegacySmsLogs() throws SQLException {
		if(containsMetadata("legacy_sms_logs_migrated"))
		{
			return;
		}
		boolean originalAutoCommit = connection.getAutoCommit();
		connection.setAutoCommit(false);
		List<LegacySmsFile> legacyFiles = new ArrayList<>();
		try
		{
			try(PreparedStatement select = connection.prepareStatement(
					"SELECT path, content FROM file_data WHERE deleted = 0 "
							+ "AND path LIKE 'resource/data/report/%.csv'");
					ResultSet files = select.executeQuery())
			{
				while(files.next())
				{
					legacyFiles.add(new LegacySmsFile(files.getString("path"), files.getBytes("content")));
				}
			}
			try(PreparedStatement insert = connection.prepareStatement(
					"INSERT INTO sms_log(created_at, modem_id, sender, receiver, message_length, status, error_code) "
							+ "VALUES(?, ?, ?, ?, ?, 'legacy', '')"))
			{
				for(LegacySmsFile legacyFile : legacyFiles)
				{
					if(legacyFile.content == null)
					{
						continue;
					}
					String content = new String(legacyFile.content, StandardCharsets.UTF_8);
					String[] lines = content.split("\\r?\\n");
					for(String line : lines)
					{
						String[] columns = line.split(",", 5);
						if(columns.length != 5)
						{
							continue;
						}
						try
						{
							insert.setLong(1, Long.parseLong(columns[0]));
							insert.setString(2, columns[1]);
							insert.setString(3, columns[2]);
							insert.setString(4, columns[3]);
							insert.setInt(5, Integer.parseInt(columns[4]));
							insert.executeUpdate();
						}
						catch(NumberFormatException e)
						{
							System.err.println("Skipping malformed legacy SMS log row in " + legacyFile.path);
						}
					}
				}
			}
			try(PreparedStatement markMigrated = connection.prepareStatement(
					"INSERT INTO metadata(key, value) VALUES('legacy_sms_logs_migrated', '1')"))
			{
				markMigrated.executeUpdate();
			}
			connection.commit();
		}
		catch(SQLException e)
		{
			try
			{
				connection.rollback();
			}
			catch(SQLException rollbackError)
			{
				e.addSuppressed(rollbackError);
			}
			throw e;
		}
		finally
		{
			connection.setAutoCommit(originalAutoCommit);
		}
	}

	private static void ensureConnected() throws SQLException {
		if(connection == null || connection.isClosed())
		{
			throw new SQLException("SQLite data store is not initialized");
		}
	}

	private static synchronized boolean containsMetadata(String key) throws SQLException {
		try(PreparedStatement statement = connection.prepareStatement(
				"SELECT 1 FROM metadata WHERE key = ?"))
		{
			statement.setString(1, key);
			try(ResultSet result = statement.executeQuery())
			{
				return result.next();
			}
		}
	}

	private static synchronized void migrateDirectory(final Path directory) throws IOException, SQLException {
		if(!Files.isDirectory(directory))
		{
			return;
		}
		Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
				if(!attributes.isRegularFile())
				{
					return FileVisitResult.CONTINUE;
				}
				try
				{
					Path canonicalFile = file.toRealPath();
					if(!canonicalFile.startsWith(storageRoot))
					{
						return FileVisitResult.CONTINUE;
					}
					String key = storageRoot.relativize(canonicalFile).toString().replace(File.separatorChar, '/');
					if(!contains(key))
					{
						write(key, Files.readAllBytes(file), attributes.lastModifiedTime().toMillis());
					}
				}
				catch(SQLException e)
				{
					throw new IOException("Unable to migrate legacy file " + file, e);
				}
				return FileVisitResult.CONTINUE;
			}
		});
	}

	private static synchronized void close() {
		if(connection != null)
		{
			try
			{
				connection.close();
			}
			catch(SQLException e)
			{
				System.err.println("Unable to close SQLite database: " + e.getMessage());
			}
			connection = null;
		}
	}

	private static class DirectoryEntry {
		private final String name;
		private boolean directory;
		private long size;
		private long modified;

		DirectoryEntry(String name) {
			this.name = name;
		}
	}

	private static class LegacySmsFile {
		private final String path;
		private final byte[] content;

		LegacySmsFile(String path, byte[] content) {
			this.path = path;
			this.content = content;
		}
	}
}
