package com.planetbiru.config;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.Utility;

public class ConfigNetWLAN {
	private ConfigNetWLAN()
	{
		
	}
	private static String essid = "";
	private static String key = "";
	private static String keyMgmt = "";
	private static String ipAddress = "";
	private static String prefix = "";
	private static String netmask = "";
	private static String gateway = "";
	private static String dns1 = "";
	
	private static String osConfigPath = "";
	private static String osConfigPathWPAPSK = "";
	private static String configPath = "";
	
	private static Logger logger = Logger.getLogger(ConfigNetWLAN.class);
	
	public static void load(String path) {
		ConfigNetWLAN.configPath = path;
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
					String lEssid = json.optString("essid", "");
					String lKeyMgmt = json.optString("keyMgmt", "");
					String lKey = json.optString("key", "");
					
					ConfigNetWLAN.ipAddress = lIpAddress;
					ConfigNetWLAN.prefix = lPrefix;
					ConfigNetWLAN.netmask = lNetmask;
					ConfigNetWLAN.gateway = lGateway;
					ConfigNetWLAN.dns1 = lDns1;
					ConfigNetWLAN.essid = lEssid;
					ConfigNetWLAN.keyMgmt = lKeyMgmt;
					ConfigNetWLAN.key = lKey;
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
		ConfigNetWLAN.save(ConfigNetWLAN.configPath);
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
	
	public static JSONObject getJSONObject() {
		JSONObject config = new JSONObject();

		config.put("ipAddress", ConfigNetWLAN.ipAddress);
		config.put("prefix", ConfigNetWLAN.prefix);
		config.put("netmask", ConfigNetWLAN.netmask);		
		config.put("gateway", ConfigNetWLAN.gateway);
		config.put("dns1", ConfigNetWLAN.dns1);
		config.put("essid", ConfigNetWLAN.essid);
		config.put("keyMgmt", ConfigNetWLAN.keyMgmt);
		config.put("key", ConfigNetWLAN.key);
		
		return config;
	}

	public static void apply(String path1, String path2)
	{
		ConfigNetWLAN.osConfigPath = path1;
		ConfigNetWLAN.osConfigPathWPAPSK = path2;
		String uuid = "605a8783-c38b-4351-8f28-e82f99fdd0c6";		
		String format = "ESSID=\"%s\"\r\n"
				+ "MODE=Ap\r\n"
				+ "KEY_MGMT=%s\r\n"
				+ "MAC_ADDRESS_RANDOMIZATION=default\r\n"
				+ "TYPE=Wireless\r\n"
				+ "PROXY_METHOD=none\r\n"
				+ "BROWSER_ONLY=no\r\n"
				+ "BOOTPROTO=none\r\n"
				+ "IPADDR=%s\r\n"
				+ "PREFIX=%s\r\n"
				+ "GATEWAY=%s\r\n"
				+ "DNS1=%s\r\n"
				+ "DEFROUTE=yes\r\n"
				+ "IPV4_FAILURE_FATAL=no\r\n"
				+ "IPV6INIT=yes\r\n"
				+ "IPV6_AUTOCONF=yes\r\n"
				+ "IPV6_DEFROUTE=yes\r\n"
				+ "IPV6_FAILURE_FATAL=no\r\n"
				+ "IPV6_ADDR_GEN_MODE=stable-privacy\r\n"
				+ "NAME=wlan0\r\n"
				+ "UUID=%s\r\n"
				+ "ONBOOT=yes\r\n"
				+ "";
		String data = String.format(format, 
				ConfigNetWLAN.essid, 
				ConfigNetWLAN.keyMgmt,
				ConfigNetWLAN.ipAddress,
				ConfigNetWLAN.prefix,
				ConfigNetWLAN.gateway,
				ConfigNetWLAN.dns1,
				uuid
				);
		try 
		{
			FileConfigUtil.write(ConfigNetWLAN.osConfigPath, data.getBytes());
			FileConfigUtil.write(ConfigNetWLAN.osConfigPathWPAPSK, ConfigNetWLAN.key.getBytes());
		}
		catch (IOException e) 
		{
			logger.error(e.getMessage(), e);
		}
	
	}

	public static String getEssid() {
		return essid;
	}

	public static void setEssid(String essid) {
		ConfigNetWLAN.essid = essid;
	}

	public static String getKey() {
		return key;
	}

	public static void setKey(String key) {
		ConfigNetWLAN.key = key;
	}

	public static String getKeyMgmt() {
		return keyMgmt;
	}

	public static void setKeyMgmt(String keyMgmt) {
		ConfigNetWLAN.keyMgmt = keyMgmt;
	}

	public static String getIpAddress() {
		return ipAddress;
	}

	public static void setIpAddress(String ipAddress) {
		ConfigNetWLAN.ipAddress = ipAddress;
	}

	public static String getPrefix() {
		return prefix;
	}

	public static void setPrefix(String prefix) {
		ConfigNetWLAN.prefix = prefix;
	}

	public static String getNetmask() {
		return netmask;
	}

	public static void setNetmask(String netmask) {
		ConfigNetWLAN.netmask = netmask;
	}

	public static String getGateway() {
		return gateway;
	}

	public static void setGateway(String gateway) {
		ConfigNetWLAN.gateway = gateway;
	}

	public static String getDns1() {
		return dns1;
	}

	public static void setDns1(String dns1) {
		ConfigNetWLAN.dns1 = dns1;
	}

	public static String getConfigPath() {
		return osConfigPath;
	}

	public static void setConfigPath(String configPath) {
		ConfigNetWLAN.osConfigPath = configPath;
	}

	public static String getConfigPathWPAPSK() {
		return osConfigPathWPAPSK;
	}

	public static void setConfigPathWPAPSK(String configPathWPAPSK) {
		ConfigNetWLAN.osConfigPathWPAPSK = configPathWPAPSK;
	}

	public static JSONObject toJSONObject()
	{
		return getJSONObject();
	}
	
	
}
