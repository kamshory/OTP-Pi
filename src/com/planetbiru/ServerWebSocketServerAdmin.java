package com.planetbiru;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

public class ServerWebSocketServerAdmin extends WebSocketServer{

	private static Collection<WebSocketConnection> clients = new ArrayList<>();
	public ServerWebSocketServerAdmin(InetSocketAddress address) {
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
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake request) {
		
		String rawCookie = request.getFieldValue("Cookie");
		CookieServer cookie = new CookieServer(rawCookie);
		String username = cookie.getSessionData().optString(JsonKey.USERNAME, "");
		String password = cookie.getSessionData().optString(JsonKey.PASSWORD, "");
		try 
		{
			if(WebUserAccount.checkUserAuth(username, password))
			{
				ServerWebSocketServerAdmin.clients.add(new WebSocketConnection(conn, request));
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
	}
	
	private void remove(WebSocket conn) {
		for(WebSocketConnection client : ServerWebSocketServerAdmin.clients)
		{
			if(client.getConn().equals(conn))
			{
				ServerWebSocketServerAdmin.clients.remove(client);
				break;
			}
		}		
	}
	
	public static void broadcastMessage(String message)
	{
		for(WebSocketConnection client : ServerWebSocketServerAdmin.clients)
		{
			client.send(message);
		}
	}
	public static void broadcastMessage(String message, String path)
	{
		for(WebSocketConnection client : ServerWebSocketServerAdmin.clients)
		{
			if(client.getPath().contains(path))
			{
				client.send(message);
			}
		}
	}
	public static void broadcastMessage(String message, WebSocket sender)
	{
		for(WebSocketConnection client : ServerWebSocketServerAdmin.clients)
		{
			if(!client.getConn().equals(sender))
			{
				client.send(message);
			}
		}
	}
	
	private void sendServerStatus(WebSocket conn) {
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

	
	
	
	
	

}
