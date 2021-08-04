package com.planetbiru.config;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.Utility;

public class ConfigSubscriberWS {
	
	private static boolean subscriberWsEnable = false;
	private static boolean subscriberWsSSL = false;
	private static String subscriberWsAddress = "";
	private static int subscriberWsPort = 0;
	private static String subscriberWsPath = "";
	private static String subscriberWsUsername = "";
	private static String subscriberWsPassword = "";
	private static String subscriberWsTopic = "";
	private static long subscriberWsTimeout = 0;
	private static long subscriberWsReconnectDelay = 0;
	private static long subscriberWsRefresh = 0;
	private static boolean connected = false;

	private static String configPath = "";
	
	private static Logger logger = Logger.getLogger(ConfigSubscriberWS.class);
	
	private ConfigSubscriberWS()
	{
		
	}
	
	public static JSONObject toJSONObject()
	{
		JSONObject setting = new JSONObject();
		setting.put("subscriberWsEnable", ConfigSubscriberWS.subscriberWsEnable);
		setting.put("subscriberWsSSL", ConfigSubscriberWS.subscriberWsSSL);
		setting.put("subscriberWsAddress", ConfigSubscriberWS.subscriberWsAddress);
		setting.put("subscriberWsPort", ConfigSubscriberWS.subscriberWsPort);
		setting.put("subscriberWsPath", ConfigSubscriberWS.subscriberWsPath);
		setting.put("subscriberWsUsername", ConfigSubscriberWS.subscriberWsUsername);
		setting.put("subscriberWsPassword", ConfigSubscriberWS.subscriberWsPassword);
		setting.put("subscriberWsTopic", ConfigSubscriberWS.subscriberWsTopic);
		setting.put("subscriberWsTimeout", ConfigSubscriberWS.subscriberWsTimeout);
		setting.put("subscriberWsReconnectDelay", ConfigSubscriberWS.subscriberWsReconnectDelay);
		setting.put("subscriberWsRefresh", ConfigSubscriberWS.subscriberWsRefresh);
		return setting;
	}
	
