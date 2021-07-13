package com.planetbiru.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
	
	private ConfigNetDHCP()
	{
		
	}

	public static void load(String path) {
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
					ConfigNetDHCP.domainNameServersAddress = json.optString("domainNameSystemAddress", "");
					ConfigNetDHCP.defaultLeaseTime = json.optString("defaultLeaseTime", "");
					ConfigNetDHCP.maxLeaseTime = json.optString("maxLeaseTime", "");
					ConfigNetDHCP.ranges = json.optJSONArray("ranges");

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
		config.put("domainNameSystemAddress", ConfigNetDHCP.domainNameServersAddress);
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
			//e.printStackTrace();
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
