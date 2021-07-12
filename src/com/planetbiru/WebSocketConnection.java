package com.planetbiru;

import java.util.Map;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import com.planetbiru.util.Utility;

public class WebSocketConnection {

	private WebSocket conn;
	private ClientHandshake request;
	private String path = "/";
	private String sessionID = Utility.sha1(System.nanoTime()+"");

	public WebSocketConnection(WebSocket conn, ClientHandshake request) {
		this.conn = conn;
		this.request = request;
		this.processRequest(request);
	}

	private void processRequest(ClientHandshake request) {
		String requestPath = request.getResourceDescriptor();
		if(requestPath.contains("?"))
		{
			String[] arr = requestPath.split("\\?", 2);
			if(arr.length > 1)
			{
				Map<String, String> query = Utility.parseQueryPairs(arr[1]);
				if(query.containsKey("path"))
				{
					this.path = query.getOrDefault("path", "");
				}
			}
		}
	}

	public void send(String message) {
		this.conn.send(message);
		
	}

	public WebSocket getConn() {
		return conn;
	}

	public void setConn(WebSocket conn) {
		this.conn = conn;
	}

	public ClientHandshake getRequest() {
		return request;
	}

	public void setRequest(ClientHandshake request) {
		this.request = request;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getSessionID() {
		return sessionID;
	}

	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}

}
