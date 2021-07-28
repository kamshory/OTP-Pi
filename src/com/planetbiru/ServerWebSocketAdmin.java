package com.planetbiru;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.config.ConfigAPI;
import com.planetbiru.config.ConfigFeederAMQP;
import com.planetbiru.config.ConfigFeederWS;
import com.planetbiru.config.ConfigModem;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.cookie.CookieServer;
import com.planetbiru.gsm.GSMUtil;
import com.planetbiru.user.NoUserRegisteredException;
import com.planetbiru.user.WebUserAccount;
import com.planetbiru.util.Utility;

public class ServerWebSocketAdmin extends WebSocketServer{

	private static Collection<WebSocketConnection> clients = new ArrayList<>();
	public ServerWebSocketAdmin(InetSocketAddress address) {
		super(address);
	}

	@Override
	public void onClose(WebSocket conn, int code, String message, boolean arg3) {
		this.remove(conn);		
	}

	@Override
	public void onError(WebSocket conn, Exception e) {
		this.remove(conn);
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		/**
		 * Do nothing
		 */
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake request) {
		String rawCookie = request.getFieldValue("Cookie");
		Map<String, String> query = this.getQuery(request);
		CookieServer cookie = new CookieServer(rawCookie);		
		
		String username = cookie.getSessionValue(JsonKey.USERNAME, "");
		String password = cookie.getSessionValue(JsonKey.PASSWORD, "");
		
		String path = "";
		if(query.containsKey("path"))
		{
			path = query.getOrDefault("path", "");
		}
		if(query.containsKey("time"))
		{
			String time = query.getOrDefault("time", "0");
			long clientTimeMills = Utility.atol(time);
			if(clientTimeMills > 0)
			{
				Date clientDate = new Date(clientTimeMills);
			    Calendar clientCalendar = new GregorianCalendar();
			    clientCalendar.setTime(clientDate);
			    
				Date serverDate = new Date();
			    
				if((clientDate.getTime() - serverDate.getTime()) > 86400000 && clientCalendar.get(Calendar.YEAR) > 2020)
				{
					DeviceAPI.updateServerTime(clientDate);
				}
			}
		}
		
		try 
		{
			if(WebUserAccount.checkUserAuth(username, password))
			{
				ServerWebSocketAdmin.clients.add(new WebSocketConnection(conn, request, path));
				this.sendServerStatus(conn);
			}
			else
			{
				conn.close();
			}
		} 
		catch (NoUserRegisteredException e) 
		{
			conn.close();
		}
	}
	
	@Override
	public void onStart() {
		/**
		 * Do nothing
		 */
	}
	
	private void remove(WebSocket conn) {
		for(WebSocketConnection client : ServerWebSocketAdmin.clients)
		{
			if(client.getConn().equals(conn))
			{
				ServerWebSocketAdmin.clients.remove(client);
				break;
			}
		}		
	}
	
	public static void broadcastMessage(String message)
	{
		for(WebSocketConnection client : ServerWebSocketAdmin.clients)
		{
			client.send(message);
		}
	}
	
	public static void broadcastMessage(String message, String path)
	{
		for(WebSocketConnection client : ServerWebSocketAdmin.clients)
		{
			if(client.getPath().contains(path))
			{
				client.send(message);
			}
		}
	}
	public static void broadcastMessage(String message, WebSocket sender)
	{
		for(WebSocketConnection client : ServerWebSocketAdmin.clients)
		{
			if(!client.getConn().equals(sender))
			{
				client.send(message);
			}
		}
	}
	
	private Map<String, String> getQuery(ClientHandshake request) {
		Map<String, String> query = new HashMap<>();
		String requestPath = request.getResourceDescriptor();
		if(requestPath.contains("?"))
		{
			String[] arr = requestPath.split("\\?", 2);
			if(arr.length > 1)
			{
				query = Utility.parseQueryPairs(arr[1]);
			}
		}
		return query;
	}
	
	private static JSONObject buildServerInvo()
	{
		JSONArray data = new JSONArray();
		JSONObject info = new JSONObject();
		
		JSONObject modem = new JSONObject();
		modem.put(JsonKey.NAME, "otp-modem-connected");
		modem.put(JsonKey.VALUE, GSMUtil.isConnected());
		modem.put(JsonKey.DATA, ConfigModem.getStatus());
		data.put(modem);
		JSONObject wsEnable = new JSONObject();
		wsEnable.put(JsonKey.NAME, "otp-ws-enable");
		wsEnable.put(JsonKey.VALUE, ConfigFeederWS.isFeederWsEnable());
		data.put(wsEnable);
		
		JSONObject wsConnected = new JSONObject();
		wsConnected.put(JsonKey.NAME, "otp-ws-connected");
		wsConnected.put(JsonKey.VALUE, ConfigFeederWS.isConnected());
		data.put(wsConnected);
		
		JSONObject amqpEnable = new JSONObject();
		amqpEnable.put(JsonKey.NAME, "otp-amqp-enable");
		amqpEnable.put(JsonKey.VALUE, ConfigFeederAMQP.isFeederAmqpEnable());
		data.put(amqpEnable);
		
		JSONObject amqpConnected = new JSONObject();
		amqpConnected.put(JsonKey.NAME, "otp-amqp-connected");
		amqpConnected.put(JsonKey.VALUE, ConfigFeederAMQP.isConnected());
		data.put(amqpConnected);
		
		JSONObject httpEnable = new JSONObject();
		httpEnable.put(JsonKey.NAME, "otp-http-enable");
		httpEnable.put(JsonKey.VALUE, ConfigAPI.isHttpEnable() || ConfigAPI.isHttpsEnable());
		data.put(httpEnable);
		
		info.put("command", "server-info");
		info.put("data", data);
		return info;
	}
	
	private void sendServerStatus(WebSocket conn) {
		
		JSONObject info = buildServerInvo();
		try 
		{
			conn.send(info.toString());
		} 
		catch (JSONException e) 
		{
			/**
			 * Do nothing
			 */
		}	
	}
	
	public static void broadcastServerInfo()
	{
		JSONObject info = buildServerInvo();
		broadcastMessage(info.toString());
	}

}
