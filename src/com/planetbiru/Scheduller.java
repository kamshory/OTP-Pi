package com.planetbiru;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.core.util.CronExpression;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.config.ConfigDDNS;
import com.planetbiru.config.ConfigSubscriberAMQP;
import com.planetbiru.config.ConfigSubscriberActiveMQ;
import com.planetbiru.config.ConfigSubscriberMQTT;
import com.planetbiru.config.ConfigSubscriberRedis;
import com.planetbiru.config.ConfigSubscriberWS;
import com.planetbiru.config.ConfigGeneral;
import com.planetbiru.config.ConfigModem;
import com.planetbiru.constant.ConstantString;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.ddns.DDNSRecord;
import com.planetbiru.ddns.DDNSUpdater;
import com.planetbiru.device.DeviceAPI;
import com.planetbiru.gsm.GSMUtil;
import com.planetbiru.util.ConfigLoader;
import com.planetbiru.util.ServerInfo;
import com.planetbiru.util.ServerStatus;
import com.planetbiru.util.Utility;

public class Scheduller extends Thread{

	private boolean running = true;

	private String cronExpressionDDNSUpdate = ConstantString.CRON_EVERY_MINUTE;
	
	private String cronExpressionDeviceCheck = ConstantString.CRON_EVERY_MINUTE;

	private String cronExpressionAMQPCheck = ConstantString.CRON_EVERY_MINUTE;

	private String cronExpressionRedisCheck = ConstantString.CRON_EVERY_MINUTE;
	
	private String cronExpressionMQTTCheck = ConstantString.CRON_EVERY_MINUTE;

	private String cronExpressionWSCheck = ConstantString.CRON_EVERY_MINUTE;

	private String cronExpressionStatusServer = ConstantString.CRON_EVERY_MINUTE;

	private String cronExpressionActiveMQCheck = ConstantString.CRON_EVERY_MINUTE;

	private long cronInterval = 2000;

	private boolean cronUpdateServerStatus = false;

	private boolean cronUpdateDDNS = false;

	private boolean cronUpdateAMQP = false;

	private boolean cronUpdateRedis = false;
	
	private boolean cronUpdateMQTT = false;

	private boolean cronUpdateActiveMQ = false;

	private boolean cronUpdateWS = false;

	private boolean cronServiceCheck = false;


	
	public Scheduller() {
		this.cronExpressionDeviceCheck = ConfigLoader.getConfig("otppi.cron.expression.device");
		
		this.cronExpressionAMQPCheck = ConfigLoader.getConfig("otppi.cron.expression.amqp");
		this.cronExpressionRedisCheck = ConfigLoader.getConfig("otppi.cron.expression.redis");
		this.cronExpressionMQTTCheck = ConfigLoader.getConfig("otppi.cron.expression.mqtt");
		this.cronExpressionActiveMQCheck = ConfigLoader.getConfig("otppi.cron.expression.activemq");
		this.cronExpressionWSCheck = ConfigLoader.getConfig("otppi.cron.expression.ws");
		
		this.cronUpdateAMQP = ConfigLoader.getConfigBoolean("otppi.cron.enable.amqp");
		this.cronUpdateMQTT = ConfigLoader.getConfigBoolean("otppi.cron.enable.mqtt");
		this.cronUpdateRedis = ConfigLoader.getConfigBoolean("otppi.cron.enable.redis");
		this.cronUpdateActiveMQ = ConfigLoader.getConfigBoolean("otppi.cron.enable.activemq");
		this.cronUpdateWS = ConfigLoader.getConfigBoolean("otppi.cron.enable.ws");	
		
		this.cronUpdateDDNS = ConfigLoader.getConfigBoolean("otppi.cron.enable.ddns");
		this.cronExpressionDDNSUpdate = ConfigLoader.getConfig("otppi.cron.expression.general");

		this.cronUpdateServerStatus = ConfigLoader.getConfigBoolean("otppi.cron.enable.server.status");
		this.cronExpressionStatusServer = ConfigLoader.getConfig("otppi.cron.expression.server.status");		
		
		this.cronServiceCheck = ConfigLoader.getConfigBoolean("otppi.cron.enable.device");
	}
	
	public void stopService() {
		this.running = false;	
	}
	
	@Override
	public void run()
	{
		do
		{
			Date currentTime = new Date();
			if(!ConfigGeneral.getNtpUpdateInterval().isEmpty())
			{
				this.updateTime(currentTime, ConfigGeneral.getNtpUpdateInterval());
			}
			
			if(!ConfigGeneral.getRestartDevice().isEmpty())
			{
				this.restartDevice(currentTime, ConfigGeneral.getRestartDevice());
			}
			if(!ConfigGeneral.getRestartService().isEmpty())
			{
				this.restartService(currentTime, ConfigGeneral.getRestartService());
			}
			
			/**
			 * Update DDNS
			 */
			this.updateDDNS(currentTime);

			/**
			 * Check modem
			 */
			this.modemCheck(currentTime);
			
			/**
			 * Check AMQP
			 */			
			this.amqpCheck(currentTime);			
	
			/**
			 * Check Redis
			 */			
			this.redisCheck(currentTime);				

			/**
			 * Check MQTT
			 */			
			this.mqttCheck(currentTime);				

			/**
			 * Check ActiveMQ
			 */			
			this.activeMQCheck(currentTime);				

			/**
			 * Check WS
			 */			
			this.wsCheck(currentTime);				

			/**
			 * Status server
			 */		
			this.updateServerStatus(currentTime);			
			
			this.delay(this.cronInterval);
		}
		while(this.running);
		
	}
	
