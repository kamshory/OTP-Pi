package com.planetbiru.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.planetbiru.config.Config;

public class FileUtil {
	private FileUtil()
	{
		
	}
	private static Map<String, String> contentCache = new HashMap<>();
	
	public static byte[] readResource(String fileName) throws FileNotFoundException {
		boolean cache = false;
		if(fileName.endsWith(".html") && Config.isCacheHTMLFile())
		{
			cache = true;
		}
		return FileUtil.readResource(fileName, cache);
	}
	
	public static byte[] readResource(String fileName, boolean cache) throws FileNotFoundException
	{
		if(cache && contentCache.containsKey(fileName))
		{
			return contentCache.get(fileName).getBytes();
		}
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
			else if(cache)
			{
				contentCache.put(fileName, new String(allBytes));
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
	
	public static JSONArray listFile(File directory)
    {
		JSONArray files = new JSONArray();
	    File[] list = directory.listFiles();
        if(list != null)
        {
	        for(File file : list)
	        {
	            if(file.isDirectory())
	            {
	            	JSONObject obj = new JSONObject();
	            	JSONArray list2 = listFile(file);
	            	obj.put("name", file.getName());
	            	obj.put("type", "dir");
	            	obj.put("child", list2);
	            	obj.put("modified", file.lastModified());
	            	JSONArray ja = new JSONArray();
	            	ja.put(obj);
	            	files.put(obj);
	            }
	            else 
	            {
	            	JSONObject obj = new JSONObject();
	            	obj.put("type", "file");
	            	obj.put("name", file.getName());
	            	obj.put("size", file.length());
	            	obj.put("modified", file.lastModified());
	            	files.put(obj);
	            }
	        }
        }
        return files;
    }

	
}

