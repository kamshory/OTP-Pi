package com.planetbiru.config;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.FileUtil;
import com.planetbiru.util.Utility;

public class ConfigSubscriberActiveMQ {

	private static String configPath = "";
	
	private static boolean subscriberActiveMQEnable = false;
	private static boolean subscriberActiveMQSSL = false;
	private static String subscriberActiveMQAddress = "127.0.0.1";
	private static int subscriberActiveMQPort = 61616;
	private static String subscriberActiveMQClientID = "";
	private static String subscriberActiveMQUsername = "";
	private static String subscriberActiveMQPassword = "";
	private static String subscriberActiveMQTopic = "sms";
	private static int subscriberActiveMQTimeout = 0;
	private static long subscriberActiveMQTimeToLeave = 6000;
	private static int subscriberActiveMQRefresh = 0;
	private static long subscriberActiveMQReconnectDelay = 0;
	private static boolean loaded = false;
	private static boolean connected = false;
	private static long subscriberTimeToLeave = 60000;
	
	private static Logger logger = Logger.getLogger(ConfigSubscriberActiveMQ.class);

	private ConfigSubscriberActiveMQ()
	{
	}
	
	public static boolean echoTest()
	{
		return false;
	}
	
	public static JSONObject toJSONObject()
	{
		JSONObject setting = new JSONObject();
		setting.put("subscriberActiveMQEnable", ConfigSubscriberActiveMQ.subscriberActiveMQEnable);
		setting.put("subscriberActiveMQSSL", ConfigSubscriberActiveMQ.subscriberActiveMQSSL);
		setting.put("subscriberActiveMQAddress", ConfigSubscriberActiveMQ.subscriberActiveMQAddress);
		setting.put("subscriberActiveMQPort", ConfigSubscriberActiveMQ.subscriberActiveMQPort);
		setting.put("subscriberActiveMQClientID", ConfigSubscriberActiveMQ.subscriberActiveMQClientID);
		setting.put("subscriberActiveMQUsername", ConfigSubscriberActiveMQ.subscriberActiveMQUsername);
		setting.put("subscriberActiveMQPassword", ConfigSubscriberActiveMQ.subscriberActiveMQPassword);
		setting.put("subscriberActiveMQTopic", ConfigSubscriberActiveMQ.subscriberActiveMQTopic);
		setting.put("subscriberActiveMQTimeout", ConfigSubscriberActiveMQ.subscriberActiveMQTimeout);
		setting.put("subscriberActiveMQTimeToLeave", ConfigSubscriberActiveMQ.subscriberActiveMQTimeToLeave);
		setting.put("subscriberActiveMQReconnectDelay", ConfigSubscriberActiveMQ.subscriberActiveMQReconnectDelay);
		setting.put("subscriberActiveMQRefresh", ConfigSubscriberActiveMQ.subscriberActiveMQRefresh);
		return setting;
	}
	
