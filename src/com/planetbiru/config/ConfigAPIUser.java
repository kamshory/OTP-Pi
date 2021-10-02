package com.planetbiru.config;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.user.User;
import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.FileUtil;
import com.planetbiru.util.Utility;

public class ConfigAPIUser {

	private static Map<String, User> users = new HashMap<>();
	private static String configPath = "";	
	private static Logger logger = Logger.getLogger(ConfigAPIUser.class);
	
	private ConfigAPIUser()
	{
		
	}
	
	public static void deleteUser(String value) {
		if(ConfigAPIUser.users.containsKey(value))
		{
			ConfigAPIUser.users.remove(value);
		}		
	}
	
	public static void activate(String value) {
		if(ConfigAPIUser.users.containsKey(value))
		{
			ConfigAPIUser.users.get(value).setActive(true);
		}		
	}
	
	public static void deactivate(String value) {
		if(ConfigAPIUser.users.containsKey(value))
		{
			ConfigAPIUser.users.get(value).setActive(false);
		}
		
	}
	
	public static void block(String value) {
		if(ConfigAPIUser.users.containsKey(value))
		{
			ConfigAPIUser.users.get(value).setBlocked(true);
		}
		
	}
	
	public static void unblock(String value) {
		if(ConfigAPIUser.users.containsKey(value))
		{
			ConfigAPIUser.users.get(value).setBlocked(false);
		}	
	}
	
	public static void load(String path)
	{
		ConfigAPIUser.configPath = path;
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		ConfigAPIUser.prepareDir(fileName);
		try 
		{
			byte[] data = FileConfigUtil.read(fileName);
			if(data != null)
			{
				String text = new String(data);
				JSONObject jsonObject = new JSONObject(text);
				Iterator<String> keys = jsonObject.keys();
				while(keys.hasNext()) {
				    String username = keys.next();
				    JSONObject user = jsonObject.optJSONObject(username);
				    ConfigAPIUser.addUser(username, user);
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
		ConfigAPIUser.save(ConfigAPIUser.configPath);
	}
	
	public static void save(String path)
	{
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		String userData = ConfigAPIUser.toJSONObject().toString();
		try 
		{
			FileUtil.write(fileName, userData.getBytes());
		} 
		catch (IOException e) 
		{
			/**
			 * Do nothing
			 */
		}
	}
	
	private static void prepareDir(String fileName) 
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
	
	public static void updateUser(User user) {
		String username = user.getUsername();
		ConfigAPIUser.users.put(username, user);
		
	}	
	
	public static void update(String text) {
		if(text != null)
		{
			ConfigAPIUser.users = new HashMap<>();
			JSONObject jsonObject = new JSONObject(text);
			Iterator<String> keys = jsonObject.keys();
			while(keys.hasNext()) {
			    String username = keys.next();
			    JSONObject user = jsonObject.optJSONObject(username);
			    ConfigAPIUser.addUser(username, user);
			}
		}	
	}
	
	public static void addUser(User user)
	{
		ConfigAPIUser.users.put(user.getUsername(), user);
	}
	
	public static void addUser(String username, JSONObject jsonObject) 
	{
		User user = new User(jsonObject);
		ConfigAPIUser.users.put(username, user);
	}
	
	public static void addUser(JSONObject jsonObject) 
	{
		User user = new User(jsonObject);
		ConfigAPIUser.users.put(jsonObject.optString(JsonKey.USERNAME, ""), user);
	}
	
	public static boolean checkUserAuth(String username, String password) 
	{
		if(username.isEmpty())
		{
			return false;
		}
		User user = ConfigAPIUser.getUser(username);
		return user.getPassword().equals(password) && user.isActive() && !user.isBlocked();
	}
	
	public static User getUser(String username)
	{		
		return ConfigAPIUser.users.getOrDefault(username, new User());
	}
	
	public static JSONObject toJSONObject()
	{
		JSONObject json = new JSONObject();
		for (Map.Entry<String, User> entry : ConfigAPIUser.users.entrySet())
		{
			String username = entry.getKey();
			JSONObject user = entry.getValue().toJSONObject();
			json.put(username, user);
		}
		return json;
	}
	
	public static String listAsString() 
	{
		return ConfigAPIUser.toJSONObject().toString();
	}

	public static void reset() {
		ConfigAPIUser.users = new HashMap<>();
		
	}

	public static void delete(Map<String, String> queryPairs) {
		for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
		{
			String value = entry.getValue();
			ConfigAPIUser.deleteUser(value);
		}
		
	}

	public static void deactivate(Map<String, String> queryPairs) {
		for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
		{
			String key = entry.getKey();
			String value = entry.getValue();
			if(key.startsWith("id["))
			{
				ConfigAPIUser.deactivate(value);
			}
		}
		
	}

	public static void activate(Map<String, String> queryPairs) {
		for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
		{
			String key = entry.getKey();
			String value = entry.getValue();
			if(key.startsWith("id["))
			{
				ConfigAPIUser.activate(value);
			}
		}
	}
}
