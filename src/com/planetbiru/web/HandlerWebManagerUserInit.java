package com.planetbiru.web;

import java.io.IOException;
import java.util.Map;

import org.json.JSONObject;

import com.planetbiru.config.Config;
import com.planetbiru.constant.ConstantString;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.cookie.CookieServer;
import com.planetbiru.user.NoUserRegisteredException;
import com.planetbiru.user.User;
import com.planetbiru.user.WebUserAccount;
import com.planetbiru.util.Utility;
import com.sun.net.httpserver.Headers; //NOSONAR
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class HandlerWebManagerUserInit implements HttpHandler {

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		CookieServer cookie = new CookieServer(requestHeaders, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		int statusCode = HttpStatus.MOVED_PERMANENTLY;
		if(WebUserAccount.isEmpty())
		{
			byte[] req = HttpUtil.getRequestBody(httpExchange);
			String requestBody = "";
			if(req != null)
			{
				requestBody = new String(req);
			}
			Map<String, String> queryPairs = Utility.parseQueryPairs(requestBody);		
		    String username = queryPairs.getOrDefault(JsonKey.USERNAME, "").trim();
		    String email = queryPairs.getOrDefault(JsonKey.EMAIL, "").trim();
		    String password = Utility.hashPasswordGenerator(queryPairs.getOrDefault(JsonKey.PASSWORD, ""));
		    String name = queryPairs.getOrDefault(JsonKey.NAME, "").trim();
		    String phone = queryPairs.getOrDefault(JsonKey.PHONE, "").trim();

			if(!username.isEmpty() && !name.isEmpty() && !phone.isEmpty() && password.length() >= 6)
			{
			    JSONObject jsonObject = new JSONObject();
				jsonObject.put(JsonKey.USERNAME, username);
				jsonObject.put(JsonKey.NAME, name);
				jsonObject.put(JsonKey.EMAIL, email);
				jsonObject.put(JsonKey.PASSWORD, password);
				jsonObject.put(JsonKey.PHONE, phone);
				jsonObject.put(JsonKey.BLOCKED, false);
				jsonObject.put(JsonKey.ACTIVE, true);
				
				WebUserAccount.addUser(new User(jsonObject));		
				WebUserAccount.save();				
				
				cookie.setSessionValue(JsonKey.USERNAME, username);
				cookie.setSessionValue(JsonKey.PASSWORD, password);
				try
				{
					WebUserAccount.load(Config.getUserSettingPath());
					if(WebUserAccount.checkUserAuth(username, password))
					{
						WebUserAccount.updateLastActive(username);
						WebUserAccount.save();
					}
				}
				catch(NoUserRegisteredException e)
				{
					/**
					 * Do nothing
					 */
				}			
				cookie.saveSessionData();
				cookie.putToHeaders(responseHeaders);
				
			}		    
		}
		
		responseHeaders.add(ConstantString.LOCATION, ConstantString.ADMIN_FILE_LEVEL_3);
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();
	}

}
