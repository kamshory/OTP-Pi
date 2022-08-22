package com.planetbiru.config;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.FileUtil;
import com.planetbiru.util.Utility;

public class ConfigSubscriberAMQP {

	private static String configPath = "";
	
	private static boolean subscriberAmqpEnable = false;
	private static boolean subscriberAmqpSSL = false;
	private static String subscriberAmqpAddress = "";
	private static int subscriberAmqpPort = 0;
	private static String subscriberAmqpPath = "";
	private static String subscriberAmqpUsername = "";
	private static String subscriberAmqpPassword = "";
	private static String subscriberAmqpTopic = "";
	private static int subscriberAmqpTimeout = 0;
	private static long subscriberAmqpReconnectDelay = 10000;
	private static int subscriberAmqpRefresh = 0;
	private static boolean loaded = false;
	private static boolean connected = false;
	private static int subscriberAmqpVersion = 0;
	
	private static Logger logger = Logger.getLogger(ConfigSubscriberAMQP.class);


	
	private ConfigSubscriberAMQP()
	{
	}
	
	public static boolean echoTest()
	{
		return false;
	}
	
	public static JSONObject toJSONObject()
	{
		JSONObject setting = new JSONObject();
		setting.put("subscriberAmqpEnable", ConfigSubscriberAMQP.subscriberAmqpEnable);
		setting.put("subscriberAmqpSSL", ConfigSubscriberAMQP.subscriberAmqpSSL);
		setting.put("subscriberAmqpAddress", ConfigSubscriberAMQP.subscriberAmqpAddress);
		setting.put("subscriberAmqpPort", ConfigSubscriberAMQP.subscriberAmqpPort);
		setting.put("subscriberAmqpPath", ConfigSubscriberAMQP.subscriberAmqpPath);
		setting.put("subscriberAmqpUsername", ConfigSubscriberAMQP.subscriberAmqpUsername);
		setting.put("subscriberAmqpPassword", ConfigSubscriberAMQP.subscriberAmqpPassword);
		setting.put("subscriberAmqpTopic", ConfigSubscriberAMQP.subscriberAmqpTopic);
		setting.put("subscriberAmqpTimeout", ConfigSubscriberAMQP.subscriberAmqpTimeout);
		setting.put("subscriberAmqpReconnectDelay", ConfigSubscriberAMQP.subscriberAmqpReconnectDelay);
		setting.put("subscriberAmqpRefresh", ConfigSubscriberAMQP.subscriberAmqpRefresh);
		setting.put("subscriberAmqpVersion", ConfigSubscriberAMQP.subscriberAmqpVersion);
		return setting;
	}
	
