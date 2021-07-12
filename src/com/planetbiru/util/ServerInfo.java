package com.planetbiru.util;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.json.JSONArray;
import org.json.JSONObject;

import com.planetbiru.WebSocketServerImpl;
import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigModem;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.gsm.GSMUtil;

public class ServerInfo {
	
	private static final String TOTAL = "total";
	private static final String USED = "used";
	private static final String FREE = "free";
	private static final String RAM = "ram";
	private static final String SWAP = "swap";
	private static final String PERCENT_USED = "percentUsed";

	private ServerInfo()
	{
		
	}
	
	public static void sendWSStatus(boolean connected, String message) 
    {
		JSONArray data = new JSONArray();
		JSONObject info = new JSONObject();
		
		JSONObject ws = new JSONObject();
		ws.put(JsonKey.NAME, "otp-ws-connected");
		ws.put(JsonKey.VALUE, connected);
		ws.put(JsonKey.MESSAGE, message);
		data.put(ws);
		
		info.put(JsonKey.COMMAND, "server-info");
		info.put(JsonKey.DATA, data);
	
		WebSocketServerImpl.broadcastMessage(info.toString(4));				
	}
	
	public static void sendWSStatus(boolean connected) {
		ServerInfo.sendWSStatus(connected, "");		
	}
	
	public static void sendAMQPStatus(boolean connected)
	{
		JSONArray data = new JSONArray();
		JSONObject info = new JSONObject();
		
		JSONObject ws = new JSONObject();
		ws.put(JsonKey.NAME, "otp-amqp-connected");
		ws.put(JsonKey.VALUE, connected);
		data.put(ws);
		
		info.put(JsonKey.COMMAND, "server-info");
		info.put(JsonKey.DATA, data);
	
		WebSocketServerImpl.broadcastMessage(info.toString(4));
	}

	public static void sendModemStatus()
	{
		JSONArray data = new JSONArray();
		JSONObject modem = new JSONObject();
		modem.put(JsonKey.NAME, "otp-modem-connected");
		modem.put(JsonKey.VALUE, GSMUtil.isConnected());
		modem.put(JsonKey.DATA, ConfigModem.getStatus());
		data.put(modem);
		JSONObject serverInfo = new JSONObject();
		serverInfo.put(JsonKey.DATA, data);
		serverInfo.put(JsonKey.COMMAND, "server-info");
		WebSocketServerImpl.broadcastMessage(serverInfo.toString());
	}

	private static String cacheServerInfo = "";
	private static long cacheServerInfoExpire = 0;
	private static long cacheLifetime = 60000;
	public static String getInfo() {
		long currentTime = System.currentTimeMillis();
		if(currentTime > getCacheServerInfoExpire())
		{
			AsyncServerInfo serverInfo = new AsyncServerInfo();
			serverInfo.start();
		}
		if(!cacheServerInfo.isEmpty())
		{
			return cacheServerInfo;
		}
		else
		{
			return "{}";
		}
	}
	
