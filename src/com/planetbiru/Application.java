package com.planetbiru;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigAPI;
import com.planetbiru.config.ConfigDDNS;
import com.planetbiru.config.ConfigGeneral;
import com.planetbiru.config.ConfigNetDHCP;
import com.planetbiru.config.ConfigNetEthernet;
import com.planetbiru.config.ConfigNetWLAN;
import com.planetbiru.config.ConfigVendorCloudflare;
import com.planetbiru.config.ConfigVendorNoIP;
import com.planetbiru.config.PropertyLoader;
import com.planetbiru.gsm.GSMUtil;
import com.planetbiru.receiver.ws.WebSocketTool;
import com.planetbiru.user.WebUserAccount;
import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.ServerInfo;
import com.planetbiru.util.ServiceHTTP;
import com.planetbiru.util.Utility;
import com.planetbiru.web.HandlerWebManager;
import com.planetbiru.web.HandlerWebManagerAPI;
import com.planetbiru.web.HandlerWebManagerData;
import com.planetbiru.web.HandlerWebManagerLogin;
import com.planetbiru.web.HandlerWebManagerLogout;
import com.planetbiru.web.HandlerWebManagerUserAdd;
import com.planetbiru.web.HandlerWebManagerUserInit;
import com.sun.net.httpserver.HttpServer;

public class Application {
	
	

	public static void main(String[] args) {
		
		ConfigLoader.load("config.ini");	
		prepareSessionDir();
		
		initHttp();

		
		// Get Server Info and save it to cache
		ServerInfo.getInfo();
		
		WebSocketTool wstool = new WebSocketTool(5000);
		wstool.start();
		
		ClientReceiverAMQP amqpReceiver = new ClientReceiverAMQP();
		amqpReceiver.init();
		
		
		InetSocketAddress address = new InetSocketAddress(8889);
		WebSocketServerImpl wsss = new WebSocketServerImpl(address);
		wsss.start();
		
		GSMUtil.init();
		
		
		SMTPServerImpl smtp = new SMTPServerImpl();
		smtp.init();
		smtp.start();
		
	}
	public static void prepareSessionDir()
	{
		String fileName = FileConfigUtil.fixFileName(Utility.getBaseDir()+"/"+Config.getSessionFilePath()+"/ses");
		FileConfigUtil.prepareDir(fileName);
	}
	
	public static void initWebManager()
	{
		ConfigDDNS.load(Config.getDdnsSettingPath());
		ConfigVendorCloudflare.load(Config.getCloudflareSettingPath());
		ConfigVendorNoIP.load(Config.getNoIPSettingPath());
		ConfigGeneral.load(Config.getGeneralSettingPath());
		ConfigAPI.load(Config.getApiSettingPath());
		WebUserAccount.load(Config.getUserSettingPath());			
		PropertyLoader.load(Config.getMimeSettingPath());	
		Config.setValidDevice(true);
	}
	
	private static void initHttp() 
	{
		initWebManager();
		try 
		{
			ServiceHTTP.setHttpServer(HttpServer.create(new InetSocketAddress(Config.getServerPort()), 0));
	        ServiceHTTP.getHttpServer().createContext("/", new HandlerWebManager());
	        ServiceHTTP.getHttpServer().createContext("/login.html", new HandlerWebManagerLogin());
	        ServiceHTTP.getHttpServer().createContext("/logout.html", new HandlerWebManagerLogout());
	        ServiceHTTP.getHttpServer().createContext("/user/add", new HandlerWebManagerUserAdd());
	        ServiceHTTP.getHttpServer().createContext("/user/init", new HandlerWebManagerUserInit());
	        ServiceHTTP.getHttpServer().createContext("/api/", new HandlerWebManagerAPI());
	        ServiceHTTP.getHttpServer().createContext("/data/", new HandlerWebManagerData());
	        ServiceHTTP.getHttpServer().start();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
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

