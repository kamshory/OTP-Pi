package com.planetbiru.web;

import java.io.IOException;
import java.util.Map;

import org.json.JSONObject;

import com.planetbiru.config.Config;
import com.planetbiru.constant.ConstantString;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.cookie.CookieServer;
import com.planetbiru.user.NoUserRegisteredException;
import com.planetbiru.user.WebUserAccount;
import com.planetbiru.util.Utility;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class HandlerWebManagerLogin implements HttpHandler {

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		
		CookieServer cookie = new CookieServer(requestHeaders, Config.getSessionName(), Config.getSessionLifetime());
		byte[] req = HttpUtil.getRequestBody(httpExchange);
		String requestBody = "";
		if(req != null)
		{
			requestBody = new String(req);
		}
		Map<String, String> queryPairs = Utility.parseQueryPairs(requestBody);
	    
	    String username = queryPairs.getOrDefault(JsonKey.USERNAME, "");
	    String password = queryPairs.getOrDefault(JsonKey.PASSWORD, "");
	    String next = queryPairs.getOrDefault(JsonKey.NEXT, "");
	    
	    if(next.isEmpty())
		{
	    	next = "/";
		}
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
	    responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
	    
	    JSONObject res = new JSONObject();
	    JSONObject payload = new JSONObject();
	    
		cookie.setSessionValue(JsonKey.USERNAME, username);
		cookie.setSessionValue(JsonKey.PASSWORD, password);
		
		byte[] responseBody = "".getBytes();
		int statusCode = HttpStatus.OK;
		try
		{
			WebUserAccount.load(Config.getUserSettingPath());
			if(WebUserAccount.checkUserAuth(username, password))
			{
				WebUserAccount.updateLastActive(username);
				WebUserAccount.save();
			    payload.put(JsonKey.NEXT_URL, next);
			    res.put(JsonKey.CODE, 0);
			    res.put(JsonKey.PAYLOAD, payload);
				responseBody = res.toString().getBytes();
			}
			else
			{
			    payload.put(JsonKey.NEXT_URL, "/");
			    res.put(JsonKey.CODE, 0);
			    res.put(JsonKey.PAYLOAD, payload);
				responseBody = res.toString().getBytes();				
			}
			cookie.saveSessionData();
			cookie.putToHeaders(responseHeaders);		
		}
		catch(NoUserRegisteredException e)
		{
		    payload.put(JsonKey.NEXT_URL, "/admin-init.html");
		    res.put(JsonKey.CODE, 0);
		    res.put(JsonKey.PAYLOAD, payload);
			responseBody = res.toString().getBytes();				
		}	
		
		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();
	}

}
