package com.planetbiru.web;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.config.Config;
import com.planetbiru.constant.ConstantString;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.cookie.CookieServer;
import com.planetbiru.device.ConfigActivation;
import com.planetbiru.device.DeviceActivation;
import com.planetbiru.user.NoUserRegisteredException;
import com.planetbiru.user.WebUserAccount;
import com.planetbiru.util.CustomHttpClient;
import com.planetbiru.util.HttpRequestException;
import com.planetbiru.util.HttpResponseString;
import com.planetbiru.util.ServerInfo;
import com.planetbiru.util.Utility;
import com.sun.net.httpserver.Headers; //NOSONAR
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class HandlerWebManagerActivation implements HttpHandler {
	
	private static Logger logger = Logger.getLogger(HandlerWebManagerActivation.class);

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		CookieServer cookie = new CookieServer(requestHeaders, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		int statusCode = HttpStatus.PERMANENT_REDIRECT;
		try
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				byte[] req = HttpUtil.getRequestBody(httpExchange);
				String requestBody = "";
				if(req != null)
				{
					requestBody = new String(req);
				}
				this.processActivation(requestBody);
				if(DeviceActivation.isActivated())
				{
					responseHeaders.add(ConstantString.LOCATION, "/activated.html");
				}
				else
				{
					responseHeaders.add(ConstantString.LOCATION, "/activation.html");					
				}
			}
			cookie.saveSessionData();
			cookie.putToHeaders(responseHeaders);		
		}
		catch(NoUserRegisteredException e)
		{
			responseHeaders.add(ConstantString.LOCATION, "/login.html");
		}	
		
		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();
	}
	
	public void processActivation(String requestBody) {
		String username = ConfigActivation.getUsername();
		String password = ConfigActivation.getPassword();
		String method = ConfigActivation.getMethod();
		String url = ConfigActivation.getUrl();
		String contentType = ConfigActivation.getContentType();
		String authorization = ConfigActivation.getAuthorization();
		int timeout = ConfigActivation.getRequestTimeout();		
		
		Map<String, String> queryPairs = Utility.parseQueryPairs(requestBody);					
		Map<String, String> parameters = null;
		
		String cpusn = ServerInfo.cpuSerialNumber();

		Headers requestHeaders = new Headers();
		requestHeaders.add(ConstantString.ACCEPT, contentType);
		requestHeaders.add(ConstantString.AUTHORIZATION, authorization+" "+Utility.base64Encode(username+":"+password));
		String requestBodyToSent = "";
		
		if(method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT))
		{
			if(contentType.toLowerCase().contains("json"))
			{
				JSONObject requestJSON = new JSONObject(queryPairs);
				requestJSON.put(JsonKey.CPUSN, cpusn);
				requestBodyToSent = requestJSON.toString(0);
				requestHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
			}
			else
			{
				requestBodyToSent = requestBody;
			}
		}
		else if(method.equals(HttpMethod.GET))
		{
			requestBodyToSent = null;			
			parameters = queryPairs;
			parameters.put(JsonKey.CPUSN, cpusn);
		}
		
		JSONObject responseJSON = new JSONObject();
		try 
		{
			HttpResponseString response = CustomHttpClient.httpExchange(method, url, parameters, requestHeaders, requestBodyToSent, timeout);
			responseJSON = new JSONObject(response.body());
			if(responseJSON.has(JsonKey.DATA) && responseJSON.getJSONObject(JsonKey.DATA) != null)
			{
				JSONObject data = responseJSON.optJSONObject(JsonKey.DATA);
				String activationCode = data.optString("activation_code", "");			
				DeviceActivation.verify(activationCode, cpusn);
				if(DeviceActivation.isActivated())
				{
					DeviceActivation.storeToEnv(activationCode);
					DeviceActivation.activate(activationCode);
				}
			}
			
		} 
		catch (JSONException | HttpRequestException | IllegalArgumentException | InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidAlgorithmParameterException | BadPaddingException | IllegalBlockSizeException e) 
		{
			logger.info(e.getMessage());
		}
	}


}
