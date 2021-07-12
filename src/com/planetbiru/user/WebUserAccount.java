package com.planetbiru.user;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.constant.JsonKey;
import com.planetbiru.cookie.CookieServer;
import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.Utility;
import com.sun.net.httpserver.Headers;

public class WebUserAccount {
	private static Map<String, User> users = new HashMap<>();
	private static String configPath = "";

	private WebUserAccount()
	{
		
	}
	
	public static void addUser(User user)
	{
		WebUserAccount.users.put(user.getUsername(), user);
	}
	
	public static void addUser(String username, JSONObject jsonObject) 
	{
		User user = new User(jsonObject);
		WebUserAccount.users.put(username, user);
	}
	
	public static void addUser(JSONObject jsonObject) {
		User user = new User(jsonObject);
		WebUserAccount.users.put(jsonObject.optString(JsonKey.USERNAME, ""), user);
	}	
	
	public static User getUser(String username) throws NoUserRegisteredException
	{
		if(WebUserAccount.users.isEmpty())
		{
			throw new NoUserRegisteredException("No user registered");
		}
		return WebUserAccount.users.getOrDefault(username, new User());
	}
	
	public static void activate(String username) throws NoUserRegisteredException 
	{
		User user = WebUserAccount.getUser(username);
		user.setActive(true);
		WebUserAccount.updateUser(user);
	}
	
	public static void deactivate(String username) throws NoUserRegisteredException 
	{
		User user = WebUserAccount.getUser(username);
		user.setActive(false);
		WebUserAccount.updateUser(user);
	}
	
	public static void block(String username) throws NoUserRegisteredException 
	{
		User user = WebUserAccount.getUser(username);
		user.setBlocked(true);
		WebUserAccount.updateUser(user);
	}
	
	public static void unblock(String username) throws NoUserRegisteredException 
	{
		User user = WebUserAccount.getUser(username);
		user.setBlocked(false);
		WebUserAccount.updateUser(user);
	}
	
	public static void updateLastActive(String username) throws NoUserRegisteredException 
	{
		User user = WebUserAccount.getUser(username);
		user.setLastActive(System.currentTimeMillis());
		WebUserAccount.updateUser(user);
	}
	
	public static void updateUser(User user)
	{
		WebUserAccount.users.put(user.getUsername(), user);
	}
	
	public static void deleteUser(User user)
	{
		WebUserAccount.users.remove(user.getUsername());
	}
	
	public static void deleteUser(String username) 
	{
		WebUserAccount.users.remove(username);
	}
	
	public static boolean checkUserAuth(Map<String, List<String>> headers) throws NoUserRegisteredException 
	{
		CookieServer cookie = new CookieServer(headers);
		String username = cookie.getSessionData().optString(JsonKey.USERNAME, "");
		String password = cookie.getSessionData().optString(JsonKey.PASSWORD, "");
		return WebUserAccount.checkUserAuth(username, password);
	}
	
	public static boolean checkUserAuth(Headers headers) throws NoUserRegisteredException
	{
		CookieServer cookie = new CookieServer(headers);
		String username = cookie.getSessionData().optString(JsonKey.USERNAME, "");
		String password = cookie.getSessionData().optString(JsonKey.PASSWORD, "");
		return WebUserAccount.checkUserAuth(username, password);
	}
	
	public static boolean checkUserAuth(String username, String password) throws NoUserRegisteredException 
	{
		if(username.isEmpty())
		{
			return false;
		}
		User user = WebUserAccount.getUser(username);
		return user.getPassword().equals(password) && user.isActive() && !user.isBlocked();
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
	
	public static void load(String path)
	{
		WebUserAccount.configPath = path;
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);

		WebUserAccount.prepareDir(fileName);
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
				    WebUserAccount.addUser(username, user);
				}
			}
		} 
		catch (FileNotFoundException | JSONException e) 
		{
			/**
			 * Do nothing
			 */
		}
	}
	
	public static void save()
	{
		WebUserAccount.save(WebUserAccount.configPath);
	}
	
	public static void save(String path)
	{		
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		String userData = WebUserAccount.listAsString();
		try 
		{
			if(userData.length() > 20)
			{
				FileConfigUtil.write(fileName, userData.getBytes());
			}
		} 
		catch (IOException e) 
		{
			/**
			 * Do nothing
			 */
		}
	}
	
	public static String listAsString()
	{
		return WebUserAccount.toJSONObject().toString();
	}
	
	public static JSONObject toJSONObject()
	{
		JSONObject json = new JSONObject();
		for (Map.Entry<String, User> entry : WebUserAccount.users.entrySet())
		{
			String username = entry.getKey();
			JSONObject user = ((User) entry.getValue()).toJSONObject();
			json.put(username, user);
		}
		return json;
	}
	
	public static User getUserByPhone(String userID) {
		for (Map.Entry<String, User> entry : WebUserAccount.users.entrySet())
		{
			if(!userID.isEmpty() && entry.getValue().getPhone().equals(userID))
			{
				return entry.getValue();
			}
		}
		return new User();
	}
	
	public static User getUserByEmail(String userID) {
		for (Map.Entry<String, User> entry : WebUserAccount.users.entrySet())
		{
			if(!userID.isEmpty() && entry.getValue().getEmail().equals(userID))
			{
				return entry.getValue();
			}
		}
		return new User();
	}
	
	public static boolean isEmpty() {
		return WebUserAccount.users.isEmpty();
	}

}
