package com.planetbiru.web;

import java.io.IOException;

import com.planetbiru.config.Config;
import com.planetbiru.constant.ConstantString;
import com.planetbiru.cookie.CookieServer;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class HandlerWebManagerLogout implements HttpHandler {

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		CookieServer cookie = new CookieServer(requestHeaders, Config.getSessionName(), Config.getSessionLifetime());		
		cookie.destroySession();
		cookie.putToHeaders(responseHeaders);
		int statusCode = HttpStatus.MOVED_PERMANENTLY;
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		responseHeaders.add(ConstantString.LOCATION, "/");	
		httpExchange.sendResponseHeaders(statusCode, 0);	 
		httpExchange.close();

	}

}
