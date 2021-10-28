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

public class ConfigSubscriberRedis {

	private static String configPath = "";
	
	private static boolean subscriberRedisEnable = false;
	private static boolean subscriberRedisSSL = false;
	private static String subscriberRedisAddress = "";
	private static int subscriberRedisPort = 0;
	private static String subscriberRedisPath = "";
	private static String subscriberRedisUsername = "";
	private static String subscriberRedisPassword = "";
	private static String subscriberRedisTopic = "";
	private static long subscriberRedisReconnectDelay = 0;
	private static int subscriberRedisTimeout = 0;
	private static int subscriberRedisRefresh = 0;
	private static boolean loaded = false;
	private static boolean connected = false;
	
	private static Logger logger = Logger.getLogger(ConfigSubscriberRedis.class);

	
	private ConfigSubscriberRedis()
	{
	}
	
	public static boolean echoTest()
	{
		return false;
	}
	
	public static JSONObject toJSONObject()
	{
		JSONObject setting = new JSONObject();
		setting.put("subscriberRedisEnable", ConfigSubscriberRedis.subscriberRedisEnable);
		setting.put("subscriberRedisSSL", ConfigSubscriberRedis.subscriberRedisSSL);
		setting.put("subscriberRedisAddress", ConfigSubscriberRedis.subscriberRedisAddress);
		setting.put("subscriberRedisPort", ConfigSubscriberRedis.subscriberRedisPort);
		setting.put("subscriberRedisPath", ConfigSubscriberRedis.subscriberRedisPath);
		setting.put("subscriberRedisUsername", ConfigSubscriberRedis.subscriberRedisUsername);
		setting.put("subscriberRedisPassword", ConfigSubscriberRedis.subscriberRedisPassword);
		setting.put("subscriberRedisTopic", ConfigSubscriberRedis.subscriberRedisTopic);
		setting.put("subscriberRedisReconnectDelay", ConfigSubscriberRedis.subscriberRedisReconnectDelay);
		setting.put("subscriberRedisTimeout", ConfigSubscriberRedis.subscriberRedisTimeout);
		setting.put("subscriberRedisRefresh", ConfigSubscriberRedis.subscriberRedisRefresh);
		return setting;
	}
	
