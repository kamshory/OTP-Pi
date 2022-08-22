package com.planetbiru.web;

import java.io.IOException;
import java.net.HttpURLConnection;

import com.planetbiru.constant.ConstantString;
import com.sun.net.httpserver.Headers; //NOSONAR
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class HandlerWebManagerPing implements HttpHandler {

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
        Headers responseHeaders = httpExchange.getResponseHeaders();       
        byte[] responseBody = "OK".getBytes();      
        responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.TEXT_PLAIN);
        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, responseBody.length);	 
        httpExchange.getResponseBody().write(responseBody);
        httpExchange.close();
	}

}
