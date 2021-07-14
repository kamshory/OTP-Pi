package com.planetbiru.api;

import java.io.IOException;
import java.net.HttpURLConnection;

import org.json.JSONObject;

import com.planetbiru.config.Config;
import com.planetbiru.constant.ConstantString;
import com.planetbiru.web.HttpUtil;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class HandlerAPISMS implements HttpHandler {

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		Headers requestHeaders = httpExchange.getRequestHeaders();
        Headers responseHeaders = httpExchange.getResponseHeaders();
        if(Config.isValidDevice())
        {
        	if(RESTAPI.isValidRequest(requestHeaders))
           	{
           		if(httpExchange.getRequestMethod().equalsIgnoreCase("POST") || httpExchange.getRequestMethod().equalsIgnoreCase("PUT"))
    	        {
    	            byte[] requestBody = HttpUtil.getRequestBody(httpExchange);
    	            String requestBodyStr = new String(requestBody);     
    	            JSONObject result = RESTAPI.processRequest(requestBodyStr);            
    	            String response = result.toString(4);            
    	            responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
    	            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());	 
    	            httpExchange.getResponseBody().write(response.getBytes());
    	        }
    	        else
    	        {
    	            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, 0);	        	
    	        }
           	}
           	else
           	{
           		httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_UNAUTHORIZED, 0);	      
           	}
        }
        else
        {
        	httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_FORBIDDEN, 0);	
        }
       	
        httpExchange.close();
	}

}
