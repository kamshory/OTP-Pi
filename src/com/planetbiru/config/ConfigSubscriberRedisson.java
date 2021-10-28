package com.planetbiru.config;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.FileUtil;
import com.planetbiru.util.Utility;

public class ConfigSubscriberRedisson {

	private static String configPath = "";
	
	private static boolean subscriberRedissonEnable = false;
	private static boolean subscriberRedissonSSL = false;
	private static String subscriberRedissonAddress = "";
	private static int subscriberRedissonPort = 0;
	private static String subscriberRedissonPath = "";
	private static int subscriberRedissonDatabase = 0;
	private static String subscriberRedissonUsername = "";
	private static String subscriberRedissonPassword = "";
	private static String subscriberRedissonTopic = "";
	private static long subscriberRedissonReconnectDelay = 0;
	private static int subscriberRedissonTimeout = 0;
	private static int subscriberRedissonRefresh = 0;
	private static boolean loaded = false;
	private static boolean connected = false;
	
	private static Logger logger = Logger.getLogger(ConfigSubscriberRedisson.class);

	
	private ConfigSubscriberRedisson()
	{
	}
	
	public static boolean echoTest()
	{
		return false;
	}
	
	public static JSONObject toJSONObject()
	{
		JSONObject setting = new JSONObject();
		setting.put("subscriberRedissonEnable", ConfigSubscriberRedisson.subscriberRedissonEnable);
		setting.put("subscriberRedissonSSL", ConfigSubscriberRedisson.subscriberRedissonSSL);
		setting.put("subscriberRedissonAddress", ConfigSubscriberRedisson.subscriberRedissonAddress);
		setting.put("subscriberRedissonPort", ConfigSubscriberRedisson.subscriberRedissonPort);
		setting.put("subscriberRedissonDatabase", ConfigSubscriberRedisson.subscriberRedissonDatabase);
		setting.put("subscriberRedissonPath", ConfigSubscriberRedisson.subscriberRedissonPath);
		setting.put("subscriberRedissonUsername", ConfigSubscriberRedisson.subscriberRedissonUsername);
		setting.put("subscriberRedissonPassword", ConfigSubscriberRedisson.subscriberRedissonPassword);
		setting.put("subscriberRedissonTopic", ConfigSubscriberRedisson.subscriberRedissonTopic);
		setting.put("subscriberRedissonReconnectDelay", ConfigSubscriberRedisson.subscriberRedissonReconnectDelay);
		setting.put("subscriberRedissonTimeout", ConfigSubscriberRedisson.subscriberRedissonTimeout);
		setting.put("subscriberRedissonRefresh", ConfigSubscriberRedisson.subscriberRedissonRefresh);
		return setting;
	}
	
