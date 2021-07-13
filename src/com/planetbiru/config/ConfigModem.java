package com.planetbiru.config;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.ServerWebSocketServerAdmin;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.gsm.DialUtil;
import com.planetbiru.gsm.GSMUtil;
import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.Utility;

public class ConfigModem {
	
	private static String configPath = "";
    private static Map<String, DataModem> modemData = new HashMap<>();
	
	private ConfigModem()
	{
		
	}
	
	public static DataModem getModemData(String modemID) {
		return ConfigModem.modemData.getOrDefault(modemID, new DataModem());
	}
	
	public static Map<String, DataModem> getModemData()
	{
		return ConfigModem.modemData;
	}
	
	public static void load(String path)
	{
		ConfigModem.configPath = path;
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		ConfigModem.prepareDir(fileName);
		try 
		{
			byte[] data = FileConfigUtil.read(fileName);
			if(data != null)
			{
				String text = new String(data);
				JSONObject jsonObject = new JSONObject(text);
				Iterator<String> keys = jsonObject.keys();
				while(keys.hasNext()) {
				    String id = keys.next();
				    JSONObject modem = jsonObject.optJSONObject(id);
				    DataModem modemData = new DataModem(modem);
				    ConfigModem.addDataModem(id, modemData);
				}
			}
		} 
		catch (FileNotFoundException | JSONException e) 
		{
			/**
			 * Do nothing
			 */
		}
	}
	
	private static void prepareDir(String fileName) 
	{
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
	
	public static void addDataModem(DataModem modem)
	{
		ConfigModem.modemData.put(modem.getId(), modem);
	}
	
	public static void addDataModem(String id, DataModem modem)
	{
		ConfigModem.modemData.put(id, modem);
	}
	
	public static void addDataModem(String id, JSONObject jsonObject) 
	{
		DataModem modem = new DataModem(jsonObject);
		ConfigModem.modemData.put(id, modem);
	}
	
	public static void addDataModem(JSONObject jsonObject) 
	{
		DataModem modem = new DataModem(jsonObject);
		ConfigModem.modemData.put(jsonObject.optString(JsonKey.ID, ""), modem);
	}
	
	public static DataModem geDataModem(String id)
	{		
		return ConfigModem.modemData.getOrDefault(id, new DataModem());
	}
	
	public static void save(String path) {
		JSONObject config = toJSONObject();
		save(path, config);
	}
	
	public static void save(String path, JSONObject config) {	
		ConfigModem.configPath = path;
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		ConfigModem.prepareDir(fileName);		
		try 
		{
			FileConfigUtil.write(fileName, config.toString().getBytes());
		}
		catch (IOException e) 
		{
			//e.printStackTrace();
		}
	}
	
	public static void save() {
		ConfigModem.save(ConfigModem.configPath, toJSONObject());			
		JSONArray data = new JSONArray();
		JSONObject modem = new JSONObject();
		modem.put(JsonKey.NAME, "otp-modem-connected");
		modem.put(JsonKey.VALUE, GSMUtil.isConnected());
		modem.put(JsonKey.DATA, ConfigModem.getStatus());
		data.put(modem);
		JSONObject serverInfo = new JSONObject();
		serverInfo.put(JsonKey.DATA, data);
		serverInfo.put(JsonKey.COMMAND, "server-info");
		ServerWebSocketServerAdmin.broadcastMessage(serverInfo.toString());
	}
	
	public static JSONObject toJSONObject()
	{
		JSONObject json = new JSONObject();
		for (Map.Entry<String, DataModem> entry : ConfigModem.modemData.entrySet())
		{
			String id = entry.getKey();
			JSONObject modem = ((DataModem) entry.getValue()).toJSONObject();
			json.put(id, modem);
		}
		return json;
	}
	
	public static JSONObject getStatus() {
		JSONObject json = new JSONObject();
		for (Map.Entry<String, DataModem> entry : ConfigModem.modemData.entrySet())
		{
			String id = entry.getKey();
			JSONObject modem = new JSONObject();
			DataModem value = entry.getValue();
			modem.put("id", id);
			modem.put("connected", GSMUtil.isConnected(id));
			modem.put("internetAccess", value.isInternetAccess());
			modem.put("internetConnected", DialUtil.isConnected(id));
			modem.put("name", value.getName());
			modem.put("imei", value.getImei());
			modem.put("active", value.isActive());
			modem.put("port", value.getPort());
			modem.put("smsCenter", value.getSmsCenter());
			modem.put("defaultModem", value.isDefaultModem());
			json.put(id, modem);
		}
		return json;
	}
	
	public static String getConfigPath() {
		return configPath;
	}

	public static void setConfigPath(String configPath) {
		ConfigModem.configPath = configPath;
	}
	
	public static void deleteRecord(String id) {
		ConfigModem.modemData.remove(id);	
	}
	
	public static void deactivate(String id) {
		DataModem modem = ConfigModem.modemData.getOrDefault(id, new DataModem());
		modem.setActive(false);
		ConfigModem.modemData.put(id, modem);	
	}
	
	public static void activate(String id) {
		DataModem modem = ConfigModem.modemData.getOrDefault(id, new DataModem());
		modem.setActive(true);
		ConfigModem.modemData.put(id, modem);		
	}
	
	public static void update(String id, DataModem modem) {
		ConfigModem.modemData.put(id, modem);		
	}

	

	
	
	
}
