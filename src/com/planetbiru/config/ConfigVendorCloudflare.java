package com.planetbiru.config;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.Utility;

public class ConfigVendorCloudflare {
	private static String endpoint = "";
	private static String accountId = "";
	private static String authEmail = "";
	private static String authApiKey = "";
	private static String authToken = "";
	private static String configPath = "";
	private static boolean active = false;
	
	private static Logger logger = Logger.getLogger(ConfigVendorCloudflare.class);
	
	private ConfigVendorCloudflare()
	{
		
	}

	public static void load(String path) {
		ConfigVendorCloudflare.configPath = path;
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
					String lAccountId = json.optString("accountId", "").trim();
					String lAuthEmail = json.optString("authEmail", "").trim();
					String lAuthApiKey = json.optString("authApiKey", "").trim();
					String lAuthToken = json.optString("authToken", "").trim();
					boolean lActive = json.optBoolean("active", false);
					
					ConfigVendorCloudflare.endpoint = lEndpoint;
					ConfigVendorCloudflare.accountId = lAccountId;
					ConfigVendorCloudflare.authEmail = lAuthEmail;
					ConfigVendorCloudflare.authApiKey = lAuthApiKey;
					ConfigVendorCloudflare.authToken = lAuthToken;
					ConfigVendorCloudflare.active = lActive;
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
		ConfigVendorCloudflare.save(ConfigVendorCloudflare.configPath);
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
		FileConfigUtil.prepareDirectory(fileName);
		
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
		config.put("endpoint", ConfigVendorCloudflare.endpoint);
		config.put("accountId", ConfigVendorCloudflare.accountId);
		config.put("authEmail", ConfigVendorCloudflare.authEmail);
		config.put("authApiKey", ConfigVendorCloudflare.authApiKey);
		config.put("authToken", ConfigVendorCloudflare.authToken);
		config.put("active", ConfigVendorCloudflare.active);

		return config;
	}

	public static String getEndpoint() {
		return endpoint;
	}

	public static void setEndpoint(String endpoint) {
		ConfigVendorCloudflare.endpoint = endpoint;
	}

	public static String getAccountId() {
		return accountId;
	}

	public static void setAccountId(String accountId) {
		ConfigVendorCloudflare.accountId = accountId;
	}

	public static String getAuthEmail() {
		return authEmail;
	}

	public static void setAuthEmail(String authEmail) {
		ConfigVendorCloudflare.authEmail = authEmail;
	}

	public static String getAuthApiKey() {
		return authApiKey;
	}

	public static void setAuthApiKey(String authApiKey) {
		ConfigVendorCloudflare.authApiKey = authApiKey;
	}

	public static String getAuthToken() {
		return authToken;
	}

	public static void setAuthToken(String authToken) {
		ConfigVendorCloudflare.authToken = authToken;
	}

	public static boolean isActive() {
		return active;
	}

	public static void setActive(boolean active) {
		ConfigVendorCloudflare.active = active;
	}

	public static JSONObject toJSONObject()
	{
		return getJSONObject();
	}

	public static void reset() {
		ConfigVendorCloudflare.endpoint = "";
		ConfigVendorCloudflare.accountId = "";
		ConfigVendorCloudflare.authEmail = "";
		ConfigVendorCloudflare.authApiKey = "";
		ConfigVendorCloudflare.authToken = "";
		ConfigVendorCloudflare.active = false;
	}
	
}
