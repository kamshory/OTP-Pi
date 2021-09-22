package com.planetbiru.config;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.Utility;

public class ConfigBell {
	private static String configPath = "";
	private static Logger logger = Logger.getLogger(ConfigBell.class);
	private static boolean smsFailure;
	private static boolean amqpDisconnected;
	private static boolean mqttDisconnected;
	private static boolean redisDisconnected;
	private static boolean wsDisconnected;
	
	private ConfigBell()
	{
		
	}
	
	public static void load(String path) {
		ConfigBell.configPath = path;
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

					boolean lsmsFailure = json.optBoolean("smsFailure", false);	
					boolean lamqpDisconnected = json.optBoolean("amqpDisconnected", false);	
					boolean lmqttDisconnected = json.optBoolean("mqttDisconnected", false);	
					boolean lwsDisconnected = json.optBoolean("wsDisconnected", false);	
					boolean lredisDisconnected = json.optBoolean("redisDisconnected", false);	
					
					ConfigBell.smsFailure = lsmsFailure;
					ConfigBell.amqpDisconnected = lamqpDisconnected;
					ConfigBell.mqttDisconnected = lmqttDisconnected;
					ConfigBell.redisDisconnected = lredisDisconnected;
					ConfigBell.wsDisconnected = lwsDisconnected;
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
		ConfigBell.save(ConfigBell.configPath);
	}
	
	public static void save(String path) {
		JSONObject config = getJSONObject();
		ConfigBell.save(path, config);
	}

	public static void save(String path, JSONObject config) {		
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		ConfigBell.prepareDir(fileName);		
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
		config.put("smsFailure", ConfigBell.smsFailure);
		config.put("amqpDisconnected", ConfigBell.amqpDisconnected);
		config.put("mqttDisconnected", ConfigBell.mqttDisconnected);
		config.put("redisDisconnected", ConfigBell.redisDisconnected);
		config.put("wsDisconnected", ConfigBell.wsDisconnected);	
		return config;
	}

	public static JSONObject toJSONObject() {
		return getJSONObject();
	}

	public static void reset() {
		ConfigBell.smsFailure = false;
		ConfigBell.amqpDisconnected = false;					
		ConfigBell.mqttDisconnected = false;	
		ConfigBell.redisDisconnected = false;
		ConfigBell.wsDisconnected = false;	
	}

	public static String getConfigPath() {
		return configPath;
	}

	public static void setConfigPath(String configPath) {
		ConfigBell.configPath = configPath;
	}

	public static boolean isSmsFailure() {
		return smsFailure;
	}

	public static void setSmsFailure(boolean smsFailure) {
		ConfigBell.smsFailure = smsFailure;
	}


	public static boolean isAmqpDisconnected() {
		return amqpDisconnected;
	}

	public static void setAmqpDisconnected(boolean amqpDisconnected) {
		ConfigBell.amqpDisconnected = amqpDisconnected;
	}

	public static boolean isRedisDisconnected() {
		return redisDisconnected;
	}

	public static void setRedisDisconnected(boolean redisDisconnected) {
		ConfigBell.redisDisconnected = redisDisconnected;
	}

	public static boolean isMqttDisconnected() {
		return mqttDisconnected;
	}

	public static void setMqttDisconnected(boolean mqttDisconnected) {
		ConfigBell.mqttDisconnected = mqttDisconnected;
	}

	public static boolean isWsDisconnected() {
		return wsDisconnected;
	}

	public static void setWsDisconnected(boolean wsDisconnected) {
		ConfigBell.wsDisconnected = wsDisconnected;
	}
	
}
