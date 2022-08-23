package com.planetbiru.config;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.gsm.GSMException;
import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.Utility;

public class ConfigBlocking {
	
	private static String countryCode = "62";
	private static Map<String, Boolean> blockList = new LinkedHashMap<>();
	private static String configPath = "";
	
	private static Logger logger = Logger.getLogger(ConfigBlocking.class);
	
	private ConfigBlocking()
	{
		
	}
	
	public static void block(String msisdn) throws GSMException {
		msisdn = Utility.canonicalMSISDN(msisdn);
		ConfigBlocking.blockList.put(msisdn, Boolean.valueOf(true));
	}
	
	public static void unblock(String msisdn) throws GSMException {
		msisdn = Utility.canonicalMSISDN(msisdn);
		ConfigBlocking.blockList.put(msisdn, Boolean.valueOf(false));
	}
	
	public static void block(String msisdn, boolean block) throws GSMException {
		msisdn = Utility.canonicalMSISDN(msisdn);
		ConfigBlocking.blockList.put(msisdn, Boolean.valueOf(block));
	}
	
	public static void add(String msisdn) throws GSMException {
		if(!msisdn.isEmpty())
		{
			msisdn = Utility.canonicalMSISDN(msisdn);
			ConfigBlocking.blockList.put(msisdn, Boolean.valueOf(true));
		}
	}
	
	public static Boolean isBlocked(String msisdn) throws GSMException
	{
		msisdn = Utility.canonicalMSISDN(msisdn);
		return ConfigBlocking.blockList.getOrDefault(msisdn, Boolean.valueOf(false));
	}
	
	public static void load(String path) {
		ConfigBlocking.configPath = path;
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		fileName = FileConfigUtil.fixFileName(fileName);
		try 
		{
			byte[] data = FileConfigUtil.read(fileName);		
			if(data != null)
			{
				String text = new String(data);
				if(text.length() > 7)
				{
					ConfigBlocking.blockList = new LinkedHashMap<>();					
					JSONObject json = new JSONObject(text);
					JSONArray keys = json.names();
					for(int i = 0; i<keys.length(); i++)
					{
						String id = keys.optString(i);
						boolean block  = json.optBoolean(id, false);
						ConfigBlocking.blockList.put(id, Boolean.valueOf(block));
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
		ConfigBlocking.save(ConfigBlocking.configPath);
	}
	public static void save(String path) {
		JSONObject config = getJSONObject();
		save(path, config);
	}

	public static void save(String path, JSONObject config) {		
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		fileName = FileConfigUtil.fixFileName(fileName);
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
		for (Map.Entry<String, Boolean> set : ConfigBlocking.blockList.entrySet()) 
		{
			 config.put(set.getKey(), set.getValue());
        }
		return config;
	}

	public static JSONObject toJSONObject() {
		return getJSONObject();
	}

	public static void remove(String value) {	
		if(ConfigBlocking.blockList.containsKey(value))
		{
			ConfigBlocking.blockList.remove(value);
		}		
	}
	
	public static String getCountryCode() {
		return countryCode;
	}
	
	public static void setCountryCode(String countryCode) {
		ConfigBlocking.countryCode = countryCode;
	}
	public static String getOrderedList() 
	{
        Map<String, Boolean> map = new TreeMap<>(ConfigBlocking.blockList);
        StringBuilder builder = new StringBuilder();
        
        builder.append("{");
        int i = 0;
        for (String sKey : map.keySet()) 
        {
        	String key = sKey.replaceAll("^\"|\"$", ""); //NOSONAR
        	if(i > 0)
        	{
        		builder.append(",");
        	}
        	builder.append("\r\n\t\""+key+"\": ");
        	builder.append(ConfigBlocking.blockList.get(sKey).toString());
        	i++;
        }
        builder.append("\r\n}");
		return builder.toString();
	}

	public static void reset() {
		ConfigBlocking.blockList = new LinkedHashMap<>();
		
	}

	public static void delete(Map<String, String> queryPairs) {
		for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
		{
			String key = entry.getKey();
			String value = entry.getValue();
			if(key.startsWith("id["))
			{
				ConfigBlocking.remove(value);
			}
		}
		
	}

	public static void block(Map<String, String> queryPairs) {
		for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
		{
			String key = entry.getKey();
			String value = entry.getValue();
			if(key.startsWith("id["))
			{
				try 
				{
					ConfigBlocking.block(value);
				} 
				catch (GSMException e) 
				{
					/**
					 * Do nothing
					 */
				}
			}
		}
		
	}

	public static void unblock(Map<String, String> queryPairs) {
		for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
		{
			String key = entry.getKey();
			String value = entry.getValue();
			if(key.startsWith("id["))
			{
				try 
				{
					ConfigBlocking.unblock(value);
				} 
				catch (GSMException e) 
				{
					/**
					 * Do nothing
					 */
				}
			}
		}
		
	}

	public static void addList(String msisdn) {
		try 
		{
			ConfigBlocking.block(msisdn);
		} 
		catch (GSMException e) 
		{
			/**
			 * Do nothing
			 */
		}
	}
	
}
