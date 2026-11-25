package com.planetbiru.server.rest;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashSet;
import java.util.Set;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.log4j.Logger;

import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigAPI;
import com.planetbiru.config.ConfigKeystore;
import com.planetbiru.config.DataKeystore;
import com.planetbiru.web.HTTPService;
import com.sun.net.httpserver.HttpServer; //NOSONAR
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
	
	public void startHTTPS() {
		if(ConfigAPI.isHttpsEnable())
		{
			ConfigKeystore.load(Config.getKeystoreSettingPath());		
			try 
			{
				DataKeystore keystore = ConfigKeystore.getActiveKeystore();
				String keystoreFile = keystore.getFullPath();
				String keystorePassword = keystore.getFilePassword();
				this.createHTTPSServer(keystoreFile, keystorePassword);		
			}
			catch (NoKeystoreException e) 
			{
				logger.error(e.getMessage());
				HTTPService.setHttpsServer(null);
				this.httpsStarted = false;
			}
			if(!this.httpsStarted)
			{
				if(HTTPService.getHttpsServer() != null)
				{
					HTTPService.getHttpsServer().stop(0);
					HTTPService.setHttpsServer(null);
				}
				HTTPService.setHttpsServer(null);
			}
		}
	}	
	
	private void createHTTPSServer(String keystoreFile, String keystorePassword) {
		HttpsServer httpsServer = null;
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
			this.registerContexts(httpsServer);
	        httpsServer.start();
			HTTPService.setHttpsServer(httpsServer);
	        this.httpsStarted = true;
			logger.info("REST API HTTPS listener opened at " + httpsServer.getAddress());
		} 
		catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException | KeyManagementException | UnrecoverableKeyException e) 
		{
			logger.error(e.getMessage());
			this.httpsStarted = false;
			HTTPService.setHttpsServer(null);
		}	
		
	}

	public void startHTTP() 
	{
		if(ConfigAPI.isHttpEnable())
		{
			try 
			{
				HTTPService.setHttpServer(HttpServer.create(new InetSocketAddress(ConfigAPI.getHttpPort()), 0));
				this.registerContexts(HTTPService.getHttpServer());
		        HTTPService.getHttpServer().start();
		        this.httpStarted = true;
		        logger.info("REST API HTTP listener opened at " + HTTPService.getHttpServer().getAddress());
			} 
			catch (IOException e) 
			{
				logger.error(e.getMessage());
				this.httpStarted = false;
			}
		}		
	}

	private void registerContexts(HttpServer server) {
		String[] paths = {
				ConfigAPI.getOtpPath(),
				ConfigAPI.getMessagePath(),
				ConfigAPI.getSmsPath(),
				ConfigAPI.getEmailPath(),
				ConfigAPI.getBlockingPath(),
				ConfigAPI.getUnblockingPath()
		};
		Set<String> registeredPaths = new HashSet<>();
		for(String path : paths)
		{
			if(path == null || path.trim().isEmpty() || !path.startsWith("/"))
			{
				logger.error("Ignoring invalid REST API context path: " + path);
				continue;
			}
			if(!registeredPaths.add(path))
			{
				logger.warn("Duplicate REST API context path configured; registering only once: " + path);
				continue;
			}
			server.createContext(path, new HandlerAPIMessage());
		}
	}

	public void stopHTTP()
	{
		if(HTTPService.getHttpServer() != null)
		{
			HTTPService.getHttpServer().stop(0);
			this.httpStarted = false;
		}
	}
	public void stopHTTPS()
	{
		if(HTTPService.getHttpsServer() != null)
		{
			HTTPService.getHttpsServer().stop(0);
			this.httpsStarted = false;
		}
	}
	public void stopService()
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
