package com.planetbiru.util;

import java.io.File;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

import com.planetbiru.Application;
import com.planetbiru.ServerWebSocketAdmin;
import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigAPI;
import com.planetbiru.config.ConfigModem;
import com.planetbiru.config.ConfigSubscriberAMQP;
import com.planetbiru.config.ConfigSubscriberActiveMQ;
import com.planetbiru.config.ConfigSubscriberMQTT;
import com.planetbiru.config.ConfigSubscriberRedis;
import com.planetbiru.config.ConfigSubscriberStomp;
import com.planetbiru.config.ConfigSubscriberWS;
import com.planetbiru.constant.ConstantString;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.gsm.GSMUtil;

public class ServerInfo {
	
	public static final String TOTAL                = "total";
	public static final String USED                 = "used";
	public static final String FREE                 = "free";
	public static final String RAM                  = "ram";
	public static final String SWAP                 = "swap";
	public static final String PERCENT_USED         = "percentUsed";
	public static final String CURRENT_TEMPERATURE  = "currentTemperature";
	public static final String HIGH_TEMPERATURE     = "highTemperature";
	public static final String CRITICAL_TEMPERATURE = "criticalTemperature";
	public static final String CPU                  = "cpu";
	public static final String SERVER_INFO          = "server-info";
	public static final String STORAGE              = "storage";
	public static final String MEMORY               = "memory";
	public static final String AVAILABLE            = "available";
	public static final String RAW                  = "raw";
	public static final String LABEL                = "label";
	public static final String VALUE                = "value";
	public static final String TEMPERATURE          = "temperature";
	public static final String ADAPTER              = "adapter";
	public static final String PORT                 = "port";
	public static final String USAGE                = "usage";
	public static final String DATETIME             = "datetime";

	private static String cacheServerInfo = "";
	private static long cacheServerInfoExpire = 0;
	private static long cacheLifetime = 5000;

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
		info.put(JsonKey.COMMAND, ServerInfo.SERVER_INFO);
		info.put(JsonKey.DATA, data);	
		ServerWebSocketAdmin.broadcastMessage(info.toString(0));				
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
		
		info.put(JsonKey.COMMAND, ServerInfo.SERVER_INFO);
		info.put(JsonKey.DATA, data);
	
