package com.planetbiru.subscriber.redis;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

import org.json.JSONObject;

import com.planetbiru.ServerWebSocketAdmin;
import com.planetbiru.api.MessageAPI;
import com.planetbiru.buzzer.Buzzer;
import com.planetbiru.config.ConfigSubscriberRedis;
import com.planetbiru.constant.ConstantString;
import com.planetbiru.constant.JsonKey;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

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
	
	private void delay(long sleep) {
		try 
		{
			Thread.sleep(sleep);
		} 
		catch (InterruptedException e) 
		{
			Thread.currentThread().interrupt();
		}
		
	}
	
	public void evtOnMessage(String topic, String message) {
        MessageAPI api = new MessageAPI();
        JSONObject response = api.processRequest(message, topic);
        JSONObject requestJSON = new JSONObject(message);
        String callbackTopic = requestJSON.optString(JsonKey.CALLBACK_TOPIC, "");
        long callbackDelay = requestJSON.optLong(JsonKey.CALLBACK_DELAY, 10);
        if(requestJSON.optString(JsonKey.COMMAND, "").equals(ConstantString.REQUEST_USSD) || requestJSON.optString(JsonKey.COMMAND, "").equals(ConstantString.GET_MODEM_LIST))
        {
        	this.delay(callbackDelay);
        	this.sendMessage(callbackTopic, response.toString());
        }
	}
	
	private void sendMessage(String callbackTopic, String message) {
		String host = ConfigSubscriberRedis.getSubscriberRedisAddress();
		int port = ConfigSubscriberRedis.getSubscriberRedisPort();
		boolean ssl = ConfigSubscriberRedis.isSubscriberRedisSSL();
		String username = ConfigSubscriberRedis.getSubscriberRedisUsername();
		String password = ConfigSubscriberRedis.getSubscriberRedisPassword();
		
		Jedis jedisSubscriber;
		if(ssl)
		{
			jedisSubscriber = new Jedis(host, port); 
		}
		else
		{
			SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		    SSLParameters sslParameters = new SSLParameters();		    
		    final HostnameVerifier allHostsValid = new HostnameVerifier() {   
				public boolean verify(String hostname, SSLSession session) {   
					return true;   
				}   
		    };
		    jedisSubscriber = new Jedis(host, port, ssl, sslSocketFactory, sslParameters, allHostsValid); 	
		}
		
		if(!username.isEmpty())
		{
			jedisSubscriber.clientSetname(username);
		}
		if(!password.isEmpty())
		{
			jedisSubscriber.auth(password);
		}
		
		try
		{
			jedisSubscriber.connect();	
			System.out.println("callbackTopic : "+callbackTopic);
			jedisSubscriber.publish(callbackTopic.getBytes(), message.getBytes());				
		}
		catch(JedisConnectionException e)
		{
			/**
			 * Do nothing
			 */
			e.printStackTrace();
		}
		finally 
		{
			jedisSubscriber.close();
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
