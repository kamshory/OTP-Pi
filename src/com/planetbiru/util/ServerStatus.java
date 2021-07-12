package com.planetbiru.util;

import java.io.File;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ServerStatus {
	private static JSONArray status = new JSONArray();
	private static int maxRecord = 1000;
	private static String configPath = "";
	private ServerStatus()
	{
		
	}
	
	public static void load(String path)
	{
		ServerStatus.configPath = path;
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		
		try 
		{
			byte[] data = FileConfigUtil.read(fileName);		
			if(data != null)
			{
				String text = new String(data);
				if(text.length() > 7)
				{
					JSONArray json = new JSONArray(text);
					ServerStatus.status = json;
				}
			}
		}
		catch(JSONException | FileNotFoundException e)
		{
			/**
			 * Do nothing
			 */
		}
	}
	
	public static JSONArray load(long from, long to)
	{
		String path = ServerStatus.configPath;
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		
		JSONArray json = new JSONArray();
		try 
		{
			byte[] data = FileConfigUtil.read(fileName);		
			if(data != null)
			{
				String text = new String(data);
				if(text.length() > 7)
				{
					JSONArray buff = new JSONArray(text);
					json = ServerStatus.filter(buff, from, to);
				}
			}
		}
		catch(JSONException | FileNotFoundException e)
		{
			/**
			 * Do nothing
			 */
		}
		return json;
	}
	
	
	private static JSONArray filter(JSONArray buff, long from, long to) {
		JSONArray json = new JSONArray();
		if(to == 0)
		{
			to = System.currentTimeMillis(); 
		}
		for(int i = 0; i<buff.length(); i++)
		{
			JSONObject jo = buff.getJSONObject(i);
			if(jo != null)
			{
				long datetime = jo.optLong("datetime", 0);
				if(datetime >= from && datetime <= to)
				{
					json.put(jo);
				}	
			}
		}
		return json;
	}

	public static void append(JSONObject data)
	{
		JSONArray ja = new JSONArray();
		int lastLength = ServerStatus.getStatus().length();
		int start = 0;
		if(lastLength >= maxRecord)
		{
			start = lastLength - maxRecord + 1;
		}
		else
		{
			start = 0;
		}
		for(int i = start; i<lastLength; i++)
		{
			ja.put(ServerStatus.getStatus().get(i));
		}
		ja.put(data);
		ServerStatus.status = ja;
	}
	
	public static JSONArray getStatus() {
		return status;
	}
	
	public static void save() {
		ServerStatus.save(ServerStatus.configPath);
	}
	
	public static void save(String path) {
		JSONArray config = ServerStatus.status;
		ServerStatus.save(path, config);
	}

	public static void save(String path, JSONArray config) {
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		ServerStatus.prepareDir(fileName);
		
		try 
		{
			FileConfigUtil.write(fileName, config.toString().getBytes());
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	
	private static void prepareDir(String fileName) {
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

}