		ServerWebSocketAdmin.broadcastMessage(info.toString(0));
	}
	
	public static void sendRedisStatus(boolean connected) {
		JSONArray data = new JSONArray();
		JSONObject info = new JSONObject();
		
		JSONObject redis = new JSONObject();
		redis.put(JsonKey.NAME, "otp-redis-connected");
		redis.put(JsonKey.VALUE, connected);
		data.put(redis);
		
		info.put(JsonKey.COMMAND, ServerInfo.SERVER_INFO);
		info.put(JsonKey.DATA, data);
	
		ServerWebSocketAdmin.broadcastMessage(info.toString(0));	
	}

	public static void sendMQTTStatus(boolean connected) {
		JSONArray data = new JSONArray();
		JSONObject info = new JSONObject();
		
		JSONObject mqtt = new JSONObject();
		mqtt.put(JsonKey.NAME, "otp-mqtt-connected");
		mqtt.put(JsonKey.VALUE, connected);
		data.put(mqtt);
		
		info.put(JsonKey.COMMAND, ServerInfo.SERVER_INFO);
		info.put(JsonKey.DATA, data);
	
		ServerWebSocketAdmin.broadcastMessage(info.toString(0));	
	}

	public static void sendActiveMQStatus(boolean connected) {
		JSONArray data = new JSONArray();
		JSONObject info = new JSONObject();
		
		JSONObject activeMQ = new JSONObject();
		activeMQ.put(JsonKey.NAME, "otp-activemq-connected");
		activeMQ.put(JsonKey.VALUE, connected);
		data.put(activeMQ);
		
		info.put(JsonKey.COMMAND, ServerInfo.SERVER_INFO);
		info.put(JsonKey.DATA, data);
	
		ServerWebSocketAdmin.broadcastMessage(info.toString(0));		
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
		serverInfo.put(JsonKey.COMMAND, ServerInfo.SERVER_INFO);
		ServerWebSocketAdmin.broadcastMessage(serverInfo.toString());
	}

	public static String getInfo() {
		JSONObject info = new JSONObject();
		info.put(ServerInfo.CPU, ServerInfo.cpuTemperatureInfo());
		info.put(ServerInfo.STORAGE, ServerInfo.storageInfo());
		info.put(ServerInfo.MEMORY, ServerInfo.memoryInfo());
		return info.toString();
	}
	
	public static JSONObject memoryInfo()
	{
		if(OSUtil.isWindows())
		{
			return memoryInfoWindows();
		}
		else
		{
			return memoryInfoLinux();
		}
	}
	
	private static JSONObject memoryInfoLinux() {
		JSONObject info = new JSONObject();
		String command = "free";
		String result = CommandLineExecutor.exec(command).toString();		
		
		result = fixingRawData(result);
		
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

	private static JSONObject memoryInfoWindows() {
		JSONObject info = new JSONObject();

		JSONObject ram = new JSONObject();

        com.sun.management.OperatingSystemMXBean os = (com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory.getOperatingSystemMXBean(); //NOSONAR
        @SuppressWarnings("deprecation")
		long total = os.getTotalPhysicalMemorySize(); //NOSONAR
        @SuppressWarnings("deprecation")
		long free = os.getFreePhysicalMemorySize(); //NOSONAR

		long used = total - free;
		float percentUsed  = 100 * ((float)used/(float)total);
		
		ram.put(ServerInfo.TOTAL, total);
		ram.put(ServerInfo.USED, used);
		ram.put(ServerInfo.FREE, free);				
		ram.put(ServerInfo.PERCENT_USED, percentUsed);				
		info.put(ServerInfo.RAM, ram);

		JSONObject swap = new JSONObject();

		swap.put(ServerInfo.TOTAL, 0);
		swap.put(ServerInfo.USED, 0);
		swap.put(ServerInfo.FREE, 0);				
		swap.put(ServerInfo.PERCENT_USED, 0);				
		info.put(ServerInfo.SWAP, swap);
		return info;
	}

	public static String cpuSerialNumber()
	{
		String serialNumber = "";
		String command = "more /proc/cpuinfo | grep Serial";
		String result = CommandLineExecutor.exec(command).toString();
		
		
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
			hMac = Utility.bytesToHex(Utility.hMac("Hmacsha512", serialNumber.getBytes(), secret.getBytes()));
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
		if(OSUtil.isWindows())
		{
			return storageInfoWindows();
		}
		else
		{
			return storageInfoLinux();
		}
	}
	private static JSONObject storageInfoWindows() {
		JSONObject info = new JSONObject();	
		String dir = Utility.getBaseDir();
		File diskPartition = new File(dir);
		
		long total = diskPartition.getTotalSpace();
		long avail = diskPartition.getUsableSpace();
		long used = total - avail;
		float percentUsed = 100 * ((float)used / (float)total);
		
		info.put(ServerInfo.TOTAL, total);
		info.put(ServerInfo.AVAILABLE, avail);	
		info.put(ServerInfo.USED, used);
		info.put(ServerInfo.PERCENT_USED, percentUsed);
		
		return info;
	}

	public static JSONObject storageInfoLinux()
	{
		String command = "df -h";
		String result = CommandLineExecutor.exec(command).toString();
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
					
					double factorTotal = getFactor(total);
					double factorUsed = getFactor(used);
					double factorAvail = getFactor(avail);
					
					info.put(ServerInfo.TOTAL, Utility.atof(total) * factorTotal);
					info.put(ServerInfo.USED, Utility.atof(used) * factorUsed);
					info.put(ServerInfo.AVAILABLE, Utility.atof(avail) * factorAvail);
					info.put(ServerInfo.PERCENT_USED, Utility.atof(percent));
				}
			}
		}	
		return info;
	}
	
	public static double getFactor(String value)
	{
		double factor = 1;
		if(value.contains("G"))
		{
			factor = 1048576;
		}
		else if(value.contains("M"))
		{
			factor = 1024;
		}
		else if(value.toUpperCase().contains("K"))
		{
			factor = 1;
		}
		return factor;
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
		String command = "/bin/sensors";
		String result = CommandLineExecutor.exec(command).toString();
		result = result.replace("°", "&deg;");
		result = fixingRawData(result);
		
		String adapter = getCPUSensorAdapter(result);
		
		JSONArray cores = getCPUTemperatureCore(result);
		
		JSONObject info = new JSONObject();
		info.put(ServerInfo.ADAPTER, adapter);
		info.put(ServerInfo.TEMPERATURE, cores);
		info.put(ServerInfo.USAGE, cpuUsage());
		return info;
	}
	
	public static JSONObject cpuTemperatureInfo2()
	{
		String command = "sensors";
		String result = CommandLineExecutor.exec(command).toString();
		
		result = result.replace("°", "&deg;");
		result = fixingRawData(result);
		
		String adapter = getCPUSensorAdapter(result);
		
		JSONArray cores = getCPUTemperatureCore(result);
		
		JSONObject info = new JSONObject();
		info.put(ServerInfo.ADAPTER, adapter);
		info.put(ServerInfo.TEMPERATURE, cores);
		info.put(ServerInfo.USAGE, cpuUsage());
		return info;
	}
	public static JSONObject cpuUsage()
	{
		if(OSUtil.isWindows())
		{
			return cpuUsageWindows();
		}
		else
		{
			return cpuUsageLinux();
		}
	}
	
	private static JSONObject cpuUsageWindows() {
		JSONObject info = new JSONObject();
		com.sun.management.OperatingSystemMXBean os = (com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory.getOperatingSystemMXBean(); //NOSONAR
		double used = os.getCpuLoad() * 100;
		double idle = 100 - used;
		info.put(JsonKey.IDLE, idle);
		info.put(ServerInfo.USED, used);
		info.put(ServerInfo.PERCENT_USED, used);
		return info;
	}

	public static JSONObject cpuUsageLinux()
	{
		String command = "/bin/mpstat";
		String result = CommandLineExecutor.exec(command).toString();		

		result = fixingRawData(result);
		result = result.replace("\r\n\r\n", "\r\n");
		
		JSONObject info = new JSONObject();
		String[] lines = result.split("\r\n");
		String iddle = "0";
		if(lines.length > 1)
		{
			String[] keys = new String[1];
			String[] values = new String[1];
			Map<String, String> pairs = new HashMap<>();
			for(int i = 0; i<lines.length;i++)
			{
				lines[i] = lines[i].replaceAll("\\s+", " ").trim();
				if(lines[i].contains("CPU "))
				{
					keys = lines[i].split(" ");
				}
				if(lines[i].contains("all "))
				{
					values = lines[i].split(" ");
				}
			}
			for(int i = 0; i<values.length && i <keys.length; i++)
			{
				pairs.put(keys[i].replace("%", ""), values[i]);
			}
			iddle = pairs.getOrDefault(JsonKey.IDLE, "0");
		}
		double idle = Utility.atof(iddle);
		double used = 100 - idle;
		info.put(JsonKey.IDLE, idle);
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
					raw.put(ServerInfo.CURRENT_TEMPERATURE, currentTemperatureentTemp.replace("+", ""));				
					value.put(ServerInfo.CURRENT_TEMPERATURE, Utility.atof(currentTemperatureentTemp));
					
					core.put(ServerInfo.LABEL, "Core 0");
					core.put(ServerInfo.RAW, raw);
					core.put(ServerInfo.VALUE, value);
					temp.put(core);
				}
			}
		}
		return temp;
	}
	
	public static JSONArray getOpenPort() {
		JSONArray info = new JSONArray();
		String command = "/sbin/lsof -i -P -n";
		String result = CommandLineExecutor.exec(command).toString();
		result = fixingRawData(result).trim();
		String[] arr = result.split("\r\n");
		if(arr.length > 1)
		{
			String line = arr[0].replaceAll("\\s+", " ").trim();
			String[] arr1 = line.split(" ", 9);
			
			for(int i = 0; i<arr1.length; i++)
			{
				arr1[i] = arr1[i].replace("/", "_").replace(" ", "_").toLowerCase().trim();
			}
			for(int j = 1; j < arr.length; j++)
			{
				JSONObject item = parseOpenPortLine(arr, arr1, j);
				if(item != null)
				{
					info.put(item);
				}
			}
		}
		return info;		
	}
	
	private static JSONObject parseOpenPortLine(String[] arr, String[] arr1, int j) {
		String[] arr2 = arr[j].replaceAll("\\s+", " ").trim().split(" ", 9);
		for(int i = 0; i<arr2.length; i++)
		{
			arr2[i] = arr2[i].trim();
		}
		if(arr1.length >= 9 && arr2.length >= 9)
		{
			JSONObject item = new JSONObject();
			for(int k = 0; k<9; k++)
			{
				item.put(arr1[k], arr2[k]);
			}
			if(item.optString("name", "").toUpperCase().contains("(LISTEN)"))
			{
				item.put(ServerInfo.PORT, extractPort(item.optString("name", "")));
				String sizeOff = item.optString("size_off", "");
				if(sizeOff.contains("t"))
				{
					String[] arr3 = sizeOff.split("t");
					int size = Utility.atoi(arr3[0]);
					int offset = Utility.atoi(arr3[1]);
					item.put("size", size);
					item.put("offset", offset);
				}
				return item;
			}
		}
		return null;
	}

	public static int extractPort(String nodeNmae)
	{
		nodeNmae = nodeNmae.replace("->", " ");
		int port = 0;
		String[] arr = nodeNmae.split(" ");
		for(int i = 0; i<arr.length; i++)
		{
			if(arr[i].contains(":"))
			{
				String[] p = arr[i].split(":", 2);
				port = Utility.atoi(p[1]);
				break;
			}
		}
		return port;
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
			
			raw.put(ServerInfo.CURRENT_TEMPERATURE, currentTemperatureentTemp.replace("+", ""));
			raw.put(ServerInfo.HIGH_TEMPERATURE, high.replace("+", ""));
			raw.put(ServerInfo.CRITICAL_TEMPERATURE, crit.replace("+", ""));
			
			value.put(ServerInfo.CURRENT_TEMPERATURE, Utility.atof(currentTemperatureentTemp));
			value.put(ServerInfo.HIGH_TEMPERATURE, Utility.atof(high));
			value.put(ServerInfo.CRITICAL_TEMPERATURE, Utility.atof(crit));
			
			core.put(ServerInfo.LABEL, cpuLabel);
			core.put(ServerInfo.RAW, raw);
			core.put(ServerInfo.VALUE, value);
		}
		return core;
	}

	public static JSONObject buildServerInfo(String services)
	{
		if(services == null || services.isEmpty())
		{
			services = ConstantString.SERVICE_ALL;
		}
		String[] arr = services.split(",");
		List<String> list = Arrays.asList(arr);
		
		JSONArray data = new JSONArray();
		JSONObject info = new JSONObject();
		
		if(list.contains(ConstantString.SERVICE_MODEM))
		{
			JSONObject modem = new JSONObject();
			modem.put(JsonKey.NAME, "otp-modem-connected");
			modem.put(JsonKey.VALUE, GSMUtil.isConnected());
			modem.put(JsonKey.DATA, ConfigModem.getStatus());
			data.put(modem);
		}
		
		if(list.contains(ConstantString.SERVICE_WS))
		{
			JSONObject wsEnable = new JSONObject();
			wsEnable.put(JsonKey.NAME, "otp-ws-enable");
			wsEnable.put(JsonKey.VALUE, ConfigSubscriberWS.isSubscriberWsEnable());
			data.put(wsEnable);
			
			JSONObject wsConnected = new JSONObject();
			wsConnected.put(JsonKey.NAME, "otp-ws-connected");
			wsConnected.put(JsonKey.VALUE, ConfigSubscriberWS.isConnected());
			data.put(wsConnected);
		}
		
		if(list.contains(ConstantString.SERVICE_AMQP))
		{
			JSONObject amqpEnable = new JSONObject();
			amqpEnable.put(JsonKey.NAME, "otp-amqp-enable");
			amqpEnable.put(JsonKey.VALUE, ConfigSubscriberAMQP.isSubscriberAmqpEnable());
			data.put(amqpEnable);
			
			JSONObject amqpConnected = new JSONObject();
			amqpConnected.put(JsonKey.NAME, "otp-amqp-connected");
			amqpConnected.put(JsonKey.VALUE, ConfigSubscriberAMQP.isConnected());
			data.put(amqpConnected);
		}
		
		if(list.contains(ConstantString.SERVICE_REDIS))
		{
			JSONObject redisEnable = new JSONObject();
			redisEnable.put(JsonKey.NAME, "otp-redis-enable");
			redisEnable.put(JsonKey.VALUE, ConfigSubscriberRedis.isSubscriberRedisEnable());
			data.put(redisEnable);
			
			JSONObject redisConnected = new JSONObject();
			redisConnected.put(JsonKey.NAME, "otp-redis-connected");
			redisConnected.put(JsonKey.VALUE, ConfigSubscriberRedis.isConnected());
			data.put(redisConnected);
		}
		
		if(list.contains(ConstantString.SERVICE_STOMP))
		{
			JSONObject stompEnable = new JSONObject();
			stompEnable.put(JsonKey.NAME, "otp-stomp-enable");
			stompEnable.put(JsonKey.VALUE, ConfigSubscriberStomp.isSubscriberStompEnable());
			data.put(stompEnable);
			
			JSONObject stompConnected = new JSONObject();
			stompConnected.put(JsonKey.NAME, "otp-stomp-connected");
			stompConnected.put(JsonKey.VALUE, ConfigSubscriberStomp.isConnected());
			data.put(stompConnected);
		}
		
		if(list.contains(ConstantString.SERVICE_MQTT))
		{
			JSONObject mqttEnable = new JSONObject();
			mqttEnable.put(JsonKey.NAME, "otp-mqtt-enable");
			mqttEnable.put(JsonKey.VALUE, ConfigSubscriberMQTT.isSubscriberMqttEnable());
			data.put(mqttEnable);
			
			JSONObject mqttConnected = new JSONObject();
			mqttConnected.put(JsonKey.NAME, "otp-mqtt-connected");
			mqttConnected.put(JsonKey.VALUE, ConfigSubscriberMQTT.isConnected());
			data.put(mqttConnected);
		}

		if(list.contains(ConstantString.SERVICE_ACTIVEMQ))
		{
			JSONObject activeMQEnable = new JSONObject();
			activeMQEnable.put(JsonKey.NAME, "otp-activemq-enable");
			activeMQEnable.put(JsonKey.VALUE, ConfigSubscriberActiveMQ.isSubscriberActiveMQEnable());
			data.put(activeMQEnable);
			
			JSONObject activeMQConnected = new JSONObject();
			activeMQConnected.put(JsonKey.NAME, "otp-activemq-connected");
			activeMQConnected.put(JsonKey.VALUE, ConfigSubscriberActiveMQ.isConnected());
			data.put(activeMQConnected);	
		}

		if(list.contains(ConstantString.SERVICE_HTTP))
		{
			JSONObject httpEnable = new JSONObject();
			httpEnable.put(JsonKey.NAME, "otp-http-enable");
			httpEnable.put(JsonKey.VALUE, ConfigAPI.isHttpEnable());
			data.put(httpEnable);		

			JSONObject httpConneted = new JSONObject();
			httpConneted.put(JsonKey.NAME, "otp-http-connected");
			httpConneted.put(JsonKey.VALUE, Application.getRest().isHttpStarted());
			data.put(httpConneted);
		}

		if(list.contains(ConstantString.SERVICE_HTTPS))
		{
			JSONObject httpsEnable = new JSONObject();
			httpsEnable.put(JsonKey.NAME, "otp-https-enable");
			httpsEnable.put(JsonKey.VALUE, ConfigAPI.isHttpsEnable());
			data.put(httpsEnable);	
			
			JSONObject httpsConneted = new JSONObject();
			httpsConneted.put(JsonKey.NAME, "otp-https-connected");
			httpsConneted.put(JsonKey.VALUE, Application.getRest().isHttpsStarted());
			data.put(httpsConneted);
		}
		
		info.put(JsonKey.COMMAND, ServerInfo.SERVER_INFO);
		info.put(JsonKey.DATA, data);
		return info;
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
