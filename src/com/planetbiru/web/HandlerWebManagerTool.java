package com.planetbiru.web;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import org.json.JSONObject;

import com.planetbiru.constant.ConstantString;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.device.DeviceAPI;
import com.planetbiru.gsm.GSMUtil;
import com.planetbiru.user.NoUserRegisteredException;
import com.planetbiru.user.WebUserAccount;
import com.planetbiru.util.Utility;
import com.sun.net.httpserver.Headers; //NOSONAR
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
				if(path.startsWith("/tool/imei") && method.equals(HttpMethod.POST))
				{
					JSONObject response = this.processIMEI(requestBody);
					responseBody = response.toString().getBytes();
				}
				else if(path.startsWith("/tool/sim") && method.equals(HttpMethod.POST))
				{
					JSONObject response = this.processSIM(requestBody);
					responseBody = response.toString().getBytes();
				}
				
			}
			if(path.startsWith("/tool/date-sync") && method.equals(HttpMethod.POST))
			{
				JSONObject response = this.processDateSync(requestBody);
				responseBody = response.toString().getBytes();
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

	private JSONObject processDateSync(String requestBody) {
		Map<String, String> queryPairs = Utility.parseQueryPairs(requestBody);
		String action = queryPairs.getOrDefault("action", ""); //NOSONAR
		String date = queryPairs.getOrDefault("date", "");
		JSONObject result = new JSONObject();
		if(action.equals("update"))
		{
			long clientTimeMills = Utility.atol(date);
			if(clientTimeMills > 0)
			{
				Date clientDate = new Date(clientTimeMills);
			    Calendar clientCalendar = new GregorianCalendar();
			    clientCalendar.setTime(clientDate);
			    
				Date serverDate = new Date();
			    
				if((clientDate.getTime() - serverDate.getTime()) > 86400000 && clientCalendar.get(Calendar.YEAR) > 2020)
				{
					DeviceAPI.updateServerTime(clientDate);
					result.put("set", true);
				}
				else
				{
					result.put("set", false);	
				}
				result.put("response_code", "0000");
			}
		}
		return result;
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
			if(response != null && response.has(JsonKey.RESPONSE) && response.optString(JsonKey.RESPONSE, "").contains(ConstantString.SUBOK))
			{
				result.put("last_imei", currentIMEI);
				result.put("imei", newIMEI);
				result.put(JsonKey.RESPONSE, ConstantString.OK);
				
			}
			else
			{
				result.put("last_imei", currentIMEI);
				result.put("imei", currentIMEI);
				result.put(JsonKey.RESPONSE, ConstantString.ERROR);
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
			if(response != null && response.has(JsonKey.RESPONSE) && response.optString(JsonKey.RESPONSE, "").contains(ConstantString.SUBOK))
			{
				result.put(JsonKey.RESPONSE, ConstantString.OK);
				
			}
			else
			{
				result.put(JsonKey.RESPONSE, ConstantString.ERROR);
			}
		}
		else if(action.equals("remove-pin"))
		{
			JSONObject response = GSMUtil.removePIN(port, currentPIN);
			if(response != null && response.has(JsonKey.RESPONSE) && response.optString(JsonKey.RESPONSE, "").contains(ConstantString.SUBOK))
			{
				result.put(JsonKey.RESPONSE, ConstantString.OK);
				
			}
			else
			{
				result.put(JsonKey.RESPONSE, ConstantString.ERROR);
			}
		}
		
		return result;
	}
	
}
