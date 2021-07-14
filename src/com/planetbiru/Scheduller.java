package com.planetbiru;

import java.text.ParseException;
import java.util.Date;

import org.apache.logging.log4j.core.util.CronExpression;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.planetbiru.config.ConfigFeederAMQP;
import com.planetbiru.config.ConfigFeederWS;
import com.planetbiru.config.ConfigGeneral;
import com.planetbiru.config.ConfigModem;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.gsm.GSMUtil;
import com.planetbiru.util.ServerInfo;
import com.planetbiru.util.ServerStatus;

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

	private boolean updateServerStatus = false;

	private boolean updateDDNS = false;

	private boolean updateAMQP;

	private boolean deviceCheck;
	
	public Scheduller() {
		this.cronExpressionDeviceCheck = ConfigLoader.getConfig("otpbroker.cron.expression.device");
		this.cronExpressionAMQPCheck = ConfigLoader.getConfig("otpbroker.cron.expression.amqp");
		this.cronExpressionDDNSUpdate = ConfigLoader.getConfig("otpbroker.cron.expression.general");
		this.cronExpressionStatusServer = ConfigLoader.getConfig("otpbroker.cron.expression.server.status");
		
		this.updateServerStatus = ConfigLoader.getConfigBoolean("otpbroker.cron.enable.server.status");
		this.updateDDNS = ConfigLoader.getConfigBoolean("otpbroker.cron.enable.ddns");
		this.updateAMQP = ConfigLoader.getConfigBoolean("otpbroker.cron.enable.amqp");
		this.deviceCheck = ConfigLoader.getConfigBoolean("otpbroker.cron.enable.device");
	}
	
	@Override
	public void run()
	{
		do
		{
			Date currentTime = new Date();
			System.out.println("Run at "+currentTime);
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
			if(this.updateDDNS)
			{
				this.updateDDNS(currentTime);
			}
			

			/**
			 * Check modem
			 */
			if(this.deviceCheck)
			{
				this.modemCheck(currentTime);
			}
			
			
			
			/**
			 * Check AMQP
			 */
			if(this.updateAMQP)
			{
				this.amqpCheck(currentTime);
			}
			
			
			/**
			 * Status server
			 */
			if(this.updateServerStatus)
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
				System.out.println("Update NTP");
				this.updateTime();
				this.nextValidNTP = nextValidTimeAfter.getTime();
			}
		}
		catch(JSONException | ParseException e)
		{
			e.printStackTrace();
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
				System.out.println("Update DDNS");
				this.updateTime();
				this.nextValidDDNSUpdate = nextValidTimeAfter.getTime();
			}
		}
		catch(JSONException | ParseException e)
		{
			e.printStackTrace();
		}
		
	}

	private void modemCheck(Date currentTime) {
		CronExpression exp;
		try
		{
			exp = new CronExpression(this.cronExpressionDeviceCheck);
			Date nextValidTimeAfter = exp.getNextValidTimeAfter(currentTime);
			if(currentTime.getTime() > this.nextValidDeviceCheck)
			{
				System.out.println("Check modem");
				this.modemCheck();
				this.nextValidDeviceCheck = nextValidTimeAfter.getTime();
			}
		}
		catch(JSONException | ParseException e)
		{
			e.printStackTrace();
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
				System.out.println("Check AMQP");
				this.amqpCheck();
				this.nextValidAMQPCheck = nextValidTimeAfter.getTime();
			}
		}
		catch(JSONException | ParseException e)
		{
			e.printStackTrace();
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
				System.out.println("Status server");
				this.updateServerStatus();
				this.nextValidStatusServer = nextValidTimeAfter.getTime();
			}
		}
		catch(JSONException | ParseException e)
		{
			e.printStackTrace();
		}
		
	}

	private void delay(long interval)
	{
		try 
		{
			Thread.sleep(this.cronInterval);
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
	
				if(currentTime.getTime() > nextValidNTP)
				{
					DeviceAPI.syncTime(ntpServer);
					nextValidNTP = nextValidTimeAfter.getTime();
				}
			}
			catch(JSONException | ParseException e)
			{

			}
		}
	}
}