	public static void save(String path) {
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		ConfigSubscriberRedisson.prepareDir(fileName);	
		try 
		{
			FileUtil.write(fileName, ConfigSubscriberRedisson.toJSONObject().toString().getBytes());
		} 
		catch (IOException e) 
		{
			logger.error(e.getMessage(), e);
		}
	}
	public static void save()
	{
		ConfigSubscriberRedisson.save(ConfigSubscriberRedisson.configPath );
	}
	public static void load(String path)
	{
		ConfigSubscriberRedisson.configPath = path;
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		ConfigSubscriberRedisson.prepareDir(fileName);
		byte[] data = null;
		try 
		{
			data = FileConfigUtil.read(fileName);
			ConfigSubscriberRedisson.loaded = true;
		} 
		catch (FileNotFoundException e1) 
		{
			ConfigSubscriberRedisson.loaded = false;
			if(Config.isLogConfigNotFound())
			{
				logger.error(e1.getMessage(), e1);
			}
		}
		if(data != null)
		{
			String text = new String(data);
			try
			{
				JSONObject setting = new JSONObject(text);

				ConfigSubscriberRedisson.subscriberRedissonEnable = setting.optBoolean("subscriberRedissonEnable", false);
				ConfigSubscriberRedisson.subscriberRedissonSSL = setting.optBoolean("subscriberRedissonSSL", false);
				ConfigSubscriberRedisson.subscriberRedissonAddress = setting.optString("subscriberRedissonAddress", "");
				ConfigSubscriberRedisson.subscriberRedissonPort = setting.optInt("subscriberRedissonPort", 0);
				ConfigSubscriberRedisson.subscriberRedissonDatabase = setting.optInt("subscriberRedissonDatabase", 0);
				ConfigSubscriberRedisson.subscriberRedissonPath = setting.optString("subscriberRedissonPath", "");
				ConfigSubscriberRedisson.subscriberRedissonUsername = setting.optString("subscriberRedissonUsername", "");
				ConfigSubscriberRedisson.subscriberRedissonPassword = setting.optString("subscriberRedissonPassword", "");
				ConfigSubscriberRedisson.subscriberRedissonTopic = setting.optString("subscriberRedissonTopic", "");
				ConfigSubscriberRedisson.subscriberRedissonReconnectDelay = setting.optLong("subscriberRedissonReconnectDelay", 0);
				ConfigSubscriberRedisson.subscriberRedissonTimeout = setting.optInt("subscriberRedissonTimeout", 0);
				ConfigSubscriberRedisson.subscriberRedissonRefresh = setting.optInt("subscriberRedissonRefresh", 0);
			}
			catch(JSONException e)
			{
				/**
				 * Do nothing
				 */
			}
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
	public static boolean isSubscriberRedissonEnable() {
		return subscriberRedissonEnable;
	}

	public static void setSubscriberRedissonEnable(boolean subscriberRedissonEnable) {
		ConfigSubscriberRedisson.subscriberRedissonEnable = subscriberRedissonEnable;
	}

	public static boolean isSubscriberRedissonSSL() {
		return subscriberRedissonSSL;
	}

	public static void setSubscriberRedissonSSL(boolean subscriberRedissonSSL) {
		ConfigSubscriberRedisson.subscriberRedissonSSL = subscriberRedissonSSL;
	}

	public static int getSubscriberRedissonDatabase() {
		return subscriberRedissonDatabase;
	}

	public static void setSubscriberRedissonDatabase(int subscriberRedissonDatabase) {
		ConfigSubscriberRedisson.subscriberRedissonDatabase = subscriberRedissonDatabase;
	}

	public static long getSubscriberRedissonReconnectDelay() {
		return subscriberRedissonReconnectDelay;
	}

	public static void setSubscriberRedissonReconnectDelay(long subscriberRedissonReconnectDelay) {
		ConfigSubscriberRedisson.subscriberRedissonReconnectDelay = subscriberRedissonReconnectDelay;
	}

	public static String getSubscriberRedissonAddress() {
		return subscriberRedissonAddress;
	}

	public static void setSubscriberRedissonAddress(String subscriberRedissonAddress) {
		ConfigSubscriberRedisson.subscriberRedissonAddress = subscriberRedissonAddress;
	}

	public static int getSubscriberRedissonPort() {
		return subscriberRedissonPort;
	}

	public static void setSubscriberRedissonPort(int subscriberRedissonPort) {
		ConfigSubscriberRedisson.subscriberRedissonPort = subscriberRedissonPort;
	}

	public static String getSubscriberRedissonPath() {
		return subscriberRedissonPath;
	}

	public static void setSubscriberRedissonPath(String subscriberRedissonPath) {
		ConfigSubscriberRedisson.subscriberRedissonPath = subscriberRedissonPath;
	}

	public static String getSubscriberRedissonUsername() {
		return subscriberRedissonUsername;
	}

	public static void setSubscriberRedissonUsername(String subscriberRedissonUsername) {
		ConfigSubscriberRedisson.subscriberRedissonUsername = subscriberRedissonUsername;
	}

	public static String getSubscriberRedissonPassword() {
		return subscriberRedissonPassword;
	}

	public static void setSubscriberRedissonPassword(String subscriberRedissonPassword) {
		ConfigSubscriberRedisson.subscriberRedissonPassword = subscriberRedissonPassword;
	}

	public static String getSubscriberRedissonTopic() {
		return subscriberRedissonTopic;
	}

	public static void setSubscriberRedissonTopic(String subscriberRedissonTopic) {
		ConfigSubscriberRedisson.subscriberRedissonTopic = subscriberRedissonTopic;
	}

	public static int getSubscriberRedissonTimeout() {
		return subscriberRedissonTimeout;
	}

	public static void setSubscriberRedissonTimeout(int subscriberRedissonTimeout) {
		ConfigSubscriberRedisson.subscriberRedissonTimeout = subscriberRedissonTimeout;
	}

	public static int getSubscriberRedissonRefresh() {
		return subscriberRedissonRefresh;
	}

	public static void setSubscriberRedissonRefresh(int subscriberRedissonRefresh) {
		ConfigSubscriberRedisson.subscriberRedissonRefresh = subscriberRedissonRefresh;
	}

	public static boolean isLoaded() {
		return loaded;
	}

	public static void setLoaded(boolean loaded) {
		ConfigSubscriberRedisson.loaded = loaded;
	}

	public static boolean isConnected() {
		return connected;
	}

	public static void setConnected(boolean connected) {
		ConfigSubscriberRedisson.connected = connected;
	}

	public static void reset() {
		ConfigSubscriberRedisson.subscriberRedissonEnable = false;
		ConfigSubscriberRedisson.subscriberRedissonSSL = false;
		ConfigSubscriberRedisson.subscriberRedissonAddress = "";
		ConfigSubscriberRedisson.subscriberRedissonPort = 0;
		ConfigSubscriberRedisson.subscriberRedissonDatabase = 0;
		ConfigSubscriberRedisson.subscriberRedissonPath = "";
		ConfigSubscriberRedisson.subscriberRedissonUsername = "";
		ConfigSubscriberRedisson.subscriberRedissonPassword = "";
		ConfigSubscriberRedisson.subscriberRedissonTopic = "";
		ConfigSubscriberRedisson.subscriberRedissonTimeout = 0;
		ConfigSubscriberRedisson.subscriberRedissonRefresh = 0;
	}

	public static long getSubscriberWsReconnectDelay() {
		return subscriberRedissonReconnectDelay;
	}

	public static void setSubscriberWsReconnectDelay(long subscriberRedissonReconnectDelay) {
		ConfigSubscriberRedisson.subscriberRedissonReconnectDelay = subscriberRedissonReconnectDelay;
	}

	
}
