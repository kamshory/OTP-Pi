package com.planetbiru;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigNetDHCP;
import com.planetbiru.config.ConfigNetEthernet;
import com.planetbiru.config.ConfigNetWLAN;
import com.planetbiru.gsm.GSMUtil;
import com.planetbiru.receiver.ws.ClientReceiverWebSocket;
import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.Utility;

public class Application {
	
	private static ServerWebSocketServerAdmin webSocketAdmin;
	private static ServerWebAdmin webAdmin;
	private static ClientReceiverWebSocket webSocketClient;	
	private static ClientReceiverAMQP amqpReceiver;
	private static ServerRESTAPI rest;
	private static ServerEmail smtp;
	
	public static void preDestroy()
	{
		Application.webAdmin.stop();
		Application.rest.stop();
		Application.smtp.stop();
		try 
		{
			Application.webSocketAdmin.stop();
		} 
		catch (IOException | InterruptedException e) 
		{
			Thread.currentThread().interrupt();

		}
	}

	public static void main(String[] args) {
		
		ConfigLoader.load("config.ini");	
		
		Application.resetConfig();
		
		GSMUtil.start();

		Application.prepareSessionDir();

		/**
		 * WebSocket Server for Admin
		 */
		InetSocketAddress address = new InetSocketAddress(Config.getServerPort()+1);
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
		
		
		/**
		 * SMTP Server for send email
		 */
		Application.smtp = new ServerEmail();
		Application.smtp.start();
		
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

