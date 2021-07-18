package com.planetbiru.config;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.Utility;

public class ConfigSMTP {
	private static String configPath = "";
	
	private static String softwareName = "";
	private static String serverName = "";
	private static String serverAddress = "";
	private static int serverPort = 25;
	private static boolean active = false;
	
	private static Logger logger = Logger.getLogger(ConfigSMTP.class);

	private ConfigSMTP()
	{
		
	}

	public static void load(String path) {
		ConfigSMTP.configPath = path;
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
					ConfigSMTP.softwareName = json.optString("softwareName", "");
					ConfigSMTP.serverName = json.optString("serverName", "").trim();
					ConfigSMTP.serverAddress = json.optString("serverAddress", "").trim();
					ConfigSMTP.serverPort = json.optInt("serverPort", 0);
					ConfigSMTP.active = json.optBoolean("active", false);
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
		ConfigSMTP.save(ConfigSMTP.configPath);
	}
	public static void save(String path) {
		JSONObject config = getJSONObject();
		ConfigSMTP.save(path, config);
	}

	public static void save(String path, JSONObject config) {		
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		ConfigSMTP.prepareDir(fileName);
		
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
		config.put("softwareName", ConfigSMTP.softwareName);
		config.put("serverName", ConfigSMTP.serverName);
		config.put("serverAddress", ConfigSMTP.serverAddress);
		config.put("serverPort", ConfigSMTP.serverPort);
		config.put("active", ConfigSMTP.active);
		return config;
	}

	public static JSONObject toJSONObject() {
		return getJSONObject();
	}

	public static String getServerName() {
		return serverName;
	}

	public static void setServerName(String serverName) {
		ConfigSMTP.serverName = serverName;
	}

	public static String getServerAddress() {
		return serverAddress;
	}

	public static void setServerAddress(String serverAddress) {
		ConfigSMTP.serverAddress = serverAddress;
	}

	public static int getServerPort() {
		return serverPort;
	}

	public static void setServerPort(int serverPort) {
		ConfigSMTP.serverPort = serverPort;
	}

	public static boolean isActive() {
		return active;
	}

	public static void setActive(boolean active) {
		ConfigSMTP.active = active;
	}

	public static String getSoftwareName() {
		return softwareName;
	}

	public static void setSoftwareName(String softwareName) {
		ConfigSMTP.softwareName = softwareName;
	}

	
	
}
