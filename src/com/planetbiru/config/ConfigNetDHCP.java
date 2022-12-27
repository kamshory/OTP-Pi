package com.planetbiru.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.Utility;

public class ConfigNetDHCP {
	
	private static String domainName = "";
	private static JSONArray domainNameServers = new JSONArray();
	private static String ipRouter = "";
	private static String netmask = "";
	private static String subnetMask = "";
	private static String domainNameServersAddress = "";
	private static String defaultLeaseTime = "";
	private static String maxLeaseTime = "";	
	private static JSONArray ranges = new JSONArray();
	
	private static String osConfigPath = "";
	private static String configPath = "";
	
	private static Logger logger = Logger.getLogger(ConfigNetDHCP.class);
	
	private ConfigNetDHCP()
	{
		
	}
	public static void load(String configPath, String systemConfigPath)
	{
		ConfigNetDHCP.load(configPath, systemConfigPath, false);
	}
	public static void load(String configPath, String systemConfigPath, boolean system) 
	{
		if(system)
		{
			ConfigNetDHCP.loadOSConfig(systemConfigPath);
		}
		else
		{
			ConfigNetDHCP.load(configPath);			
		}
	}	
	public static void loadOSConfig(String fileName) {
		try 
		{
			byte[] data = FileConfigUtil.read(fileName);
			String configStr = new String(data);
			List<String> tempList = Arrays.asList(configStr.split("\r\n"));
			ConfigNetDHCP.domainName = ConfigNetDHCP.getConfig("option domain-name", tempList);
			ConfigNetDHCP.domainNameServers = ConfigNetDHCP.toJSONArray(ConfigNetDHCP.getConfig("option domain-name-servers", tempList), ",");
			ConfigNetDHCP.ipRouter = ConfigNetDHCP.getConfig("option routers", tempList);
			ConfigNetDHCP.netmask = ConfigNetDHCP.getNetmask(tempList);
			ConfigNetDHCP.subnetMask = ConfigNetDHCP.getConfig("option subnet-mask", tempList);
			ConfigNetDHCP.domainNameServersAddress = ConfigNetDHCP.getConfig("option domain-name-servers", tempList, true);
			ConfigNetDHCP.defaultLeaseTime = ConfigNetDHCP.getConfig("default-lease-time", tempList);
			ConfigNetDHCP.maxLeaseTime = ConfigNetDHCP.getConfig("max-lease-time", tempList);
			ConfigNetDHCP.ranges = ConfigNetDHCP.getRange(tempList);
		}
		catch (FileNotFoundException e) 
		{
			logger.error(e.getMessage(), e);
		}	
	}

