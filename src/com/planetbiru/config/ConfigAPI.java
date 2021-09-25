package com.planetbiru.config;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.Utility;

public class ConfigAPI {
	private static String configPath = "";
	
	private static Logger logger = Logger.getLogger(ConfigAPI.class);
	private ConfigAPI()
	{
		
	}

	private static int httpPort = 80;
	private static int httpsPort = 443;
	private static boolean httpEnable = true;	
	private static boolean httpsEnable = false;	
	private static String otpPath = "/";
	private static String messagePath = "/";
	private static String smsPath = "/";
	private static String emailPath = "/";
	private static String blockingPath = "/";
	private static String unblockingPath = "/";
	
	public static void load(String path) {
		ConfigAPI.configPath = path;
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

					int lHttpPort = json.optInt("httpPort", 0);
					int lHttpsPort = json.optInt("httpsPort", 0);
					boolean lHttpEnable = json.optBoolean("httpEnable", false);	
					boolean lHttpsEnable = json.optBoolean("httpsEnable", false);	
					String lMessagePath = json.optString("messagePath", "");
					String lOTPPath = json.optString("otpPath", "");
					String lSMSPath = json.optString("smsPath", "");
					String lEmailPath = json.optString("emailPath", "");
					String lBlockingPath = json.optString("blockingPath", "");
					String lUnblockingPath = json.optString("unblockingPath", "");
					
					ConfigAPI.httpPort = lHttpPort;
					ConfigAPI.httpsPort = lHttpsPort;
					ConfigAPI.httpEnable = lHttpEnable;
					ConfigAPI.httpsEnable = lHttpsEnable;					
					ConfigAPI.otpPath = lOTPPath;
					ConfigAPI.messagePath = lMessagePath;
					ConfigAPI.smsPath = lSMSPath;
					ConfigAPI.emailPath = lEmailPath;
					ConfigAPI.blockingPath = lBlockingPath;
					ConfigAPI.unblockingPath = lUnblockingPath;
				}
			}
		} 
		catch (FileNotFoundException | JSONException e) 
		{
			if(Config.isLogConfigNotFound())
			{
				logger.error(e.getMessage(), e);
			}
		}
		
	}	

	public static void save() {
		ConfigAPI.save(ConfigAPI.configPath);
	}
	public static void save(String path) {
		JSONObject config = getJSONObject();
		ConfigAPI.save(path, config);
	}

	public static void save(String path, JSONObject config) {		
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		ConfigAPI.prepareDir(fileName);
		
		try 
		{
			FileConfigUtil.write(fileName, config.toString().getBytes());
		}
		catch (IOException e) 
		{
			logger.error(e.getMessage(), e);
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
	
	public static JSONObject getJSONObject() {
		JSONObject config = new JSONObject();
		config.put("httpPort", ConfigAPI.httpPort);
		config.put("httpsPort", ConfigAPI.httpsPort);
		config.put("httpEnable", ConfigAPI.httpEnable);
		config.put("httpsEnable", ConfigAPI.httpsEnable);
		config.put("otpPath", ConfigAPI.otpPath);
		config.put("messagePath", ConfigAPI.messagePath);
		config.put("smsPath", ConfigAPI.smsPath);
		config.put("emailPath", ConfigAPI.emailPath);
		config.put("blockingPath", ConfigAPI.blockingPath);
		config.put("unblockingPath", ConfigAPI.unblockingPath);
		return config;
	}

	public static JSONObject toJSONObject() {
		return getJSONObject();
	}

	public static String getConfigPath() {
		return configPath;
	}

	public static void setConfigPath(String configPath) {
		ConfigAPI.configPath = configPath;
	}

	public static int getHttpPort() {
		return httpPort;
	}

	public static void setHttpPort(int httpPort) {
		ConfigAPI.httpPort = httpPort;
	}

	public static int getHttpsPort() {
		return httpsPort;
	}

	public static void setHttpsPort(int httpsPort) {
		ConfigAPI.httpsPort = httpsPort;
	}

	public static boolean isHttpEnable() {
		return httpEnable;
	}

	public static void setHttpEnable(boolean httpEnable) {
		ConfigAPI.httpEnable = httpEnable;
	}

	public static boolean isHttpsEnable() {
		return httpsEnable;
	}

	public static void setHttpsEnable(boolean httpsEnable) {
		ConfigAPI.httpsEnable = httpsEnable;
	}

	public static String getOtpPath() {
		return otpPath;
	}

	public static void setOtpPath(String otpPath) {
		ConfigAPI.otpPath = otpPath;
	}

	public static String getMessagePath() {
		return messagePath;
	}

	public static void setMessagePath(String messagePath) {
		ConfigAPI.messagePath = messagePath;
	}

	public static String getSmsPath() {
		return smsPath;
	}

	public static void setSmsPath(String smsPath) {
		ConfigAPI.smsPath = smsPath;
	}

	public static String getEmailPath() {
		return emailPath;
	}

	public static void setEmailPath(String emailPath) {
		ConfigAPI.emailPath = emailPath;
	}

	public static String getBlockingPath() {
		return blockingPath;
	}

	public static void setBlockingPath(String blockingPath) {
		ConfigAPI.blockingPath = blockingPath;
	}

	public static String getUnblockingPath() {
		return unblockingPath;
	}

	public static void setUnblockingPath(String unblockingPath) {
		ConfigAPI.unblockingPath = unblockingPath;
	}

	public static void reset() {
		ConfigAPI.httpPort = 80;
		ConfigAPI.httpsPort = 443;
		ConfigAPI.httpEnable = false;
		ConfigAPI.httpsEnable = false;					
		ConfigAPI.otpPath = "/api/otp";
		ConfigAPI.messagePath = "/api/message";
		ConfigAPI.smsPath = "/api/sms";
		ConfigAPI.emailPath = "/api/email";
		ConfigAPI.blockingPath = "/api/blocking";
		ConfigAPI.unblockingPath = "/api/unblocking";
		
	}

	
	
	
}
