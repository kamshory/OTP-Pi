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
import java.util.regex.Pattern;

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
			 throw new FileNotFoundException(ex.getMessage());
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
			return "\\";
		}
		else
		{
			return "/";
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
	
	/**
	 * Prepare directory before save a file
	 * @param fileName File path to be save after directory created
	 */
	@Deprecated
	public static void prepareDir(String fileName)
	{
		String parent1 = getParentName(fileName);
		String parent2 = getParentName(parent1);
		String parent3 = getParentName(parent2);
		String parent4 = getParentName(parent3);
		String parent5 = getParentName(parent4);
		
		File d1 = new File(parent4);
		if(!d1.exists())
		{
			d1.mkdirs();
		}

		File d0 = new File(parent5);
		if(!d0.exists())
		{
			d0.mkdirs();
		}

		File d2 = new File(parent3);
		if(!d2.exists())
		{
			d2.mkdirs();
		}		

		File d3 = new File(parent2);
		if(!d3.exists())
		{
			d3.mkdirs();
		}
		
		File d4 = new File(parent1);
		if(!d4.exists())
		{
			d4.mkdirs();
		}
		
	}
	
	public static void prepareDirectory(String fileName)
	{
		String separatorDir = FileConfigUtil.getDirSeparator();

		String[] arr;
		if(OSUtil.getOS().equals(OS.WINDOWS))
		{
			arr = fileName.split(Pattern.quote("\\"));
		}
		else
		{
			arr = fileName.split("/");
		}
		StringBuilder bld = new StringBuilder();
		for(int i = 0; i<arr.length - 1; i++)
		{
			bld.append(arr[i]);
			String dir = bld.toString();
			bld.append(separatorDir);
			File d = new File(dir);
			if(!d.exists())
			{
				d.mkdirs();
			}
			
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
			if(arr.length > 0)
			{
				name = arr[arr.length - 1];
			}
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
