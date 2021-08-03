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

public class ConfigFeederAMQP {

	private static String configPath = "";
	
	private static boolean feederAmqpEnable = false;
	private static boolean feederAmqpSSL = false;
	private static String feederAmqpAddress = "";
	private static int feederAmqpPort = 0;
	private static String feederAmqpPath = "";
	private static String feederAmqpUsername = "";
	private static String feederAmqpPassword = "";
	private static String feederAmqpTopic = "";
	private static int feederAmqpTimeout = 0;
	private static int feederAmqpRefresh = 0;
	private static boolean loaded = false;
	private static boolean connected = false;
	
	private static Logger logger = Logger.getLogger(ConfigFeederAMQP.class);

	
	private ConfigFeederAMQP()
	{
	}
	
	public static boolean echoTest()
	{
		return false;
	}
	
	public static JSONObject toJSONObject()
	{
		JSONObject setting = new JSONObject();
		setting.put("feederAmqpEnable", ConfigFeederAMQP.feederAmqpEnable);
		setting.put("feederAmqpSSL", ConfigFeederAMQP.feederAmqpSSL);
		setting.put("feederAmqpAddress", ConfigFeederAMQP.feederAmqpAddress);
		setting.put("feederAmqpPort", ConfigFeederAMQP.feederAmqpPort);
		setting.put("feederAmqpPath", ConfigFeederAMQP.feederAmqpPath);
		setting.put("feederAmqpUsername", ConfigFeederAMQP.feederAmqpUsername);
		setting.put("feederAmqpPassword", ConfigFeederAMQP.feederAmqpPassword);
		setting.put("feederAmqpTopic", ConfigFeederAMQP.feederAmqpTopic);
		setting.put("feederAmqpTimeout", ConfigFeederAMQP.feederAmqpTimeout);
		setting.put("feederAmqpRefresh", ConfigFeederAMQP.feederAmqpRefresh);
		return setting;
	}
	
	public static void save(String path) {
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		ConfigFeederAMQP.prepareDir(fileName);	
		try 
		{
			FileUtil.write(fileName, ConfigFeederAMQP.toJSONObject().toString().getBytes());
		} 
		catch (IOException e) 
		{
			logger.error(e.getMessage(), e);
		}
	}
	public static void save()
	{
		ConfigFeederAMQP.save(ConfigFeederAMQP.configPath );
	}
	public static void load(String path)
	{
		ConfigFeederAMQP.configPath = path;
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		ConfigFeederAMQP.prepareDir(fileName);
		byte[] data = null;
		try 
		{
			data = FileConfigUtil.read(fileName);
			ConfigFeederAMQP.loaded = true;
		} 
		catch (FileNotFoundException e1) 
		{
			ConfigFeederAMQP.loaded = false;
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

				ConfigFeederAMQP.feederAmqpEnable = setting.optBoolean("feederAmqpEnable", false);
				ConfigFeederAMQP.feederAmqpSSL = setting.optBoolean("feederAmqpSSL", false);
				ConfigFeederAMQP.feederAmqpAddress = setting.optString("feederAmqpAddress", "");
				ConfigFeederAMQP.feederAmqpPort = setting.optInt("feederAmqpPort", 0);
				ConfigFeederAMQP.feederAmqpPath = setting.optString("feederAmqpPath", "");
				ConfigFeederAMQP.feederAmqpUsername = setting.optString("feederAmqpUsername", "");
				ConfigFeederAMQP.feederAmqpPassword = setting.optString("feederAmqpPassword", "");
				ConfigFeederAMQP.feederAmqpTopic = setting.optString("feederAmqpTopic", "");
				ConfigFeederAMQP.feederAmqpTimeout = setting.optInt("feederAmqpTimeout", 0);
				ConfigFeederAMQP.feederAmqpRefresh = setting.optInt("feederAmqpRefresh", 0);
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
	public static boolean isFeederAmqpEnable() {
		return feederAmqpEnable;
	}

	public static void setFeederAmqpEnable(boolean feederAmqpEnable) {
		ConfigFeederAMQP.feederAmqpEnable = feederAmqpEnable;
	}

	public static boolean isFeederAmqpSSL() {
		return feederAmqpSSL;
	}

	public static void setFeederAmqpSSL(boolean feederAmqpSSL) {
		ConfigFeederAMQP.feederAmqpSSL = feederAmqpSSL;
	}

	public static String getFeederAmqpAddress() {
		return feederAmqpAddress;
	}

	public static void setFeederAmqpAddress(String feederAmqpAddress) {
		ConfigFeederAMQP.feederAmqpAddress = feederAmqpAddress;
	}

	public static int getFeederAmqpPort() {
		return feederAmqpPort;
	}

	public static void setFeederAmqpPort(int feederAmqpPort) {
		ConfigFeederAMQP.feederAmqpPort = feederAmqpPort;
	}

	public static String getFeederAmqpPath() {
		return feederAmqpPath;
	}

	public static void setFeederAmqpPath(String feederAmqpPath) {
		ConfigFeederAMQP.feederAmqpPath = feederAmqpPath;
	}

	public static String getFeederAmqpUsername() {
		return feederAmqpUsername;
	}

	public static void setFeederAmqpUsername(String feederAmqpUsername) {
		ConfigFeederAMQP.feederAmqpUsername = feederAmqpUsername;
	}

	public static String getFeederAmqpPassword() {
		return feederAmqpPassword;
	}

	public static void setFeederAmqpPassword(String feederAmqpPassword) {
		ConfigFeederAMQP.feederAmqpPassword = feederAmqpPassword;
	}

	public static String getFeederAmqpTopic() {
		return feederAmqpTopic;
	}

	public static void setFeederAmqpTopic(String feederAmqpTopic) {
		ConfigFeederAMQP.feederAmqpTopic = feederAmqpTopic;
	}

	public static int getFeederAmqpTimeout() {
		return feederAmqpTimeout;
	}

	public static void setFeederAmqpTimeout(int feederAmqpTimeout) {
		ConfigFeederAMQP.feederAmqpTimeout = feederAmqpTimeout;
	}

	public static int getFeederAmqpRefresh() {
		return feederAmqpRefresh;
	}

	public static void setFeederAmqpRefresh(int feederAmqpRefresh) {
		ConfigFeederAMQP.feederAmqpRefresh = feederAmqpRefresh;
	}

	public static boolean isLoaded() {
		return loaded;
	}

	public static void setLoaded(boolean loaded) {
		ConfigFeederAMQP.loaded = loaded;
	}

	public static boolean isConnected() {
		return connected;
	}

	public static void setConnected(boolean connected) {
		ConfigFeederAMQP.connected = connected;
	}

	public static void reset() {
		ConfigFeederAMQP.feederAmqpEnable = false;
		ConfigFeederAMQP.feederAmqpSSL = false;
		ConfigFeederAMQP.feederAmqpAddress = "";
		ConfigFeederAMQP.feederAmqpPort = 0;
		ConfigFeederAMQP.feederAmqpPath = "";
		ConfigFeederAMQP.feederAmqpUsername = "";
		ConfigFeederAMQP.feederAmqpPassword = "";
		ConfigFeederAMQP.feederAmqpTopic = "";
		ConfigFeederAMQP.feederAmqpTimeout = 0;
		ConfigFeederAMQP.feederAmqpRefresh = 0;
	}

}