	public static void save(String path) {
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		FileConfigUtil.prepareDir(fileName);	
		try 
		{
			FileUtil.write(fileName, ConfigSubscriberAMQP.toJSONObject().toString().getBytes());
		} 
		catch (IOException e) 
		{
			logger.error(e.getMessage(), e);
		}
	}
	public static void save()
	{
		ConfigSubscriberAMQP.save(ConfigSubscriberAMQP.configPath );
	}
	public static void load(String path)
	{
		ConfigSubscriberAMQP.configPath = path;
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		FileConfigUtil.prepareDir(fileName);
		byte[] data = null;
		try 
		{
			data = FileConfigUtil.read(fileName);
			ConfigSubscriberAMQP.loaded = true;
		} 
		catch (FileNotFoundException e1) 
		{
			ConfigSubscriberAMQP.loaded = false;
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

				ConfigSubscriberAMQP.subscriberAmqpEnable = setting.optBoolean("subscriberAmqpEnable", false);
				ConfigSubscriberAMQP.subscriberAmqpSSL = setting.optBoolean("subscriberAmqpSSL", false);
				ConfigSubscriberAMQP.subscriberAmqpAddress = setting.optString("subscriberAmqpAddress", "");
				ConfigSubscriberAMQP.subscriberAmqpPort = setting.optInt("subscriberAmqpPort", 0);
				ConfigSubscriberAMQP.subscriberAmqpPath = setting.optString("subscriberAmqpPath", "");
				ConfigSubscriberAMQP.subscriberAmqpUsername = setting.optString("subscriberAmqpUsername", "");
				ConfigSubscriberAMQP.subscriberAmqpPassword = setting.optString("subscriberAmqpPassword", "");
				ConfigSubscriberAMQP.subscriberAmqpTopic = setting.optString("subscriberAmqpTopic", "");
				ConfigSubscriberAMQP.subscriberAmqpTimeout = setting.optInt("subscriberAmqpTimeout", 0);
				ConfigSubscriberAMQP.subscriberAmqpReconnectDelay = setting.optLong("subscriberAmqpReconnectDelay", 0);
				ConfigSubscriberAMQP.subscriberAmqpRefresh = setting.optInt("subscriberAmqpRefresh", 0);
				ConfigSubscriberAMQP.subscriberAmqpVersion = setting.optInt("subscriberAmqpVersion", 0);
			}
			catch(JSONException e)
			{
				/**
				 * Do nothing
				 */
			}
		}
	}

	public static boolean isSubscriberAmqpEnable() {
		return subscriberAmqpEnable;
	}

	public static void setSubscriberAmqpEnable(boolean subscriberAmqpEnable) {
		ConfigSubscriberAMQP.subscriberAmqpEnable = subscriberAmqpEnable;
	}

	public static boolean isSubscriberAmqpSSL() {
		return subscriberAmqpSSL;
	}

	public static void setSubscriberAmqpSSL(boolean subscriberAmqpSSL) {
		ConfigSubscriberAMQP.subscriberAmqpSSL = subscriberAmqpSSL;
	}

	public static String getSubscriberAmqpAddress() {
		return subscriberAmqpAddress;
	}

	public static void setSubscriberAmqpAddress(String subscriberAmqpAddress) {
		ConfigSubscriberAMQP.subscriberAmqpAddress = subscriberAmqpAddress;
	}

	public static int getSubscriberAmqpPort() {
		return subscriberAmqpPort;
	}

	public static void setSubscriberAmqpPort(int subscriberAmqpPort) {
		ConfigSubscriberAMQP.subscriberAmqpPort = subscriberAmqpPort;
	}

	public static String getSubscriberAmqpPath() {
		return subscriberAmqpPath;
	}

	public static void setSubscriberAmqpPath(String subscriberAmqpPath) {
		ConfigSubscriberAMQP.subscriberAmqpPath = subscriberAmqpPath;
	}

	public static String getSubscriberAmqpUsername() {
		return subscriberAmqpUsername;
	}

	public static void setSubscriberAmqpUsername(String subscriberAmqpUsername) {
		ConfigSubscriberAMQP.subscriberAmqpUsername = subscriberAmqpUsername;
	}

	public static String getSubscriberAmqpPassword() {
		return subscriberAmqpPassword;
	}

	public static void setSubscriberAmqpPassword(String subscriberAmqpPassword) {
		ConfigSubscriberAMQP.subscriberAmqpPassword = subscriberAmqpPassword;
	}

	public static String getSubscriberAmqpTopic() {
		return subscriberAmqpTopic;
	}

	public static void setSubscriberAmqpTopic(String subscriberAmqpTopic) {
		ConfigSubscriberAMQP.subscriberAmqpTopic = subscriberAmqpTopic;
	}

	public static int getSubscriberAmqpTimeout() {
		return subscriberAmqpTimeout;
	}

	public static void setSubscriberAmqpTimeout(int subscriberAmqpTimeout) {
		ConfigSubscriberAMQP.subscriberAmqpTimeout = subscriberAmqpTimeout;
	}

	public static int getSubscriberAmqpRefresh() {
		return subscriberAmqpRefresh;
	}

	public static void setSubscriberAmqpRefresh(int subscriberAmqpRefresh) {
		ConfigSubscriberAMQP.subscriberAmqpRefresh = subscriberAmqpRefresh;
	}

	public static boolean isLoaded() {
		return loaded;
	}

	public static void setLoaded(boolean loaded) {
		ConfigSubscriberAMQP.loaded = loaded;
	}

	public static boolean isConnected() {
		return connected;
	}

	public static void setConnected(boolean connected) {
		ConfigSubscriberAMQP.connected = connected;
	}

	public static long getSubscriberAmqpReconnectDelay() {
		return subscriberAmqpReconnectDelay;
	}

	public static void setSubscriberAmqpReconnectDelay(long subscriberAmqpReconnectDelay) {
		ConfigSubscriberAMQP.subscriberAmqpReconnectDelay = subscriberAmqpReconnectDelay;
	}

	public static int getSubscriberAmqpVersion() {
		return subscriberAmqpVersion;
	}

	public static void setSubscriberAmqpVersion(int subscriberAmqpVersion) {
		ConfigSubscriberAMQP.subscriberAmqpVersion = subscriberAmqpVersion;
	}

	public static void reset() {
		ConfigSubscriberAMQP.subscriberAmqpEnable = false;
		ConfigSubscriberAMQP.subscriberAmqpSSL = false;
		ConfigSubscriberAMQP.subscriberAmqpAddress = "";
		ConfigSubscriberAMQP.subscriberAmqpPort = 0;
		ConfigSubscriberAMQP.subscriberAmqpPath = "";
		ConfigSubscriberAMQP.subscriberAmqpUsername = "";
		ConfigSubscriberAMQP.subscriberAmqpPassword = "";
		ConfigSubscriberAMQP.subscriberAmqpTopic = "";
		ConfigSubscriberAMQP.subscriberAmqpTimeout = 0;
		ConfigSubscriberAMQP.subscriberAmqpReconnectDelay = 0;
		ConfigSubscriberAMQP.subscriberAmqpRefresh = 0;
	}

}
