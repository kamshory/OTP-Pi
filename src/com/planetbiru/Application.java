package com.planetbiru;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigAPI;
import com.planetbiru.config.ConfigAPIUser;
import com.planetbiru.config.ConfigBlocking;
import com.planetbiru.config.ConfigDDNS;
import com.planetbiru.config.ConfigEmail;
import com.planetbiru.config.ConfigSubscriberAMQP;
import com.planetbiru.config.ConfigSubscriberMQTT;
import com.planetbiru.config.ConfigSubscriberWS;
import com.planetbiru.config.ConfigFirewall;
import com.planetbiru.config.ConfigKeystore;
import com.planetbiru.config.ConfigModem;
import com.planetbiru.config.ConfigNetDHCP;
import com.planetbiru.config.ConfigNetEthernet;
import com.planetbiru.config.ConfigNetWLAN;
import com.planetbiru.config.ConfigSMS;
import com.planetbiru.config.ConfigSMTP;
import com.planetbiru.config.ConfigVendorAfraid;
import com.planetbiru.config.ConfigVendorCloudflare;
import com.planetbiru.config.ConfigVendorDynu;
import com.planetbiru.config.ConfigVendorNoIP;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.gsm.InternetDialUtil;
import com.planetbiru.gsm.GSMUtil;
import com.planetbiru.subscriber.amqp.SubscriberAMQP;
import com.planetbiru.subscriber.mqtt.SubscriberMQTT;
import com.planetbiru.subscriber.ws.SubscriberWebSocket;
import com.planetbiru.user.WebUserAccount;
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
	
	private static SubscriberMQTT mqttSubscriber;
	private static SubscriberWebSocket webSocketSubscriber;	
	private static SubscriberAMQP amqpSubscriber;
	
	private static Logger logger = Logger.getLogger(Application.class);
	

	public static void main(String[] args) {
		File currentJavaJarFile = new File(Application.class.getProtectionDomain().getCodeSource().getLocation().getPath());   
		String currentJavaJarFilePath = currentJavaJarFile.getAbsolutePath();
		String currentRootDirectoryPath = currentJavaJarFilePath.replace(currentJavaJarFile.getName(), "");

		boolean configLoaded = loadConfig(currentRootDirectoryPath, "config.ini");
		if(configLoaded)
		{
			Application.startService(args);
		}
		else
		{
			logger.info("Service not started because failed to read config file");
		}		
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
				e1.printStackTrace();
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
		List<String> argList = new ArrayList<>();
		if(args != null)
		{
			argList = Arrays.asList(args);
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
				Application.resetConfig();
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
			 * Mosquitto Client for subscriber
			 */
			Application.subscriberMQTTStart();
			
			/**
			 * REST API HTTP
			 */
			Application.restAPIStart();
					
	
			Application.modemSMSStart();
			
			Application.modemInternetStart();

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
	
	private static void schedulerStart() {
		Application.scheduller = new Scheduller();
		Application.scheduller.start();		
	}

	private static void serverEmailStart() {
		Application.smtp = new ServerEmail();
		Application.smtp.start();
		
	}

	private static void modemInternetStart() {
		InternetDialUtil.start();
	}

	private static void modemSMSStart() {
		GSMUtil.start();	
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
		if(ConfigSubscriberAMQP.isSubscriberAmqpEnable() && (Application.amqpSubscriber == null || !Application.amqpSubscriber.isRunning()))
		{
			Application.amqpSubscriber = new SubscriberAMQP();
			Application.amqpSubscriber.start();
		}		
	}
	public static void subscriberAMQPStop() {
		if(Application.amqpSubscriber != null && Application.amqpSubscriber.isRunning())
		{
			Application.amqpSubscriber.stopService();
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
		info.put(JsonKey.COMMAND, "server-shutdown");
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
		Application.amqpSubscriber.stopService();
		Application.mqttSubscriber.stopService();
	}	
	
	public static void prepareSessionDir()
	{
		String fileName = FileConfigUtil.fixFileName(Utility.getBaseDir()+FileSystems.getDefault().getSeparator()+Config.getSessionFilePath()+"/ses");
		FileConfigUtil.prepareDir(fileName);
	}
	private static void resetBasicConfig(Properties props)
	{
		if(props.getOrDefault("RESET_DHCP", "").toString().equalsIgnoreCase("true"))
		{
			String defaultConfigDHCP = Config.getDhcpSettingPathDefault();
			String configDHCP = Config.getDhcpSettingPath();
			ConfigNetDHCP.load(defaultConfigDHCP);
			ConfigNetDHCP.save(configDHCP);
			ConfigNetDHCP.apply(Config.getOsDHCPConfigPath());
		}
		if(props.getOrDefault("RESET_WLAN", "").toString().equalsIgnoreCase("true"))
		{
			String defaultConfigWLAN = Config.getWlanSettingPathDefault();
			String configWLAN = Config.getWlanSettingPath();
			ConfigNetWLAN.load(defaultConfigWLAN);
			ConfigNetWLAN.save(configWLAN);
			ConfigNetWLAN.apply(Config.getOsWLANConfigPath(), Config.getOsWLANConfigPath());
			
		}
		if(props.getOrDefault("RESET_ETHERNET", "").toString().equalsIgnoreCase("true"))
		{
			String defaultConfigEthernet = Config.getEthernetSettingPathDefault();	
			String configEthernet = Config.getEthernetSettingPath();
			ConfigNetEthernet.load(defaultConfigEthernet);
			ConfigNetEthernet.save(configEthernet);
			ConfigNetEthernet.apply(Config.getOsEthernetConfigPath());
		}
		if(props.getOrDefault("RESET_USER", "").toString().equalsIgnoreCase("true"))
		{
			WebUserAccount.reset();
			WebUserAccount.save();
		}
		
	}
	
	private static void resetConfig() 
	{
		logger.info("Reset Config");
		Properties props = loadResetProfile();
		if(props != null)
		{
			resetBasicConfig(props);
			resetAdvancedConfig(props);
			resetVendorConfig(props);		
			CommandLineExecutor.exec(Config.getRestartCommand());
		}	
		else
		{
			logger.info("Reset File Not Exists");
		}
	}
	
	private static void resetVendorConfig(Properties props) {
		if(props.getOrDefault("RESET_VENDOR_AFRAID", "").toString().equalsIgnoreCase("true"))
		{
			ConfigVendorAfraid.reset();
			ConfigVendorAfraid.save();
		}
		if(props.getOrDefault("RESET_VENDOR_CLOUDFLARE", "").toString().equalsIgnoreCase("true"))
		{
			ConfigVendorCloudflare.reset();
			ConfigVendorCloudflare.save();
		}
		if(props.getOrDefault("RESET_VENDOR_DYNU", "").toString().equalsIgnoreCase("true"))
		{
			ConfigVendorDynu.reset();
			ConfigVendorDynu.save();
		}
		if(props.getOrDefault("RESET_VENDOR_NOIP", "").toString().equalsIgnoreCase("true"))
		{
			ConfigVendorNoIP.reset();
			ConfigVendorNoIP.save();
		}		
	}

	private static void resetAdvancedConfig(Properties props) {
		if(props.getOrDefault("RESET_API", "").toString().equalsIgnoreCase("true"))
		{
			ConfigAPI.reset();
			ConfigAPI.save();
		}
		if(props.getOrDefault("RESET_API_USER", "").toString().equalsIgnoreCase("true"))
		{
			ConfigAPIUser.reset();
			ConfigAPIUser.save();
		}
		if(props.getOrDefault("RESET_BLOCKING", "").toString().equalsIgnoreCase("true"))
		{
			ConfigBlocking.reset();
			ConfigBlocking.save();
		}
		if(props.getOrDefault("RESET_DDNS", "").toString().equalsIgnoreCase("true"))
		{
			ConfigDDNS.reset();
			ConfigDDNS.save();
		}
		if(props.getOrDefault("RESET_EMAIL", "").toString().equalsIgnoreCase("true"))
		{
			ConfigEmail.reset();
			ConfigEmail.save();
		}
		if(props.getOrDefault("RESET_FEEDER_AMQP", "").toString().equalsIgnoreCase("true"))
		{
			ConfigSubscriberAMQP.reset();
			ConfigSubscriberAMQP.save();
		}
		if(props.getOrDefault("RESET_FEEDER_WS", "").toString().equalsIgnoreCase("true"))
		{
			ConfigSubscriberWS.reset();
			ConfigSubscriberWS.save();
		}
		if(props.getOrDefault("RESET_FIREWALL", "").toString().equalsIgnoreCase("true"))
		{
			ConfigFirewall.reset();
			ConfigFirewall.save();
		}
		if(props.getOrDefault("RESET_KEYSTORE", "").toString().equalsIgnoreCase("true"))
		{
			ConfigKeystore.reset();
			ConfigKeystore.save();
		}
		if(props.getOrDefault("RESET_MODEM", "").toString().equalsIgnoreCase("true"))
		{
			ConfigModem.reset();
			ConfigModem.save();
		}
		if(props.getOrDefault("RESET_SMS", "").toString().equalsIgnoreCase("true"))
		{
			ConfigSMS.reset();
			ConfigSMS.save();
		}
		if(props.getOrDefault("RESET_SMTP", "").toString().equalsIgnoreCase("true"))
		{
			ConfigSMTP.reset();
			ConfigSMTP.save();
		}
		
	}

	private static Properties loadResetProfile() 
	{
		List<String> usbDrives = new ArrayList<>();
		usbDrives.add("/media/usb/a");
		usbDrives.add("/media/usb/b");
		usbDrives.add("/media/usb/c");
		usbDrives.add("/media/usb/d");		
		String fileName = Config.getResetConfigPath();
		
		for(int i = 0; i<usbDrives.size(); i++)
		{
			String path = usbDrives.get(i) + fileName;
			try 
			{
				Properties props = getResetProperties(path);
				if(verifyResetFile(props.getOrDefault("VERIFY", "").toString(), Config.getResetDeviceType(), Config.getResetDeviceFile()))
				{
					return props;
				}
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
			
		}
		return null;
	}

	private static boolean verifyResetFile(String verifyString, String deviceType, String baseName) {
		try
		{
			String decoded = Utility.base64Decode(verifyString);
			JSONObject json = new JSONObject(decoded);
			return (json.optString("deviceType", "").equals(deviceType) && json.optString("baseName", "").equals(baseName));
		}
		catch(JSONException e)
		{
			return false;
		}
	}

	private static Properties getResetProperties(String path) throws IOException {
		Properties props = new Properties();
		InputStream inputStream = null;
		try
		{
			inputStream = new FileInputStream(new File(path));
			props.load(inputStream);
		}
		finally 
		{
			if(inputStream != null)
			{
				inputStream.close();
			}
		}
		return props;
	}


}