	private void restartDevice(Date currentTime, String restartDevice) {
		CronExpression exp;
		try
		{
			exp = new CronExpression(restartDevice);
			Date nextValidTimeAfter = exp.getNextValidTimeAfter(currentTime);
			if(currentTime.getTime() > DeviceAPI.getLastReboot() && currentTime.getTime() > nextValidTimeAfter.getTime())
			{
				DeviceAPI.reboot();
				DeviceAPI.setLastReboot(nextValidTimeAfter.getTime());
			}
		}
		catch(JSONException | ParseException e)
		{
			/**
			 * Do nothing
			 */
		}		
	}
	
	private void restartService(Date currentTime, String restartService) {
		CronExpression exp;
		try
		{
			exp = new CronExpression(restartService);
			Date nextValidTimeAfter = exp.getNextValidTimeAfter(currentTime);
			if(currentTime.getTime() > DeviceAPI.getLastRestart() && currentTime.getTime() > nextValidTimeAfter.getTime())
			{
				DeviceAPI.restart();
				DeviceAPI.setLastRestart(nextValidTimeAfter.getTime());
			}
		}
		catch(JSONException | ParseException e)
		{
			/**
			 * Do nothing
			 */
		}		
	}

	private void updateTime(Date currentTime, String cronExpressionNTP) {
		CronExpression exp;
		try
		{
			exp = new CronExpression(cronExpressionNTP);
			Date nextValidTimeAfter = exp.getNextValidTimeAfter(currentTime);
			String ntpServer = ConfigGeneral.getNtpServer();		

			if(currentTime.getTime() > DeviceAPI.getLastUpdateNTP() && ntpServer != null && !ntpServer.isEmpty())
			{
				DeviceAPI.syncTime(ntpServer);
				DeviceAPI.setLastUpdateNTP(nextValidTimeAfter.getTime());
			}
		}
		catch(JSONException | ParseException e)
		{
			/**
			 * Do nothing
			 */
		}		
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
				if(currentTime.getTime() > DeviceAPI.getLastUpdateNTP())
				{
					DeviceAPI.syncTime(ntpServer);
					DeviceAPI.setLastUpdateNTP(nextValidTimeAfter.getTime());
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

	private void updateDDNS(Date currentTime) {
		if(this.cronUpdateDDNS)
		{
			CronExpression exp;
			try
			{
				exp = new CronExpression(this.cronExpressionDDNSUpdate);
				Date nextValidTimeAfter = exp.getNextValidTimeAfter(currentTime);
				if(currentTime.getTime() > DDNSUpdater.getLastUpdate())
				{
					this.updateDDNSRecord();
					DDNSUpdater.setLastUpdate(nextValidTimeAfter.getTime());
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
			/**
			 * Do nothing
			 */
		}
		return update;	
	}

	private void modemCheck(Date currentTime) {
		if(this.cronServiceCheck)
		{
			CronExpression exp;
			try
			{
				exp = new CronExpression(this.cronExpressionDeviceCheck);
				Date nextValidTimeAfter = exp.getNextValidTimeAfter(currentTime);
				if(currentTime.getTime() > DeviceAPI.getLastCheckModem())
				{
					this.modemCheck();
					DeviceAPI.setLastCheckModem(nextValidTimeAfter.getTime());
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

	private void amqpCheck(Date currentTime) {
		if(this.cronUpdateAMQP && ConfigSubscriberAMQP.isSubscriberAmqpEnable())
		{
			CronExpression exp;		
			try
			{
				exp = new CronExpression(this.cronExpressionAMQPCheck);
				Date nextValidTimeAfter = exp.getNextValidTimeAfter(currentTime);
				if(currentTime.getTime() > DeviceAPI.getLastCheckAMQP())
				{
					this.amqpCheck();
					DeviceAPI.setLastCheckAMQP(nextValidTimeAfter.getTime());
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
	
	private void redisCheck(Date currentTime) {
		if(this.cronUpdateRedis && ConfigSubscriberRedis.isSubscriberRedisEnable())
		{
			CronExpression exp;		
			try
			{
				exp = new CronExpression(this.cronExpressionRedisCheck);
				Date nextValidTimeAfter = exp.getNextValidTimeAfter(currentTime);
				if(currentTime.getTime() > DeviceAPI.getLastCheckAMQP())
				{
					this.redisCheck();
					DeviceAPI.setLastCheckRedis(nextValidTimeAfter.getTime());
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

	private void mqttCheck(Date currentTime) {
		if(this.cronUpdateMQTT && ConfigSubscriberMQTT.isSubscriberMqttEnable())
		{
			CronExpression exp;		
			try
			{
				exp = new CronExpression(this.cronExpressionMQTTCheck);
				Date nextValidTimeAfter = exp.getNextValidTimeAfter(currentTime);
				if(currentTime.getTime() > DeviceAPI.getLastCheckMQTT())
				{
					this.mqttCheck();
					DeviceAPI.setLastCheckMQTT(nextValidTimeAfter.getTime());
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
	
	private void activeMQCheck(Date currentTime) {
		if(this.cronUpdateActiveMQ && ConfigSubscriberActiveMQ.isSubscriberActiveMQEnable())
		{
			CronExpression exp;		
			try
			{
				exp = new CronExpression(this.cronExpressionActiveMQCheck);
				Date nextValidTimeAfter = exp.getNextValidTimeAfter(currentTime);
				if(currentTime.getTime() > DeviceAPI.getLastCheckMQTT())
				{
					this.activeMQCheck();
					DeviceAPI.setLastCheckActiveMQ(nextValidTimeAfter.getTime());
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
	
	private void wsCheck(Date currentTime) {
		if(this.cronUpdateWS && ConfigSubscriberWS.isSubscriberWsEnable())
		{
			CronExpression exp;		
			try
			{
				exp = new CronExpression(this.cronExpressionWSCheck);
				Date nextValidTimeAfter = exp.getNextValidTimeAfter(currentTime);
				if(currentTime.getTime() > DeviceAPI.getLastCheckWS())
				{
					this.wsCheck();
					DeviceAPI.setLastCheckWS(nextValidTimeAfter.getTime());
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

	private void updateServerStatus(Date currentTime) {
		if(this.cronUpdateServerStatus)
		{
			CronExpression exp;		
			try
			{
				exp = new CronExpression(this.cronExpressionStatusServer);
				Date nextValidTimeAfter = exp.getNextValidTimeAfter(currentTime);
				if(currentTime.getTime() > DeviceAPI.getLastCheckStatus())
				{
					this.updateServerStatus();
					DeviceAPI.setLastCheckStatus(nextValidTimeAfter.getTime());
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
		modem.put(JsonKey.NAME, ConstantString.OTP_MODEM_CONNECTED);
		modem.put(JsonKey.VALUE, GSMUtil.isConnected());
		modem.put(JsonKey.DATA, ConfigModem.getStatus());
		data.put(modem);
		JSONObject serverInfo = new JSONObject();
		serverInfo.put(JsonKey.DATA, data);
		serverInfo.put(JsonKey.COMMAND, ConstantString.SERVER_INFO);
		ServerWebSocketAdmin.broadcastMessage(serverInfo.toString());
	}

	private void amqpCheck()
	{
		boolean connected = ConfigSubscriberAMQP.isConnected();
		ServerInfo.sendAMQPStatus(connected);
	}

	private void redisCheck()
	{
		boolean connected = Application.getRedisSubscriber().ping(5000);
		ServerInfo.sendRedisStatus(connected);
	}

	private void mqttCheck()
	{
		boolean connected = ConfigSubscriberMQTT.isConnected();
		ServerInfo.sendMQTTStatus(connected);
	}

	private void activeMQCheck()
	{
		boolean connected = ConfigSubscriberActiveMQ.isConnected();
		ServerInfo.sendActiveMQStatus(connected);
	}

	private void wsCheck()
	{
		boolean connected = ConfigSubscriberWS.isConnected();
		ServerInfo.sendWSStatus(connected);
	}

	public void updateServerStatus()
	{
		JSONObject data = new JSONObject();
		data.put(JsonKey.DATETIME, System.currentTimeMillis());
		
		JSONObject memory = ServerInfo.memoryInfo();
		JSONObject cpu = ServerInfo.cpuUsage();
		JSONObject storage = ServerInfo.storageInfo();

		data.put(JsonKey.STORAGE, storage.optDouble(JsonKey.PERCENT_USED, 0));
		data.put(JsonKey.CPU, cpu.optDouble(JsonKey.PERCENT_USED, 0));
		data.put(JsonKey.RAM, (memory.optJSONObject(JsonKey.RAM) != null)?memory.optJSONObject(JsonKey.RAM).optDouble(JsonKey.PERCENT_USED, 0):0);
		data.put(JsonKey.SWAP, (memory.optJSONObject(JsonKey.SWAP) != null)?memory.optJSONObject(JsonKey.SWAP).optDouble(JsonKey.PERCENT_USED, 0):0);
		data.put(JsonKey.MODEM, GSMUtil.isConnected());
		data.put(JsonKey.WS, ConfigSubscriberWS.isConnected());
		data.put(JsonKey.AMQP, ConfigSubscriberAMQP.isConnected());
		data.put(JsonKey.REDIS, ConfigSubscriberRedis.isConnected());

		ServerStatus.append(data);
		ServerStatus.save();
	}
}
