package com.planetbiru;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.log4j.Logger;

import com.planetbiru.web.HandlerWebManager;
import com.planetbiru.web.HandlerWebManagerAPI;
import com.planetbiru.web.HandlerWebManagerActivation;
import com.planetbiru.web.HandlerWebManagerData;
import com.planetbiru.web.HandlerWebManagerLogin;
import com.planetbiru.web.HandlerWebManagerLogout;
import com.planetbiru.web.HandlerWebManagerPing;
import com.planetbiru.web.HandlerWebManagerTool;
import com.planetbiru.web.HandlerWebManagerUserAdd;
import com.planetbiru.web.HandlerWebManagerUserInit;
import com.planetbiru.web.HTTPService;
import com.sun.net.httpserver.HttpServer; //NOSONAR

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
			HTTPService.setHttpServer(HttpServer.create(new InetSocketAddress(this.serverPort), 0));
	        HTTPService.getHttpServer().createContext("/", new HandlerWebManager());
	        HTTPService.getHttpServer().createContext("/login.html", new HandlerWebManagerLogin());
	        HTTPService.getHttpServer().createContext("/logout.html", new HandlerWebManagerLogout());
	        HTTPService.getHttpServer().createContext("/device-activation", new HandlerWebManagerActivation());
	        HTTPService.getHttpServer().createContext("/user/add", new HandlerWebManagerUserAdd());
	        HTTPService.getHttpServer().createContext("/user/init", new HandlerWebManagerUserInit());
	        HTTPService.getHttpServer().createContext("/api/", new HandlerWebManagerAPI());
	        HTTPService.getHttpServer().createContext("/data/", new HandlerWebManagerData());
	        HTTPService.getHttpServer().createContext("/ping/", new HandlerWebManagerPing());
	        HTTPService.getHttpServer().createContext("/tool/", new HandlerWebManagerTool());
	        HTTPService.getHttpServer().start();
		} 
		catch (IOException e) 
		{
			logger.error(e.getMessage());
			logger.info("Exit...");
			App.exit();
		}
	}

	public void stopService() {
		if(HTTPService.getHttpServer() != null){
			HTTPService.getHttpServer().stop(0);
		}	
	}
	

}
