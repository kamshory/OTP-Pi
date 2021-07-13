package com.planetbiru.config;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.ddns.DDNSRecord;
import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.Utility;

public class ConfigDDNS {
	private static Map<String, DDNSRecord> records = new HashMap<>();
	private static String configPath = "";
	
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
							DDNSRecord rec = new DDNSRecord(lID, lZone, lRecordName, type, lProxied, lTTL, lForceCreateZone, lProvider, lActive, lcronExpression, nextValid, lastUpdate);
							ConfigDDNS.getRecords().put(lID, rec);
					    }
					}				
				}
			}
		} 
		catch (FileNotFoundException | JSONException e) 
		{
			//e.printStackTrace();
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
			//e.printStackTrace();
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
		((DDNSRecord) ConfigDDNS.records.getOrDefault(value, new DDNSRecord())).setActive(false);	
	}

	public static void activate(String value) {
		((DDNSRecord) ConfigDDNS.records.getOrDefault(value, new DDNSRecord())).setActive(true);	
	}

	public static void updateRecord(DDNSRecord record) {
		String id = record.getId();
		ConfigDDNS.records.put(id, record);		
	}

	public static void proxied(String value) {
		((DDNSRecord) ConfigDDNS.records.getOrDefault(value, new DDNSRecord())).setProxied(true);
		
	}
	public static void unproxied(String value) {
		((DDNSRecord) ConfigDDNS.records.getOrDefault(value, new DDNSRecord())).setProxied(false);		
	}

	public static JSONObject toJSONObject()
	{
		return getJSONObject();
	}
}