	private static String getNetmask(List<String> tempList) {
		String val = "";
		for(int i = 0; i<tempList.size(); i++)
		{
			String line = tempList.get(i);
			if(line.trim().startsWith("subnet ") && line.trim().contains("netmask "))
			{
				int start = line.indexOf("netmask ");
				val = line.substring(start).trim();
				if(val.endsWith("{"))
				{
					val = val.substring(0, val.length()-1);
				}
			}
		}
		return val;
	}
	private static JSONArray getRange(List<String> tempList) {
		JSONArray ranges = new JSONArray();
		for(int i = 0; i<tempList.size(); i++)
		{
			String line = tempList.get(i);
			if(line.trim().startsWith("range "))
			{
				String val = ConfigNetDHCP.getValue("range", line).trim();
				String[] arr = val.split(" ");
				if(arr.length > 1)
				{
					ranges.put(new JSONObject().put("begin", arr[0]).put("end", arr[1]));
				}
			}
		}
		return ranges;
	}
	private static JSONArray toJSONArray(String config, String separator) {
		if(config == null || separator == null)
		{
			return new JSONArray();
		}
		return new JSONArray(config.split(separator));
	}
	private static String getConfig(String string, List<String> tempList)
	{
		return ConfigNetDHCP.getConfig(string, tempList, false);
	}
	private static String getConfig(String string, List<String> tempList, boolean inBracket) {
		List<String> keys = Arrays.asList(string.split(" "));
		boolean throughBracket = false;
		for(int i = 0; i<tempList.size(); i++)
		{
			String line = tempList.get(i);
			boolean found = true;
			for(int j = 0; j < keys.size(); j++)
			{
				if(!line.contains(keys.get(j)))
				{
					found = false;
				}
			}
			if(line.contains("{"))
			{
				throughBracket = true;
			}
			if(found && (!inBracket || throughBracket))
			{
				return ConfigNetDHCP.getValue(string, line);
			}
		}
		return null;
	}
	private static String getValue(String keys, String line) {
		String val = line.trim();
		String key = keys.trim();
		String value = "";
		if(val.length() > key.length())
		{
			value = val.substring(key.length()).trim();
		}
		
		if(value.endsWith(";"))
		{
			value = value.substring(0, value.length()-1);
		}
		
		if(value.startsWith("\"") && value.endsWith("\""))
		{
			value = value.substring(1, value.length()-1);
		}
		
		return value;
	}
	private static void load(String path) {
		ConfigNetDHCP.configPath = path;
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
					
					ConfigNetDHCP.domainName = json.optString("domainName", "");
					ConfigNetDHCP.domainNameServers = json.optJSONArray("domainNameServers");
					ConfigNetDHCP.ipRouter = json.optString("ipRouter", "");
					ConfigNetDHCP.netmask = json.optString("netmask", "");
					ConfigNetDHCP.subnetMask = json.optString("subnetMask", "");
					ConfigNetDHCP.domainNameServersAddress = json.optString("domainNameServersAddress", "");
					ConfigNetDHCP.defaultLeaseTime = json.optString("defaultLeaseTime", "");
					ConfigNetDHCP.maxLeaseTime = json.optString("maxLeaseTime", "");
					ConfigNetDHCP.ranges = json.optJSONArray("ranges");

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
		ConfigNetDHCP.save(ConfigNetDHCP.configPath);
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
	
	public static JSONObject toJSONObject() {
		return getJSONObject();
	}
	
	
	public static JSONObject getJSONObject() {
		JSONObject config = new JSONObject();
		
		config.put("domainName", ConfigNetDHCP.domainName);
		config.put("domainNameServers", ConfigNetDHCP.domainNameServers);
		config.put("ipRouter", ConfigNetDHCP.ipRouter);
		config.put("netmask", ConfigNetDHCP.netmask);
		config.put("subnetMask", ConfigNetDHCP.subnetMask);
		config.put("domainNameServersAddress", ConfigNetDHCP.domainNameServersAddress);
		config.put("defaultLeaseTime", ConfigNetDHCP.defaultLeaseTime);
		config.put("maxLeaseTime", ConfigNetDHCP.maxLeaseTime);
		config.put("ranges", ConfigNetDHCP.ranges);

		return config;
	}

	public static void apply(String path)
	{
		ConfigNetDHCP.osConfigPath = path;
		String data = ConfigNetDHCP.buildConfig();
		try 
		{
			FileConfigUtil.write(ConfigNetDHCP.osConfigPath, data.getBytes());
		}
		catch (IOException e) 
		{
			logger.error(e.getMessage(), e);
		}
	
	}
	
	public static String buildConfig()
	{
		StringBuilder builder = new StringBuilder();
		if(!ConfigNetDHCP.domainName.isEmpty())
		{
			builder.append(String.format("option domain-name \"%s\";%n", ConfigNetDHCP.domainName));
		}
		if(ConfigNetDHCP.domainNameServers != null && !ConfigNetDHCP.domainNameServers.isEmpty())
		{
			builder.append(String.format("option domain-name-servers %s;%n", ConfigNetDHCP.join(", ", ConfigNetDHCP.domainNameServers)));
		}
		if(!ConfigNetDHCP.defaultLeaseTime.isEmpty())
		{
			builder.append(String.format("default-lease-time %s;%n", ConfigNetDHCP.defaultLeaseTime));
		}
		if(!ConfigNetDHCP.maxLeaseTime.isEmpty())
		{
			builder.append(String.format("max-lease-time %s;%n", ConfigNetDHCP.maxLeaseTime));
		}
		builder.append("authoritative;\r\n\r\n");
		
		builder.append(ConfigNetDHCP.buildSubnet());
		return builder.toString();
	}
	
	private static String join(String separator, JSONArray domainNameServers) {
		List<String> list = new ArrayList<>();
		for(int i = 0; i<domainNameServers.length(); i++)
		{
			list.add(domainNameServers.optString(i, ""));
		}
		return String.join(separator, list);
	}

	public static String buildSubnet()
	{
		String[] arr = ConfigNetDHCP.ipRouter.split("\\.");
		String baseAddress = "";
		if(arr.length > 3)
		{
			baseAddress = arr[0]+"."+arr[1]+"."+arr[2]+".0";
		}
		String firstLine = String.format("subnet %s netmask %s {", baseAddress, ConfigNetDHCP.subnetMask);
		String lastLine = "}";
		
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("%s%n", firstLine));
		
		if(!ConfigNetDHCP.ipRouter.isEmpty())
		{
			builder.append(String.format("    option routers                  %s;%n", ConfigNetDHCP.ipRouter));
		}
		if(!ConfigNetDHCP.subnetMask.isEmpty())
		{
			builder.append(String.format("    option subnet-mask              %s;%n", ConfigNetDHCP.subnetMask));
		}
		if(!ConfigNetDHCP.domainName.isEmpty())
		{
			builder.append(String.format("    option domain-search            \"%s\";%n", ConfigNetDHCP.domainName));
		}
		if(!ConfigNetDHCP.domainNameServersAddress.isEmpty())
		{
			builder.append(String.format("    option domain-name-servers      %s;%n", ConfigNetDHCP.domainNameServersAddress));
		}
		
		if(ConfigNetDHCP.ranges != null && !ConfigNetDHCP.ranges.isEmpty())
		{
			builder.append(String.format("%s", ConfigNetDHCP.buildRange()));			
		}
		
		builder.append(String.format("%s%n", lastLine));
		
		return builder.toString();
		
	}

