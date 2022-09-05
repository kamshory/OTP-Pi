package com.planetbiru.config;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.Utility;

public class ConfigVendorNoIP {
	private static String configPath = "";
	
	private static Logger logger = Logger.getLogger(ConfigVendorNoIP.class);
	
	private ConfigVendorNoIP()
	{
		
	}
	
	private static String endpoint = "";
	private static String username = "";
	private static String email = "";
	private static String password = "";
	private static String company = "";
	private static boolean active = false;
	
	public static void load(String path) {
		ConfigVendorNoIP.configPath = path;
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
					
					ConfigVendorNoIP.endpoint = lEndpoint;
					ConfigVendorNoIP.username = lUsername;
					ConfigVendorNoIP.email = lEmail;
					ConfigVendorNoIP.password = lPassword;
					ConfigVendorNoIP.company = lCompany;
					ConfigVendorNoIP.active = lActive;
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
		ConfigVendorNoIP.save(ConfigVendorNoIP.configPath);
	}
	
	public static void save(String path) 
	{
		JSONObject config = getJSONObject();
		save(path, config);
	}

	public static void save(String path, JSONObject config) 
	{
		
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
	
	public static JSONObject getJSONObject() 
	{
		JSONObject config = new JSONObject();
		config.put("endpoint", ConfigVendorNoIP.endpoint);
		config.put("username", ConfigVendorNoIP.username);
		config.put("email", ConfigVendorNoIP.email);
		config.put("password", ConfigVendorNoIP.password);
		config.put("company", ConfigVendorNoIP.company);
		config.put("active", ConfigVendorNoIP.active);
		return config;
	}

	public static String getEndpoint() {
		return endpoint;
	}

	public static void setEndpoint(String endpoint) {
		ConfigVendorNoIP.endpoint = endpoint;
	}

	public static String getUsername() {
		return username;
	}

	public static void setUsername(String username) {
		ConfigVendorNoIP.username = username;
	}

	public static String getEmail() {
		return email;
	}

	public static void setEmail(String email) {
		ConfigVendorNoIP.email = email;
	}

	public static String getPassword() {
		return password;
	}

	public static void setPassword(String password) {
		ConfigVendorNoIP.password = password;
	}

	public static String getCompany() {
		return company;
	}

	public static void setCompany(String company) {
		ConfigVendorNoIP.company = company;
	}

	public static boolean isActive() {
		return active;
	}
	public static void setActive(boolean active) {
		ConfigVendorNoIP.active = active;
	}
	public static JSONObject toJSONObject()
	{
		return getJSONObject();
	}
	public static void reset() {
		ConfigVendorNoIP.endpoint = "";
		ConfigVendorNoIP.username = "";
		ConfigVendorNoIP.email = "";
		ConfigVendorNoIP.password = "";
		ConfigVendorNoIP.company = "";
		ConfigVendorNoIP.active = false;
		
	}
	
}
