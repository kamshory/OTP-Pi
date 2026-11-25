package com.planetbiru.web;

import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

import com.planetbiru.ServerWebSocketAdmin;
import com.planetbiru.constant.JsonKey;

public class HttpBroadcaster extends Thread {

	private String message = "";
	private long delay = 0;

	public HttpBroadcaster(String message) {
		this.message = message;
	}

	public HttpBroadcaster(String message, long delay) {
		this.message = message;
		this.delay = delay;
	}

	@Override
	public void run()
	{
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		this.broadcast();
	}
	
	public void broadcast()
	{
		JSONObject messageJSON = new JSONObject();
		messageJSON.put(JsonKey.COMMAND, "broadcast-message");
		JSONArray data = new JSONArray();
		JSONObject itemData = new JSONObject();
		String uuid = UUID.randomUUID().toString();
		itemData.put(JsonKey.ID, uuid);
		itemData.put(JsonKey.MESSAGE, this.message);
		data.put(itemData);
		messageJSON.put(JsonKey.DATA, data);	
		ServerWebSocketAdmin.broadcastMessage(messageJSON.toString(0));	
	}
}
