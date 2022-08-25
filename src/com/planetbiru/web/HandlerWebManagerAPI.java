package com.planetbiru.web;

import java.io.IOException;
import java.util.Map;

import javax.mail.MessagingException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.App;
import com.planetbiru.ServerWebSocketAdmin;
import com.planetbiru.buzzer.Music;
import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigEmail;
import com.planetbiru.config.ConfigModem;
import com.planetbiru.constant.ConstantString;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.constant.ResponseCode;
import com.planetbiru.device.DeviceAPI;
import com.planetbiru.gsm.InternetDialUtil;
import com.planetbiru.gsm.GSMException;
import com.planetbiru.gsm.GSMUtil;
import com.planetbiru.gsm.InvalidPortException;
import com.planetbiru.gsm.InvalidSIMPinException;
import com.planetbiru.gsm.SerialPortConnectionException;
import com.planetbiru.gsm.USSDParser;
import com.planetbiru.mail.MailUtil;
import com.planetbiru.mail.NoEmailAccountException;
import com.planetbiru.user.NoUserRegisteredException;
import com.planetbiru.user.WebUserAccount;
import com.planetbiru.util.ServerInfo;
import com.planetbiru.util.Utility;
import com.sun.net.httpserver.Headers; //NOSONAR
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.apache.log4j.Logger;

public class HandlerWebManagerAPI implements HttpHandler {
	
	private static Logger logger = Logger.getLogger(HandlerWebManagerAPI.class);

	@Override
	public void handle(HttpExchange httpExchange) throws IOException //NOSONAR
	{
		String path = httpExchange.getRequestURI().getPath();
		String method = httpExchange.getRequestMethod();
		if(method.equals(HttpMethod.POST))
		{
			if(path.startsWith("/api/device"))
			{
				this.modemTool(httpExchange);
			}
			else if(path.startsWith("/api/internet-dial"))
			{
				this.internetConnect(httpExchange);
			}
			else if(path.startsWith("/api/modem-sms"))
			{
				this.modemChangeState(httpExchange, ConstantString.SMS);
			}
			else if(path.startsWith("/api/modem-internet"))
			{
				this.modemChangeState(httpExchange, ConstantString.INTERNET);
			}
			else if(path.startsWith("/api/modem"))
			{
				this.modemChangeState(httpExchange, ConstantString.ALL);
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
			else if(path.startsWith("/api/subscriber-ws"))
			{
				this.subscriberWS(httpExchange);
			}
			else if(path.startsWith("/api/subscriber-amqp"))
			{
				this.subscriberAMQP(httpExchange);
			}
			else if(path.startsWith("/api/subscriber-mqtt"))
			{
				this.subscriberMQTT(httpExchange);
			}
			else if(path.startsWith("/api/subscriber-activemq"))
			{
				this.subscriberActiveMQ(httpExchange);
			}
			else if(path.startsWith("/api/subscriber-stomp"))
			{
				this.subscriberStomp(httpExchange);
			}
			else if(path.startsWith("/api/subscriber-redis"))
			{
				this.subscriberRedis(httpExchange);
			}
			else if(path.startsWith("/api/subscriber-https"))
			{
				this.subscriberHTTPS(httpExchange);
			}
			else if(path.startsWith("/api/subscriber-http"))
			{
				this.subscriberHTTP(httpExchange);
			}
			else if(path.startsWith("/api/delete/sms"))
			{
				this.deleteSMS(httpExchange);
			}
			else if(path.startsWith("/api/bell/test"))
			{
				this.testBell(httpExchange);
			}
			else if(path.startsWith("/api/tone"))
			{
				this.testTone(httpExchange);
			}						
		}
	}
	
	//@PostMapping(path="/api/tone")
	public void testTone(HttpExchange httpExchange) throws IOException
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
				String action = queryPairs.getOrDefault(JsonKey.ACTION, "");
				String song = queryPairs.getOrDefault("song", "");
				int octave = Utility.atoi(queryPairs.getOrDefault("octave", "0"));
				int tempo = Utility.atoi(queryPairs.getOrDefault("tempo", "0"));
				if(action.equals("play"))
				{
					Music.play(Config.getSoundPIN(), song, octave, tempo);
				}
				else if(action.equals("stop"))
				{
					Music.stop(Config.getSoundPIN());
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
		byte[] responseBody = responseJSON.toString(0).getBytes();
		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();
	}
	//@PostMapping(path="/api/bell/test")
	public void testBell(HttpExchange httpExchange) throws IOException
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
				String action = queryPairs.getOrDefault(JsonKey.ACTION, "");
				if(action.equals("ring"))
				{
					Music.play(Config.getSoundPIN(), Config.getSoundTestTone(), Config.getSoundTestOctave(), Config.getSoundTestTempo());
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
		byte[] responseBody = responseJSON.toString(0).getBytes();
		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();
	}
	//@PostMapping(path="/api/delete/sms")
	public void deleteSMS(HttpExchange httpExchange) throws IOException
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
				String action = queryPairs.getOrDefault(JsonKey.ACTION, "");
				if(action.equals("delete-sms"))
				{
					String modemID = queryPairs.getOrDefault(JsonKey.MODEM_ID, "");					
					String storage = queryPairs.getOrDefault("storage", "");					
					int smsID = Utility.atoi(queryPairs.getOrDefault("sms_id", "0"));	
					GSMUtil.get(modemID).deleteSMS(smsID, storage);
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
		catch (GSMException | InvalidSIMPinException | SerialPortConnectionException e) 
		{
			logger.error(e.getMessage(), e);
		}
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		byte[] responseBody = responseJSON.toString(0).getBytes();

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();
	}
	//@PostMapping(path="/api/subscriber-ws")
	public void subscriberWS(HttpExchange httpExchange) throws IOException
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
				String action = queryPairs.getOrDefault(JsonKey.ACTION, "");
				if(action.equals(JsonKey.START))
				{
					App.subscriberWSStart();					
				}
				else
				{
					App.subscriberWSStop(true);
				} 
				ServerWebSocketAdmin.broadcastServerInfo(ConstantString.SERVICE_WS);
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
		byte[] responseBody = responseJSON.toString(0).getBytes();

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();
	}
	
