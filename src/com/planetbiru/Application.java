package com.planetbiru;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.List;
import org.json.JSONObject;

import com.planetbiru.api.OTP;
import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigAPI;
import com.planetbiru.config.ConfigSubscriberAMQP;
import com.planetbiru.config.ConfigSubscriberActiveMQ;
import com.planetbiru.config.ConfigSubscriberMQTT;
import com.planetbiru.config.ConfigSubscriberRedis;
import com.planetbiru.config.ConfigSubscriberWS;
import com.planetbiru.constant.ConstantString;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.device.ResetDevice;
import com.planetbiru.gsm.InternetDialUtil;
import com.planetbiru.gsm.ModemInspector;
import com.planetbiru.mail.ServerEmail;
import com.planetbiru.server.rest.ServerRESTAPI;
import com.planetbiru.gsm.GSMUtil;
import com.planetbiru.subscriber.activemq.SubscriberActiveMQ;
import com.planetbiru.subscriber.amqp.SubscriberAMQP;
import com.planetbiru.subscriber.mqtt.SubscriberMQTT;
import com.planetbiru.subscriber.redis.SubscriberRedis;
import com.planetbiru.subscriber.ws.SubscriberWebSocket;
import com.planetbiru.util.CommandLineExecutor;
import com.planetbiru.util.ConfigLoader;
import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.ProcessKiller;
import com.planetbiru.util.Utility;

import org.apache.log4j.Logger;

public class Application {
	
	private static ServerWebSocketAdmin webSocketAdmin;
	private static ServerWebAdmin webAdmin;
	private static ServerRESTAPI rest;
	private static ServerEmail smtp;
	private static Scheduller scheduller;	
	private static SubscriberWebSocket webSocketSubscriber;	
	private static SubscriberMQTT mqttSubscriber;
	private static SubscriberRedis redisSubscriber;
	private static SubscriberAMQP amqpSubscriber;	
	private static Logger logger = Logger.getLogger(Application.class);
	private static ModemInspector modemInspector = null;
	private static SubscriberActiveMQ activeMQSubscriber;
	 
	public static void main(String[] args) {
		String currentRootDirectoryPath = Application.getConfigRoot();
		boolean configLoaded = Application.loadConfig(currentRootDirectoryPath, "config.ini");
		if(configLoaded)
		{
			Application.startService(args);
		}
		else
		{
			logger.error("Service not started because failed to read config file");
			logger.error("System can not find "+currentRootDirectoryPath+"/config.ini");
		}		
	}
	
	private static String getConfigRoot()
	{
		File currentJavaJarFile = new File(Application.class.getProtectionDomain().getCodeSource().getLocation().getPath());   
		String currentJavaJarFilePath = currentJavaJarFile.getAbsolutePath();
		return currentJavaJarFilePath.replace(currentJavaJarFile.getName(), "");
	}
	
	public static boolean loadConfig(String currentRootDirectoryPath, String fileName)
	{
		boolean loaded = false;
		try 
		{
			ConfigLoader.load(fileName);
			loaded = true;
		} 
		catch (FileNotFoundException e) 
		{
			try 
			{
				ConfigLoader.load(currentRootDirectoryPath+"/"+fileName);
				loaded = true;
			} 
			catch (FileNotFoundException e1) 
			{
				logger.error(e1.getMessage(), e1);
			}
		}
		return loaded;	
	}
	
