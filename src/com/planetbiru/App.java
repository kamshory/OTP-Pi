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

public class App {
	
	private static ServerWebSocketAdmin webSocketAdmin;
	private static ServerWebAdmin webAdmin;
	private static ServerRESTAPI rest = new ServerRESTAPI();
	private static ServerEmail smtp;
	private static Scheduller scheduller;	
	private static SubscriberWebSocket webSocketSubscriber;	
	private static SubscriberMQTT mqttSubscriber;
	private static SubscriberRedis redisSubscriber;
	private static SubscriberStomp stompSubscriber;
	private static SubscriberAMQP amqpSubscriber;	
	private static SubscriberActiveMQ activeMQSubscriber;	
	private static ModemInspector modemInspector = null;
	
	
	private static Logger logger = Logger.getLogger(App.class);
	/**
	 * Main method
	 * @param args Parameters from command line
	 */
	public static void main(String[] args) {
		String currentRootDirectoryPath = App.getConfigRoot();
		boolean configLoaded = App.loadConfig(currentRootDirectoryPath, "config.ini");
		if(configLoaded)
		{
			App.startService(args);
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
		File currentJavaJarFile = new File(App.class.getProtectionDomain().getCodeSource().getLocation().getPath());   
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
	
			App.prepareSessionDir();
	
			
			/**
			 * Web Server for Admin
			 */
			App.webAdminServerStart();
			

			/**
			 * WebSocket Client for subscriber
			 */
			App.subscriberWSStart();
			
			
			/**
			 * RabbitMQ Client for subscriber
			 */
			App.subscriberAMQPStart();

			
			/**
			 * Redis Client for subscriber
			 */
			App.subscriberRedisStart();
			
			
			/**
			 * Redisson Client for subscriber
			 */
			App.subscriberStompStart();

			
			/**
			 * Mosquitto Client for subscriber
			 */
			App.subscriberMQTTStart();
			
			
			/**
			 * ActiveMQ Client for subscriber
			 */
			App.subscriberActiveMQStart();
			
			
			/**
			 * REST API HTTP
			 */
			App.restAPIStart();			
			App.otpStart();			
			App.modemSMSStart();
			App.modemInternetStart();
			App.modemInspectorStart(Config.getInspectModemInterval());

			/**
			 * WebSocket Server for Admin
			 */
			App.webSocketAdminServerStart();

			/**
			 * SMTP Server for send email
			 */
			App.serverEmailStart();
			
			App.schedulerStart();
			
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
		App.scheduller = new Scheduller();
		App.scheduller.start();		
	}

	/**
	 * Start SMTP server
	 */
	public static void serverEmailStart() {
		App.smtp = new ServerEmail();
		App.smtp.start();
		
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
		if(App.modemInspector != null)
		{
			try
			{
				App.modemInspector.stopService();				
			}
			catch(Exception e)
			{
				/**
				 * Do nothing
				 */
			}
		}
		App.modemInspector = new ModemInspector(delay);
		App.modemInspector.start();
	}

	/**
	 * Start REST server
	 */
	private static void restAPIStart() {
		App.rest = new ServerRESTAPI();
		App.rest.start();
	}

	/**
	 * Start websocket server for admin
	 */
	private static void webSocketAdminServerStart() {
		int port = Config.getServerPort() + 1;
		InetSocketAddress address = new InetSocketAddress(port);
		App.webSocketAdmin = new ServerWebSocketAdmin(address);
		App.webSocketAdmin.start();
		
	}

	/**
	 * Start web server for admin
	 */
	private static void webAdminServerStart() {
		int port = Config.getServerPort();
		App.webAdmin = new ServerWebAdmin(port);
		App.webAdmin.start();	
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
		App.rest = rest;
	}
	
	/**
	 * Start RESTAPI server
	 */
	public static void subscriberHTTPStart() {
		if (ConfigAPI.isHttpEnable() && !App.rest.isHttpStarted()) {
			App.rest.startHTTP();
		}
	}

	/**
	 * Stop RESTAPI server
	 * @param force Set true for force stop
	 */
	public static void subscriberHTTPStop(boolean force) {
		if (force || (App.rest.isHttpStarted())) {
			App.rest.stopHTTP();
		}
	}

	/**
	 * Start RESTAPI server with SSL
	 */
	public static void subscriberHTTPSStart() {
		if (ConfigAPI.isHttpsEnable() && !App.rest.isHttpsStarted()) {
			App.rest.startHTTPS();
		}
	}

	/**
	 * Stop RESTAPI server with SSL
	 * @param force Set true for force stop
	 */
	public static void subscriberHTTPSStop(boolean force) {
		if (force || (App.rest.isHttpsStarted())) {
			App.rest.stopHTTPS();
		}
	}

	/**
	 * Start ActiveMQ subscription
	 */
	public static void subscriberActiveMQStart() {
		if(ConfigSubscriberActiveMQ.isSubscriberActiveMQEnable() && (App.activeMQSubscriber == null || !App.activeMQSubscriber.isRunning()))
		{
			App.activeMQSubscriber = new SubscriberActiveMQ();
			App.activeMQSubscriber.start();
		}
	}

	/**
	 * Stop ActiveMQ subscription
	 * @param force Set true for force stop
	 */
	public static void subscriberActiveMQStop(boolean force) {
		if(App.activeMQSubscriber != null && (force || App.activeMQSubscriber.isRunning()))
		{
			App.activeMQSubscriber.stopService();
		}		
	}

	/**
	 * Start MQTT subscription
	 */
	public static void subscriberMQTTStart() {
		if(ConfigSubscriberMQTT.isSubscriberMqttEnable() && (App.mqttSubscriber == null || !App.mqttSubscriber.isRunning()))
		{
			App.mqttSubscriber = new SubscriberMQTT();
			App.mqttSubscriber.start();
		}		
	}

	/**
	 * Stop MQTT subscription
	 * @param force Set true for force stop
	 */
	public static void subscriberMQTTStop(boolean force) {
		if(App.mqttSubscriber != null && (force || App.mqttSubscriber.isRunning()))
		{
			App.mqttSubscriber.stopService();
		}		
	}

	/**
	 * Start AMQP subscription
	 */
	public static void subscriberAMQPStart() {
		if(ConfigSubscriberAMQP.isSubscriberAmqpEnable() && (App.amqpSubscriber == null || !App.amqpSubscriber.isRunning()))
		{
			App.amqpSubscriber = new SubscriberAMQP();
			App.amqpSubscriber.start();
		}		
	}
	
	/**
	 * Stop AMQP subscription
	 * @param force Set true for force stop
	 */
	public static void subscriberAMQPStop(boolean force) {
		if(App.amqpSubscriber != null && (force || App.amqpSubscriber.isRunning()))
		{
			App.amqpSubscriber.stopService();
		}		
	}
	
	/**
	 * Start Redis subscription
	 */
	public static void subscriberRedisStart() {
		if(ConfigSubscriberRedis.isSubscriberRedisEnable() && (App.getRedisSubscriber() == null || !App.getRedisSubscriber().isRunning()))
		{
			App.setRedisSubscriber(new SubscriberRedis());
			App.getRedisSubscriber().setRunning(true);
			App.getRedisSubscriber().start();
		}		
	}
	
	/**
	 * Stop Redis subscription
	 * @param force Set true for force stop
	 */
	public static void subscriberRedisStop(boolean force) {
		if(App.getRedisSubscriber() != null && (force && App.getRedisSubscriber().isRunning()))
		{
			App.getRedisSubscriber().stopService();
		}		
	}
	
	public static void subscriberStompStart() {
		if(ConfigSubscriberStomp.isSubscriberStompEnable() && (App.stompSubscriber == null || !App.stompSubscriber.isRunning()))
		{
			App.stompSubscriber = new SubscriberStomp();
			App.stompSubscriber.setRunning(true);
			App.stompSubscriber.start();
		}		
	}
	public static void subscriberStompStop(boolean force) {
		if(App.stompSubscriber != null && (force || App.stompSubscriber.isRunning()))
		{
			App.stompSubscriber.stopService();
		}		
	}

	public static void subscriberWSStart() {
		if(ConfigSubscriberWS.isSubscriberWsEnable() && (App.webSocketSubscriber == null || !App.webSocketSubscriber.isRunning()))
		{
			App.webSocketSubscriber = new SubscriberWebSocket(Config.getReconnectDelay(), Config.getWaitLoopParent(), Config.getWaitLoopChild());
			App.webSocketSubscriber.start();	
		}
	}

	public static void subscriberWSStop(boolean force) {
		
		if(App.webSocketSubscriber != null && (force || App.webSocketSubscriber.isRunning()))
		{
			App.webSocketSubscriber.stopService();	
		}
	}

	public static void restartService()
	{
		JSONObject info = new JSONObject();
		info.put(JsonKey.COMMAND, ConstantString.SERVER_SHUTDOWN);
		ServerWebSocketAdmin.broadcastMessage(info.toString());	
		App.stopAllServices();
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
		App.smtp.stopService();
		App.rest.stopService();
		App.webAdmin.stopService();
		try 
		{
			App.webSocketAdmin.stopService();
		} 
		catch (IOException | InterruptedException e) 
		{
			Thread.currentThread().interrupt();
		}
		App.scheduller.stopService();
		App.webSocketSubscriber.stopService();
		App.amqpSubscriber.stopService();
		App.mqttSubscriber.stopService();
	}	
	
	public static void prepareSessionDir()
	{
		String fileName = FileConfigUtil.fixFileName(Utility.getBaseDir()+FileSystems.getDefault().getSeparator()+Config.getSessionFilePath()+"/ses");
		FileConfigUtil.prepareDir(fileName);
	}

	public static void exit()
	{
		System.exit(0);
	}

	public static SubscriberRedis getRedisSubscriber() {
		return redisSubscriber;
	}

	public static void setRedisSubscriber(SubscriberRedis redisSubscriber) {
		App.redisSubscriber = redisSubscriber;
	}
}

