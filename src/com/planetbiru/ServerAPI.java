package com.planetbiru;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import com.planetbiru.api.HandlerAPIBlocking;
import com.planetbiru.api.HandlerAPIMessage;
import com.planetbiru.api.HandlerAPIUnblocking;
import com.planetbiru.api.RESTAPI;
import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigAPI;
import com.planetbiru.config.ConfigBlocking;
import com.planetbiru.config.ConfigEmail;
import com.planetbiru.config.ConfigKeystore;
import com.planetbiru.config.ConfigModem;
import com.planetbiru.config.ConfigSMS;
import com.planetbiru.config.DataKeystore;
import com.planetbiru.gsm.DialUtil;
import com.planetbiru.gsm.GSMUtil;
import com.planetbiru.gsm.SMSLogger;
import com.planetbiru.receiver.amqp.RabbitMQReceiver;
import com.planetbiru.receiver.ws.WebSocketClientImpl;
import com.planetbiru.util.ServiceHTTP;
import com.planetbiru.util.Utility;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

public class ServerAPI {
	
	public void init()
	{

		
		
	
		this.initHttp();
		this.initHttps();
		
		ConfigSMS.load(Config.getSmsSettingPath());
		
		ConfigBlocking.setCountryCode(ConfigSMS.getCountryCode());
		ConfigBlocking.load(Config.getBlockingSettingPath());

		ConfigModem.load(Config.getModemSettingPath());
		if(ConfigSMS.isLogSMS())
		{
			SMSLogger.setPath(Config.getSmsLogPath());
		}
		GSMUtil.init();	
		DialUtil.init(Config.getWvdialSettingPath(), Config.getWvdialCommandConnect(), Config.getWvdialCommandDisconnect());
		
		GSMUtil.getCallerType().put(Utility.getClassName(RabbitMQReceiver.class.toString()), "amqp");
		GSMUtil.getCallerType().put(Utility.getClassName(WebSocketClientImpl.class.toString()), "ws");
		GSMUtil.getCallerType().put(Utility.getClassName(RESTAPI.class.toString()), "rest");
		
		ConfigAPI.load(Config.getApiSettingPath());	
		
		/**
		 * Override email setting if exists
		 */
		ConfigEmail.load(Config.getEmailSettingPath());
	}
	
	
	
	private void initHttps() {
		if(ConfigAPI.isHttpsEnable())
		{
			ConfigKeystore.load(Config.getKeystoreSettingPath());		
			boolean started = false;
			try 
			{
				DataKeystore keystore = ConfigKeystore.getActiveKeystore();
				String keystoreFile = keystore.getFullPath();
				String keystorePassword = keystore.getFilePassword();
				try (FileInputStream fileInputStream = new FileInputStream(keystoreFile))
				{
					HttpsServer httpsServer = HttpsServer.create(new InetSocketAddress(ConfigAPI.getHttpsPort()), 0);
					ServiceHTTP.setHttpsServer(httpsServer);
					char[] password = keystorePassword.toCharArray();
				    KeyStore keyStore;
					keyStore = KeyStore.getInstance("JKS");	
					SSLContext sslContext = SSLContext.getInstance("TLS");
					keyStore.load(fileInputStream, password);
				    KeyManagerFactory keyManagementFactory = KeyManagerFactory.getInstance("SunX509");
				    keyManagementFactory.init (keyStore, password);
				    TrustManagerFactory trustFactory = TrustManagerFactory.getInstance("SunX509");
				    trustFactory.init(keyStore);
				    sslContext.init(keyManagementFactory.getKeyManagers(), trustFactory.getTrustManagers(), null);		
					HttpsConfigurator httpsConfigurator = new HttpsConfigurator(sslContext);
					ServiceHTTP.getHttpsServer().setHttpsConfigurator(httpsConfigurator);	
					
			        ServiceHTTP.getHttpsServer().createContext(ConfigAPI.getMessagePath(), new HandlerAPIMessage());
			        ServiceHTTP.getHttpsServer().createContext(ConfigAPI.getBlockingPath(), new HandlerAPIBlocking());
			        ServiceHTTP.getHttpsServer().createContext(ConfigAPI.getUnblockingPath(), new HandlerAPIUnblocking());
			        
			        ServiceHTTP.getHttpsServer().start();
			        started = true;
				} 
				catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException | KeyManagementException | UnrecoverableKeyException e) 
				{
					e.printStackTrace();
					started = false;
				}			
			} 
			catch (KeyStoreException e2) 
			{
				e2.printStackTrace();
				started = false;
			}
			if(!started)
			{
				if(ServiceHTTP.getHttpsServer() != null)
				{
					ServiceHTTP.getHttpsServer().stop(0);
				}
				ServiceHTTP.setHttpsServer(null);
			}
		}	
	}	
	
	private void initHttp() 
	{
		if(ConfigAPI.isHttpsEnable())
		{
			try 
			{
				ServiceHTTP.setHttpServer(HttpServer.create(new InetSocketAddress(ConfigAPI.getHttpPort()), 0));
		        ServiceHTTP.getHttpServer().createContext(ConfigAPI.getMessagePath(), new HandlerAPIMessage());
		        ServiceHTTP.getHttpServer().createContext(ConfigAPI.getBlockingPath(), new HandlerAPIBlocking());
		        ServiceHTTP.getHttpServer().createContext(ConfigAPI.getUnblockingPath(), new HandlerAPIUnblocking());
		        ServiceHTTP.getHttpServer().start();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}		
	}
	
	public void destroy()
	{
		if(ServiceHTTP.getHttpServer() != null)
		{
			ServiceHTTP.getHttpServer().stop(0);
		}
		if(ServiceHTTP.getHttpsServer() != null)
		{
			ServiceHTTP.getHttpsServer().stop(0);
		}
	}
	
	
	
}
