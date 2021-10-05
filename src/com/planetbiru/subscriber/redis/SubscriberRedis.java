package com.planetbiru.subscriber.redis;

import org.json.JSONObject;

import com.planetbiru.ServerWebSocketAdmin;
import com.planetbiru.api.MessageAPI;
import com.planetbiru.buzzer.Buzzer;
import com.planetbiru.config.ConfigSubscriberRedis;
import com.planetbiru.constant.JsonKey;

public class SubscriberRedis extends Thread {
	
	private boolean connected = false;
	private boolean running = true;

	private boolean pong = false;
	private RedisClientThread clientThread;

	@Override
	public void run()
	{
		if(ConfigSubscriberRedis.isSubscriberRedisEnable())
		{
			long sleep = ConfigSubscriberRedis.getSubscriberWsReconnectDelay();
			if(sleep == 0)
			{
				sleep = 10000;
			}
			this.connect();
			do 
			{
				try 
				{
					Thread.sleep(sleep);
				} 
				catch (InterruptedException e) 
				{
					Thread.currentThread().interrupt();
				}
				if(!this.isConnected())
				{
					this.disconnect();
					this.connect();
				}
			}
			while(this.isRunning());
		}
	}

	private void disconnect() {
		if(this.clientThread != null)
		{
			this.clientThread.setRunning(false);	
		}
	}

	private void connect() {
		this.clientThread = new RedisClientThread(this);
		this.clientThread.start();
	}
	public boolean ping(long timeout)
	{
		this.pong = false;
		this.clientThread.ping();
		long start = System.currentTimeMillis();
		long end = 0;
		do {
			try 
			{
				Thread.sleep(50);
			} 
			catch (InterruptedException e) 
			{
				Thread.currentThread().interrupt();
			}
			end = System.currentTimeMillis();
		}
		while(!this.pong && timeout > (end - start));
		if(!this.pong)
		{
			this.setConnected(false);
		}
		return this.pong;
	}
	public void evtOnPong(String pattern)
	{
		this.pong = true;
		this.setConnected(true);
	}
	
	public void evtOnUnsubscribe(String topic, int subscribedChannels) {
		this.setConnected(false);
	}
	
	public void evtOnSubscribe(String topic, int subscribedChannels) {
		this.setConnected(true);
	}
	
	public void evtOnMessage(String topic, String message) {
        MessageAPI api = new MessageAPI();
        JSONObject response = api.processRequest(message, topic);
        JSONObject requestJSON = new JSONObject(message);
        if(requestJSON.optString(JsonKey.COMMAND, "").equals("request-ussd") || requestJSON.optString(JsonKey.COMMAND, "").equals("list-modem"))
        {
        	
        }
	}
	
	public void stopService() {
		this.setRunning(false);	
		this.flagDisconnected();
	}
	
	public void flagDisconnected()
	{
		Buzzer.toneDisconnectRedis();
		this.clientThread.getSubscriber().disconnect();
		this.setConnected(false);
		this.clientThread.setSubscriberRedis(null);
		ConfigSubscriberRedis.setConnected(false);
		ServerWebSocketAdmin.broadcastServerInfo();
	}
	
	public void flagConnected(boolean connected) {
		ConfigSubscriberRedis.setConnected(connected);
		ServerWebSocketAdmin.broadcastServerInfo();
	}

	public boolean isRunning() {
		return this.running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	
	
}