	public static JSONObject memoryInfo()
	{
		String result =   "              total        used        free      shared  buff/cache   available\r\n"
						+ "Mem:        1877280      674068      211560       98540      991652      926216\r\n"
						+ "Swap:       4194300      166400     4027900";
		
		String command = "free";
		int sleep = 10;
		try 
		{
			result = CommandLineExecutor.execSSH(command, sleep);
		} 
		catch (IOException e) 
		{
			
		}
		
		
		result = fixingRawData(result);

		JSONObject info = new JSONObject();
		
		String[] lines = result.split("\r\n");
		for(int i = 0; i<lines.length;i++)
		{
			lines[i] = lines[i].replaceAll("\\s+", " ").trim();
			if(lines[i].contains("Mem:"))
			{
				String[] arr2 = lines[i].split(" ");
				if(arr2.length >= 4)
				{
					String totalStr = arr2[1];
					String usedStr = arr2[2];
					String freeStr = arr2[3];		
					double total = Utility.atof(totalStr);
					double used = Utility.atof(usedStr);
					double free = Utility.atof(freeStr);
					double percentUsed  = 100 * used/total;
					JSONObject ram = new JSONObject();
					
					ram.put(ServerInfo.TOTAL, total);
					ram.put(ServerInfo.USED, used);
					ram.put(ServerInfo.FREE, free);				
					ram.put(ServerInfo.PERCENT_USED, percentUsed);				
					info.put(ServerInfo.RAM, ram);
				}
			}
			
			if(lines[i].contains("Swap:"))
			{
				String[] arr2 = lines[i].split(" ");
				if(arr2.length >= 4)
				{
					String totalStr = arr2[1];
					String usedStr = arr2[2];
					String freeStr = arr2[3];		
					int total = Utility.atoi(totalStr);
					int used = Utility.atoi(usedStr);
					int free = Utility.atoi(freeStr);
					float percentUsed  = 100 * ((float)used/(float)total);
					JSONObject swap = new JSONObject();
					
					swap.put(ServerInfo.TOTAL, total);
					swap.put(ServerInfo.USED, used);
					swap.put(ServerInfo.FREE, free);				
					swap.put(ServerInfo.PERCENT_USED, percentUsed);				
					info.put(ServerInfo.SWAP, swap);
				}
			}
		}
		return info;
	}
	public static String cpuSerialNumber()
	{
		String serialNumber = "";
		String result =   "Serial Number : 00000000f069215d";

		
		String command = "more /proc/cpuinfo | grep Serial";
		int sleep = 10;
		try 
		{
			result = CommandLineExecutor.execSSH(command, sleep);
		} 
		catch (IOException e) 
		{
		}
		
		
		result = fixingRawData(result);
		
		String[] arr = result.split("\r\n");
		String str = arr[0];
		if(str.contains(":"))
		{
			String[] arr2 = str.split("\\:", 2);
			serialNumber = arr2[1].trim();		
		}
		
		return serialNumber;
	}
	
	public static String cpuSerialNumberHmac()
	{
		String serialNumber = ServerInfo.cpuSerialNumber();
		String secret = Config.getCpuSecret();
		String hMac = "";
		try 
		{
			hMac = Utility.bytesToHex(Utility.hMac("sha512", serialNumber.getBytes(), secret.getBytes()));
		} 
		catch (InvalidKeyException | NoSuchAlgorithmException e) 
		{
			/**
			 * Do nothing
			 */
		}
		return hMac;
	}
	
	public static JSONObject storageInfo()
	{
		String result =   "Filesystem     1K-blocks     Used Available Use% Mounted on\r\n"
						+ "devtmpfs          914364        0    914364   0% /dev\r\n"
						+ "tmpfs             938640        0    938640   0% /dev/shm\r\n"
						+ "tmpfs             938640   107104    831536  12% /run\r\n"
						+ "tmpfs             938640        0    938640   0% /sys/fs/cgroup\r\n"
						+ "/dev/vda1       41931756 24046252  17885504  58% /\r\n"
						+ "tmpfs             187728        0    187728   0% /run/user/0";
		
		String command = "df -h";
		int sleep = 10;
		
		try 
		{
			result = CommandLineExecutor.execSSH(command, sleep);
		} 
		catch (IOException e) 
		{
			
		}
		
		


		result = fixingRawData(result);	
		String[] lines = result.split("\r\n");	
		JSONObject info = new JSONObject();	
		if(lines.length > 1)
		{
			for(int i = 1; i<lines.length;i++)
			{
				lines[i] = lines[i].replaceAll("\\s+", " ").trim();
				String[] arr2 = lines[i].split(" ", 6);
				if(arr2.length >= 6 && arr2[5].equals("/"))
				{
					String total = arr2[1];
					String used = arr2[2];
					String avail = arr2[3];
					String percent = arr2[4];
					info.put(ServerInfo.TOTAL, Utility.atof(total));
					info.put(ServerInfo.USED, Utility.atof(used));
					info.put("available", Utility.atof(avail));
					info.put(ServerInfo.PERCENT_USED, Utility.atof(percent));
				}
			}
		}	
		return info;
	}
	
	public static String fixingRawData(String result)
	{
		result = result.replace("\n", "\r\n");
		result = result.replace("\r\r\n", "\r\n");
		result = result.replace("\r", "\r\n");
		result = result.replace("\r\n\n", "\r\n");
		return result;
	}
	
