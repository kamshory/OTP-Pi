package com.planetbiru;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
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
import com.planetbiru.config.ConfigFeederAMQP;
import com.planetbiru.config.ConfigFeederWS;
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
import com.planetbiru.gsm.DialUtil;
import com.planetbiru.gsm.GSMUtil;
import com.planetbiru.receiver.amqp.ClientReceiverAMQP;
import com.planetbiru.receiver.ws.ClientReceiverWebSocket;
import com.planetbiru.user.WebUserAccount;
import com.planetbiru.util.CommandLineExecutor;
import com.planetbiru.util.ConfigLoader;
import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.ProcessKiller;
import com.planetbiru.util.Utility;

import org.apache.log4j.Logger;

public class Application {
	
	private static ServerWebSocketServerAdmin webSocketAdmin;
	private static ServerWebAdmin webAdmin;
	private static ClientReceiverWebSocket webSocketClient;	
	private static ClientReceiverAMQP amqpReceiver;
	private static ServerRESTAPI rest;
	private static ServerEmail smtp;
	private static Scheduller scheduller;
	
	private static Logger logger = Logger.getLogger(Application.class);
	

	public static void main(String[] args) {
		File currentJavaJarFile = new File(Application.class.getProtectionDomain().getCodeSource().getLocation().getPath());   
		String currentJavaJarFilePath = currentJavaJarFile.getAbsolutePath();
		String currentRootDirectoryPath = currentJavaJarFilePath.replace(currentJavaJarFile.getName(), "");
		List<String> argList = Arrays.asList(args);

		boolean loaded = loadConfig(currentRootDirectoryPath, "config.ini");
		boolean needToStart = true;
		if(loaded)
		{
			String imageName = ConfigLoader.getConfig("otpbroker.image.name");
			Config.setImageName(imageName);
			
			if(args != null)
			{
				if(argList.contains("--start"))
				{
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
				
				if(argList.contains("--start"))
				{
					resetConfig();
				}
		
				Application.prepareSessionDir();
		
				int wsport = Config.getServerPort()+1;

				/**
				 * Web Server for Admin
				 */
				Application.webAdmin = new ServerWebAdmin();
				Application.webAdmin.start();

				/**
				 * WebSocket Server for Admin
				 */
				InetSocketAddress address = new InetSocketAddress(wsport);
				Application.webSocketAdmin = new ServerWebSocketServerAdmin(address);
				Application.webSocketAdmin.start();		
		
				
				/**
				 * WebSocket Client for feeder
				 */
				Application.webSocketClient = new ClientReceiverWebSocket();
				Application.webSocketClient.start();
				
				/**
				 * RabbitMQ Client for feeder
				 */
				Application.amqpReceiver = new ClientReceiverAMQP();
				Application.amqpReceiver.start();
				
				/**
				 * REST API
				 */
				Application.rest = new ServerRESTAPI();
				Application.rest.start();			
		
				GSMUtil.start();
				DialUtil.start();
		
				/**
				 * SMTP Server for send email
				 */
				Application.smtp = new ServerEmail();
				Application.smtp.start();
				
				Application.scheduller = new Scheduller();
				Application.scheduller.start();
				logger.info("Service started");
			}
			else
			{
				logger.info("Service already started");
			}
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
			}
		}
		return loaded;	
	}
	
	public static void restartService()
	{
		JSONObject info = new JSONObject();
		info.put(JsonKey.COMMAND, "server-shutdown");
		ServerWebSocketServerAdmin.broadcastMessage(info.toString());	
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
		Application.smtp.stop();
		Application.rest.stop();
		Application.webAdmin.stop();
		try 
		{
			Application.webSocketAdmin.stop();
		} 
		catch (IOException | InterruptedException e) 
		{
			Thread.currentThread().interrupt();
		}
	}	
	
	public static void prepareSessionDir()
	{
		String fileName = FileConfigUtil.fixFileName(Utility.getBaseDir()+"/"+Config.getSessionFilePath()+"/ses");
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
			ConfigFeederAMQP.reset();
			ConfigFeederAMQP.save();
		}
		if(props.getOrDefault("RESET_FEEDER_WS", "").toString().equalsIgnoreCase("true"))
		{
			ConfigFeederWS.reset();
			ConfigFeederWS.save();
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
				if(verifyResetFile(props.getOrDefault("VERIFY", "").toString()))
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

	private static boolean verifyResetFile(String verifyString) {
		try
		{
			JSONObject json = new JSONObject(verifyString);
			return (json.optString("deviceType", "").equals("RPi") && json.optString("baseName", "").equals("reset-config.ini"));
		}
		catch(JSONException e)
		{
			return false;
		}
	}

	private static Properties getResetProperties(String path) throws IOException {
		Properties props = new Properties();
		InputStream inputStream = new FileInputStream(new File(path));
		props.load(inputStream);
		return props;
	}

}

