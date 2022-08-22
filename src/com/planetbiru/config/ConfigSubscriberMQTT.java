package com.planetbiru.config;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.FileUtil;
import com.planetbiru.util.Utility;

public class ConfigSubscriberMQTT {

	private static String configPath = "";
	
	private static boolean subscriberMqttEnable = false;
	private static boolean subscriberMqttSSL = false;
	private static String subscriberMqttAddress = "127.0.0.1";
	private static int subscriberMqttPort = 1883;
	private static String subscriberMqttClientID = "";
	private static String subscriberMqttUsername = "";
	private static String subscriberMqttPassword = "";
	private static String subscriberMqttTopic = "sms";
	private static int subscriberMqttTimeout = 0;
	private static int subscriberMqttRefresh = 0;
	private static long subscriberMqttReconnectDelay = 0;
	private static long subscriberMqttQos = 0;
	private static boolean loaded = false;
	private static boolean connected = false;
	
	private static Logger logger = Logger.getLogger(ConfigSubscriberMQTT.class);


	
	private ConfigSubscriberMQTT()
	{
	}
	
	public static boolean echoTest()
	{
		return false;
	}
	
	public static JSONObject toJSONObject()
	{
		JSONObject setting = new JSONObject();
		setting.put("subscriberMqttEnable", ConfigSubscriberMQTT.subscriberMqttEnable);
		setting.put("subscriberMqttSSL", ConfigSubscriberMQTT.subscriberMqttSSL);
		setting.put("subscriberMqttAddress", ConfigSubscriberMQTT.subscriberMqttAddress);
		setting.put("subscriberMqttPort", ConfigSubscriberMQTT.subscriberMqttPort);
		setting.put("subscriberMqttClientID", ConfigSubscriberMQTT.subscriberMqttClientID);
		setting.put("subscriberMqttUsername", ConfigSubscriberMQTT.subscriberMqttUsername);
		setting.put("subscriberMqttPassword", ConfigSubscriberMQTT.subscriberMqttPassword);
		setting.put("subscriberMqttTopic", ConfigSubscriberMQTT.subscriberMqttTopic);
		setting.put("subscriberMqttTimeout", ConfigSubscriberMQTT.subscriberMqttTimeout);
		setting.put("subscriberMqttReconnectDelay", ConfigSubscriberMQTT.subscriberMqttReconnectDelay);
		setting.put("subscriberMqttRefresh", ConfigSubscriberMQTT.subscriberMqttRefresh);
		setting.put("subscriberMqttQos", ConfigSubscriberMQTT.subscriberMqttQos);
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
			FileUtil.write(fileName, ConfigSubscriberMQTT.toJSONObject().toString().getBytes());
		} 
		catch (IOException e) 
		{
			logger.error(e.getMessage(), e);
		}
	}
	public static void save()
	{
		ConfigSubscriberMQTT.save(ConfigSubscriberMQTT.configPath );
	}
	public static void load(String path)
	{
		ConfigSubscriberMQTT.configPath = path;
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
			ConfigSubscriberMQTT.loaded = true;
		} 
		catch (FileNotFoundException e1) 
		{
			ConfigSubscriberMQTT.loaded = false;
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

				ConfigSubscriberMQTT.subscriberMqttEnable = setting.optBoolean("subscriberMqttEnable", false);
				ConfigSubscriberMQTT.subscriberMqttSSL = setting.optBoolean("subscriberMqttSSL", false);
				ConfigSubscriberMQTT.subscriberMqttAddress = setting.optString("subscriberMqttAddress", "");
				ConfigSubscriberMQTT.subscriberMqttPort = setting.optInt("subscriberMqttPort", 0);
				ConfigSubscriberMQTT.subscriberMqttClientID = setting.optString("subscriberMqttClientID", "");
				ConfigSubscriberMQTT.subscriberMqttUsername = setting.optString("subscriberMqttUsername", "");
				ConfigSubscriberMQTT.subscriberMqttPassword = setting.optString("subscriberMqttPassword", "");
				ConfigSubscriberMQTT.subscriberMqttTopic = setting.optString("subscriberMqttTopic", "");
				ConfigSubscriberMQTT.subscriberMqttTimeout = setting.optInt("subscriberMqttTimeout", 0);
				ConfigSubscriberMQTT.subscriberMqttReconnectDelay = setting.optLong("subscriberMqttReconnectDelay", 0);
				ConfigSubscriberMQTT.subscriberMqttRefresh = setting.optInt("subscriberMqttRefresh", 0);
				ConfigSubscriberMQTT.subscriberMqttQos = setting.optInt("subscriberMqttQos", 0);
			}
			catch(JSONException e)
			{
				/**
				 * Do nothing
				 */
			}
		}
	}

	public static boolean isSubscriberMqttEnable() {
		return subscriberMqttEnable;
	}

	public static void setSubscriberMqttEnable(boolean subscriberMqttEnable) {
		ConfigSubscriberMQTT.subscriberMqttEnable = subscriberMqttEnable;
	}

	public static boolean isSubscriberMqttSSL() {
		return subscriberMqttSSL;
	}

	public static void setSubscriberMqttSSL(boolean subscriberMqttSSL) {
		ConfigSubscriberMQTT.subscriberMqttSSL = subscriberMqttSSL;
	}

	public static String getSubscriberMqttAddress() {
		return subscriberMqttAddress;
	}

	public static void setSubscriberMqttAddress(String subscriberMqttAddress) {
		ConfigSubscriberMQTT.subscriberMqttAddress = subscriberMqttAddress;
	}

	public static int getSubscriberMqttPort() {
		return subscriberMqttPort;
	}

	public static void setSubscriberMqttPort(int subscriberMqttPort) {
		ConfigSubscriberMQTT.subscriberMqttPort = subscriberMqttPort;
	}

	public static String getSubscriberMqttClientID() {
		return subscriberMqttClientID;
	}

	public static void setSubscriberMqttClientID(String subscriberMqttClientID) {
		ConfigSubscriberMQTT.subscriberMqttClientID = subscriberMqttClientID;
	}

	public static String getSubscriberMqttUsername() {
		return subscriberMqttUsername;
	}

	public static void setSubscriberMqttUsername(String subscriberMqttUsername) {
		ConfigSubscriberMQTT.subscriberMqttUsername = subscriberMqttUsername;
	}

	public static String getSubscriberMqttPassword() {
		return subscriberMqttPassword;
	}

	public static void setSubscriberMqttPassword(String subscriberMqttPassword) {
		ConfigSubscriberMQTT.subscriberMqttPassword = subscriberMqttPassword;
	}

	public static String getSubscriberMqttTopic() {
		return subscriberMqttTopic;
	}

	public static void setSubscriberMqttTopic(String subscriberMqttTopic) {
		ConfigSubscriberMQTT.subscriberMqttTopic = subscriberMqttTopic;
	}

	public static int getSubscriberMqttTimeout() {
		return subscriberMqttTimeout;
	}

	public static void setSubscriberMqttTimeout(int subscriberMqttTimeout) {
		ConfigSubscriberMQTT.subscriberMqttTimeout = subscriberMqttTimeout;
	}

	public static int getSubscriberMqttRefresh() {
		return subscriberMqttRefresh;
	}

	public static void setSubscriberMqttRefresh(int subscriberMqttRefresh) {
		ConfigSubscriberMQTT.subscriberMqttRefresh = subscriberMqttRefresh;
	}

	public static boolean isLoaded() {
		return loaded;
	}

	public static void setLoaded(boolean loaded) {
		ConfigSubscriberMQTT.loaded = loaded;
	}

	public static boolean isConnected() {
		return connected;
	}

	public static void setConnected(boolean connected) {
		ConfigSubscriberMQTT.connected = connected;
	}

	public static void reset() {
		ConfigSubscriberMQTT.subscriberMqttEnable = false;
		ConfigSubscriberMQTT.subscriberMqttSSL = false;
		ConfigSubscriberMQTT.subscriberMqttAddress = "";
		ConfigSubscriberMQTT.subscriberMqttPort = 0;
		ConfigSubscriberMQTT.subscriberMqttClientID = "";
		ConfigSubscriberMQTT.subscriberMqttUsername = "";
		ConfigSubscriberMQTT.subscriberMqttPassword = "";
		ConfigSubscriberMQTT.subscriberMqttTopic = "";
		ConfigSubscriberMQTT.subscriberMqttTimeout = 0;
		ConfigSubscriberMQTT.subscriberMqttRefresh = 0;
		ConfigSubscriberMQTT.subscriberMqttQos = 0;
	}

	public static long getsubscriberMqttReconnectDelay() {
		return subscriberMqttReconnectDelay;
	}

	public static void setSubscriberMqttReconnectDelay(long subscriberMqttReconnectDelay) {
		ConfigSubscriberMQTT.subscriberMqttReconnectDelay = subscriberMqttReconnectDelay;
	}

	public static long getSubscriberMqttQos() {
		return subscriberMqttQos;
	}

	public static void setSubscriberMqttQos(long subscriberMqttQos) {
		ConfigSubscriberMQTT.subscriberMqttQos = subscriberMqttQos;
	}

}