	//@PostMapping(path="/api/subscriber-amqp")
	public void subscriberAMQP(HttpExchange httpExchange) throws IOException
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
				String action = queryPairs.getOrDefault(JsonKey.ACTION, "");
				if(action.equals(JsonKey.START))
				{
					App.subscriberAMQPStart();				
				}
				else
				{
					App.subscriberAMQPStop(true);
				} 
				ServerWebSocketAdmin.broadcastServerInfo(ConstantString.SERVICE_AMQP);
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
		byte[] responseBody = responseJSON.toString(0).getBytes();

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();
	}
	
	//@PostMapping(path="/api/subscriber-redis")
	public void subscriberRedis(HttpExchange httpExchange) throws IOException
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
				String action = queryPairs.getOrDefault(JsonKey.ACTION, "");
				if(action.equals(JsonKey.START))
				{
					App.subscriberRedisStart();				
				}
				else
				{
					App.subscriberRedisStop(true);
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
		byte[] responseBody = responseJSON.toString(0).getBytes();

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();
	}
	//@PostMapping(path="/api/subscriber-redisson")
	public void subscriberStomp(HttpExchange httpExchange) throws IOException
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
				String action = queryPairs.getOrDefault(JsonKey.ACTION, "");
				if(action.equals(JsonKey.START))
				{
					App.subscriberStompStart();
				}
				else
				{
					App.subscriberStompStop(true);
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
		byte[] responseBody = responseJSON.toString(0).getBytes();

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();
	}
	//@PostMapping(path="/api/subscriber-mqtt")
	public void subscriberMQTT(HttpExchange httpExchange) throws IOException
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
				String action = queryPairs.getOrDefault(JsonKey.ACTION, "");
				if(action.equals(JsonKey.START))
				{
					App.subscriberMQTTStart();				
				}
				else
				{
					App.subscriberMQTTStop(true);
				}
				ServerWebSocketAdmin.broadcastServerInfo(ConstantString.SERVICE_MQTT);
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
		byte[] responseBody = responseJSON.toString(0).getBytes();

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();
	}
	
	//@PostMapping(path="/api/subscriber-activemq")
	public void subscriberActiveMQ(HttpExchange httpExchange) throws IOException
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
				String action = queryPairs.getOrDefault(JsonKey.ACTION, "");
				if(action.equals(JsonKey.START))
				{
					App.subscriberActiveMQStart();			
				}
				else
				{
					App.subscriberActiveMQStop(true);
				}
				ServerWebSocketAdmin.broadcastServerInfo(ConstantString.SERVICE_ACTIVEMQ);
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
		byte[] responseBody = responseJSON.toString(0).getBytes();

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();
	}
	
	//@PostMapping(path="/api/subscriber-http")
	public void subscriberHTTP(HttpExchange httpExchange) throws IOException
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
				String action = queryPairs.getOrDefault(JsonKey.ACTION, "");
				if(action.equals(JsonKey.START))
				{
					App.subscriberHTTPStart();				
				}
				else
				{
					App.subscriberHTTPStop(true);
				}
				ServerWebSocketAdmin.broadcastServerInfo(ConstantString.SERVICE_HTTP);
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
		byte[] responseBody = responseJSON.toString(0).getBytes();

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();
	}
	
	//@PostMapping(path="/api/subscriber-https")
	public void subscriberHTTPS(HttpExchange httpExchange) throws IOException
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
				String action = queryPairs.getOrDefault(JsonKey.ACTION, "");
				if(action.equals(JsonKey.START))
				{
					App.subscriberHTTPSStart();				
				}
				else
				{
					App.subscriberHTTPSStop(true);
				}
				ServerWebSocketAdmin.broadcastServerInfo(ConstantString.SERVICE_HTTPS);
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
		byte[] responseBody = responseJSON.toString(0).getBytes();

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();
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
	//@PostMapping(path="/api/modem")
	private void modemChangeState(HttpExchange httpExchange, String modemFunction) throws IOException {
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
				responseJSON = this.modemAction(queryPairs, modemFunction);
				ServerInfo.sendModemStatus();			
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
		byte[] responseBody = responseJSON.toString(0).getBytes();

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();
		
	}
	private JSONObject modemAction(Map<String, String> queryPairs, String modemFunction) {
		JSONObject result = new JSONObject();
		String action = queryPairs.getOrDefault(JsonKey.ACTION, "");
		if(action.equals(ConstantString.CONNECT))
		{
			if(modemFunction.equals(ConstantString.SMS) || modemFunction.equals(ConstantString.ALL))
			{
				App.modemSMSStart();
			}
			if(modemFunction.equals(ConstantString.INTERNET) || modemFunction.equals(ConstantString.ALL))
			{
				App.modemInternetStart();
			}
		}
		else
		{
			if(modemFunction.equals(ConstantString.SMS) || modemFunction.equals(ConstantString.ALL))
			{
				App.modemSMSStop();
			}
			if(modemFunction.equals(ConstantString.INTERNET) || modemFunction.equals(ConstantString.ALL))
			{
				App.modemInternetStop();
			}
		} 
		return result;
	}

	//@PostMapping(path="/api/device/**")
	public void modemTool(HttpExchange httpExchange) throws IOException
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
		String modemID = queryPairs.getOrDefault("id", "");
		int statusCode;
		JSONObject responseJSON = new JSONObject();
		statusCode = HttpStatus.OK;
		try 
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				responseJSON = this.deviceAction(queryPairs, modemID);
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
		catch (SerialPortConnectionException e) 
		{
			try 
			{
				GSMUtil.reconnectModem(modemID);
				responseJSON.put("errorMessage", e.getMessage());
			} 
			catch (GSMException e1) 
			{
				responseJSON.put("errorMessage", e1.getMessage());
			}
			responseJSON.put("status", "ERROR");
		}
		catch (NoUserRegisteredException e) 
		{
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		byte[] responseBody = responseJSON.toString(0).getBytes();

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();
	}
	
	private JSONObject deviceAction(Map<String, String> queryPairs, String modemID) throws GSMException, InvalidPortException, SerialPortConnectionException
	{
		JSONObject responseJSON = new JSONObject();
		String action = queryPairs.getOrDefault(JsonKey.ACTION, "");
		if(!modemID.isEmpty())
		{
			if(action.equals(ConstantString.CONNECT))
			{
				GSMUtil.connect(modemID);						
				ServerInfo.sendModemStatus();
			}
			else if(action.equals(ConstantString.DISCONNECT))
			{
				GSMUtil.disconnect(modemID);
				ServerInfo.sendModemStatus();
			} 
			else if(action.equals("request-signal-strength"))
			{
				ConfigModem.setLastRequestSignalStrength(System.currentTimeMillis());
				ServerInfo.sendModemStatus();
			} 
			else if(action.equals("test-at"))
			{
				JSONObject resp = GSMUtil.testAT(modemID);				
				JSONArray data = new JSONArray();
				JSONObject item = new JSONObject();
				String message = "";
				if(resp.optString("result", "").contains("OK"))
				{
					message = "Devive is connected properly";
				}
				else
				{
					message = "Devive is not connected properly";
				}
				responseJSON.put(JsonKey.COMMAND, "broadcast-message");
				item.put("message", message);				
				data.put(item);
				responseJSON.put(JsonKey.DATA, data);
			} 
		}
		else
		{
			if(action.equals(ConstantString.CONNECT))
			{
				App.modemSMSStart();
				App.modemInternetStart();
			}
			else if(action.equals("request-signal-strength"))
			{
				ConfigModem.setLastRequestSignalStrength(System.currentTimeMillis());
			}
			else
			{
				App.modemSMSStop();
				App.modemInternetStop();
			}
		}
		return responseJSON;
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
				String action = queryPairs.getOrDefault(JsonKey.ACTION, "");
				String modemID = queryPairs.getOrDefault("id", "");
				if(!modemID.isEmpty())
				{
					if(action.equals(ConstantString.CONNECT))
					{
						InternetDialUtil.connect(modemID);						
					}
					else
					{
						InternetDialUtil.disconnect(modemID);
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
		byte[] responseBody = responseJSON.toString(0).getBytes();

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
				response.put(JsonKey.MESSAGE, result);
				HttpUtil.broardcastWebSocket(result);
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
			String message = e.getMessage();
			HttpUtil.broardcastWebSocket(message);
			response.put(JsonKey.SUCCESS, false);
			HttpUtil.broardcastWebSocket(message);
		}
		catch (NoUserRegisteredException e) 
		{
			response.put(JsonKey.SUCCESS, false);
			response.put(JsonKey.MESSAGE, ConstantString.UNAUTHORIZED);
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		byte[] responseBody = responseJSON.toString(0).getBytes();

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
				String modemID = queryPairs.getOrDefault(JsonKey.MODEM_ID, "");
				String receiver = queryPairs.getOrDefault("receiver", "");
				String message = queryPairs.getOrDefault(JsonKey.MESSAGE, "");
				String result = "";
				response = GSMUtil.sendSMS(receiver, message, modemID);
				if(response.optString("result", "").contains("SUCCESS"))
				{
					result = "The message was sent via device "+modemID;
				}
				else
				{
					result = "The message was not sent via device "+modemID;
				}
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
			String message = e.getMessage();
			response.put(JsonKey.MESSAGE, message);
			response.put(JsonKey.SUCCESS, false);
			HttpUtil.broardcastWebSocket(message);
		}
		catch (NoUserRegisteredException e) 
		{
			statusCode = HttpStatus.UNAUTHORIZED;
			response.put(JsonKey.SUCCESS, false);
			response.put(JsonKey.MESSAGE, ConstantString.UNAUTHORIZED);
		} 
		catch (InvalidSIMPinException e) 
		{
			String message = e.getMessage();
			statusCode = HttpStatus.UNAUTHORIZED;
			response.put(JsonKey.SUCCESS, false);
			response.put(JsonKey.MESSAGE, ConstantString.SIM_CARD_NOT_READY);
			HttpUtil.broardcastWebSocket(message);
		}
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		byte[] responseBody = responseJSON.toString(0).getBytes();

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
		boolean replyable = false;
		try 
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				response = new JSONObject();
				String ussdCode = queryPairs.getOrDefault("ussd", "");
				String modemID = queryPairs.getOrDefault(JsonKey.MODEM_ID, "");
				String message = "";
				if(ussdCode != null && !ussdCode.isEmpty())
				{
					USSDParser ussd = GSMUtil.executeUSSD(ussdCode, modemID);
					message = ussd.getContent();
					replyable = ussd.isReplyable();
					response.put(JsonKey.SUCCESS, true);		
				}
				response.put(JsonKey.MESSAGE, message);
				response.put(JsonKey.REPLYABLE, replyable);
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;
				response.put(JsonKey.SUCCESS, false);	
				response.put(JsonKey.MESSAGE, ConstantString.UNAUTHORIZED);
				response.put(JsonKey.REPLYABLE, replyable);				
			}
		} 
		catch (GSMException e) 
		{
			String message = e.getMessage();
			response.put(JsonKey.MESSAGE, message);
			response.put(JsonKey.SUCCESS, false);	
			HttpUtil.broardcastWebSocket(message);
		}		
		catch (JSONException e)
		{
			String message = e.getMessage();
			response.put(JsonKey.MESSAGE, message);
			HttpUtil.broardcastWebSocket(message);
		}
		catch(NoUserRegisteredException e)
		{
			String message = e.getMessage();
			statusCode = HttpStatus.UNAUTHORIZED;		
			response.put(JsonKey.MESSAGE, ConstantString.UNAUTHORIZED);
			response.put(JsonKey.SUCCESS, false);
			HttpUtil.broardcastWebSocket(message);
		}
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		byte[] responseBody = responseJSON.toString(0).getBytes();
		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();
	}

}
