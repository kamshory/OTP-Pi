package com.planetbiru.config;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.Utility;

public class ConfigVendorAfraid {
	private static String endpoint = "";
	private static String username = "";
	private static String email = "";
	private static String password = "";
	private static String company = "";
	private static String configPath = "";
	private static boolean active = false;
	
	private static Logger logger = Logger.getLogger(ConfigVendorAfraid.class);
	
	private ConfigVendorAfraid()
	{
		
	}
	
	public static void load(String path) {
		ConfigVendorAfraid.configPath = path;
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
					JSONObject json = new JSONObject(text);
					String lEndpoint = json.optString("endpoint", "").trim();
					String lUsername = json.optString("username", "").trim();
					String lEmail = json.optString("email", "").trim();
					String lPassword = json.optString("password", "").trim();
					String lCompany = json.optString("company", "").trim();
					boolean lActive = json.optBoolean("active", false);
					
					ConfigVendorAfraid.endpoint = lEndpoint;
					ConfigVendorAfraid.username = lUsername;
					ConfigVendorAfraid.email = lEmail;
					ConfigVendorAfraid.password = lPassword;
					ConfigVendorAfraid.company = lCompany;
					ConfigVendorAfraid.active = lActive;
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
		ConfigVendorAfraid.save(ConfigVendorAfraid.configPath);
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
		FileConfigUtil.prepareDir(fileName);
		
		try 
		{
			FileConfigUtil.write(fileName, config.toString().getBytes());
		}
		catch (IOException e) 
		{
			logger.error(e.getMessage(), e);
		}
	}
	
	public static JSONObject getJSONObject() {
		JSONObject config = new JSONObject();

		config.put("endpoint", ConfigVendorAfraid.endpoint);
		config.put("username", ConfigVendorAfraid.username);
		config.put("email", ConfigVendorAfraid.email);
		config.put("password", ConfigVendorAfraid.password);
		config.put("company", ConfigVendorAfraid.company);
		config.put("active", ConfigVendorAfraid.active);
		return config;
	}

	public static String getEndpoint() {
		return endpoint;
	}

	public static void setEndpoint(String endpoint) {
		ConfigVendorAfraid.endpoint = endpoint;
	}

	

	public static String getUsername() {
		return username;
	}

	public static void setUsername(String username) {
		ConfigVendorAfraid.username = username;
	}

	public static String getEmail() {
		return email;
	}

	public static void setEmail(String email) {
		ConfigVendorAfraid.email = email;
	}

	public static String getPassword() {
		return password;
	}

	public static void setPassword(String password) {
		ConfigVendorAfraid.password = password;
	}

	public static String getCompany() {
		return company;
	}

	public static void setCompany(String company) {
		ConfigVendorAfraid.company = company;
	}

	public static JSONObject toJSONObject()
	{
		return getJSONObject();
	}

	public static boolean isActive() {
		return active;
	}

	public static void setActive(boolean active) {
		ConfigVendorAfraid.active = active;
	}

	public static void reset() {
		ConfigVendorAfraid.endpoint = "";
		ConfigVendorAfraid.username = "";
		ConfigVendorAfraid.email = "";
		ConfigVendorAfraid.password = "";
		ConfigVendorAfraid.company = "";
		ConfigVendorAfraid.active = false;
	}
	
}