	public static void save(String path) {
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		FileConfigUtil.prepareDirectory(fileName);	
		try 
		{
			FileUtil.write(fileName, ConfigSubscriberActiveMQ.toJSONObject().toString().getBytes());
		} 
		catch (IOException e) 
		{
			logger.error(e.getMessage(), e);
		}
	}
	public static void save()
	{
		ConfigSubscriberActiveMQ.save(ConfigSubscriberActiveMQ.configPath);
	}
	public static void load(String path)
	{
		ConfigSubscriberActiveMQ.configPath = path;
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		FileConfigUtil.prepareDirectory(fileName);
		byte[] data = null;
		try 
		{
			data = FileConfigUtil.read(fileName);
			ConfigSubscriberActiveMQ.loaded = true;
		} 
		catch (FileNotFoundException e1) 
		{
			ConfigSubscriberActiveMQ.loaded = false;
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

				ConfigSubscriberActiveMQ.subscriberActiveMQEnable = setting.optBoolean("subscriberActiveMQEnable", false);
				ConfigSubscriberActiveMQ.subscriberActiveMQSSL = setting.optBoolean("subscriberActiveMQSSL", false);
				ConfigSubscriberActiveMQ.subscriberActiveMQAddress = setting.optString("subscriberActiveMQAddress", "");
				ConfigSubscriberActiveMQ.subscriberActiveMQPort = setting.optInt("subscriberActiveMQPort", 0);
				ConfigSubscriberActiveMQ.subscriberActiveMQClientID = setting.optString("subscriberActiveMQClientID", "");
				ConfigSubscriberActiveMQ.subscriberActiveMQUsername = setting.optString("subscriberActiveMQUsername", "");
				ConfigSubscriberActiveMQ.subscriberActiveMQPassword = setting.optString("subscriberActiveMQPassword", "");
				ConfigSubscriberActiveMQ.subscriberActiveMQTopic = setting.optString("subscriberActiveMQTopic", "");
				ConfigSubscriberActiveMQ.subscriberActiveMQTimeout = setting.optInt("subscriberActiveMQTimeout", 0);
				ConfigSubscriberActiveMQ.subscriberActiveMQTimeToLeave = setting.optInt("subscriberActiveMQTimeToLeave", 0);
				ConfigSubscriberActiveMQ.subscriberActiveMQReconnectDelay = setting.optLong("subscriberActiveMQReconnectDelay", 0);
				ConfigSubscriberActiveMQ.subscriberActiveMQRefresh = setting.optInt("subscriberActiveMQRefresh", 0);
			}
			catch(JSONException e)
			{
				/**
				 * Do nothing
				 */
			}
		}
	}
	
	public static void reset() {
		ConfigSubscriberActiveMQ.subscriberActiveMQEnable = false;
		ConfigSubscriberActiveMQ.subscriberActiveMQSSL = false;
		ConfigSubscriberActiveMQ.subscriberActiveMQAddress = "";
		ConfigSubscriberActiveMQ.subscriberActiveMQPort = 0;
		ConfigSubscriberActiveMQ.subscriberActiveMQClientID = "";
		ConfigSubscriberActiveMQ.subscriberActiveMQUsername = "";
		ConfigSubscriberActiveMQ.subscriberActiveMQPassword = "";
		ConfigSubscriberActiveMQ.subscriberActiveMQTopic = "";
		ConfigSubscriberActiveMQ.subscriberActiveMQTimeout = 0;
		ConfigSubscriberActiveMQ.subscriberActiveMQRefresh = 0;
	}

	public static boolean isSubscriberActiveMQEnable() {
		return subscriberActiveMQEnable;
	}

	public static void setSubscriberActiveMQEnable(boolean subscriberActiveMQEnable) {
		ConfigSubscriberActiveMQ.subscriberActiveMQEnable = subscriberActiveMQEnable;
	}

	public static boolean isSubscriberActiveMQSSL() {
		return subscriberActiveMQSSL;
	}

	public static void setSubscriberActiveMQSSL(boolean subscriberActiveMQSSL) {
		ConfigSubscriberActiveMQ.subscriberActiveMQSSL = subscriberActiveMQSSL;
	}

	public static String getSubscriberActiveMQAddress() {
		return subscriberActiveMQAddress;
	}

	public static void setSubscriberActiveMQAddress(String subscriberActiveMQAddress) {
		ConfigSubscriberActiveMQ.subscriberActiveMQAddress = subscriberActiveMQAddress;
	}

	public static int getSubscriberActiveMQPort() {
		return subscriberActiveMQPort;
	}

	public static void setSubscriberActiveMQPort(int subscriberActiveMQPort) {
		ConfigSubscriberActiveMQ.subscriberActiveMQPort = subscriberActiveMQPort;
	}

	public static String getSubscriberActiveMQClientID() {
		return subscriberActiveMQClientID;
	}

	public static void setSubscriberActiveMQClientID(String subscriberActiveMQClientID) {
		ConfigSubscriberActiveMQ.subscriberActiveMQClientID = subscriberActiveMQClientID;
	}

	public static String getSubscriberActiveMQUsername() {
		return subscriberActiveMQUsername;
	}

	public static void setSubscriberActiveMQUsername(String subscriberActiveMQUsername) {
		ConfigSubscriberActiveMQ.subscriberActiveMQUsername = subscriberActiveMQUsername;
	}

	public static String getSubscriberActiveMQPassword() {
		return subscriberActiveMQPassword;
	}

	public static void setSubscriberActiveMQPassword(String subscriberActiveMQPassword) {
		ConfigSubscriberActiveMQ.subscriberActiveMQPassword = subscriberActiveMQPassword;
	}

	public static String getSubscriberActiveMQTopic() {
		return subscriberActiveMQTopic;
	}

	public static void setSubscriberActiveMQTopic(String subscriberActiveMQTopic) {
		ConfigSubscriberActiveMQ.subscriberActiveMQTopic = subscriberActiveMQTopic;
	}

	public static int getSubscriberActiveMQTimeout() {
		return subscriberActiveMQTimeout;
	}

	public static void setSubscriberActiveMQTimeout(int subscriberActiveMQTimeout) {
		ConfigSubscriberActiveMQ.subscriberActiveMQTimeout = subscriberActiveMQTimeout;
	}

	public static long getSubscriberActiveMQTimeToLeave() {
		return subscriberActiveMQTimeToLeave;
	}

	public static void setSubscriberActiveMQTimeToLeave(long subscriberActiveMQTimeToLeave) {
		ConfigSubscriberActiveMQ.subscriberActiveMQTimeToLeave = subscriberActiveMQTimeToLeave;
	}

	public static int getSubscriberActiveMQRefresh() {
		return subscriberActiveMQRefresh;
	}

	public static void setSubscriberActiveMQRefresh(int subscriberActiveMQRefresh) {
		ConfigSubscriberActiveMQ.subscriberActiveMQRefresh = subscriberActiveMQRefresh;
	}

	public static long getSubscriberActiveMQReconnectDelay() {
		return subscriberActiveMQReconnectDelay;
	}

	public static void setSubscriberActiveMQReconnectDelay(long subscriberActiveMQReconnectDelay) {
		ConfigSubscriberActiveMQ.subscriberActiveMQReconnectDelay = subscriberActiveMQReconnectDelay;
	}

	public static boolean isLoaded() {
		return loaded;
	}

	public static void setLoaded(boolean loaded) {
		ConfigSubscriberActiveMQ.loaded = loaded;
	}

	public static boolean isConnected() {
		return connected;
	}

	public static void setConnected(boolean connected) {
		ConfigSubscriberActiveMQ.connected = connected;
	}

	public static long getSubscriberTimeToLeave() {
		return subscriberTimeToLeave;
	}

	public static void setSubscriberTimeToLeave(long subscriberTimeToLeave) {
		ConfigSubscriberActiveMQ.subscriberTimeToLeave = subscriberTimeToLeave;
	}

	

}