	public static void save(String path) {
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		ConfigSubscriberWS.prepareDir(fileName);	
		try 
		{
			FileConfigUtil.write(fileName, ConfigSubscriberWS.toJSONObject().toString().getBytes());
		} 
		catch (IOException e) 
		{
			logger.error(e.getMessage(), e);
		}
	}
	public static void save()
	{
		ConfigSubscriberWS.save(ConfigSubscriberWS.configPath);
	}
	public static void load(String path)
	{
		ConfigSubscriberWS.configPath = path;
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		ConfigSubscriberWS.prepareDir(fileName);
		byte[] data = null;
		try 
		{
			data = FileConfigUtil.read(fileName);
		} 
		catch (FileNotFoundException e1) 
		{
			if(Config.isLogConfigNotFound())
			{
				logger.error(e1.getMessage(), e1);
			}
			/**
			 * Do nothing
			 */
		}
		if(data != null)
		{
			String text = new String(data);
			try
			{
				JSONObject setting = new JSONObject(text);
				ConfigSubscriberWS.subscriberWsEnable = setting.optBoolean("subscriberWsEnable", false);
				ConfigSubscriberWS.subscriberWsSSL = setting.optBoolean("subscriberWsSSL", false);
				ConfigSubscriberWS.subscriberWsAddress = setting.optString("subscriberWsAddress", "");
				ConfigSubscriberWS.subscriberWsPort = setting.optInt("subscriberWsPort", 0);
				ConfigSubscriberWS.subscriberWsPath = setting.optString("subscriberWsPath", "");
				ConfigSubscriberWS.subscriberWsUsername = setting.optString("subscriberWsUsername", "");
				ConfigSubscriberWS.subscriberWsPassword = setting.optString("subscriberWsPassword", "");
				ConfigSubscriberWS.subscriberWsTopic = setting.optString("subscriberWsTopic", "");
				ConfigSubscriberWS.subscriberWsTimeout = setting.optInt("subscriberWsTimeout", 0);
				ConfigSubscriberWS.subscriberWsReconnectDelay = setting.optInt("subscriberWsReconnectDelay", 0);
				ConfigSubscriberWS.subscriberWsRefresh = setting.optInt("subscriberWsRefresh", 0);

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

	public static boolean isSubscriberWsEnable() {
		return subscriberWsEnable;
	}

	public static void setSubscriberWsEnable(boolean subscriberWsEnable) {
		ConfigSubscriberWS.subscriberWsEnable = subscriberWsEnable;
	}

	public static boolean isSubscriberWsSSL() {
		return subscriberWsSSL;
	}

	public static void setSubscriberWsSSL(boolean subscriberWsSSL) {
		ConfigSubscriberWS.subscriberWsSSL = subscriberWsSSL;
	}

	public static String getSubscriberWsAddress() {
		return subscriberWsAddress;
	}

	public static void setSubscriberWsAddress(String subscriberWsAddress) {
		ConfigSubscriberWS.subscriberWsAddress = subscriberWsAddress;
	}

	public static int getSubscriberWsPort() {
		return subscriberWsPort;
	}

	public static void setSubscriberWsPort(int subscriberWsPort) {
		ConfigSubscriberWS.subscriberWsPort = subscriberWsPort;
	}

	public static String getSubscriberWsPath() {
		return subscriberWsPath;
	}

	public static void setSubscriberWsPath(String subscriberWsPath) {
		ConfigSubscriberWS.subscriberWsPath = subscriberWsPath;
	}

	public static String getSubscriberWsUsername() {
		return subscriberWsUsername;
	}

	public static void setSubscriberWsUsername(String subscriberWsUsername) {
		ConfigSubscriberWS.subscriberWsUsername = subscriberWsUsername;
	}

	public static String getSubscriberWsPassword() {
		return subscriberWsPassword;
	}

	public static void setSubscriberWsPassword(String subscriberWsPassword) {
		ConfigSubscriberWS.subscriberWsPassword = subscriberWsPassword;
	}

	public static String getSubscriberWsTopic() {
		return subscriberWsTopic;
	}

	public static void setSubscriberWsTopic(String subscriberWsTopic) {
		ConfigSubscriberWS.subscriberWsTopic = subscriberWsTopic;
	}

	public static long getSubscriberWsTimeout() {
		return subscriberWsTimeout;
	}

	public static void setSubscriberWsTimeout(long subscriberWsTimeout) {
		ConfigSubscriberWS.subscriberWsTimeout = subscriberWsTimeout;
	}

	public static long getSubscriberWsReconnectDelay() {
		return subscriberWsReconnectDelay;
	}

	public static void setSubscriberWsReconnectDelay(long subscriberWsReconnectDelay) {
		ConfigSubscriberWS.subscriberWsReconnectDelay = subscriberWsReconnectDelay;
	}

	public static long getSubscriberWsRefresh() {
		return subscriberWsRefresh;
	}

	public static void setSubscriberWsRefresh(long subscriberWsRefresh) {
		ConfigSubscriberWS.subscriberWsRefresh = subscriberWsRefresh;
	}

	public static boolean isConnected() {
		return connected;
	}

	public static void setConnected(boolean connected) {
		ConfigSubscriberWS.connected = connected;
	}

	public static void reset() {
		ConfigSubscriberWS.subscriberWsEnable = false;
		ConfigSubscriberWS.subscriberWsSSL = false;
		ConfigSubscriberWS.subscriberWsAddress = "";
		ConfigSubscriberWS.subscriberWsPort = 0;
		ConfigSubscriberWS.subscriberWsPath = "";
		ConfigSubscriberWS.subscriberWsUsername = "";
		ConfigSubscriberWS.subscriberWsPassword = "";
		ConfigSubscriberWS.subscriberWsTopic = "";
		ConfigSubscriberWS.subscriberWsTimeout = 0;
		ConfigSubscriberWS.subscriberWsReconnectDelay = 0;
		ConfigSubscriberWS.subscriberWsRefresh = 0;
	}

	

	
}
