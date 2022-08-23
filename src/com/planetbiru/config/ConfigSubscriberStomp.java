package com.planetbiru.config;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.FileUtil;
import com.planetbiru.util.Utility;

public class ConfigSubscriberStomp {

	private static String configPath = "";
	
	private static boolean subscriberStompEnable = false;
	private static boolean subscriberStompSSL = false;
	private static String subscriberStompAddress = "";
	private static int subscriberStompPort = 0;
	private static String subscriberStompPath = "";
	private static int subscriberStompDatabase = 0;
	private static String subscriberStompUsername = "";
	private static String subscriberStompPassword = "";
	private static String subscriberStompTopic = "";
	private static long subscriberStompReconnectDelay = 0;
	private static int subscriberStompTimeout = 0;
	private static int subscriberStompRefresh = 0;
	private static boolean loaded = false;
	private static boolean connected = false;
	
	private static Logger logger = Logger.getLogger(ConfigSubscriberStomp.class);

	
	private ConfigSubscriberStomp()
	{
	}
	
	public static boolean echoTest()
	{
		return false;
	}
	
	public static JSONObject toJSONObject()
	{
		JSONObject setting = new JSONObject();
		setting.put("subscriberStompEnable", ConfigSubscriberStomp.subscriberStompEnable);
		setting.put("subscriberStompSSL", ConfigSubscriberStomp.subscriberStompSSL);
		setting.put("subscriberStompAddress", ConfigSubscriberStomp.subscriberStompAddress);
		setting.put("subscriberStompPort", ConfigSubscriberStomp.subscriberStompPort);
		setting.put("subscriberStompDatabase", ConfigSubscriberStomp.subscriberStompDatabase);
		setting.put("subscriberStompPath", ConfigSubscriberStomp.subscriberStompPath);
		setting.put("subscriberStompUsername", ConfigSubscriberStomp.subscriberStompUsername);
		setting.put("subscriberStompPassword", ConfigSubscriberStomp.subscriberStompPassword);
		setting.put("subscriberStompTopic", ConfigSubscriberStomp.subscriberStompTopic);
		setting.put("subscriberStompReconnectDelay", ConfigSubscriberStomp.subscriberStompReconnectDelay);
		setting.put("subscriberStompTimeout", ConfigSubscriberStomp.subscriberStompTimeout);
		setting.put("subscriberStompRefresh", ConfigSubscriberStomp.subscriberStompRefresh);
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
			FileUtil.write(fileName, ConfigSubscriberStomp.toJSONObject().toString().getBytes());
		} 
		catch (IOException e) 
		{
			logger.error(e.getMessage(), e);
		}
	}
	public static void save()
	{
		ConfigSubscriberStomp.save(ConfigSubscriberStomp.configPath );
	}
	public static void load(String path)
	{
		ConfigSubscriberStomp.configPath = path;
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
			ConfigSubscriberStomp.loaded = true;
		} 
		catch (FileNotFoundException e1) 
		{
			ConfigSubscriberStomp.loaded = false;
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

				ConfigSubscriberStomp.subscriberStompEnable = setting.optBoolean("subscriberStompEnable", false);
				ConfigSubscriberStomp.subscriberStompSSL = setting.optBoolean("subscriberStompSSL", false);
				ConfigSubscriberStomp.subscriberStompAddress = setting.optString("subscriberStompAddress", "");
				ConfigSubscriberStomp.subscriberStompPort = setting.optInt("subscriberStompPort", 0);
				ConfigSubscriberStomp.subscriberStompDatabase = setting.optInt("subscriberStompDatabase", 0);
				ConfigSubscriberStomp.subscriberStompPath = setting.optString("subscriberStompPath", "");
				ConfigSubscriberStomp.subscriberStompUsername = setting.optString("subscriberStompUsername", "");
				ConfigSubscriberStomp.subscriberStompPassword = setting.optString("subscriberStompPassword", "");
				ConfigSubscriberStomp.subscriberStompTopic = setting.optString("subscriberStompTopic", "");
				ConfigSubscriberStomp.subscriberStompReconnectDelay = setting.optLong("subscriberStompReconnectDelay", 0);
				ConfigSubscriberStomp.subscriberStompTimeout = setting.optInt("subscriberStompTimeout", 0);
				ConfigSubscriberStomp.subscriberStompRefresh = setting.optInt("subscriberStompRefresh", 0);
			}
			catch(JSONException e)
			{
				/**
				 * Do nothing
				 */
			}
		}
	}

	public static boolean isSubscriberStompEnable() {
		return subscriberStompEnable;
	}

	public static void setSubscriberStompEnable(boolean subscriberStompEnable) {
		ConfigSubscriberStomp.subscriberStompEnable = subscriberStompEnable;
	}

	public static boolean isSubscriberStompSSL() {
		return subscriberStompSSL;
	}

	public static void setSubscriberStompSSL(boolean subscriberStompSSL) {
		ConfigSubscriberStomp.subscriberStompSSL = subscriberStompSSL;
	}

	public static int getSubscriberStompDatabase() {
		return subscriberStompDatabase;
	}

	public static void setSubscriberStompDatabase(int subscriberStompDatabase) {
		ConfigSubscriberStomp.subscriberStompDatabase = subscriberStompDatabase;
	}

	public static long getSubscriberStompReconnectDelay() {
		return subscriberStompReconnectDelay;
	}

	public static void setSubscriberStompReconnectDelay(long subscriberStompReconnectDelay) {
		ConfigSubscriberStomp.subscriberStompReconnectDelay = subscriberStompReconnectDelay;
	}

	public static String getSubscriberStompAddress() {
		return subscriberStompAddress;
	}

	public static void setSubscriberStompAddress(String subscriberStompAddress) {
		ConfigSubscriberStomp.subscriberStompAddress = subscriberStompAddress;
	}

	public static int getSubscriberStompPort() {
		return subscriberStompPort;
	}

	public static void setSubscriberStompPort(int subscriberStompPort) {
		ConfigSubscriberStomp.subscriberStompPort = subscriberStompPort;
	}

	public static String getSubscriberStompPath() {
		return subscriberStompPath;
	}

	public static void setSubscriberStompPath(String subscriberStompPath) {
		ConfigSubscriberStomp.subscriberStompPath = subscriberStompPath;
	}

	public static String getSubscriberStompUsername() {
		return subscriberStompUsername;
	}

	public static void setSubscriberStompUsername(String subscriberStompUsername) {
		ConfigSubscriberStomp.subscriberStompUsername = subscriberStompUsername;
	}

	public static String getSubscriberStompPassword() {
		return subscriberStompPassword;
	}

	public static void setSubscriberStompPassword(String subscriberStompPassword) {
		ConfigSubscriberStomp.subscriberStompPassword = subscriberStompPassword;
	}

	public static String getSubscriberStompTopic() {
		return subscriberStompTopic;
	}

	public static void setSubscriberStompTopic(String subscriberStompTopic) {
		ConfigSubscriberStomp.subscriberStompTopic = subscriberStompTopic;
	}

	public static int getSubscriberStompTimeout() {
		return subscriberStompTimeout;
	}

	public static void setSubscriberStompTimeout(int subscriberStompTimeout) {
		ConfigSubscriberStomp.subscriberStompTimeout = subscriberStompTimeout;
	}

	public static int getSubscriberStompRefresh() {
		return subscriberStompRefresh;
	}

	public static void setSubscriberStompRefresh(int subscriberStompRefresh) {
		ConfigSubscriberStomp.subscriberStompRefresh = subscriberStompRefresh;
	}

	public static boolean isLoaded() {
		return loaded;
	}

	public static void setLoaded(boolean loaded) {
		ConfigSubscriberStomp.loaded = loaded;
	}

	public static boolean isConnected() {
		return connected;
	}

	public static void setConnected(boolean connected) {
		ConfigSubscriberStomp.connected = connected;
	}

	public static void reset() {
		ConfigSubscriberStomp.subscriberStompEnable = false;
		ConfigSubscriberStomp.subscriberStompSSL = false;
		ConfigSubscriberStomp.subscriberStompAddress = "";
		ConfigSubscriberStomp.subscriberStompPort = 0;
		ConfigSubscriberStomp.subscriberStompDatabase = 0;
		ConfigSubscriberStomp.subscriberStompPath = "";
		ConfigSubscriberStomp.subscriberStompUsername = "";
		ConfigSubscriberStomp.subscriberStompPassword = "";
		ConfigSubscriberStomp.subscriberStompTopic = "";
		ConfigSubscriberStomp.subscriberStompTimeout = 0;
		ConfigSubscriberStomp.subscriberStompRefresh = 0;
	}

	public static long getSubscriberWsReconnectDelay()  //NOSONAR
	{
		return subscriberStompReconnectDelay;
	}

	public static void setSubscriberWsReconnectDelay(long subscriberStompReconnectDelay)  //NOSONAR
	{
		ConfigSubscriberStomp.subscriberStompReconnectDelay = subscriberStompReconnectDelay;
	}

	
}