	private static void startService(String[] args) {
		boolean needToStart = true;
		boolean needToReset = false;
		String imageName = ConfigLoader.getConfig("otpbroker.image.name");
		Config.setImageName(imageName);
		if(args != null)
		{
			List<String> argList = Arrays.asList(args);
			if(argList.contains("--start"))
			{
				needToReset = true;
				String pingCommand = ConfigLoader.getConfig("otpbroker.ssh.ping.command");
				String result = CommandLineExecutor.exec(pingCommand).toString();
				if(result.equals("OK"))
				{
					needToStart = false;
				}
			}
			else if(argList.contains("--restart"))
			{
				ProcessKiller killer = new ProcessKiller(Config.getImageName(), true);
				killer.stop();					
			}
			else if(argList.contains("--stop"))
			{
				ProcessKiller killer = new ProcessKiller(Config.getImageName(), true);
				killer.stop();
				needToStart = false;
				System.exit(0);
			}
		}	
		if(needToStart)
		{
			ConfigLoader.init();
			
			
			if(needToReset)
			{
				ResetDevice.resetConfig();
			}
	
			Application.prepareSessionDir();
	
			
			/**
			 * Web Server for Admin
			 */
			Application.webAdminServerStart();
			

			/**
			 * WebSocket Client for subscriber
			 */
			Application.subscriberWSStart();
			
			/**
			 * RabbitMQ Client for subscriber
			 */
			Application.subscriberAMQPStart();

			/**
			 * Redis Client for subscriber
			 */
			Application.subscriberRedisStart();

			/**
			 * Mosquitto Client for subscriber
			 */
			Application.subscriberMQTTStart();
			
			Application.subscriberActiveMQStart();
			
			/**
			 * REST API HTTP
			 */
			Application.restAPIStart();
			
			Application.otpStart();
					
	
			Application.modemSMSStart();
			
			Application.modemInternetStart();
			
			Application.modemInspectorStart(5000);

			/**
			 * WebSocket Server for Admin
			 */
			Application.webSocketAdminServerStart();

			/**
			 * SMTP Server for send email
			 */
			Application.serverEmailStart();
			
			Application.schedulerStart();
			logger.info("Service started");
		}
		else
		{
			logger.info("Service already started");
		}
		
	}

	private static void otpStart() {
		OTP.initialize(Config.getOtpCacheFile());
		OTP.start();
	}

	public static void schedulerStart() {
		Application.scheduller = new Scheduller();
		Application.scheduller.start();		
	}

	public static void serverEmailStart() {
		Application.smtp = new ServerEmail();
		Application.smtp.start();
		
	}

	public static void modemInternetStart() {
		InternetDialUtil.start();
	}

	public static void modemSMSStart() {
		GSMUtil.start();	
	}
	
	public static void modemInternetStop() {
		InternetDialUtil.stop();
	}

	public static void modemSMSStop() {
		GSMUtil.stop();	
	}
	
	public static void modemInspectorStart(long delay) {
		if(Application.modemInspector != null)
		{
			try
			{
				Application.modemInspector.stopService();				
			}
			catch(Exception e)
			{
				/**
				 * Do nothing
				 */
			}
		}
		Application.modemInspector = new ModemInspector(delay);
		Application.modemInspector.start();
	}

	private static void restAPIStart() {
		Application.rest = new ServerRESTAPI();
		Application.rest.start();
	}

	private static void webSocketAdminServerStart() {
		int port = Config.getServerPort() + 1;
		InetSocketAddress address = new InetSocketAddress(port);
		Application.webSocketAdmin = new ServerWebSocketAdmin(address);
		Application.webSocketAdmin.start();
		
	}

	private static void webAdminServerStart() {
		int port = Config.getServerPort();
		Application.webAdmin = new ServerWebAdmin(port);
		Application.webAdmin.start();	
	}

	public static ServerRESTAPI getRest() {
		return rest;
	}

	public static void setRest(ServerRESTAPI rest) {
		Application.rest = rest;
	}
	
	public static void subscriberHTTPStart() {
		if (ConfigAPI.isHttpEnable() && !Application.rest.isHttpStarted()) {
			Application.rest.startHTTP();
		}
	}

	public static void subscriberHTTPStop() {
		if (ConfigAPI.isHttpEnable() && Application.rest.isHttpStarted()) {
			Application.rest.stopHTTP();
		}
	}

	public static void subscriberHTTPSStart() {
		if (ConfigAPI.isHttpsEnable() && !Application.rest.isHttpsStarted()) {
			Application.rest.startHTTPS();
		}
	}

	public static void subscriberHTTPSStop() {
		if (ConfigAPI.isHttpsEnable() && Application.rest.isHttpsStarted()) {
			Application.rest.stopHTTPS();
		}
	}

	public static void subscriberActiveMQStart() {
		if(ConfigSubscriberActiveMQ.isSubscriberActiveMQEnable() && (Application.activeMQSubscriber == null || !Application.activeMQSubscriber.isRunning()))
		{
			Application.activeMQSubscriber = new SubscriberActiveMQ();
			Application.activeMQSubscriber.start();
		}
	}

	public static void subscriberActiveMQStop() {
		if(Application.activeMQSubscriber != null)
		{
			Application.activeMQSubscriber.stopService();
		}		
	}

