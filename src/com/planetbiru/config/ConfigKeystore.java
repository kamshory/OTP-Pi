package com.planetbiru.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStoreException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.Utility;

public class ConfigKeystore {
	private static String configPath = "";
	
	private static Map<String, DataKeystore> keystores = new HashMap<>();
	
	private static Logger logger = Logger.getLogger(ConfigKeystore.class);
	
	private ConfigKeystore()
	{
		
	}
	
	public static void update(String key, JSONObject data)
	{
		ConfigKeystore.keystores.put(key, new DataKeystore(data));
	}
	public static void add(JSONObject data) {
		String id = data.optString("id", "");
		if(!id.isEmpty())
		{
			ConfigKeystore.keystores.put(id, new DataKeystore(data));
		}
	}
	public static DataKeystore get(String key)
	{
		return ConfigKeystore.keystores.getOrDefault(key, new DataKeystore());
	}
	
	public static void load(String path) {
		ConfigKeystore.configPath = path;
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		fileName = FileConfigUtil.fixFileName(fileName);
		try 
		{
			byte[] data = FileConfigUtil.read(fileName);		
			if(data != null)
			{
				String text = new String(data);
				if(text.length() > 7)
				{
					ConfigKeystore.keystores = new HashMap<>();
					JSONObject json = new JSONObject(text);
					JSONArray keys = json.names();
					for(int i = 0; i<keys.length(); i++)
					{
						String id = keys.optString(i);
						JSONObject keystore = json.optJSONObject(id);
						ConfigKeystore.keystores.put(id, new DataKeystore(keystore));
					}
				}
			}
		} 
		catch (FileNotFoundException | JSONException e) 
		{
			if(Config.isLogConfigNotFound())
			{
				logger.error(e.getMessage(), e);
			}
		}	
	}	
	
	public static void save()
	{
		ConfigKeystore.save(ConfigKeystore.configPath);
	}
	
	public static void save(String path) {
		JSONObject config = getJSONObject();
		save(path, config);
	}

	public static void save(String path, JSONObject config) {		
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		fileName = FileConfigUtil.fixFileName(fileName);
		prepareDir(fileName);
		
		try 
		{
			FileConfigUtil.write(fileName, config.toString().getBytes());
		}
		catch (IOException e) 
		{
			logger.error(e.getMessage(), e);
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
		File file3 = new File(fileName);
		file3.getParentFile().mkdirs();
	}
	
	public static JSONObject getJSONObject() {
		JSONObject config = new JSONObject();
		for (Map.Entry<String, DataKeystore> set : ConfigKeystore.keystores.entrySet()) 
		{
			 config.put(set.getKey(), ((DataKeystore) set.getValue()).toJSONObject());
        }
		return config;
	}

	public static JSONObject toJSONObject() {
		return getJSONObject();
	}

	public static void remove(String value) {	
		if(ConfigKeystore.keystores.containsKey(value))
		{
			ConfigKeystore.keystores.remove(value);
		}		
	}

	public static void deactivate(String value) {
		if(ConfigKeystore.keystores.containsKey(value))
		{
			ConfigKeystore.keystores.get(value).setActive(false);
		}		
	}

	public static void activate(String value) {
		if(ConfigKeystore.keystores.containsKey(value))
		{
			ConfigKeystore.keystores.get(value).setActive(true);
		}		
	}

	public static void writeFile(String keystoreDataSettingPath, String id, byte[] binaryData) {
		String path = id;
		String dir = Utility.getBaseDir() + "/" + keystoreDataSettingPath;
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		if(!dir.endsWith("/") && !path.startsWith("/"))
		{
			dir = dir + "/";
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		fileName = FileConfigUtil.fixFileName(fileName);
		prepareDir(fileName);
		try 
		{
			FileConfigUtil.write(fileName, binaryData);
		}
		catch (IOException e) 
		{
			logger.error(e.getMessage(), e);
		}		
	}

	public static DataKeystore getActiveKeystore() throws KeyStoreException {
		for (Entry<String, DataKeystore> entry : ConfigKeystore.keystores.entrySet()) {
			if(entry.getValue().isActive())
			{
				return entry.getValue();
			}
	    }
		throw new KeyStoreException("No active keystore found");
	}

	public static void reset() {
		for (Map.Entry<String, DataKeystore> set : ConfigKeystore.keystores.entrySet()) 
		{
			String filePath = set.getValue().getFullPath();
			File file = new File(filePath);
			Path path = file.toPath();
			try {
				Files.delete(path);
			} catch (IOException e) {
				logger.error(e.getMessage());
			} 	
		}
		ConfigKeystore.keystores = new HashMap<>();
		
	}
	
}
