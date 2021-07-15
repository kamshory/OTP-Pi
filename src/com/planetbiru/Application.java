package com.planetbiru;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;
import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigNetDHCP;
import com.planetbiru.config.ConfigNetEthernet;
import com.planetbiru.config.ConfigNetWLAN;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.gsm.DialUtil;
import com.planetbiru.gsm.GSMUtil;
import com.planetbiru.receiver.ws.ClientReceiverWebSocket;
import com.planetbiru.util.CommandLineExecutor;
import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.ProcessKiller;
import com.planetbiru.util.Utility;

public class Application {
	
	private static ServerWebSocketServerAdmin webSocketAdmin;
	private static ServerWebAdmin webAdmin;
	private static ClientReceiverWebSocket webSocketClient;	
	private static ClientReceiverAMQP amqpReceiver;
	private static ServerRESTAPI rest;
	private static ServerEmail smtp;
	private static Scheduller scheduller;
	
	public static void main(String[] args) {

		File currentJavaJarFile = new File(Application.class.getProtectionDomain().getCodeSource().getLocation().getPath());   
		String currentJavaJarFilePath = currentJavaJarFile.getAbsolutePath();
		String currentRootDirectoryPath = currentJavaJarFilePath.replace(currentJavaJarFile.getName(), "");

		boolean loaded = loadConfig(currentRootDirectoryPath, "config.ini");
		boolean needToStart = true;
		if(loaded)
		{
			String imageName = ConfigLoader.getConfig("otpbroker.image.name");
			Config.setImageName(imageName);
			
			if(args != null)
			{
				List<String> list = Arrays.asList(args);
				if(list.contains("--start"))
				{
					String pingCommand = ConfigLoader.getConfig("otpbroker.ssh.ping.command");
					String result = CommandLineExecutor.exec(pingCommand).toString();
					if(result.equals("OK"))
					{
						needToStart = false;
					}
				}
				else if(list.contains("--restart"))
				{
					ProcessKiller killer = new ProcessKiller(Config.getImageName(), true);
					killer.stop();					
				}
				else if(list.contains("--stop"))
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
				
				Application.resetConfig();
		
				Application.prepareSessionDir();
		
				int wsport = Config.getServerPort()+1;
				/**
				 * WebSocket Server for Admin
				 */
				InetSocketAddress address = new InetSocketAddress(wsport);
				Application.webSocketAdmin = new ServerWebSocketServerAdmin(address);
				Application.webSocketAdmin.start();		
		
				/**
				 * Web Server for Admin
				 */
				Application.webAdmin = new ServerWebAdmin();
				Application.webAdmin.start();
				
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
				System.out.println("Service started");
			}
			else
			{
				System.out.println("Service already started");
			}
		}
		else
		{
			System.out.println("Service not started because failed to read config file");
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
	
	
	private static void resetConfig() 
	{
		if(usbPluged())
		{
			String defaultConfigDHCP = Config.getDhcpSettingPathDefault();
			String defaultConfigWLAN = Config.getWlanSettingPathDefault();
			String defaultConfigEthernet = Config.getEthernetSettingPathDefault();
	
			String configDHCP = Config.getDhcpSettingPath();
			String configWLAN = Config.getWlanSettingPath();
			String configEthernet = Config.getEthernetSettingPath();
			
			ConfigNetDHCP.load(defaultConfigDHCP);
			ConfigNetWLAN.load(defaultConfigWLAN);
			ConfigNetEthernet.load(defaultConfigEthernet);
			
			ConfigNetDHCP.save(configDHCP);
			ConfigNetWLAN.save(configWLAN);
			ConfigNetEthernet.save(configEthernet);
			
			ConfigNetDHCP.apply(Config.getOsDHCPConfigPath());
			ConfigNetWLAN.apply(Config.getOsWLANConfigPath(), Config.getOsWLANConfigPath());
			ConfigNetEthernet.apply(Config.getOsEthernetConfigPath());
		}		
	}
	
	private static boolean usbPluged() 
	{
		return false;
	}
}

