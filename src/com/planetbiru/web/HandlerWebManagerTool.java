package com.planetbiru.web;

import java.io.IOException;
import java.util.Map;

import org.json.JSONObject;

import com.planetbiru.constant.JsonKey;
import com.planetbiru.gsm.GSMUtil;
import com.planetbiru.user.NoUserRegisteredException;
import com.planetbiru.user.WebUserAccount;
import com.planetbiru.util.Utility;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class HandlerWebManagerTool implements HttpHandler {

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		Headers headers = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		String path = httpExchange.getRequestURI().getPath();
		String method = httpExchange.getRequestMethod();
		byte[] req = HttpUtil.getRequestBody(httpExchange);
		String requestBody = "";
		if(req != null)
		{
			requestBody = new String(req);
		}
		byte[] responseBody = "".getBytes();
		try 
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				if(path.startsWith("/tool/imei") && method.equals("POST"))
				{
					JSONObject response = this.processIMEI(requestBody);
					responseBody = response.toString().getBytes();
				}
				else if(path.startsWith("/tool/sim") && method.equals("POST"))
				{
					JSONObject response = this.processSIM(requestBody);
					responseBody = response.toString().getBytes();
				}
			}
		} 
		catch (NoUserRegisteredException e) 
		{
			/**
			 * Do nothing
			 */
		}
		responseHeaders.add("Content-type", "application/json");
		httpExchange.sendResponseHeaders(HttpStatus.OK, responseBody.length);	
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();
	}

	private JSONObject processIMEI(String requestBody) {
		Map<String, String> queryPairs = Utility.parseQueryPairs(requestBody);
		String action = queryPairs.getOrDefault("action", "");
		String port = queryPairs.getOrDefault("port", "");
		String currentIMEI = queryPairs.getOrDefault("current_imei", "");
		String newIMEI = queryPairs.getOrDefault("new_imei", "");
		JSONObject result = new JSONObject();
		if(action.equals("update"))
		{
			JSONObject response = GSMUtil.changeIMEI(port, currentIMEI, newIMEI);
			if(response != null && response.has(JsonKey.RESPONSE) && response.optString(JsonKey.RESPONSE, "").contains("\r\nOK"))
			{
				result.put("last_imei", currentIMEI);
				result.put("imei", newIMEI);
				result.put(JsonKey.RESPONSE, "OK");
				
			}
			else
			{
				result.put("last_imei", currentIMEI);
				result.put("imei", currentIMEI);
				result.put(JsonKey.RESPONSE, "ERROR");
			}
		}
		
		return result;
	}
	
	private JSONObject processSIM(String requestBody) {
		Map<String, String> queryPairs = Utility.parseQueryPairs(requestBody);
		String action = queryPairs.getOrDefault("action", "");
		String port = queryPairs.getOrDefault("port", "");
		String currentPIN = queryPairs.getOrDefault("current_pin", "");
		String pin1 = queryPairs.getOrDefault("pin1", "");
		String pin2 = queryPairs.getOrDefault("pin2", "");
		JSONObject result = new JSONObject();
		if(action.equals("add-pin") && pin1 != null && pin2 != null && !pin1.isEmpty() && pin1.equals(pin2))
		{
			JSONObject response = GSMUtil.addPIN(port, currentPIN, pin1);
			if(response != null && response.has(JsonKey.RESPONSE) && response.optString(JsonKey.RESPONSE, "").contains("\r\nOK"))
			{
				result.put(JsonKey.RESPONSE, "OK");
				
			}
			else
			{
				result.put(JsonKey.RESPONSE, "ERROR");
			}
		}
		
		return result;
	}
	
}
