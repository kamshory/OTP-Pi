package com.planetbiru;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.logging.log4j.core.util.CronExpression;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.config.ConfigDDNS;
import com.planetbiru.config.ConfigFeederAMQP;
import com.planetbiru.config.ConfigFeederWS;
import com.planetbiru.config.ConfigGeneral;
import com.planetbiru.config.ConfigModem;
import com.planetbiru.constant.ConstantString;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.ddns.DDNSRecord;
import com.planetbiru.ddns.DDNSUpdater;
import com.planetbiru.gsm.GSMUtil;
import com.planetbiru.util.ConfigLoader;
import com.planetbiru.util.ServerInfo;
import com.planetbiru.util.ServerStatus;
import com.planetbiru.util.Utility;

public class Scheduller extends Thread{

	private boolean running = true;

	private long nextValidNTP = 0;

	private long nextValidDDNSUpdate = 0;

	private String cronExpressionDDNSUpdate = "0 * * * * ?";
	
	private long nextValidDeviceCheck = 0;

	private String cronExpressionDeviceCheck = "0 * * * * ?";

	private long nextValidAMQPCheck = 0;

	private String cronExpressionAMQPCheck = "0 * * * * ?";

	private long cronInterval = 20000;

	private String cronExpressionStatusServer = "0 * * * * ?";

	private long nextValidStatusServer = 0;

	private boolean cronUpdateServerStatus = false;

	private boolean cronUpdateDDNS = false;

	private boolean cronUpdateAMQP = false;

	private boolean cronCeviceCheck = false;
	
	private static Logger logger = Logger.getLogger(Scheduller.class);
	
	public Scheduller() {
		this.cronExpressionDeviceCheck = ConfigLoader.getConfig("otpbroker.cron.expression.device");
		this.cronExpressionAMQPCheck = ConfigLoader.getConfig("otpbroker.cron.expression.amqp");
		this.cronExpressionDDNSUpdate = ConfigLoader.getConfig("otpbroker.cron.expression.general");
		this.cronExpressionStatusServer = ConfigLoader.getConfig("otpbroker.cron.expression.server.status");		
		this.cronUpdateServerStatus = ConfigLoader.getConfigBoolean("otpbroker.cron.enable.server.status");
		this.cronUpdateDDNS = ConfigLoader.getConfigBoolean("otpbroker.cron.enable.ddns");
		this.cronUpdateAMQP = ConfigLoader.getConfigBoolean("otpbroker.cron.enable.amqp");
		this.cronCeviceCheck = ConfigLoader.getConfigBoolean("otpbroker.cron.enable.device");
	}
	
	@Override
	public void run()
	{
		do
		{
			Date currentTime = new Date();
			/**
			 * Update time
			 */
			if(!ConfigGeneral.getNtpUpdateInterval().isEmpty())
			{
				this.updateTime(currentTime, ConfigGeneral.getNtpUpdateInterval());
			}			
			
			/**
			 * Update DDNS
			 */
			if(this.cronUpdateDDNS)
			{
				this.updateDDNS(currentTime);
			}			

			/**
			 * Check modem
			 */
			if(this.cronCeviceCheck)
			{
				this.modemCheck(currentTime);
			}
			
			/**
			 * Check AMQP
			 */
			if(this.cronUpdateAMQP)
			{
				this.amqpCheck(currentTime);
			}			
			
			/**
			 * Status server
			 */
			if(this.cronUpdateServerStatus)
			{
				this.updateServerStatus(currentTime);
			}
			this.delay(this.cronInterval);
		}
		while(this.running);
		
	}
	
	private void updateTime(Date currentTime, String cronExpressionNTP) {
		CronExpression exp;
		try
		{
			exp = new CronExpression(cronExpressionNTP);
			Date nextValidTimeAfter = exp.getNextValidTimeAfter(currentTime);
			if(currentTime.getTime() > this.nextValidNTP)
			{
				this.updateTime();
				this.nextValidNTP = nextValidTimeAfter.getTime();
			}
		}
		catch(JSONException | ParseException e)
		{
			/**
			 * Do nothing
			 */
		}
		
	}

