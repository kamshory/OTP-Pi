package com.planetbiru.web;

import java.io.IOException;
import java.util.Map;

import javax.mail.MessagingException;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.DeviceAPI;
import com.planetbiru.ServerWebSocketAdmin;
import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigEmail;
import com.planetbiru.constant.ConstantString;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.constant.ResponseCode;
import com.planetbiru.gsm.DialUtil;
import com.planetbiru.gsm.GSMException;
import com.planetbiru.gsm.GSMUtil;
import com.planetbiru.gsm.InvalidPortException;
import com.planetbiru.mail.MailUtil;
import com.planetbiru.mail.NoEmailAccountException;
import com.planetbiru.user.NoUserRegisteredException;
import com.planetbiru.user.WebUserAccount;
import com.planetbiru.util.ServerInfo;
import com.planetbiru.util.Utility;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class HandlerWebManagerAPI implements HttpHandler {

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		String path = httpExchange.getRequestURI().getPath();
		String method = httpExchange.getRequestMethod();
		if(method.equals("POST"))
		{
			if(path.startsWith("/api/device"))
			{
				this.modemConnect(httpExchange);
			}
			else if(path.startsWith("/api/internet-dial"))
			{
				this.internetConnect(httpExchange);
			}
			else if(path.startsWith("/api/email"))
			{
				this.sendEmail(httpExchange);
			}
			else if(path.startsWith("/api/sms"))
			{
				this.sendSMS(httpExchange);
			}
			else if(path.startsWith("/api/ussd"))
			{
				this.sendUSSD(httpExchange);
			}
			else if(path.startsWith("/api/reboot"))
			{
				this.reboot(httpExchange);
			}
			else if(path.startsWith("/api/restart"))
			{
				this.restart(httpExchange);
			}
			else if(path.startsWith("/api/cleanup"))
			{
				this.cleanup(httpExchange);
			}
			else if(path.startsWith("/api/expand"))
			{
				this.expandStorage(httpExchange);
			}
		}
	}
	
	//@PostMapping(path="/api/expand")
	public void expandStorage(HttpExchange httpExchange) throws IOException
	{
		Headers responseHeaders = httpExchange.getResponseHeaders();
		int statusCode = HttpStatus.OK;
		DeviceAPI.expand();		

		byte[] responseBody = ServerInfo.getInfo().getBytes();
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();
	}
	
	//@PostMapping(path="/api/reboot")
		public void reboot(HttpExchange httpExchange) throws IOException
		{
			Headers responseHeaders = httpExchange.getResponseHeaders();
			JSONObject jo = new JSONObject();
			jo.put(JsonKey.RESPONSE_CODE, ResponseCode.SUCCESS);
			byte[] responseBody = jo.toString().getBytes();
			int statusCode = HttpStatus.OK;

			responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
			responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
			httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
			httpExchange.getResponseBody().write(responseBody);
			httpExchange.close();
			DeviceAPI.reboot();		
		}
	
	//@PostMapping(path="/api/restart")
	public void restart(HttpExchange httpExchange) throws IOException
	{
		Headers responseHeaders = httpExchange.getResponseHeaders();
		JSONObject jo = new JSONObject();
		jo.put(JsonKey.RESPONSE_CODE, ResponseCode.SUCCESS);
		byte[] responseBody = jo.toString().getBytes();
		int statusCode = HttpStatus.OK;

		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();
		DeviceAPI.restart();		
	}
	
	//@PostMapping(path="/api/cleanup")
	public void cleanup(HttpExchange httpExchange) throws IOException
	{
		Headers responseHeaders = httpExchange.getResponseHeaders();
		JSONObject jo = new JSONObject();
		jo.put(JsonKey.RESPONSE_CODE, ResponseCode.SUCCESS);
		byte[] responseBody = jo.toString().getBytes();
		int statusCode = HttpStatus.OK;
		DeviceAPI.cleanup();		

		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();
	}
	
	
	//@PostMapping(path="/api/device/**")
	public void modemConnect(HttpExchange httpExchange) throws IOException
	{
		byte[] req = HttpUtil.getRequestBody(httpExchange);
		String requestBody = "";
		if(req != null)
		{
			requestBody = new String(req);
		}
		Map<String, String> queryPairs = Utility.parseQueryPairs(requestBody);
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		int statusCode;
		JSONObject responseJSON = new JSONObject();
		statusCode = HttpStatus.OK;
		try 
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				String action = queryPairs.getOrDefault("action", "");
				String modemID = queryPairs.getOrDefault("id", "");
				if(!modemID.isEmpty())
				{
					if(action.equals("connect"))
					{
						GSMUtil.connect(modemID);						
					}
					else
					{
						GSMUtil.disconnect(modemID);
					} 
					ServerInfo.sendModemStatus();
				}
			} 
			else 
			{
				statusCode = HttpStatus.UNAUTHORIZED;
			}
		} 
		catch (GSMException | InvalidPortException e) 
		{
			ServerWebSocketAdmin.broadcastMessage(e.getMessage());
		}
		catch (NoUserRegisteredException e) 
		{
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		byte[] responseBody = responseJSON.toString(4).getBytes();


		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();
	}
	
	//@PostMapping(path="/api/internet-dial/**")
	public void internetConnect(HttpExchange httpExchange) throws IOException
	{
		byte[] req = HttpUtil.getRequestBody(httpExchange);
		String requestBody = "";
		if(req != null)
		{
			requestBody = new String(req);
		}
		Map<String, String> queryPairs = Utility.parseQueryPairs(requestBody);
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		int statusCode;
		JSONObject responseJSON = new JSONObject();
		statusCode = HttpStatus.OK;
		try 
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				String action = queryPairs.getOrDefault("action", "");
				String modemID = queryPairs.getOrDefault("id", "");
				if(!modemID.isEmpty())
				{
					if(action.equals("connect"))
					{
						DialUtil.connect(modemID);						
					}
					else
					{
						DialUtil.disconnect(modemID);
					} 
					ServerInfo.sendModemStatus();
				}
			} 
			else 
			{
				statusCode = HttpStatus.UNAUTHORIZED;
			}
		} 
		catch (NoUserRegisteredException e) 
		{
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		byte[] responseBody = responseJSON.toString(4).getBytes();

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();
	}

	//@PostMapping(path="/api/email**")
	public void sendEmail(HttpExchange httpExchange) throws IOException
	{
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];  
		byte[] req = HttpUtil.getRequestBody(httpExchange);
		String requestBody = "";
		if(req != null)
		{
			requestBody = new String(req);
		}
		Map<String, String> queryPairs = Utility.parseQueryPairs(requestBody);
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		int statusCode;
		JSONObject responseJSON = new JSONObject();
		statusCode = HttpStatus.OK;
		JSONObject response = new JSONObject();
		try 
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				ConfigEmail.load(Config.getEmailSettingPath());
				String id = queryPairs.getOrDefault("id", "").trim();
				String to = queryPairs.getOrDefault("recipient", "").trim();
				String subject = queryPairs.getOrDefault("subject", "").trim();
				String message = queryPairs.getOrDefault(JsonKey.MESSAGE, "").trim();
				String result = "";

				if(id.isEmpty())
				{
					MailUtil.send(to, subject, message, ste);
				}
				else
				{
					MailUtil.send(to, subject, message, ste, id);	
				}
				result = "The message was sent successfuly";
				response.put(JsonKey.SUCCESS, true);
				ServerWebSocketAdmin.broadcastMessage(result);
				response.put(JsonKey.MESSAGE, result);
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;
				response.put(JsonKey.SUCCESS, false);	
				response.put(JsonKey.MESSAGE, ConstantString.UNAUTHORIZED);
			}
		} 
		catch (MessagingException | NoEmailAccountException e) 
		{
			String result = e.getMessage();
			ServerWebSocketAdmin.broadcastMessage(result);
			response.put(JsonKey.SUCCESS, false);
		}
		catch (NoUserRegisteredException e) 
		{
			response.put(JsonKey.SUCCESS, false);
			response.put(JsonKey.MESSAGE, ConstantString.UNAUTHORIZED);
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		byte[] responseBody = responseJSON.toString(4).getBytes();

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();
	}
	
	//@PostMapping(path="/api/sms**")
	public void sendSMS(HttpExchange httpExchange) throws IOException
	{		
		byte[] req = HttpUtil.getRequestBody(httpExchange);
		String requestBody = "";
		if(req != null)
		{
			requestBody = new String(req);
		}
		Map<String, String> queryPairs = Utility.parseQueryPairs(requestBody);
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		int statusCode;
		JSONObject responseJSON = new JSONObject();
		statusCode = HttpStatus.OK;
		JSONObject response = new JSONObject();
		try 
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				String modemID = queryPairs.getOrDefault("modem_id", "");
				String receiver = queryPairs.getOrDefault("receiver", "");
				String message = queryPairs.getOrDefault(JsonKey.MESSAGE, "");
				String result = "";
				GSMUtil.sendSMS(receiver, message, modemID);
				result = "The message was sent via device "+modemID;
				response.put(JsonKey.SUCCESS, false);
				response.put(JsonKey.MESSAGE, result);
			}
			else
			{
				response.put(JsonKey.SUCCESS, false);
				response.put(JsonKey.MESSAGE, ConstantString.UNAUTHORIZED);
				statusCode = HttpStatus.UNAUTHORIZED;
			}
		} 
		catch (GSMException e) 
		{
			String result = e.getMessage();
			response.put(JsonKey.MESSAGE, result);
			response.put(JsonKey.SUCCESS, false);
		}
		catch (NoUserRegisteredException e) 
		{
			statusCode = HttpStatus.UNAUTHORIZED;
			response.put(JsonKey.SUCCESS, false);
			response.put(JsonKey.MESSAGE, ConstantString.UNAUTHORIZED);
		}
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		byte[] responseBody = responseJSON.toString(4).getBytes();

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();
	}

	//@PostMapping(path="/api/ussd**")
	public void sendUSSD(HttpExchange httpExchange) throws IOException
	{		
		byte[] req = HttpUtil.getRequestBody(httpExchange);
		String requestBody = "";
		if(req != null)
		{
			requestBody = new String(req);
		}
		Map<String, String> queryPairs = Utility.parseQueryPairs(requestBody);
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		int statusCode;
		JSONObject responseJSON = new JSONObject();
		statusCode = HttpStatus.OK;
		JSONObject response = new JSONObject();
		try 
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				response = new JSONObject();
				String ussd = queryPairs.getOrDefault("ussd", "");
				String modemID = queryPairs.getOrDefault("modem_id", "");
				String message = "";
				if(ussd != null && !ussd.isEmpty())
				{
					message = GSMUtil.executeUSSD(ussd, modemID);
					response.put(JsonKey.SUCCESS, true);		
				}
				response.put(JsonKey.MESSAGE, message);
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;
				response.put(JsonKey.SUCCESS, false);	
				response.put(JsonKey.MESSAGE, ConstantString.UNAUTHORIZED);
				
			}
		} 
		catch (GSMException e) 
		{
			String message = e.getMessage();
			response.put(JsonKey.MESSAGE, message);
			response.put(JsonKey.SUCCESS, false);	
		}		
		catch (JSONException e)
		{
			response.put(JsonKey.MESSAGE, e.getMessage());
		}
		catch(NoUserRegisteredException e)
		{
			statusCode = HttpStatus.UNAUTHORIZED;		
			response.put(JsonKey.MESSAGE, ConstantString.UNAUTHORIZED);
			response.put(JsonKey.SUCCESS, false);	
		}
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		byte[] responseBody = responseJSON.toString(4).getBytes();

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();
	}

}