	public static JSONObject cpuTemperatureInfo()
	{
		String result =   "Adapter: ISA adapter\r\n"
						+ "Core 0:       +48.0°C  (high = +98.0°C, crit = +98.0°C)\r\n"
						+ "Core 1:       +48.0°C  (high = +98.0°C, crit = +98.0°C)\r\n"
						+ "Core 2:       +48.0°C  (high = +98.0°C, crit = +98.0°C)\r\n"
						+ "Core 3:       +47.0°C  (high = +98.0°C, crit = +98.0°C)";
		
		
		String command = "sensors";
		int sleep = 10;
		try 
		{
			result = CommandLineExecutor.execSSH(command, sleep);
		} 
		catch (IOException e) 
		{
			
		}
		
		result = result.replace("°", "&deg;");
		result = fixingRawData(result);
		
		String adapter = getCPUSensorAdapter(result);
		
		JSONArray cores = getCPUTemperatureCore(result);
		
		JSONObject info = new JSONObject();
		info.put("adapter", adapter);
		info.put("temperature", cores);
		info.put("usage", cpuUsage());
		return info;
	}
	
	public static JSONObject cpuTemperatureInfo2()
	{
		String result =   "Adapter: ISA adapter\r\n"
						+ "Core 0:       +48.0°C  (high = +98.0°C, crit = +98.0°C)\r\n"
						+ "Core 1:       +48.0°C  (high = +98.0°C, crit = +98.0°C)\r\n"
						+ "Core 2:       +48.0°C  (high = +98.0°C, crit = +98.0°C)\r\n"
						+ "Core 3:       +47.0°C  (high = +98.0°C, crit = +98.0°C)";
		
		
		String command = "sensors";
		int sleep = 10;
		try 
		{
			result = CommandLineExecutor.execSSH(command, sleep);
		} 
		catch (IOException e) 
		{
			
		}

		
		result = result.replace("°", "&deg;");
		result = fixingRawData(result);
		
		String adapter = getCPUSensorAdapter(result);
		
		JSONArray cores = getCPUTemperatureCore(result);
		
		JSONObject info = new JSONObject();
		info.put("adapter", adapter);
		info.put("temperature", cores);
		info.put("usage", cpuUsage());
		return info;
	}
	
	public static JSONObject cpuUsage()
	{
		String result =   "Linux 3.10.0-1160.11.1.el7.x86_64 (server.planetbiru.com)       06/16/2021      _x86_64_        (2 CPU)\r\n"
						+ "\r\n"
						+ "07:08:48 PM  CPU    %usr   %nice    %sys %iowait    %irq   %soft  %steal  %guest  %gnice   %idle\r\n"
						+ "07:08:48 PM  all   44.34    0.00    1.09    0.03    0.00    0.08    0.07    0.00    0.00   54.39";
						// 02:50:41 AM  all    8.93    0.04    1.76    1.34    0.00    0.11    0.00    0.00    0.00   87.82

		
		String command = "mpstat | grep all";
		int sleep = 10;
		try 
		{
			result = CommandLineExecutor.execSSH(command, sleep);
		} 
		catch (IOException e) 
		{
			
		}
		

		result = fixingRawData(result);
		result = result.replace("\r\n\r\n", "\r\n");
		
		JSONObject info = new JSONObject();
		String[] lines = result.split("\r\n");
		String[] values = new String[1];
		for(int i = 0; i<lines.length;i++)
		{
			lines[i] = lines[i].replaceAll("\\s+", " ").trim();
			if(lines[i].contains("CPU "))
			{
			}
			if(lines[i].contains("all "))
			{
				values = lines[i].split(" ");
			}
		}
		String iddle = values[12];
		double idle = Utility.atof(iddle);
		double used = 100 - idle;
		info.put("idle", idle);
		info.put(ServerInfo.USED, used);
		info.put(ServerInfo.PERCENT_USED, used);
		return info;	
	}

	private static String getCPUSensorAdapter(String result) {
		
		String[] lines = result.split("\r\n");
		String adapter = "";
		for(int i = 0; i<lines.length;i++)
		{
			if(lines[i].contains("Adapter:"))
			{
				String[] arr2 = lines[i].split("\\:", 2);
				if(arr2.length == 2)
				{
					adapter = arr2[1].trim();
				}
			}
		}
		return adapter;
	}
	