	private void updateDDNS(Date currentTime) {
		CronExpression exp;
		try
		{
			exp = new CronExpression(this.cronExpressionDDNSUpdate);
			Date nextValidTimeAfter = exp.getNextValidTimeAfter(currentTime);
			if(currentTime.getTime() > this.nextValidDDNSUpdate)
			{
				this.updateDDNSRecord();
				this.nextValidDDNSUpdate = nextValidTimeAfter.getTime();
			}
		}
		catch(JSONException | ParseException e)
		{
			/**
			 * Do nothing
			 */
		}
		
	}

	private void updateDDNSRecord() {
		int countUpdate = 0;	
		Map<String, DDNSRecord> list = ConfigDDNS.getRecords();
		for(Entry<String, DDNSRecord> set : list.entrySet())
		{
			String ddnsId = set.getKey();
			DDNSRecord ddnsRecord = set.getValue();
			if(ddnsRecord.isActive())
			{
				boolean update = updateDNSRecord(ddnsRecord, ddnsId);
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

	private boolean updateDNSRecord(DDNSRecord ddnsRecord, String ddnsId) {
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
			logger.error("updateDNS ERROR "+e.getMessage()+" "+cronExpression);
		}
		return update;	
	}

	private void modemCheck(Date currentTime) {
		CronExpression exp;
		try
		{
			exp = new CronExpression(this.cronExpressionDeviceCheck);
			Date nextValidTimeAfter = exp.getNextValidTimeAfter(currentTime);
			if(currentTime.getTime() > this.nextValidDeviceCheck)
			{
				this.modemCheck();
				this.nextValidDeviceCheck = nextValidTimeAfter.getTime();
			}
		}
		catch(JSONException | ParseException e)
		{
			/**
			 * Do nothing
			 */
		}
		
	}

	private void amqpCheck(Date currentTime) {
		CronExpression exp;		
		try
		{
			exp = new CronExpression(this.cronExpressionAMQPCheck);
			Date nextValidTimeAfter = exp.getNextValidTimeAfter(currentTime);
			if(currentTime.getTime() > this.nextValidAMQPCheck)
			{
				this.amqpCheck();
				this.nextValidAMQPCheck = nextValidTimeAfter.getTime();
			}
		}
		catch(JSONException | ParseException e)
		{
			/**
			 * Do nothing
			 */
		}
		
	}

	private void updateServerStatus(Date currentTime) {
		CronExpression exp;		
		try
		{
			exp = new CronExpression(this.cronExpressionStatusServer);
			Date nextValidTimeAfter = exp.getNextValidTimeAfter(currentTime);
			if(currentTime.getTime() > this.nextValidStatusServer)
			{
				this.updateServerStatus();
				this.nextValidStatusServer = nextValidTimeAfter.getTime();
			}
		}
		catch(JSONException | ParseException e)
		{
			/**
			 * Do nothing
			 */
		}
		
	}

	private void delay(long interval)
	{
		try 
		{
			Thread.sleep(interval);
		} 
		catch (InterruptedException e) 
		{
			Thread.currentThread().interrupt();
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
		ServerInfo.sendAMQPStatus(ConfigFeederAMQP.isConnected());
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
		data.put("ram", (memory.optJSONObject("ram") != null)?memory.optJSONObject("ram").optDouble("percentUsed", 0):0);
		data.put("swap", (memory.optJSONObject("swap") != null)?memory.optJSONObject("swap").optDouble("percentUsed", 0):0);
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
				if(currentTime.getTime() > this.nextValidNTP)
				{
					DeviceAPI.syncTime(ntpServer);
					this.nextValidNTP = nextValidTimeAfter.getTime();
				}
			}
			catch(JSONException | ParseException e)
			{
				/**
				 * Do nothing
				 */
			}
		}
	}
}