	private static String buildRange() {
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i<ConfigNetDHCP.ranges.length(); i++)
		{
			JSONObject map = ConfigNetDHCP.ranges.optJSONObject(i);
			String begin = map.optString("begin", "").trim();
			String end = map.optString("end", "").trim();
			
			if(begin.isEmpty() && !end.isEmpty())
			{
				begin = end;
			}
			if(end.isEmpty() && !begin.isEmpty())
			{
				end = begin;
			}
			if(!begin.isEmpty() && !end.isEmpty())
			{
				builder.append(String.format("    range %s %s;%n", begin, end));
			}
		}
		return builder.toString();
	}

	public static String getDomainName() {
		return domainName;
	}

	public static void setDomainName(String domainName) {
		ConfigNetDHCP.domainName = domainName;
	}

	public static JSONArray getDomainNameServers() {
		return domainNameServers;
	}

	public static void setDomainNameServers(JSONArray domainNameServers) {
		ConfigNetDHCP.domainNameServers = domainNameServers;
	}

	public static String getIpRouter() {
		return ipRouter;
	}

	public static void setIpRouter(String ipRouter) {
		ConfigNetDHCP.ipRouter = ipRouter;
	}

	public static String getNetmask() {
		return netmask;
	}

	public static void setNetmask(String netmask) {
		ConfigNetDHCP.netmask = netmask;
	}

	public static String getSubnetMask() {
		return subnetMask;
	}

	public static void setSubnetMask(String subnetMask) {
		ConfigNetDHCP.subnetMask = subnetMask;
	}

	public static String getDomainNameServersAddress() {
		return domainNameServersAddress;
	}

	public static void setDomainNameServersAddress(String domainNameSystemAddress) {
		ConfigNetDHCP.domainNameServersAddress = domainNameSystemAddress;
	}

	public static String getDefaultLeaseTime() {
		return defaultLeaseTime;
	}

	public static void setDefaultLeaseTime(String defaultLeaseTime) {
		ConfigNetDHCP.defaultLeaseTime = defaultLeaseTime;
	}

	public static String getMaxLeaseTime() {
		return maxLeaseTime;
	}

	public static void setMaxLeaseTime(String maxLeaseTime) {
		ConfigNetDHCP.maxLeaseTime = maxLeaseTime;
	}

	public static JSONArray getRanges() {
		return ranges;
	}

	public static void setRanges(JSONArray ranges) {
		ConfigNetDHCP.ranges = ranges;
	}

	public static String getConfigPath() {
		return osConfigPath;
	}

	public static void setConfigPath(String path) {
		ConfigNetDHCP.osConfigPath = path;
	}

	
	
	
}