	public static void save(String path) {
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		ConfigSubscriberRedis.prepareDir(fileName);	
		try 
		{
			FileUtil.write(fileName, ConfigSubscriberRedis.toJSONObject().toString().getBytes());
		} 
		catch (IOException e) 
		{
			logger.error(e.getMessage(), e);
		}
	}
	public static void save()
	{
		ConfigSubscriberRedis.save(ConfigSubscriberRedis.configPath );
	}
	public static void load(String path)
	{
		ConfigSubscriberRedis.configPath = path;
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		ConfigSubscriberRedis.prepareDir(fileName);
		byte[] data = null;
		try 
		{
			data = FileConfigUtil.read(fileName);
			ConfigSubscriberRedis.loaded = true;
		} 
		catch (FileNotFoundException e1) 
		{
			ConfigSubscriberRedis.loaded = false;
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

				ConfigSubscriberRedis.subscriberRedisEnable = setting.optBoolean("subscriberRedisEnable", false);
				ConfigSubscriberRedis.subscriberRedisSSL = setting.optBoolean("subscriberRedisSSL", false);
				ConfigSubscriberRedis.subscriberRedisAddress = setting.optString("subscriberRedisAddress", "");
				ConfigSubscriberRedis.subscriberRedisPort = setting.optInt("subscriberRedisPort", 0);
				ConfigSubscriberRedis.subscriberRedisPath = setting.optString("subscriberRedisPath", "");
				ConfigSubscriberRedis.subscriberRedisUsername = setting.optString("subscriberRedisUsername", "");
				ConfigSubscriberRedis.subscriberRedisPassword = setting.optString("subscriberRedisPassword", "");
				ConfigSubscriberRedis.subscriberRedisTopic = setting.optString("subscriberRedisTopic", "");
				ConfigSubscriberRedis.subscriberRedisReconnectDelay = setting.optLong("subscriberRedisReconnectDelay", 0);
				ConfigSubscriberRedis.subscriberRedisTimeout = setting.optInt("subscriberRedisTimeout", 0);
				ConfigSubscriberRedis.subscriberRedisRefresh = setting.optInt("subscriberRedisRefresh", 0);
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
	public static boolean isSubscriberRedisEnable() {
		return subscriberRedisEnable;
	}

	public static void setSubscriberRedisEnable(boolean subscriberRedisEnable) {
		ConfigSubscriberRedis.subscriberRedisEnable = subscriberRedisEnable;
	}

	public static boolean isSubscriberRedisSSL() {
		return subscriberRedisSSL;
	}

	public static void setSubscriberRedisSSL(boolean subscriberRedisSSL) {
		ConfigSubscriberRedis.subscriberRedisSSL = subscriberRedisSSL;
	}

	public static String getSubscriberRedisAddress() {
		return subscriberRedisAddress;
	}

	public static void setSubscriberRedisAddress(String subscriberRedisAddress) {
		ConfigSubscriberRedis.subscriberRedisAddress = subscriberRedisAddress;
	}

	public static int getSubscriberRedisPort() {
		return subscriberRedisPort;
	}

	public static void setSubscriberRedisPort(int subscriberRedisPort) {
		ConfigSubscriberRedis.subscriberRedisPort = subscriberRedisPort;
	}

	public static String getSubscriberRedisPath() {
		return subscriberRedisPath;
	}

	public static void setSubscriberRedisPath(String subscriberRedisPath) {
		ConfigSubscriberRedis.subscriberRedisPath = subscriberRedisPath;
	}

	public static String getSubscriberRedisUsername() {
		return subscriberRedisUsername;
	}

	public static void setSubscriberRedisUsername(String subscriberRedisUsername) {
		ConfigSubscriberRedis.subscriberRedisUsername = subscriberRedisUsername;
	}

	public static String getSubscriberRedisPassword() {
		return subscriberRedisPassword;
	}

	public static void setSubscriberRedisPassword(String subscriberRedisPassword) {
		ConfigSubscriberRedis.subscriberRedisPassword = subscriberRedisPassword;
	}

	public static String getSubscriberRedisTopic() {
		return subscriberRedisTopic;
	}

	public static void setSubscriberRedisTopic(String subscriberRedisTopic) {
		ConfigSubscriberRedis.subscriberRedisTopic = subscriberRedisTopic;
	}

	public static int getSubscriberRedisTimeout() {
		return subscriberRedisTimeout;
	}

	public static void setSubscriberRedisTimeout(int subscriberRedisTimeout) {
		ConfigSubscriberRedis.subscriberRedisTimeout = subscriberRedisTimeout;
	}

	public static long getSubscriberRedisReconnectDelay() {
		return subscriberRedisReconnectDelay;
	}

	public static void setSubscriberRedisReconnectDelay(long subscriberRedisReconnectDelay) {
		ConfigSubscriberRedis.subscriberRedisReconnectDelay = subscriberRedisReconnectDelay;
	}

	public static int getSubscriberRedisRefresh() {
		return subscriberRedisRefresh;
	}

	public static void setSubscriberRedisRefresh(int subscriberRedisRefresh) {
		ConfigSubscriberRedis.subscriberRedisRefresh = subscriberRedisRefresh;
	}

	public static boolean isLoaded() {
		return loaded;
	}

	public static void setLoaded(boolean loaded) {
		ConfigSubscriberRedis.loaded = loaded;
	}

	public static boolean isConnected() {
		return connected;
	}

	public static void setConnected(boolean connected) {
		ConfigSubscriberRedis.connected = connected;
	}

	public static void reset() {
		ConfigSubscriberRedis.subscriberRedisEnable = false;
		ConfigSubscriberRedis.subscriberRedisSSL = false;
		ConfigSubscriberRedis.subscriberRedisAddress = "";
		ConfigSubscriberRedis.subscriberRedisPort = 0;
		ConfigSubscriberRedis.subscriberRedisPath = "";
		ConfigSubscriberRedis.subscriberRedisUsername = "";
		ConfigSubscriberRedis.subscriberRedisPassword = "";
		ConfigSubscriberRedis.subscriberRedisTopic = "";
		ConfigSubscriberRedis.subscriberRedisTimeout = 0;
		ConfigSubscriberRedis.subscriberRedisRefresh = 0;
	}

	

	
}
