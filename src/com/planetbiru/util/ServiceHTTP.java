package com.planetbiru.util;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;

public class ServiceHTTP {
	
	private ServiceHTTP()
	{
		
	}

	public static HttpsServer getHttpsServer() {
		return httpsServer;
	}
	public static void setHttpsServer(HttpsServer httpsServer) {
		ServiceHTTP.httpsServer = httpsServer;
	}

	public static HttpServer getHttpServer() {
		return httpServer;
	}

	public static void setHttpServer(HttpServer httpServer) {
		ServiceHTTP.httpServer = httpServer;
	}

	private static HttpServer httpServer;
	private static HttpsServer httpsServer;

}
