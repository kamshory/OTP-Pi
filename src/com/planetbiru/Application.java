package com.planetbiru;

import java.net.InetSocketAddress;

import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigNetDHCP;
import com.planetbiru.config.ConfigNetEthernet;
import com.planetbiru.config.ConfigNetWLAN;
import com.planetbiru.gsm.GSMUtil;
import com.planetbiru.receiver.ws.WebSocketTool;
import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.Utility;

public class Application {
	
	

	public static void main(String[] args) {
		
		ConfigLoader.load("config.ini");	
		
		GSMUtil.init();

		Application.prepareSessionDir();

		/**
		 * WebSocket Server for Admin
		 */
		InetSocketAddress address = new InetSocketAddress(Config.getServerPort()+1);
		WebSocketServerImpl wsss = new WebSocketServerImpl(address);
		wsss.start();		

		/**
		 * Web Server for Admin
		 */
		ServerWeb web = new ServerWeb();
		web.init();
		
		/**
		 * WebSocket Client for feeder
		 */
		WebSocketTool wstool = new WebSocketTool();
		wstool.start();
		
		/**
		 * RabbitMQ Client for feeder
		 */
		ClientReceiverAMQP amqpReceiver = new ClientReceiverAMQP();
		amqpReceiver.init();
		
		/**
		 * REST API
		 */
		ServerAPI rest = new ServerAPI();
		rest.init();
		
		
		/**
		 * SMTP Server for send email
		 */
		ServerEmail smtp = new ServerEmail();
		smtp.init();
		
	}
	public static void prepareSessionDir()
	{
		String fileName = FileConfigUtil.fixFileName(Utility.getBaseDir()+"/"+Config.getSessionFilePath()+"/ses");
		FileConfigUtil.prepareDir(fileName);
	}
	
	
	public void init()
	{
		resetConfig();
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