	public static void subscriberMQTTStart() {
		if(ConfigSubscriberMQTT.isSubscriberMqttEnable() && (Application.mqttSubscriber == null || !Application.mqttSubscriber.isRunning()))
		{
			Application.mqttSubscriber = new SubscriberMQTT();
			Application.mqttSubscriber.start();
		}		
	}

	public static void subscriberMQTTStop() {
		if(Application.mqttSubscriber != null && Application.mqttSubscriber.isRunning())
		{
			Application.mqttSubscriber.stopService();
		}		
	}

	public static void subscriberAMQPStart() {
		if(ConfigSubscriberAMQP.isSubscriberAmqpEnable() && (Application.getAmqpSubscriber() == null || !Application.getAmqpSubscriber().isRunning()))
		{
			Application.setAmqpSubscriber(new SubscriberAMQP());
			Application.getAmqpSubscriber().start();
		}		
	}
	public static void subscriberRedisStart() {
		if(ConfigSubscriberRedis.isSubscriberRedisEnable() && (Application.getRedisSubscriber() == null || !Application.getRedisSubscriber().isRunning()))
		{
			Application.setRedisSubscriber(new SubscriberRedis());
			Application.getRedisSubscriber().setRunning(true);
			Application.getRedisSubscriber().start();
		}		
	}
	public static void subscriberRedisStop() {
		if(Application.getRedisSubscriber() != null && Application.getRedisSubscriber().isRunning())
		{
			Application.getRedisSubscriber().stopService();
		}		
	}

	public static void subscriberAMQPStop() {
		if(Application.getAmqpSubscriber() != null && Application.getAmqpSubscriber().isRunning())
		{
			Application.getAmqpSubscriber().stopService();
		}		
	}

	public static void subscriberWSStart() {
		if(ConfigSubscriberWS.isSubscriberWsEnable() && (Application.webSocketSubscriber == null || !Application.webSocketSubscriber.isRunning()))
		{
			Application.webSocketSubscriber = new SubscriberWebSocket(Config.getReconnectDelay(), Config.getWaitLoopParent(), Config.getWaitLoopChild());
			Application.webSocketSubscriber.start();	
		}
	}

	public static void subscriberWSStop() {
		if(Application.webSocketSubscriber != null && Application.webSocketSubscriber.isRunning())
		{
			Application.webSocketSubscriber.stopService();	
		}
	}

	public static void restartService()
	{
		JSONObject info = new JSONObject();
		info.put(JsonKey.COMMAND, ConstantString.SERVER_SHUTDOWN);
		ServerWebSocketAdmin.broadcastMessage(info.toString());	
		Application.stopAllServices();
		/**
		 * Wait 50 mili seconds before restart the application
		 */
		try 
		{
			Thread.sleep(50);
		} 
		catch (InterruptedException e) 
		{
			Thread.currentThread().interrupt();
		}
		CommandLineExecutor.exec(Config.getRestartCommand());
	}
	
	public static void stopAllServices()
	{
		Application.smtp.stopService();
		Application.rest.stopService();
		Application.webAdmin.stopService();
		try 
		{
			Application.webSocketAdmin.stopService();
		} 
		catch (IOException | InterruptedException e) 
		{
			Thread.currentThread().interrupt();
		}
		Application.scheduller.stopService();
		Application.webSocketSubscriber.stopService();
		Application.getAmqpSubscriber().stopService();
		Application.mqttSubscriber.stopService();
	}	
	
	public static void prepareSessionDir()
	{
		String fileName = FileConfigUtil.fixFileName(Utility.getBaseDir()+FileSystems.getDefault().getSeparator()+Config.getSessionFilePath()+"/ses");
		FileConfigUtil.prepareDir(fileName);
	}

	public static SubscriberAMQP getAmqpSubscriber() {
		return Application.amqpSubscriber;
	}

	public static void setAmqpSubscriber(SubscriberAMQP amqpSubscriber) {
		Application.amqpSubscriber = amqpSubscriber;
	}

	public static SubscriberRedis getRedisSubscriber() {
		return Application.redisSubscriber;
	}

	public static void setRedisSubscriber(SubscriberRedis redisSubscriber) {
		Application.redisSubscriber = redisSubscriber;
	}


}

