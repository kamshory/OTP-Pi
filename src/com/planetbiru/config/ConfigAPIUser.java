package com.planetbiru.config;

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
		FileConfigUtil.prepareDirectory(fileName);
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
		catch (JSONException e) 
		{
			logger.error(e.getMessage(), e);
		}
		catch (FileNotFoundException e) 
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

	public static void reset() 
	{
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

	public static void block(Map<String, String> queryPairs) {
		for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
		{
			String key = entry.getKey();
			String value = entry.getValue();
			if(key.startsWith("id["))
			{
				ConfigAPIUser.block(value);
			}
		}	
	}

	public static void unblock(Map<String, String> queryPairs) {
		for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
		{
			String key = entry.getKey();
			String value = entry.getValue();
			if(key.startsWith("id["))
			{
				ConfigAPIUser.unblock(value);
			}
		}		
	}

	public static void update(Map<String, String> queryPairs) {
		String username = queryPairs.getOrDefault(JsonKey.USERNAME, "").trim();
		String name = queryPairs.getOrDefault(JsonKey.NAME, "").trim();
		String phone = queryPairs.getOrDefault(JsonKey.PHONE, "").trim();
		String email = queryPairs.getOrDefault(JsonKey.EMAIL, "").trim();
		String password = queryPairs.getOrDefault(JsonKey.PASSWORD, "").trim();
		boolean blocked = queryPairs.getOrDefault(JsonKey.BLOCKED, "").equals("1");
		boolean active = queryPairs.getOrDefault(JsonKey.ACTIVE, "").equals("1");

		if(!username.isEmpty())
		{
			ConfigAPIUser.load(Config.getUserAPISettingPath());
	
		    JSONObject jsonObject = new JSONObject();
			jsonObject.put(JsonKey.USERNAME, username);
			jsonObject.put(JsonKey.NAME, name);
			jsonObject.put(JsonKey.EMAIL, email);
			jsonObject.put(JsonKey.PHONE, phone);
			jsonObject.put(JsonKey.BLOCKED, blocked);
			jsonObject.put(JsonKey.ACTIVE, active);
			if(!username.isEmpty())
			{
				jsonObject.put(JsonKey.USERNAME, username);
			}
			if(!password.isEmpty())
			{
				jsonObject.put(JsonKey.PASSWORD, password);
			}
			ConfigAPIUser.updateUser(new User(jsonObject));		
		}
		
	}

	public static void add(Map<String, String> queryPairs) {
		String username = queryPairs.getOrDefault(JsonKey.USERNAME, "");
	    String password = queryPairs.getOrDefault(JsonKey.PASSWORD, "");
	    String email = queryPairs.getOrDefault(JsonKey.EMAIL, "");
	    String name = queryPairs.getOrDefault(JsonKey.NAME, "");
	    String phone = queryPairs.getOrDefault(JsonKey.PHONE, "");
	    
	    JSONObject jsonObject = new JSONObject();
		jsonObject.put(JsonKey.USERNAME, username);
		jsonObject.put(JsonKey.NAME, name);
		jsonObject.put(JsonKey.EMAIL, email);
		jsonObject.put(JsonKey.PASSWORD, password);
		jsonObject.put(JsonKey.PHONE, phone);
		jsonObject.put(JsonKey.BLOCKED, false);
		jsonObject.put(JsonKey.ACTIVE, true);
		
		if(!username.isEmpty())
		{
			ConfigAPIUser.addUser(new User(jsonObject));		
		}	
	}
}
