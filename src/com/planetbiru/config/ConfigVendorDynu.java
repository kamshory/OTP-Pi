package com.planetbiru.config;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.Utility;

public class ConfigVendorDynu {
	private static String apiVersion = "nic";
	private static String endpoint = "https://api.dynu.com/nic/update";
	private static String username = "";
	private static String email = "";
	private static String password = "";
	private static String apiKey = "";
	private static String company = "";

	private static String configPath = "";
	
	private static Logger logger = Logger.getLogger(ConfigVendorDynu.class);
	
	private ConfigVendorDynu()
	{
		
	}

	public static void load(String path) {
		ConfigVendorDynu.configPath = path;
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
					String lVersion = json.optString("apiVersion", "").trim();
					String lEndpoint = json.optString("endpoint", "").trim();
					String lUsername = json.optString("username", "").trim();
					String lEmail = json.optString("email", "").trim();
					String lPassword = json.optString("password", "").trim();
					String lCompany = json.optString("company", "").trim();
					String lApiKey = json.optString("apiKey", "").trim();
					
					ConfigVendorDynu.endpoint = lEndpoint;
					ConfigVendorDynu.username = lUsername;
					ConfigVendorDynu.email = lEmail;
					ConfigVendorDynu.password = lPassword;
					ConfigVendorDynu.company = lCompany;
					ConfigVendorDynu.apiVersion = lVersion;
					ConfigVendorDynu.apiKey = lApiKey;
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
		ConfigVendorDynu.save(ConfigVendorDynu.configPath);
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
	}
	
	public static JSONObject getJSONObject() {
		JSONObject config = new JSONObject();
		config.put("apiVersion", ConfigVendorDynu.apiVersion);
		config.put("apiKey", ConfigVendorDynu.apiKey);
		config.put("endpoint", ConfigVendorDynu.endpoint);
		config.put("username", ConfigVendorDynu.username);
		config.put("email", ConfigVendorDynu.email);
		config.put("password", ConfigVendorDynu.password);
		config.put("company", ConfigVendorDynu.company);
		return config;
	}

	public static String getEndpoint() {
		return endpoint;
	}

	public static void setEndpoint(String endpoint) {
		ConfigVendorDynu.endpoint = endpoint;
	}

	

	public static String getUsername() {
		return username;
	}

	public static void setUsername(String username) {
		ConfigVendorDynu.username = username;
	}

	public static String getEmail() {
		return email;
	}

	public static void setEmail(String email) {
		ConfigVendorDynu.email = email;
	}

	public static String getPassword() {
		return password;
	}

	public static void setPassword(String password) {
		ConfigVendorDynu.password = password;
	}

	public static String getCompany() {
		return company;
	}

	public static void setCompany(String company) {
		ConfigVendorDynu.company = company;
	}

	public static JSONObject toJSONObject()
	{
		return getJSONObject();
	}

	public static String getApiVersion() {
		return apiVersion;
	}

	public static void setApiVersion(String apiVersion) {
		ConfigVendorDynu.apiVersion = apiVersion;
	}

	public static String getApiKey() {
		return apiKey;
	}

	public static void setApiKey(String apiKey) {
		ConfigVendorDynu.apiKey = apiKey;
	}

	public static void reset() {
		ConfigVendorDynu.endpoint = "";
		ConfigVendorDynu.username = "";
		ConfigVendorDynu.email = "";
		ConfigVendorDynu.password = "";
		ConfigVendorDynu.company = "";
		ConfigVendorDynu.apiVersion = "";
		ConfigVendorDynu.apiKey = "";
		
	}
	
}
