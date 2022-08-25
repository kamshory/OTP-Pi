package com.planetbiru;

import java.io.IOException;
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

import com.planetbiru.constant.ConstantString;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.cookie.CookieServer;
import com.planetbiru.device.DeviceAPI;
import com.planetbiru.user.NoUserRegisteredException;
import com.planetbiru.user.User;
import com.planetbiru.user.WebUserAccount;
import com.planetbiru.util.ServerInfo;
import com.planetbiru.util.Utility;

public class ServerWebSocketAdmin extends WebSocketServer{

	private static Collection<WebSocketConnection> clients = new ArrayList<>();
	private User user = new User();
	
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
		try
		{
			JSONObject obj = new JSONObject(message);
			String command = obj.optString(JsonKey.COMMAND, "");
			if(command.equals("broadcast-message"))
			{
				ServerWebSocketAdmin.addSenderInfo(obj, this.user);
				this.broadcastMessage(obj.toString(), conn, true);
			}
		}
		catch(JSONException e)
		{
			/**
			 * Do nothing
			 */
		}
	}

	private static void addSenderInfo(JSONObject obj, User user) {
		if(obj != null && obj.has(JsonKey.DATA) && obj.get(JsonKey.DATA) instanceof JSONArray)
		{
			JSONArray data = obj.getJSONArray(JsonKey.DATA);
			if(data.length() > 0)
			{
				for(int i = 0; i<data.length(); i++)
				{
					JSONObject item = data.getJSONObject(i);
					if(item != null)
					{
						item.put("sender_info", new JSONObject().put("username", user.getUsername()).put("name", user.getName()));
					}
				}
			}
		}
		
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
			User lUser = WebUserAccount.getUser(username);
			if(WebUserAccount.checkUserAuth(lUser, password))
			{
				this.user = lUser;
				ServerWebSocketAdmin.clients.add(new WebSocketConnection(conn, lUser, request, path));
				this.sendServerStatus(ConstantString.SERVICE_ALL, conn);
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
	public void stopService() throws IOException, InterruptedException
	{
		this.stop();
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
	
	public void broadcastMessage(String message, WebSocket sender, boolean exceptMe)
	{
		for(WebSocketConnection client : ServerWebSocketAdmin.clients)
		{
			if(!exceptMe || !client.getConn().equals(sender))
			{
				client.send(message);
			}
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
	
	private void sendServerStatus(String services, WebSocket conn) {
		
		JSONObject info = ServerInfo.buildServerInfo(services);
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
	
	public static void broadcastServerInfo(String services)
	{
		JSONObject info = ServerInfo.buildServerInfo(services);
		broadcastMessage(info.toString());
	}

}
