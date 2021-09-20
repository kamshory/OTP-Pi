package com.planetbiru.web;

import java.io.IOException;
import java.util.Map;

import org.json.JSONObject;

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
		JSONObject response = new JSONObject();
		JSONObject result = new JSONObject();
		if(action.equals("update"))
		{
			response = GSMUtil.changeIMEI(port, currentIMEI, newIMEI);
			if(response.has("response") && response.optString("response", "").contains("\r\nOK"))
			{
				result.put("last_imei", currentIMEI);
				result.put("imei", newIMEI);
				result.put("response", "OK");
				
			}
			else
			{
				result.put("last_imei", currentIMEI);
				result.put("imei", currentIMEI);
				result.put("response", "ERROR");
			}
		}
		
		return result;
	}
}
