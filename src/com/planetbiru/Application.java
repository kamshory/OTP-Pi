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
import com.planetbiru.config.ConfigSubscriberStomp;
import com.planetbiru.config.ConfigSubscriberWS;
import com.planetbiru.constant.ConstantString;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.device.DeviceActivation;
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
import com.planetbiru.subscriber.stomp.SubscriberStomp;
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
	private static ServerRESTAPI rest = new ServerRESTAPI();
	private static ServerEmail smtp;
	private static Scheduller scheduller;	
	private static SubscriberWebSocket webSocketSubscriber;	
	private static SubscriberMQTT mqttSubscriber;
	private static SubscriberRedis redisSubscriber;
	private static SubscriberStomp redissonSubscriber;
	private static SubscriberAMQP amqpSubscriber;	
	private static SubscriberActiveMQ activeMQSubscriber;
	private static Logger logger = Logger.getLogger(Application.class);
	private static ModemInspector modemInspector = null;
	
	/**
	 * Main method
	 * @param args Parameters from command line
	 */
	public static void main(String[] args) {
		String currentRootDirectoryPath = Application.getConfigRoot();
		boolean configLoaded = Application.loadConfig(currentRootDirectoryPath, "config.ini");
		if(configLoaded)
		{
			Application.startService(args);
		}
		else
		{
			logger.error("Service not started because failed to read configuration file");
			logger.error("System can not find "+currentRootDirectoryPath+"/config.ini");
		}		
	}
	
	/**
	 * Get absolute directory of configuration
	 * @return
	 */
	private static String getConfigRoot()
	{
		File currentJavaJarFile = new File(Application.class.getProtectionDomain().getCodeSource().getLocation().getPath());   
		String currentJavaJarFilePath = currentJavaJarFile.getAbsolutePath();
		return currentJavaJarFilePath.replace(currentJavaJarFile.getName(), "");
	}
	
	/**
	 * Load configuration file
	 * @param currentRootDirectoryPath Current directory of application
	 * @param fileName Configuration file name
	 * @return true if success and false if failed
	 */
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
	
	/**
	 * Start service
	 * @param args Parameters from command line
	 */
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
			 * Redisson Client for subscriber
			 */
			Application.subscriberStompStart();

			
			/**
			 * Mosquitto Client for subscriber
			 */
			Application.subscriberMQTTStart();
			
			
			/**
			 * ActiveMQ Client for subscriber
			 */
			Application.subscriberActiveMQStart();
			
			
			/**
			 * REST API HTTP
			 */
			Application.restAPIStart();			
			Application.otpStart();			
			Application.modemSMSStart();
			Application.modemInternetStart();
			Application.modemInspectorStart(Config.getInspectModemInterval());

			/**
			 * WebSocket Server for Admin
			 */
			Application.webSocketAdminServerStart();

			/**
			 * SMTP Server for send email
			 */
			Application.serverEmailStart();
			
			Application.schedulerStart();
			
			DeviceActivation.verify();
			
			logger.info("Service started");
		}
		else
		{
			logger.info("Service already started");
		}	
	}

	/**
	 * Start OTP
	 */
	private static void otpStart() {
		OTP.initialize(Config.getOtpCacheFile());
		OTP.start();
	}

	/**
	 * Start scheduler
	 */
	public static void schedulerStart() {
		Application.scheduller = new Scheduller();
		Application.scheduller.start();		
	}

	/**
	 * Start SMTP server
	 */
	public static void serverEmailStart() {
		Application.smtp = new ServerEmail();
		Application.smtp.start();
		
	}

	/**
	 * Start internet modem
	 */
	public static void modemInternetStart() {
		InternetDialUtil.start();
	}

	/**
	 * Start SMS modem
	 */
	public static void modemSMSStart() {
		GSMUtil.start();	
	}
	
	/**
	 * Stop internet modem
	 */
	public static void modemInternetStop() {
		InternetDialUtil.stop();
	}

	/**
	 * Stop SMS modem
	 */
	public static void modemSMSStop() {
		GSMUtil.stop();	
	}
	
	/**
	 * Inspect modem
	 * @param delay Check interval
	 */
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

	/**
	 * Start REST server
	 */
	private static void restAPIStart() {
		Application.rest = new ServerRESTAPI();
		Application.rest.start();
	}

	/**
	 * Start websocket server for admin
	 */
	private static void webSocketAdminServerStart() {
		int port = Config.getServerPort() + 1;
		InetSocketAddress address = new InetSocketAddress(port);
		Application.webSocketAdmin = new ServerWebSocketAdmin(address);
		Application.webSocketAdmin.start();
		
	}

	/**
	 * Start web server for admin
	 */
	private static void webAdminServerStart() {
		int port = Config.getServerPort();
		Application.webAdmin = new ServerWebAdmin(port);
		Application.webAdmin.start();	
	}

	/**
	 * Get RESTAPI server
	 * @return RESTAPI server
	 */
	public static ServerRESTAPI getRest() {
		return rest;
	}

	/**
	 * Set RESTAPI server
	 * @param rest RESTAPI server
	 */
	public static void setRest(ServerRESTAPI rest) {
		Application.rest = rest;
	}
	
	/**
	 * Start RESTAPI server
	 */
	public static void subscriberHTTPStart() {
		if (ConfigAPI.isHttpEnable() && !Application.rest.isHttpStarted()) {
			Application.rest.startHTTP();
		}
	}

	/**
	 * Stop RESTAPI server
	 * @param force Set true for force stop
	 */
	public static void subscriberHTTPStop(boolean force) {
		if (force || (Application.rest.isHttpStarted())) {
			Application.rest.stopHTTP();
		}
	}

	/**
	 * Start RESTAPI server with SSL
	 */
	public static void subscriberHTTPSStart() {
		if (ConfigAPI.isHttpsEnable() && !Application.rest.isHttpsStarted()) {
			Application.rest.startHTTPS();
		}
	}

	/**
	 * Stop RESTAPI server with SSL
	 * @param force Set true for force stop
	 */
	public static void subscriberHTTPSStop(boolean force) {
		if (force || (Application.rest.isHttpsStarted())) {
			Application.rest.stopHTTPS();
		}
	}

	/**
	 * Start ActiveMQ subscription
	 */
	public static void subscriberActiveMQStart() {
		if(ConfigSubscriberActiveMQ.isSubscriberActiveMQEnable() && (Application.activeMQSubscriber == null || !Application.activeMQSubscriber.isRunning()))
		{
			Application.activeMQSubscriber = new SubscriberActiveMQ();
			Application.activeMQSubscriber.start();
		}
	}

	/**
	 * Stop ActiveMQ subscription
	 * @param force Set true for force stop
	 */
	public static void subscriberActiveMQStop(boolean force) {
		if(Application.activeMQSubscriber != null && (force || Application.activeMQSubscriber.isRunning()))
		{
			Application.activeMQSubscriber.stopService();
		}		
	}

	/**
	 * Start MQTT subscription
	 */
	public static void subscriberMQTTStart() {
		if(ConfigSubscriberMQTT.isSubscriberMqttEnable() && (Application.mqttSubscriber == null || !Application.mqttSubscriber.isRunning()))
		{
			Application.mqttSubscriber = new SubscriberMQTT();
			Application.mqttSubscriber.start();
		}		
	}

	/**
	 * Stop MQTT subscription
	 * @param force Set true for force stop
	 */
	public static void subscriberMQTTStop(boolean force) {
		if(Application.mqttSubscriber != null && (force || Application.mqttSubscriber.isRunning()))
		{
			Application.mqttSubscriber.stopService();
		}		
	}

	/**
	 * Start AMQP subscription
	 */
	public static void subscriberAMQPStart() {
		if(ConfigSubscriberAMQP.isSubscriberAmqpEnable() && (Application.getAmqpSubscriber() == null || !Application.getAmqpSubscriber().isRunning()))
		{
			Application.setAmqpSubscriber(new SubscriberAMQP());
			Application.getAmqpSubscriber().start();
		}		
	}
	
	/**
	 * Stop AMQP subscription
	 * @param force Set true for force stop
	 */
	public static void subscriberAMQPStop(boolean force) {
		if(Application.getAmqpSubscriber() != null && (force || Application.getAmqpSubscriber().isRunning()))
		{
			Application.getAmqpSubscriber().stopService();
		}		
	}
	
	/**
	 * Start Redis subscription
	 */
	public static void subscriberRedisStart() {
		if(ConfigSubscriberRedis.isSubscriberRedisEnable() && (Application.getRedisSubscriber() == null || !Application.getRedisSubscriber().isRunning()))
		{
			Application.setRedisSubscriber(new SubscriberRedis());
			Application.getRedisSubscriber().setRunning(true);
			Application.getRedisSubscriber().start();
		}		
	}
	
	/**
	 * Stop Redis subscription
	 * @param force Set true for force stop
	 */
	public static void subscriberRedisStop(boolean force) {
		if(Application.getRedisSubscriber() != null && (force && Application.getRedisSubscriber().isRunning()))
		{
			Application.getRedisSubscriber().stopService();
		}		
	}
	
	public static void subscriberStompStart() {
		if(ConfigSubscriberStomp.isSubscriberStompEnable() && (Application.getStompSubscriber() == null || !Application.getStompSubscriber().isRunning()))
		{
			Application.setStompSubscriber(new SubscriberStomp());
			Application.getStompSubscriber().setRunning(true);
			Application.getStompSubscriber().start();
		}		
	}
	public static void subscriberStompStop(boolean force) {
		if(Application.getStompSubscriber() != null && (force || Application.getStompSubscriber().isRunning()))
		{
			Application.getStompSubscriber().stopService();
		}		
	}

	public static void subscriberWSStart() {
		if(ConfigSubscriberWS.isSubscriberWsEnable() && (Application.webSocketSubscriber == null || !Application.webSocketSubscriber.isRunning()))
		{
			Application.webSocketSubscriber = new SubscriberWebSocket(Config.getReconnectDelay(), Config.getWaitLoopParent(), Config.getWaitLoopChild());
			Application.webSocketSubscriber.start();	
		}
	}

	public static void subscriberWSStop(boolean force) {
		
		if(Application.webSocketSubscriber != null && (force || Application.webSocketSubscriber.isRunning()))
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

	public static SubscriberStomp getStompSubscriber() {
		return redissonSubscriber;
	}

	public static void setStompSubscriber(SubscriberStomp redissonSubscriber) {
		Application.redissonSubscriber = redissonSubscriber;
	}
	
	public static void exit()
	{
		System.exit(0);
	}
}