	public static JSONArray getCPUTemperature(String result) {
		String[] lines = result.split("\r\n");
		JSONArray cores = new JSONArray();
		for(int i = 0; i<lines.length;i++)
		{
			
			if(lines[i].contains(":") && !lines[i].contains("Adapter"))
			{
				String[] arr2 = lines[i].split("\\:", 2);
				if(arr2.length == 2)
				{
					JSONObject core = getCPUTemperature(arr2);
					if(core != null)
					{
						cores.put(core);
					}
				}
			}
		}
		return cores;
	}

	public static JSONArray getCPUTemperatureCore(String cpuInfo) {
		String[] arr3 = cpuInfo.split("\r\n");
		JSONArray temp = new JSONArray();
		for(int i = 0; i<arr3.length; i++)
		{
			arr3[i] = arr3[i].replaceAll("\\s+", " ").trim();
			if(arr3[i].contains("temp1:"))
			{
				String[] arr4 = arr3[i].split(" ");
				if(arr4.length > 1)
				{
					JSONObject core = new JSONObject();
					JSONObject raw = new JSONObject();
					JSONObject value = new JSONObject();
					String currentTemperatureentTemp = arr4[1];
					raw.put("currentTemperature", currentTemperatureentTemp.replace("+", ""));				
					value.put("currentTemperature", Utility.atof(currentTemperatureentTemp));
					
					core.put("label", "Core 0");
					core.put("raw", raw);
					core.put("value", value);
					temp.put(core);
				}
			}
		}
		return temp;
	}
	
	public static JSONObject getCPUTemperature(String[] arr2) {
		String cpuLabel = arr2[0].trim();
		String cpuInfo = arr2[1].trim();
		cpuInfo = cpuInfo.replaceAll("\\s+"," ");
		cpuInfo = cpuInfo.replace("(", " ");
		cpuInfo = cpuInfo.replace(")", " ");
		String[] arr3 = cpuInfo.split(" ", 2);
		JSONObject core = null;
		if(arr3.length == 2)
		{
			String currentTemperatureentTemp = arr3[0];
			String[] arr4 = arr3[1].split("\\,");
			String high = "";
			String crit = "";
			
			for(int j = 0; j < arr4.length; j++)
			{
				if(arr4[j].contains("high"))
				{
					high = arr4[j];
					high = high.replace("high", "");
					high = high.replace("=", "");
					high = high.replaceAll("\\s+", "");
				}
				if(arr4[j].contains("crit"))
				{
					crit = arr4[j];
					crit = crit.replace("crit", "");
					crit = crit.replace("=", "");
					crit = crit.replaceAll("\\s+", "");
				}
			}
			core = new JSONObject();
			JSONObject raw = new JSONObject();
			JSONObject value = new JSONObject();
			
			raw.put("currentTemperature", currentTemperatureentTemp.replace("+", ""));
			raw.put("hightTemperature", high.replace("+", ""));
			raw.put("criticalTemperature", crit.replace("+", ""));
			
			value.put("currentTemperature", Utility.atof(currentTemperatureentTemp));
			value.put("hightTemperature", Utility.atof(high));
			value.put("criticalTemperature", Utility.atof(crit));
			
			core.put("label", cpuLabel);
			core.put("raw", raw);
			core.put("value", value);
		}
		return core;
	}

	public static String getCacheServerInfo() {
		return cacheServerInfo;
	}

	public static void setCacheServerInfo(String cacheServerInfo) {
		ServerInfo.cacheServerInfo = cacheServerInfo;
	}

	public static long getCacheServerInfoExpire() {
		return cacheServerInfoExpire;
	}

	public static void setCacheServerInfoExpire(long cacheServerInfoExpire) {
		ServerInfo.cacheServerInfoExpire = cacheServerInfoExpire;
	}

	public static long getCacheLifetime() {
		return cacheLifetime;
	}

	public static void setCacheLifetime(long cacheLifetime) {
		ServerInfo.cacheLifetime = cacheLifetime;
	}
}






