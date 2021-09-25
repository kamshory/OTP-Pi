package com.planetbiru.server.rest;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.api.MessageAPI;
import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigAPIUser;
import com.planetbiru.constant.ConstantString;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.constant.ResponseCode;
import com.planetbiru.util.Utility;
import com.planetbiru.web.HttpMethod;
import com.planetbiru.web.HttpUtil;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class HandlerAPIMessage implements HttpHandler {
	
	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		Headers requestHeaders = httpExchange.getRequestHeaders();
        Headers responseHeaders = httpExchange.getResponseHeaders();
        if(Config.isValidDevice())
        {
        	if(HandlerAPIMessage.isValidRequest(requestHeaders))
           	{
           		if(httpExchange.getRequestMethod().equalsIgnoreCase(HttpMethod.POST) || httpExchange.getRequestMethod().equalsIgnoreCase(HttpMethod.PUT))
    	        {
    	            byte[] requestBody = HttpUtil.getRequestBody(httpExchange);
    	            String requestBodyStr = new String(requestBody);     
    	            MessageAPI api = new MessageAPI();
    	            JSONObject result = api.processRequest(requestBodyStr);            
    	            byte[] response = result.toString(4).getBytes();            
    	            responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
    	            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);	 
    	            httpExchange.getResponseBody().write(response);
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
	public static JSONObject unauthorized(String requestBody) {
		JSONObject requestJSON = new JSONObject();
		String command = "";
		try
		{
			requestJSON = new JSONObject(requestBody);
			command = requestJSON.optString(JsonKey.COMMAND, "");
		}
		catch(JSONException e)
		{
			/**
			 * Do nothing
			 */
		}		
		JSONArray data = new JSONArray();
		requestJSON.put(JsonKey.DATA, data);
		requestJSON.put(JsonKey.COMMAND, command);
		requestJSON.put(JsonKey.RESPONSE_CODE, ResponseCode.UNAUTHORIZED);
		return requestJSON;
	}

	public static boolean isValidRequest(Headers headers) 
	{
		return HandlerAPIMessage.checkValidRequest(headers);
	}
	
	public static boolean checkValidRequest(Map<String, List<String>> requestHeaders) 
    {
		String username = "";
		String password = "";
    	for (Map.Entry<String, List<String>> headers : requestHeaders.entrySet()) 
    	{
			String key = headers.getKey();
			List<String> valueList = headers.getValue();
			for (String value : valueList) 
			{
				if(key.equalsIgnoreCase("authorization") && value.startsWith("Basic "))
				{
					String auth = value.substring(6);
					String decoded = Utility.base64Decode(auth);
					String[] arr = decoded.split(":", 3);
					username = arr[0];
					if(arr.length > 1)
					{
						password = arr[1];
						return ConfigAPIUser.checkUserAuth(username, password);
					}
				}
			}
    	}
    	return false;
	}
}
