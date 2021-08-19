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

import org.apache.log4j.Logger;

import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigAPI;
import com.planetbiru.config.ConfigKeystore;
import com.planetbiru.config.DataKeystore;
import com.planetbiru.subscriber.rest.HandlerAPIMessage;
import com.planetbiru.util.ServiceHTTP;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

public class ServerRESTAPI {
	
	private static Logger logger = Logger.getLogger(ServerRESTAPI.class);
	private boolean httpsStarted = false;
	private boolean httpStarted = false;
	
	public void start()
	{
		this.startHTTP();
		this.startHTTPS();
	}
	
	void startHTTPS() {
		if(ConfigAPI.isHttpsEnable())
		{
			HttpsServer httpsServer = null;
			ConfigKeystore.load(Config.getKeystoreSettingPath());		
			try 
			{
				DataKeystore keystore = ConfigKeystore.getActiveKeystore();
				String keystoreFile = keystore.getFullPath();
				String keystorePassword = keystore.getFilePassword();
				try (FileInputStream fileInputStream = new FileInputStream(keystoreFile))
				{
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
					
					httpsServer = HttpsServer.create(new InetSocketAddress(ConfigAPI.getHttpsPort()), 0);
					httpsServer.setHttpsConfigurator(httpsConfigurator);
			        httpsServer.createContext(ConfigAPI.getMessagePath(), new HandlerAPIMessage());
			        httpsServer.createContext(ConfigAPI.getSmsPath(), new HandlerAPIMessage());
			        httpsServer.createContext(ConfigAPI.getEmailPath(), new HandlerAPIMessage());
			        httpsServer.createContext(ConfigAPI.getBlockingPath(), new HandlerAPIMessage());
			        httpsServer.createContext(ConfigAPI.getUnblockingPath(), new HandlerAPIMessage());
			        httpsServer.start();
					ServiceHTTP.setHttpsServer(httpsServer);
			        this.httpsStarted = true;
				} 
				catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException | KeyManagementException | UnrecoverableKeyException e) 
				{
					logger.error(e.getMessage());
					this.httpsStarted = false;
					ServiceHTTP.setHttpsServer(null);
				}			
			} 
			catch (NoKeyStoreException e2) 
			{
				logger.error(e2.getMessage());
				ServiceHTTP.setHttpsServer(null);
				this.httpsStarted = false;
			}
			if(!this.httpsStarted)
			{
				if(ServiceHTTP.getHttpsServer() != null)
				{
					ServiceHTTP.getHttpsServer().stop(0);
					ServiceHTTP.setHttpsServer(null);
				}
				ServiceHTTP.setHttpsServer(null);
			}
		}
	}	
	
	void startHTTP() 
	{
		if(ConfigAPI.isHttpsEnable())
		{
			try 
			{
				ServiceHTTP.setHttpServer(HttpServer.create(new InetSocketAddress(ConfigAPI.getHttpPort()), 0));
		        ServiceHTTP.getHttpServer().createContext(ConfigAPI.getMessagePath(), new HandlerAPIMessage());
		        ServiceHTTP.getHttpServer().createContext(ConfigAPI.getSmsPath(), new HandlerAPIMessage());
		        ServiceHTTP.getHttpServer().createContext(ConfigAPI.getEmailPath(), new HandlerAPIMessage());
		        ServiceHTTP.getHttpServer().createContext(ConfigAPI.getBlockingPath(), new HandlerAPIMessage());
		        ServiceHTTP.getHttpServer().createContext(ConfigAPI.getUnblockingPath(), new HandlerAPIMessage());
		        ServiceHTTP.getHttpServer().start();
		        this.httpStarted = true;
			} 
			catch (IOException e) 
			{
				logger.error(e.getMessage());
				this.httpStarted = false;
			}
		}		
	}
	public void stopHTTP()
	{
		if(ServiceHTTP.getHttpServer() != null)
		{
			ServiceHTTP.getHttpServer().stop(0);
			this.httpStarted = false;
		}
	}
	public void stopHTTPS()
	{
		if(ServiceHTTP.getHttpsServer() != null)
		{
			ServiceHTTP.getHttpsServer().stop(0);
			this.httpsStarted = false;
		}
	}
	public void stop()
	{
		this.stopHTTP();
		this.stopHTTPS();
	}

	public boolean isHttpsStarted() {
		return httpsStarted;
	}

	public void setHttpsStarted(boolean httpsStarted) {
		this.httpsStarted = httpsStarted;
	}

	public boolean isHttpStarted() {
		return httpStarted;
	}

	public void setHttpStarted(boolean httpStarted) {
		this.httpStarted = httpStarted;
	}	
	
}
