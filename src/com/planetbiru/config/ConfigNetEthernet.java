package com.planetbiru.config;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.Utility;

public class ConfigNetEthernet {
	
	private static String ipAddress = "";
	private static String prefix = "";
	private static String netmask = "";
	private static String gateway = "";
	private static String dns1 = "";
	private static String dns2 = "";
	
	private static String osConfigPath = "";

	private static String configPath = "";
	
	private static Logger logger = Logger.getLogger(ConfigNetEthernet.class);
	
	private ConfigNetEthernet()
	{
		
	}

	public static void load(String path) {
		ConfigNetEthernet.configPath = path;
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
					
					String lIpAddress = json.optString("ipAddress", "");
					String lPrefix = json.optString("prefix", "");
					String lNetmask = json.optString("netmask", "");
					String lGateway = json.optString("gateway", "");
					String lDns1 = json.optString("dns1", "");
					String lDns2 = json.optString("dns2", "");
					
					ConfigNetEthernet.ipAddress = lIpAddress;
					ConfigNetEthernet.prefix = lPrefix;
					ConfigNetEthernet.netmask = lNetmask;
					ConfigNetEthernet.gateway = lGateway;
					ConfigNetEthernet.dns1 = lDns1;
					ConfigNetEthernet.dns2 = lDns2;
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
		ConfigNetEthernet.save(ConfigNetEthernet.configPath);
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

		config.put("ipAddress", ConfigNetEthernet.ipAddress);
		config.put("prefix", ConfigNetEthernet.prefix);
		config.put("netmask", ConfigNetEthernet.netmask);		
		config.put("gateway", ConfigNetEthernet.gateway);
		config.put("dns1", ConfigNetEthernet.dns1);
		config.put("dns2", ConfigNetEthernet.dns2);
		
		return config;
	}

	public static void apply(String path)
	{
		ConfigNetEthernet.osConfigPath = path;
		String uuid = "a5ae9a6c-3951-4e8a-b99d-a4ea5dc33bf1";
		String format = "TYPE=\"Ethernet\"\r\n"
				+ "BOOTPROTO=none\r\n"
				+ "NM_CONTROLLED=yes\r\n"
				+ "DEFROUTE=yes\r\n"
				+ "NAME=\"eth0\"\r\n"
				+ "UUID=\"%s\"\r\n"
				+ "ONBOOT=yes\r\n"
				+ "DNS1=%s\r\n"
				+ "IPV4_FAILURE_FATAL=no\r\n"
				+ "IPV6INIT=no\r\n"
				+ "IPADDR=%s\r\n"
				+ "PREFIX=%s\r\n"
				+ "GATEWAY=%s\r\n"
				+ "NETMASK=%s\r\n"
				+ "DNS2=%s";
		String data = String.format(format, uuid, 
				ConfigNetEthernet.dns1, 
				ConfigNetEthernet.ipAddress, 
				ConfigNetEthernet.prefix, 
				ConfigNetEthernet.gateway,
				ConfigNetEthernet.netmask,
				ConfigNetEthernet.dns2
				);
		try 
		{
			FileConfigUtil.write(ConfigNetEthernet.osConfigPath, data.getBytes());
		}
		catch (IOException e) 
		{
			logger.error(e.getMessage(), e);
		}
	
	}

	public static String getIpAddress() {
		return ipAddress;
	}

	public static void setIpAddress(String ipAddress) {
		ConfigNetEthernet.ipAddress = ipAddress;
	}

	public static String getPrefix() {
		return prefix;
	}

	public static void setPrefix(String prefix) {
		ConfigNetEthernet.prefix = prefix;
	}

	public static String getNetmask() {
		return netmask;
	}

	public static void setNetmask(String netmask) {
		ConfigNetEthernet.netmask = netmask;
	}

	public static String getGateway() {
		return gateway;
	}

	public static void setGateway(String gateway) {
		ConfigNetEthernet.gateway = gateway;
	}

	public static String getDns1() {
		return dns1;
	}

	public static void setDns1(String dns1) {
		ConfigNetEthernet.dns1 = dns1;
	}

	public static String getDns2() {
		return dns2;
	}

	public static void setDns2(String dns2) {
		ConfigNetEthernet.dns2 = dns2;
	}

	public static String getConfigPath() {
		return osConfigPath;
	}

	public static void setConfigPath(String configPath) {
		ConfigNetEthernet.osConfigPath = configPath;
	}

	public static JSONObject toJSONObject()
	{
		return getJSONObject();
	}

	
	
}
