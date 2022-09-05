package com.planetbiru.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.constant.JsonKey;
import com.planetbiru.util.CommandLineExecutor;
import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.Utility;

public class ConfigFirewall {
	private static JSONArray fwRecords = new JSONArray();
	private static String configPath = "";
	
	private static Logger logger = Logger.getLogger(ConfigFirewall.class);
	private ConfigFirewall()
	{
		
	}
	
	public static void load(String path) {
		ConfigFirewall.configPath = path;
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		FileConfigUtil.prepareDirectory(fileName);	
		
		
		try 
		{
			byte[] data = FileConfigUtil.read(fileName);		
			if(data != null)
			{
				String text = new String(data);
				if(text.length() > 7)
				{
					JSONArray list = new JSONArray(text);				
					ConfigFirewall.setRecords(list);
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
	public static void add(int port, String protocol) {
		String id = String.format("%s%d", protocol, port);
		JSONObject fwRecord = new JSONObject();
		fwRecord.put("id", id);
		fwRecord.put("port", port);
		fwRecord.put(JsonKey.PROTOCOL, protocol);
		fwRecord.put(JsonKey.ACTIVE, true);
		fwRecord.put(JsonKey.LAST_UPDATE, System.currentTimeMillis());
		ConfigFirewall.fwRecords.put(fwRecord);		
		ConfigFirewall.activate(fwRecord);
	}
	
	public static void save()
	{
		ConfigFirewall.save(ConfigFirewall.configPath);
	}
	
	public static void save(String path) {
		save(path, ConfigFirewall.getRecords());
	}

	public static JSONObject get(String id)
	{
		for(int i = 0; i<ConfigFirewall.getRecords().length(); i++)
		{
			if(ConfigFirewall.getRecords().get(i) != null && ConfigFirewall.getRecords().optJSONObject(i).optString("id").equals(id))
			{
				return ConfigFirewall.getRecords().optJSONObject(i);
			}
		}
		return new JSONObject();
	}
	
	public static void remove(String id) {
		JSONArray list = new JSONArray();
		for(int i = 0; i<ConfigFirewall.getRecords().length(); i++)
		{
			if(!ConfigFirewall.getRecords().optJSONObject(i).optString("id").equals(id))
			{
				list.put(ConfigFirewall.getRecords().optJSONObject(i));
			}
		}	
		ConfigFirewall.fwRecords = list;
	}
	
	public static void activate(String id)
	{
		JSONObject fwRecord = ConfigFirewall.get(id);
		ConfigFirewall.activate(fwRecord);
		ConfigFirewall.get(id).put(JsonKey.ACTIVE, true);
		ConfigFirewall.get(id).put(JsonKey.LAST_UPDATE, System.currentTimeMillis());
	}
	
	public static void deactivate(String id)
	{
		JSONObject fwRecord = ConfigFirewall.get(id);
		List<Integer> servicePorts = ConfigFirewall.getServicePorts();
		if(!servicePorts.contains(fwRecord.optInt("port")))
		{
			ConfigFirewall.deactivate(fwRecord);
			ConfigFirewall.get(id).put(JsonKey.ACTIVE, false);
			ConfigFirewall.get(id).put(JsonKey.LAST_UPDATE, System.currentTimeMillis());
		}
	}
	
	private static void activate(JSONObject fwRecord) 
	{
		String command1 = String.format("firewall-cmd --permanent --add-port=%d/%s", fwRecord.optInt("port", 0), fwRecord.optString(JsonKey.PROTOCOL, ""));
		String command2 = "firewall-cmd --reload"; 
		CommandLineExecutor.exec(command1);
		CommandLineExecutor.exec(command2);
	}

	private static void deactivate(JSONObject fwRecord) 
	{
		String command1 = String.format("firewall-cmd --permanent --remove-port=%d/%s", fwRecord.optInt("port", 0), fwRecord.optString(JsonKey.PROTOCOL, ""));
		String command2 = "firewall-cmd --reload"; 
		CommandLineExecutor.exec(command1);
		CommandLineExecutor.exec(command2);
	}

	public static List<Integer> getServicePorts()
	{
		List<Integer> servicePorts = new ArrayList<>();
		servicePorts.add(Config.getPortManager());
		servicePorts.add(Config.getPortManager()+1);
		servicePorts.add(ConfigAPI.getHttpPort());
		servicePorts.add(ConfigAPI.getHttpsPort());
		servicePorts.add(22);
		servicePorts.add(55);
		return servicePorts;
	}

	public static void save(String path, JSONArray config) {
		
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
	
	public static JSONArray getRecords() {
		return fwRecords;
	}

	public static void setRecords(JSONArray records) {
		ConfigFirewall.fwRecords = records;
	}

	public static void reset() {
		ConfigFirewall.fwRecords = new JSONArray();		
	}

	public static void delete(Map<String, String> queryPairs) {
		for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
		{
			String key = entry.getKey();
			String value = entry.getValue();
			if(key.startsWith("id["))
			{
				ConfigFirewall.remove(value);
			}
		}
		
	}

	public static void activate(Map<String, String> queryPairs) {
		for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
		{
			String key = entry.getKey();
			String value = entry.getValue();
			if(key.startsWith("id["))
			{
				ConfigFirewall.activate(value);
			}
		}
		
	}

	public static void deactivate(Map<String, String> queryPairs) {
		for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
		{
			String key = entry.getKey();
			String value = entry.getValue();
			if(key.startsWith("id["))
			{
				ConfigFirewall.deactivate(value);
			}
		}
	}

}
