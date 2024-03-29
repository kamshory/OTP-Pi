package com.planetbiru.config;

import java.io.IOException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.App;
import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.Utility;

public class ConfigGeneral {
	private static String configPath = "";
	private static Date nextValid = new Date();
	
	private static String deviceName = "";
	private static String deviceTimeZone = "";
	private static String ntpServer = "";
	private static String ntpUpdateInterval = "";	
	private static String restartService = "";
	private static String restartDevice = "";
	private static boolean dropExpireOTP = false;
	private static long otpExpirationOffset = 30000;
	private static long inspectModemInterval = 0;
	
	private static Logger logger = Logger.getLogger(ConfigGeneral.class);

	private ConfigGeneral()
	{
		
	}

	public static void load(String path) {
		ConfigGeneral.configPath = path;
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

					ConfigGeneral.deviceName = json.optString("deviceName", "").trim();
					ConfigGeneral.deviceTimeZone = json.optString("deviceTimeZone", "").trim();
					ConfigGeneral.ntpServer = json.optString("ntpServer", "").trim();
					ConfigGeneral.ntpUpdateInterval = json.optString("ntpUpdateInterval", "").trim();
					ConfigGeneral.restartService = json.optString("restartService", "").trim();
					ConfigGeneral.restartDevice = json.optString("restartDevice", "").trim();
					ConfigGeneral.dropExpireOTP = json.optBoolean("dropExpireOTP", false);
					ConfigGeneral.otpExpirationOffset = json.optLong("otpExpirationOffset", 0);
					ConfigGeneral.inspectModemInterval = json.optLong("inspectModemInterval", 0);
					
					App.modemInspectorStart(0);
				}
			}
		} 
		catch (JSONException e) 
		{
			logger.error(e.getMessage(), e);
		}
		catch (FileNotFoundException e) 
		{
			if(Config.isLogConfigNotFound())
			{
				logger.error(e.getMessage(), e);
			}
		}		
	}	

	public static void save() {
		ConfigGeneral.save(ConfigGeneral.configPath);
	}
	public static void save(String path) {
		JSONObject config = getJSONObject();
		ConfigGeneral.save(path, config);
	}

	public static void save(String path, JSONObject config) {		
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		FileConfigUtil.prepareDirectory(fileName);
		
		try 
		{
			FileConfigUtil.write(fileName, config.toString().getBytes());
		}
		catch (IOException e) 
		{
			logger.error(e.getMessage(), e);
		}
	}
	
	public static JSONObject getJSONObject() {
		JSONObject config = new JSONObject();
		config.put("deviceName", ConfigGeneral.deviceName);
		config.put("deviceTimeZone", ConfigGeneral.deviceTimeZone);
		config.put("ntpServer", ConfigGeneral.ntpServer);
		config.put("ntpUpdateInterval", ConfigGeneral.ntpUpdateInterval);
		config.put("restartService", ConfigGeneral.restartService);
		config.put("restartDevice", ConfigGeneral.restartDevice);
		config.put("dropExpireOTP", ConfigGeneral.dropExpireOTP);
		config.put("otpExpirationOffset", ConfigGeneral.otpExpirationOffset);
		config.put("inspectModemInterval", ConfigGeneral.inspectModemInterval);
		return config;
	}

	public static JSONObject toJSONObject() {
		return getJSONObject();
	}

	public static String getDeviceName() {
		return deviceName;
	}

	public static void setDeviceName(String deviceName) {
		ConfigGeneral.deviceName = deviceName;
	}

	public static String getDeviceTimeZone() {
		return deviceTimeZone;
	}

	public static void setDeviceTimeZone(String deviceTimeZone) {
		ConfigGeneral.deviceTimeZone = deviceTimeZone;
	}

	public static String getNtpServer() {
		return ntpServer;
	}

	public static void setNtpServer(String ntpServer) {
		ConfigGeneral.ntpServer = ntpServer;
	}

	public static String getNtpUpdateInterval() {
		return ntpUpdateInterval;
	}

	public static void setNtpUpdateInterval(String ntpUpdateInterval) {
		ConfigGeneral.ntpUpdateInterval = ntpUpdateInterval;
	}

	public static String getRestartService() {
		return restartService;
	}

	public static void setRestartService(String restartService) {
		ConfigGeneral.restartService = restartService;
	}

	public static String getRestartDevice() {
		return restartDevice;
	}

	public static void setRestartDevice(String restartDevice) {
		ConfigGeneral.restartDevice = restartDevice;
	}

	public static Date getNextValid() {
		return nextValid;
	}

	public static void setNextValid(Date nextValid) {
		ConfigGeneral.nextValid = nextValid;
	}

	public static boolean isDropExpireOTP() {
		return dropExpireOTP;
	}

	public static void setDropExpireOTP(boolean dropOTPExpire) {
		ConfigGeneral.dropExpireOTP = dropOTPExpire;
	}

	public static long getOtpExpirationOffset() {
		return otpExpirationOffset;
	}

	public static void setOtpExpirationOffset(long otpExpirationOffset) {
		ConfigGeneral.otpExpirationOffset = otpExpirationOffset;
	}

	public static long getInspectModemInterval() {
		return inspectModemInterval;
	}

	public static void setInspectModemInterval(long inspectModemInterval) {
		ConfigGeneral.inspectModemInterval = inspectModemInterval;
	}
	
}
