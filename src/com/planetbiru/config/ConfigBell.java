package com.planetbiru.config;

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
	private static boolean smsFailure = false;
	private static boolean amqpDisconnected = false;
	private static boolean mqttDisconnected = false;
	private static boolean redisDisconnected = false;
	private static boolean wsDisconnected = false;
	private static boolean activeMQDisconnected = false;
	private static boolean stompDisconnected = false;
	
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
					boolean lactiveMQDisconnected = json.optBoolean("activeMQDisconnected", false);	
					boolean lredisDisconnected = json.optBoolean("redisDisconnected", false);	
					boolean lwsDisconnected = json.optBoolean("wsDisconnected", false);	
					boolean lstompDisconnected = json.optBoolean("stompDisconnected", false);	
					
					ConfigBell.smsFailure = lsmsFailure;
					ConfigBell.amqpDisconnected = lamqpDisconnected;
					ConfigBell.mqttDisconnected = lmqttDisconnected;
					ConfigBell.activeMQDisconnected = lactiveMQDisconnected ;
					ConfigBell.redisDisconnected = lredisDisconnected;
					ConfigBell.wsDisconnected = lwsDisconnected;
					ConfigBell.stompDisconnected = lstompDisconnected;
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
		FileConfigUtil.prepareDir(fileName);		
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
		config.put("smsFailure", ConfigBell.smsFailure);
		config.put("amqpDisconnected", ConfigBell.amqpDisconnected);
		config.put("mqttDisconnected", ConfigBell.mqttDisconnected);
		config.put("activeMQDisconnected", ConfigBell.activeMQDisconnected);
		config.put("redisDisconnected", ConfigBell.redisDisconnected);
		config.put("wsDisconnected", ConfigBell.wsDisconnected);	
		config.put("stompDisconnected", ConfigBell.stompDisconnected);	
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
		ConfigBell.amqpDisconnected = false;	
		ConfigBell.stompDisconnected = false;	
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

	public static boolean isActiveMQDisconnected() {
		return activeMQDisconnected;
	}

	public static void setActiveMQDisconnected(boolean activeMQDisconnected) {
		ConfigBell.activeMQDisconnected = activeMQDisconnected;
	}

	public static boolean isStompDisconnected() {
		return stompDisconnected;
	}

	public static void setStompDisconnected(boolean stompDisconnected) {
		ConfigBell.stompDisconnected = stompDisconnected;
	}
	
}
