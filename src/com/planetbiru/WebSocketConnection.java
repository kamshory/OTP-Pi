package com.planetbiru;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import com.planetbiru.user.User;
import com.planetbiru.util.Utility;

public class WebSocketConnection {

	private WebSocket conn;
	private ClientHandshake request;
	private String path = "/";
	private String sessionID = Utility.sha1(System.nanoTime()+"");
	private User user = new User();

	public WebSocketConnection(WebSocket conn, ClientHandshake request, String path) {
		this.conn = conn;
		this.request = request;
		this.path = path;
	}

	public WebSocketConnection(WebSocket conn, User user, ClientHandshake request, String path) {
		this.conn = conn;
		this.user = user;
		this.request = request;
		this.path = path;
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

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
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
