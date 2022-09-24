package com.planetbiru.user;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.config.Config;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.cookie.CookieServer;
import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.Utility;
import com.sun.net.httpserver.Headers; //NOSONAR

public class WebUserAccount {
	private static Map<String, User> users = new HashMap<>();
	private static String configPath = "";
	
	private static Logger logger = Logger.getLogger(WebUserAccount.class);

	private WebUserAccount()
	{
		
	}
	
	public static void addUser(User user)
	{
		WebUserAccount.getUsers().put(user.getUsername(), user);
	}
	
	public static void addUser(String username, JSONObject jsonObject) 
	{
		User user = new User(jsonObject);
		WebUserAccount.getUsers().put(username, user);
	}
	
	public static void addUser(JSONObject jsonObject) {
		User user = new User(jsonObject);
		WebUserAccount.getUsers().put(jsonObject.optString(JsonKey.USERNAME, ""), user);
	}	
	
	public static User getUser(String username) throws NoUserRegisteredException
	{
		if(WebUserAccount.getUsers().isEmpty())
		{
			throw new NoUserRegisteredException("No user registered");
		}
		return WebUserAccount.getUsers().getOrDefault(username, new User());
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
		WebUserAccount.getUsers().put(user.getUsername(), user);
	}
	
	public static void deleteUser(User user)
	{
		WebUserAccount.getUsers().remove(user.getUsername());
	}
	
	public static void deleteUser(String username) 
	{
		WebUserAccount.getUsers().remove(username);
	}
	
	public static boolean checkUserAuth(Map<String, List<String>> headers) throws NoUserRegisteredException 
	{
		return WebUserAccount.checkUserAuth(new CookieServer(headers));
	}
	
	public static boolean checkUserAuth(Headers headers) throws NoUserRegisteredException
	{
		return WebUserAccount.checkUserAuth(new CookieServer(headers));
	}
	
	public static boolean checkUserAuth(CookieServer cookie) throws NoUserRegisteredException {
		String username = cookie.getSessionValue(JsonKey.USERNAME, "");
		String password = cookie.getSessionValue(JsonKey.PASSWORD, "");
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
	
	public static boolean checkUserAuth(User user, String password) 
	{
		return user.getPassword().equals(password) && user.isActive() && !user.isBlocked();
	}
	
	/**
	 * Prepare directory before save a file
	 * @param fileName File path to be save after directory created
	 */
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
		for (Map.Entry<String, User> entry : WebUserAccount.getUsers().entrySet())
		{
			String username = entry.getKey();
			JSONObject user = entry.getValue().toJSONObject();
			json.put(username, user);
		}
		return json;
	}
	
	public static User getUserByPhone(String userID) {
		for (Map.Entry<String, User> entry : WebUserAccount.getUsers().entrySet())
		{
			if(!userID.isEmpty() && entry.getValue().getPhone().equals(userID))
			{
				return entry.getValue();
			}
		}
		return new User();
	}
	
	public static User getUserByEmail(String userID) {
		for (Map.Entry<String, User> entry : WebUserAccount.getUsers().entrySet())
		{
			if(!userID.isEmpty() && entry.getValue().getEmail().equals(userID))
			{
				return entry.getValue();
			}
		}
		return new User();
	}
	public static void reset()
	{
		WebUserAccount.setUsers(new HashMap<>());
	}
	public static boolean isEmpty() {
		return WebUserAccount.getUsers().isEmpty();
	}

	public static void delete(Map<String, String> queryPairs, String loggedUsername) {
		for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
		{
			String key = entry.getKey();
			String value = entry.getValue();
			if(key.startsWith("id[") && !value.equals(loggedUsername))
			{
				WebUserAccount.deleteUser(value);
			}
		}
		
	}

	public static void deactivate(Map<String, String> queryPairs, String loggedUsername) {
		for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
		{
			String key = entry.getKey();
			String value = entry.getValue();
			if(key.startsWith("id[") && !value.equals(loggedUsername))
			{
				try 
				{
					WebUserAccount.deactivate(value);
				} 
				catch (NoUserRegisteredException e) 
				{
					/**
					 * Do nothing
					 */
				}
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
				try 
				{
					WebUserAccount.activate(value);
				} 
				catch (NoUserRegisteredException e) 
				{
					/**
					 * Do nothing
					 */
				}
			}
		}
		
	}

	public static void block(Map<String, String> queryPairs, String loggedUsername) {
		for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
		{
			String key = entry.getKey();
			String value = entry.getValue();
			if(key.startsWith("id[") && !value.equals(loggedUsername))
			{
				try 
				{
					WebUserAccount.block(value);
				} 
				catch (NoUserRegisteredException e) 
				{
					/**
					 * Do nothing
					 */
				}
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
				try 
				{
					WebUserAccount.unblock(value);
				} 
				catch (NoUserRegisteredException e) 
				{
					/**
					 * Do nothing
					 */
				}
			}
		}
	}

	public static void updateData(Map<String, String> queryPairs) {
		String pkID = queryPairs.getOrDefault("pk_id", "");
		String field = queryPairs.getOrDefault("field", "");
		String value = queryPairs.getOrDefault("value", "");
		if(!field.equals(JsonKey.USERNAME))
		{
			User user;
			try 
			{
				user = WebUserAccount.getUser(pkID);
				if(field.equals(JsonKey.PHONE))
				{
					user.setPhone(value);
				}
				if(field.equals(JsonKey.NAME))
				{
					user.setName(value);
				}
				WebUserAccount.updateUser(user);
			} 
			catch (NoUserRegisteredException e) 
			{
				/**
				 * Do nothing
				 */
			}
		}	
	}

	public static void update(Map<String, String> queryPairs, String loggedUsername) {
		String username = queryPairs.getOrDefault(JsonKey.USERNAME, "").trim();
		String name = queryPairs.getOrDefault(JsonKey.NAME, "").trim();
		String phone = queryPairs.getOrDefault(JsonKey.PHONE, "").trim();
		String email = queryPairs.getOrDefault(JsonKey.EMAIL, "").trim();
		String password = Utility.hashPasswordGenerator(queryPairs.getOrDefault(JsonKey.PASSWORD, ""));
		boolean blocked = queryPairs.getOrDefault(JsonKey.BLOCKED, "").equals("1");
		boolean active = queryPairs.getOrDefault(JsonKey.ACTIVE, "").equals("1");

		if(!username.isEmpty())
		{
			User user;
			try 
			{
				user = WebUserAccount.getUser(username);
				if(!username.equals(loggedUsername) && !user.getUsername().isEmpty())
				{
					user.setUsername(username);
				}
				if(!name.isEmpty())
				{
					user.setName(name);
				}
				user.setPhone(phone);
				user.setEmail(email);
				if(!password.isEmpty())
				{
					user.setPassword(password);
				}
				if(!username.equals(loggedUsername))
				{
					user.setBlocked(blocked);
				}
				if(!username.equals(loggedUsername))
				{
					user.setActive(active);
				}
				WebUserAccount.updateUser(user);
			} 
			catch (NoUserRegisteredException e) 
			{
				/**
				 * Do nothing
				 */
			}
		}		
	}

	public static void add(Map<String, String> queryPairs) {
		String username = queryPairs.getOrDefault(JsonKey.USERNAME, "");
	    String password = Utility.hashPasswordGenerator(queryPairs.getOrDefault(JsonKey.PASSWORD, ""));
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
			WebUserAccount.addUser(new User(jsonObject));		
		}			
	}

	public static Map<String, User> getUsers() {
		return users;
	}

	public static void setUsers(Map<String, User> users) {
		WebUserAccount.users = users;
	}
}
