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
	private static int smsFailure = 0;
	private static int amqpDisconnected = 0;
	private static int mqttDisconnected = 0;
	private static int redisDisconnected = 0;
	private static int wsDisconnected = 0;
	private static int activeMQDisconnected = 0;
	private static int stompDisconnected = 0;
	
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

					int lsmsFailure = json.optInt("smsFailure", 0);	
					int lamqpDisconnected = json.optInt("amqpDisconnected", 0);	
					int lmqttDisconnected = json.optInt("mqttDisconnected", 0);	
					int lactiveMQDisconnected = json.optInt("activeMQDisconnected", 0);	
					int lredisDisconnected = json.optInt("redisDisconnected", 0);	
					int lwsDisconnected = json.optInt("wsDisconnected", 0);	
					int lstompDisconnected = json.optInt("stompDisconnected", 0);	
					
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
		ConfigBell.smsFailure = 0;
		ConfigBell.amqpDisconnected = 0;					
		ConfigBell.mqttDisconnected = 0;	
		ConfigBell.redisDisconnected = 0;
		ConfigBell.wsDisconnected = 0;	
		ConfigBell.amqpDisconnected = 0;	
		ConfigBell.stompDisconnected = 0;	
	}

	public static String getConfigPath() {
		return configPath;
	}

	public static void setConfigPath(String configPath) {
		ConfigBell.configPath = configPath;
	}

	public static int isSmsFailure() {
		return smsFailure;
	}

	public static void setSmsFailure(int smsFailure) {
		ConfigBell.smsFailure = smsFailure;
	}


	public static int isAmqpDisconnected() {
		return amqpDisconnected;
	}

	public static void setAmqpDisconnected(int amqpDisconnected) {
		ConfigBell.amqpDisconnected = amqpDisconnected;
	}

	public static int isRedisDisconnected() {
		return redisDisconnected;
	}

	public static void setRedisDisconnected(int redisDisconnected) {
		ConfigBell.redisDisconnected = redisDisconnected;
	}

	public static int isMqttDisconnected() {
		return mqttDisconnected;
	}

	public static void setMqttDisconnected(int mqttDisconnected) {
		ConfigBell.mqttDisconnected = mqttDisconnected;
	}

	public static int isWsDisconnected() {
		return wsDisconnected;
	}

	public static void setWsDisconnected(int wsDisconnected) {
		ConfigBell.wsDisconnected = wsDisconnected;
	}

	public static int isActiveMQDisconnected() {
		return activeMQDisconnected;
	}

	public static void setActiveMQDisconnected(int activeMQDisconnected) {
		ConfigBell.activeMQDisconnected = activeMQDisconnected;
	}

	public static int isStompDisconnected() {
		return stompDisconnected;
	}

	public static void setStompDisconnected(int stompDisconnected) {
		ConfigBell.stompDisconnected = stompDisconnected;
	}
	
}
