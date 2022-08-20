package com.planetbiru.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import com.planetbiru.util.OSUtil.OS;

public class FileConfigUtil {
	private FileConfigUtil()
	{
		
	}
	
	public static byte[] read(String fileName) throws FileNotFoundException
	{
		byte[] allBytes = null;
		try 
		(
				InputStream inputStream = new FileInputStream(fileName);
		) 
		{
			File resource = new File(fileName);		
			long fileSize = resource.length();
			allBytes = new byte[(int) fileSize];
			int length = inputStream.read(allBytes);
			if(length == 0)
			{
				allBytes = null;
			}
		 } 
		 catch (IOException ex) 
		 {
			 throw new FileNotFoundException(ex);
		 }
		 return allBytes;
	}
	
	public static void write(String fileName, byte[] data) throws IOException
	{
		String dirName = FileConfigUtil.getParentName(fileName);
		dirName = FileConfigUtil.fixFileName(dirName);
		File file = new File(dirName);
		if(!file.exists())
		{
			file.mkdirs();
		}
		try 
		(
			OutputStream os = new FileOutputStream(fileName);
		)
		{
		    final PrintStream printStream = new PrintStream(os);
		    printStream.write(data);
		    printStream.close();
		}
		catch (IOException ex) 
		{
			throw new IOException(ex);
		}
	}
	
	public static String getDirSeparator() {
		if(OSUtil.getOS().equals(OS.WINDOWS))
		{
			return "/";
		}
		else
		{
			return "\\";
		}
	}

	public static String removeParentWithDot(String path) {
		path = path.replace("/../", "/");
		path = path.replace("\\..\\", "\\");
		return path;
	}
	
	public static String fixFileName(String fileName) {
		if(OSUtil.getOS().equals(OS.WINDOWS))
		{
			fileName = fileName.replace("/", "\\");
			fileName = fileName.replace("\\\\", "\\");
			fileName = fileName.replace("..\\", "");
			fileName = fileName.replace("\\..", "");
		}
		else
		{
			fileName = fileName.replace("\\", "/");		
			fileName = fileName.replace("//", "/");
			fileName = fileName.replace("../", "");
			fileName = fileName.replace("/..", "");
		}
		return fileName;
	}
	
	public static String getFileExtension(String fileName) 
	{
		String extension = fileName;
		int index = fileName.lastIndexOf('.');
		if (index > 0) {
		      extension = fileName.substring(index + 1);
		}
		return extension;
	}
	
	public static void prepareDir(String fileName)
	{
		File file = new File(fileName);
		String directory1 = file.getParent();
		File file2 = new File(directory1);
		String directory2 = file2.getParent();
		
		File d1 = new File(directory1);
		File d2 = new File(directory2);		

		if(!d2.exists())
		{
			d2.mkdir();
		}
		if(!d1.exists())
		{
			d1.mkdir();
		}
	}
	
	public static String getFolderName(String fullPath)
	{
		String folder = "";
		if(fullPath.contains("\\"))
		{
			fullPath = fullPath.replace("\\", "/");
		}
		if(fullPath.contains("/"))
		{			
			String[] arr = fullPath.split("/");
			folder = arr[arr.length - 2];
		}
		folder = folder.replace("/", "");
		folder = folder.replace("\\", "");
		return folder;
	}
	
	public static String getParentName(String fullPath)
	{
		if(fullPath.contains("/") || fullPath.contains("\\"))
		{
			String baseName = FileConfigUtil.getBaseName(fullPath);
			return fullPath.substring(0, fullPath.length() - baseName.length() - 1);
		}
		else
		{
			return FileConfigUtil.getDirSeparator();
		}
	}
	
	public static String getBaseName(String fullPath)
	{
		String name = fullPath;
		if(fullPath.contains("\\"))
		{
			fullPath = fullPath.replace("\\", "/");
		}
		if(fullPath.contains("/"))
		{			
			String[] arr = fullPath.split("/");
			name = arr[arr.length - 1];
		}
		return name;
	}
	
	public static void deleteDirectoryWalkTree(Path path) throws IOException {
		FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {	             
			@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}
	
			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}
	
			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				if (exc != null) 
				{
					throw exc;
				}
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		};
		Files.walkFileTree(path, visitor);
	}

}
