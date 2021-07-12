package com.planetbiru;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.core.util.CronExpression;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigDDNS;
import com.planetbiru.config.ConfigFeederAMQP;
import com.planetbiru.config.ConfigFeederWS;
import com.planetbiru.config.ConfigGeneral;
import com.planetbiru.config.ConfigModem;
import com.planetbiru.constant.ConstantString;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.ddns.DDNSUpdater;
import com.planetbiru.gsm.GSMUtil;
import com.planetbiru.ddns.DDNSRecord;
import com.planetbiru.util.ServerInfo;
import com.planetbiru.util.ServerStatus;
import com.planetbiru.util.Utility;

public class ServerScheduler {
	
	

	public void init()
	{
	}
	
	
	public void updateServerStatus()
	{
		JSONObject data = new JSONObject();
		data.put("datetime", System.currentTimeMillis());
		
		JSONObject memory = ServerInfo.memoryInfo();
		JSONObject cpu = ServerInfo.cpuUsage();
		JSONObject storage = ServerInfo.storageInfo();

		data.put("storage", storage.optDouble("percentUsed", 0));
		data.put("cpu", cpu.optDouble("percentUsed", 0));
		data.put("ram", memory.optJSONObject("ram").optDouble("percentUsed", 0));
		data.put("swap", memory.optJSONObject("swap").optDouble("percentUsed", 0));
		data.put("modem", GSMUtil.isConnected());
		data.put("ws", ConfigFeederWS.isConnected());
		data.put("amqp", ConfigFeederAMQP.isConnected());

		ServerStatus.append(data);
		ServerStatus.save();
	}
	
	
	public void updateTime()
	{
		String cronExpression = ConfigGeneral.getNtpUpdateInterval();		
		String ntpServer = ConfigGeneral.getNtpServer();		
		if(!cronExpression.isEmpty() && !ntpServer.isEmpty())
		{
			CronExpression exp;		
			try
			{
				exp = new CronExpression(cronExpression);
				Date currentTime = new Date();
				Date nextValidTimeAfter = exp.getNextValidTimeAfter(currentTime);
	
				if(currentTime.getTime() > ConfigGeneral.getNextValid().getTime())
				{
					DeviceAPI.syncTime(ntpServer);
				    ConfigGeneral.setNextValid(nextValidTimeAfter);
				}
			}
			catch(JSONException | ParseException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void inspectDevice()
	{
		if(Config.isCronDeviceEnable())
		{
			modemCheck();
		}
	}
	
	public void inspectAMQP()
	{
		if(Config.isCronAMQPEnable() && ConfigFeederAMQP.isFeederAmqpEnable())
		{
			amqpCheck();
		}
	}
	
	private void modemCheck()
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
		ServerWebSocketServerAdmin.broadcastMessage(serverInfo.toString());
	}

	private void amqpCheck()
	{
		boolean connected = ConfigFeederAMQP.echoTest();
		ConfigFeederAMQP.setConnected(connected);	
		ServerInfo.sendAMQPStatus(ConfigFeederAMQP.isConnected());
	}
	
	public void generalCronChecker() 
	{
		if(Config.isTimeUpdate())
		{
			updateTime();
		}		
		if(Config.isDdnsUpdate())
		{
			updateDNS();
		}
	}
	
	private void updateDNS()
	{
		int countUpdate = 0;	
		Map<String, DDNSRecord> list = ConfigDDNS.getRecords();
		for(Entry<String, DDNSRecord> set : list.entrySet())
		{
			String ddnsId = set.getKey();
			DDNSRecord ddnsRecord = set.getValue();
			if(ddnsRecord.isActive())
			{
				boolean update = updateDNS(ddnsRecord, ddnsId);
				if(update)
				{
					countUpdate++;
				}
			}
		}
		if(countUpdate > 0)
		{
			ConfigDDNS.save();
		}	
	}
	

	private boolean updateDNS(DDNSRecord ddnsRecord, String ddnsId) 
	{
		boolean update = false;
		String cronExpression = ddnsRecord.getCronExpression();		
		CronExpression exp;		
		try
		{
			exp = new CronExpression(cronExpression);
			Date currentTime = new Date();
			Date prevFireTime = exp.getPrevFireTime(currentTime);
			Date nextValidTimeAfter = exp.getNextValidTimeAfter(currentTime);

			String prevFireTimeStr = Utility.date(ConstantString.MYSQL_DATE_TIME_FORMAT_MS, prevFireTime);
			String currentTimeStr = Utility.date(ConstantString.MYSQL_DATE_TIME_FORMAT_MS, currentTime);
			String nextValidTimeAfterStr = Utility.date(ConstantString.MYSQL_DATE_TIME_FORMAT_MS, nextValidTimeAfter);
			
			if(currentTime.getTime() > ddnsRecord.getNextValid().getTime())
			{
				DDNSUpdater ddns = new DDNSUpdater(ddnsRecord, prevFireTimeStr, currentTimeStr, nextValidTimeAfterStr);
				ddns.start();
				
				ConfigDDNS.getRecords().get(ddnsId).setNextValid(nextValidTimeAfter);		
				ConfigDDNS.getRecords().get(ddnsId).setLastUpdate(currentTime);
				update = true;
			}
		}
		catch(JSONException | ParseException e)
		{
			e.printStackTrace();
		}
		return update;	
	}   
}

