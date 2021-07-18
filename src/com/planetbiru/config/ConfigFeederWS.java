package com.planetbiru.config;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.Utility;

public class ConfigFeederWS {
	
	private static boolean feederWsEnable = false;
	private static boolean feederWsSSL = false;
	private static String feederWsAddress = "";
	private static int feederWsPort = 0;
	private static String feederWsPath = "";
	private static String feederWsUsername = "";
	private static String feederWsPassword = "";
	private static String feederWsChannel = "";
	private static long feederWsTimeout = 0;
	private static long feederWsReconnectDelay = 0;
	private static long feederWsRefresh = 0;
	private static boolean connected = false;

	private static String configPath = "";
	
	private static Logger logger = Logger.getLogger(ConfigFeederWS.class);
	
	private ConfigFeederWS()
	{
		
	}
	
	public static JSONObject toJSONObject()
	{
		JSONObject setting = new JSONObject();
		setting.put("feederWsEnable", ConfigFeederWS.feederWsEnable);
		setting.put("feederWsSSL", ConfigFeederWS.feederWsSSL);
		setting.put("feederWsAddress", ConfigFeederWS.feederWsAddress);
		setting.put("feederWsPort", ConfigFeederWS.feederWsPort);
		setting.put("feederWsPath", ConfigFeederWS.feederWsPath);
		setting.put("feederWsUsername", ConfigFeederWS.feederWsUsername);
		setting.put("feederWsPassword", ConfigFeederWS.feederWsPassword);
		setting.put("feederWsChannel", ConfigFeederWS.feederWsChannel);
		setting.put("feederWsTimeout", ConfigFeederWS.feederWsTimeout);
		setting.put("feederWsReconnectDelay", ConfigFeederWS.feederWsReconnectDelay);
		setting.put("feederWsRefresh", ConfigFeederWS.feederWsRefresh);
		return setting;
	}
	
	public static void save(String path) {
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		ConfigFeederWS.prepareDir(fileName);	
		try 
		{
			FileConfigUtil.write(fileName, ConfigFeederWS.toJSONObject().toString().getBytes());
		} 
		catch (IOException e) 
		{
			logger.error(e.getMessage(), e);
		}
	}
	public static void save()
	{
		ConfigFeederWS.save(ConfigFeederWS.configPath);
	}
	public static void load(String path)
	{
		ConfigFeederWS.configPath = path;
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		ConfigFeederWS.prepareDir(fileName);
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
				ConfigFeederWS.feederWsEnable = setting.optBoolean("feederWsEnable", false);
				ConfigFeederWS.feederWsSSL = setting.optBoolean("feederWsSSL", false);
				ConfigFeederWS.feederWsAddress = setting.optString("feederWsAddress", "");
				ConfigFeederWS.feederWsPort = setting.optInt("feederWsPort", 0);
				ConfigFeederWS.feederWsPath = setting.optString("feederWsPath", "");
				ConfigFeederWS.feederWsUsername = setting.optString("feederWsUsername", "");
				ConfigFeederWS.feederWsPassword = setting.optString("feederWsPassword", "");
				ConfigFeederWS.feederWsChannel = setting.optString("feederWsChannel", "");
				ConfigFeederWS.feederWsTimeout = setting.optInt("feederWsTimeout", 0);
				ConfigFeederWS.feederWsReconnectDelay = setting.optInt("feederWsReconnectDelay", 0);
				ConfigFeederWS.feederWsRefresh = setting.optInt("feederWsRefresh", 0);

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

	public static boolean isFeederWsEnable() {
		return feederWsEnable;
	}

	public static void setFeederWsEnable(boolean feederWsEnable) {
		ConfigFeederWS.feederWsEnable = feederWsEnable;
	}

	public static boolean isFeederWsSSL() {
		return feederWsSSL;
	}

	public static void setFeederWsSSL(boolean feederWsSSL) {
		ConfigFeederWS.feederWsSSL = feederWsSSL;
	}

	public static String getFeederWsAddress() {
		return feederWsAddress;
	}

	public static void setFeederWsAddress(String feederWsAddress) {
		ConfigFeederWS.feederWsAddress = feederWsAddress;
	}

	public static int getFeederWsPort() {
		return feederWsPort;
	}

	public static void setFeederWsPort(int feederWsPort) {
		ConfigFeederWS.feederWsPort = feederWsPort;
	}

	public static String getFeederWsPath() {
		return feederWsPath;
	}

	public static void setFeederWsPath(String feederWsPath) {
		ConfigFeederWS.feederWsPath = feederWsPath;
	}

	public static String getFeederWsUsername() {
		return feederWsUsername;
	}

	public static void setFeederWsUsername(String feederWsUsername) {
		ConfigFeederWS.feederWsUsername = feederWsUsername;
	}

	public static String getFeederWsPassword() {
		return feederWsPassword;
	}

	public static void setFeederWsPassword(String feederWsPassword) {
		ConfigFeederWS.feederWsPassword = feederWsPassword;
	}

	public static String getFeederWsChannel() {
		return feederWsChannel;
	}

	public static void setFeederWsChannel(String feederWsChannel) {
		ConfigFeederWS.feederWsChannel = feederWsChannel;
	}

	public static long getFeederWsTimeout() {
		return feederWsTimeout;
	}

	public static void setFeederWsTimeout(long feederWsTimeout) {
		ConfigFeederWS.feederWsTimeout = feederWsTimeout;
	}

	public static long getFeederWsReconnectDelay() {
		return feederWsReconnectDelay;
	}

	public static void setFeederWsReconnectDelay(long feederWsReconnectDelay) {
		ConfigFeederWS.feederWsReconnectDelay = feederWsReconnectDelay;
	}

	public static long getFeederWsRefresh() {
		return feederWsRefresh;
	}

	public static void setFeederWsRefresh(long feederWsRefresh) {
		ConfigFeederWS.feederWsRefresh = feederWsRefresh;
	}

	public static boolean isConnected() {
		return connected;
	}

	public static void setConnected(boolean connected) {
		ConfigFeederWS.connected = connected;
	}

	

	
}
