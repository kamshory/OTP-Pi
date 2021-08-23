package com.planetbiru;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.log4j.Logger;

import com.planetbiru.util.ServiceHTTP;
import com.planetbiru.web.HandlerWebManager;
import com.planetbiru.web.HandlerWebManagerAPI;
import com.planetbiru.web.HandlerWebManagerData;
import com.planetbiru.web.HandlerWebManagerLogin;
import com.planetbiru.web.HandlerWebManagerLogout;
import com.planetbiru.web.HandlerWebManagerPing;
import com.planetbiru.web.HandlerWebManagerUserAdd;
import com.planetbiru.web.HandlerWebManagerUserInit;
import com.sun.net.httpserver.HttpServer;

public class ServerWebAdmin {
	
	private static Logger logger = Logger.getLogger(ServerWebAdmin.class);
	private int serverPort;
	
	public ServerWebAdmin(int serverPort)
	{
		this.serverPort = serverPort;
	}
	
	public void start() 
	{
		try 
		{
			ServiceHTTP.setHttpServer(HttpServer.create(new InetSocketAddress(this.serverPort), 0));
	        ServiceHTTP.getHttpServer().createContext("/", new HandlerWebManager());
	        ServiceHTTP.getHttpServer().createContext("/login.html", new HandlerWebManagerLogin());
	        ServiceHTTP.getHttpServer().createContext("/logout.html", new HandlerWebManagerLogout());
	        ServiceHTTP.getHttpServer().createContext("/user/add", new HandlerWebManagerUserAdd());
	        ServiceHTTP.getHttpServer().createContext("/user/init", new HandlerWebManagerUserInit());
	        ServiceHTTP.getHttpServer().createContext("/api/", new HandlerWebManagerAPI());
	        ServiceHTTP.getHttpServer().createContext("/data/", new HandlerWebManagerData());
	        ServiceHTTP.getHttpServer().createContext("/ping/", new HandlerWebManagerPing());
	        ServiceHTTP.getHttpServer().start();
		} 
		catch (IOException e) 
		{
			logger.error(e.getMessage(), e);
		}
	}

	public void stopService() {
		if(ServiceHTTP.getHttpServer() != null)
		{
			ServiceHTTP.getHttpServer().stop(0);
		}
		
	}
	

}
