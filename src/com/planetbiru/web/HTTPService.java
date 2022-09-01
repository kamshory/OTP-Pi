package com.planetbiru.web;

import com.sun.net.httpserver.HttpServer; //NOSONAR
import com.sun.net.httpserver.HttpsServer;

public class HTTPService {
	
	private HTTPService()
	{
		
	}

	public static HttpsServer getHttpsServer() {
		return httpsServer;
	}
	public static void setHttpsServer(HttpsServer httpsServer) {
		HTTPService.httpsServer = httpsServer;
	}

	public static HttpServer getHttpServer() {
		return httpServer;
	}

	public static void setHttpServer(HttpServer httpServer) {
		HTTPService.httpServer = httpServer;
	}

	private static HttpServer httpServer;
	private static HttpsServer httpsServer;

}
