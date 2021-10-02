package com.planetbiru.config;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.constant.JsonKey;
import com.planetbiru.ddns.DDNSRecord;
import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.Utility;

public class ConfigDDNS {
	private static Map<String, DDNSRecord> records = new HashMap<>();
	private static String configPath = "";
	
	private static Logger logger = Logger.getLogger(ConfigDDNS.class);
	
	private ConfigDDNS()
	{
		
	}
	
	public static void load(String path) {
		ConfigDDNS.configPath = path;
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		prepareDir(fileName);	
		try 
		{
			byte[] data = FileConfigUtil.read(fileName);		
			if(data != null)
			{
				String text = new String(data);
				if(text.length() > 7)
				{
					JSONObject list = new JSONObject(text);				
					ConfigDDNS.setRecords(new HashMap<>());
					
					JSONArray keys = list.names ();
					for(int i = 0; i < keys.length (); ++i) 
					{
						String key = keys.getString(i); 
					    if (list.get(key) instanceof JSONObject) 
					    {
					    	JSONObject json = list.getJSONObject(key);						
							String lID = json.optString("id", "").trim();
							String lZone = json.optString("zone", "").trim();
							String lRecordName = json.optString("recordName", "").trim();
							String lProvider = json.optString("provider", "").trim();
							boolean lProxied = json.optBoolean("proxied", false);
							int lTTL = json.optInt("ttl", 0);
							boolean lActive = json.optBoolean("active", false);
							boolean lForceCreateZone = json.optBoolean("forceCreateZone", false);
							String lcronExpression = json.optString("cronExpression", "").trim();
							Date lastUpdate = DDNSRecord.longToDate(json.optLong("lastUpdate", 0));
							Date nextValid = DDNSRecord.longToDate(json.optLong("nextValid", 0));
							String type = "A";
							DDNSRecord ddnsRecord = new DDNSRecord();
							ddnsRecord.setId(lID);
							ddnsRecord.setZone(lZone);
							ddnsRecord.setRecordName(lRecordName);
							ddnsRecord.setType(type);
							ddnsRecord.setProxied(lProxied);
							ddnsRecord.setTtl(lTTL);
							ddnsRecord.setForceCreateZone(lForceCreateZone);
							ddnsRecord.setProvider(lProvider);
							ddnsRecord.setActive(lActive);
							ddnsRecord.setCronExpression(lcronExpression);
							
							ddnsRecord.setNextValid(nextValid);
							ddnsRecord.setLastUpdate(lastUpdate);
							
							ConfigDDNS.getRecords().put(lID, ddnsRecord);
					    }
					}				
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
	
	public static void save()
	{
		ConfigDDNS.save(ConfigDDNS.configPath);
	}
	
	public static void save(String path) {
		JSONObject config = getJSONObject();
		save(path, config);
	}

	public static JSONObject getJSONObject() {
		JSONObject jo = new JSONObject();
		for (Entry<String, DDNSRecord> set : ConfigDDNS.getRecords().entrySet()) 
		{
			String key = set.getKey();
			JSONObject value = set.getValue().toJSONObject();
			jo.put(key, value);
		}
		return jo;
	}

	public static void save(String path, JSONObject config) {	
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		prepareDir(fileName);
		
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
	
	public static JSONObject getJSONObject(String zone, String recordname) {
		String id = Utility.md5(zone+":"+recordname);
		if(ConfigDDNS.getRecords().containsKey(id))
		{
			return ConfigDDNS.getRecords().get(id).toJSONObject();
		}
		return new JSONObject();
	}

	public static Map<String, DDNSRecord> getRecords() {
		return ConfigDDNS.records;
	}

	public static void setRecords(Map<String, DDNSRecord> records) {
		ConfigDDNS.records = records;
	}

	public static Object getJSONObject(String id) {
		if(ConfigDDNS.getRecords().containsKey(id))
		{
			return ConfigDDNS.getRecords().get(id).toJSONObject();
		}
		return new JSONObject();
	}

	public static void deleteRecord(String value) {
		ConfigDDNS.records.remove(value);	
	}

	public static void deactivate(String value) {
		ConfigDDNS.records.getOrDefault(value, new DDNSRecord()).setActive(false);	
	}

	public static void activate(String value) {
		ConfigDDNS.records.getOrDefault(value, new DDNSRecord()).setActive(true);	
	}

	public static void updateRecord(DDNSRecord ddnsRecord) {
		String id = ddnsRecord.getId();
		ConfigDDNS.records.put(id, ddnsRecord);		
	}

	public static void proxied(String value) {
		ConfigDDNS.records.getOrDefault(value, new DDNSRecord()).setProxied(true);
		
	}
	public static void unproxied(String value) {
		ConfigDDNS.records.getOrDefault(value, new DDNSRecord()).setProxied(false);		
	}

	public static JSONObject toJSONObject()
	{
		return getJSONObject();
	}

	public static void reset() {
		ConfigDDNS.records = new HashMap<>();		
	}

	public static void delete(Map<String, String> queryPairs) {
		for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
		{
			String key = entry.getKey();
			String value = entry.getValue();
			if(key.startsWith("id["))
			{
				ConfigDDNS.deleteRecord(value);
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
				ConfigDDNS.deactivate(value);
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
				ConfigDDNS.activate(value);
			}
		}
		
	}

	public static void proxied(Map<String, String> queryPairs) {
		for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
		{
			String key = entry.getKey();
			String value = entry.getValue();
			if(key.startsWith("id["))
			{
				ConfigDDNS.proxied(value);
			}
		}
		
	}

	public static void unproxied(Map<String, String> queryPairs) {
		for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
		{
			String key = entry.getKey();
			String value = entry.getValue();
			if(key.startsWith("id["))
			{
				ConfigDDNS.unproxied(value);
			}
		}
		
	}

	public static void update(Map<String, String> queryPairs) {
		String id = queryPairs.getOrDefault(JsonKey.ID, "").trim();
		String provider = queryPairs.getOrDefault(JsonKey.PROVIDER, "").trim();
		String zone = queryPairs.getOrDefault(JsonKey.ZONE, "").trim();
		String recordName = queryPairs.getOrDefault(JsonKey.RECORD_NAME, "").trim();
		String ttls = queryPairs.getOrDefault(JsonKey.TTL, "").trim();
		String cronExpression = queryPairs.getOrDefault(JsonKey.CRON_EXPRESSION, "").trim();
		boolean proxied = queryPairs.getOrDefault(JsonKey.PROXIED, "").equals("1");
		boolean forceCreateZone = queryPairs.getOrDefault(JsonKey.FORCE_CREATE_ZONE, "").equals("1");
		boolean active = queryPairs.getOrDefault(JsonKey.ACTIVE, "").equals("1");
		String type = queryPairs.getOrDefault(JsonKey.TYPE, "0");
		int ttl = Utility.atoi(ttls);
		
		if(!id.isEmpty())
		{
			DDNSRecord ddnsRecord = ConfigDDNS.getRecords().getOrDefault(id, new DDNSRecord());
			if(!id.isEmpty())
			{
				ddnsRecord.setId(id);
			}
			if(!zone.isEmpty())
			{
				ddnsRecord.setZone(zone);
			}
			if(!recordName.isEmpty())
			{
				ddnsRecord.setRecordName(recordName);
			}
			ddnsRecord.setProvider(provider);
			ddnsRecord.setProxied(proxied);
			ddnsRecord.setForceCreateZone(forceCreateZone);
			ddnsRecord.setCronExpression(cronExpression);
			ddnsRecord.setTtl(ttl);
			ddnsRecord.setActive(active);		
			ddnsRecord.setType(type);
			ConfigDDNS.updateRecord(ddnsRecord);
		}
		
	}

	public static void add(Map<String, String> queryPairs) {
		String provider = queryPairs.getOrDefault(JsonKey.PROVIDER, "").trim();
		String zone = queryPairs.getOrDefault(JsonKey.ZONE, "").trim();
		String recordName = queryPairs.getOrDefault(JsonKey.RECORD_NAME, "").trim();
		String cronExpression = queryPairs.getOrDefault("cron_expression", "").trim();
		boolean proxied = queryPairs.getOrDefault(JsonKey.PROXIED, "").trim().equals("1");
		boolean forceCreateZone = queryPairs.getOrDefault(JsonKey.FORCE_CREATE_ZONE, "").trim().equals("1");
		boolean active = queryPairs.getOrDefault(JsonKey.ACTIVE, "").trim().equals("1");
		
		String ttls = queryPairs.getOrDefault(JsonKey.TTL, "0");
		int ttl = Utility.atoi(ttls);
		String type = queryPairs.getOrDefault(JsonKey.TYPE, "0");
		String id = Utility.md5(zone+":"+recordName);
		DDNSRecord ddnsRecord = new DDNSRecord();
		
		ddnsRecord.setId(id);
		ddnsRecord.setZone(zone);
		ddnsRecord.setRecordName(recordName);
		ddnsRecord.setType(type);
		ddnsRecord.setProxied(proxied);
		ddnsRecord.setTtl(ttl);
		ddnsRecord.setForceCreateZone(forceCreateZone);
		ddnsRecord.setProvider(provider);
		ddnsRecord.setActive(active);
		ddnsRecord.setCronExpression(cronExpression);
		
		if(!zone.isEmpty() && !recordName.isEmpty())
		{
			ConfigDDNS.getRecords().put(id, ddnsRecord);	
		}
		
	}
}
