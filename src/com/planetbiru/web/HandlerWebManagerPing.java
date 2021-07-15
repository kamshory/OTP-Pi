package com.planetbiru.web;

import java.io.IOException;
import java.net.HttpURLConnection;

import com.planetbiru.constant.ConstantString;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class HandlerWebManagerPing implements HttpHandler {

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
        Headers responseHeaders = httpExchange.getResponseHeaders();       
        String response = "OK";      
        responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.TEXT_PLAIN);
        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());	 
        httpExchange.getResponseBody().write(response.getBytes());
        httpExchange.close();
	}

}
