package com.planetbiru.subscriber.redis;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

import org.json.JSONObject;

import com.planetbiru.api.MessageAPI;
import com.planetbiru.config.ConfigSubscriberRedis;
import com.planetbiru.constant.ConstantString;
import com.planetbiru.constant.JsonKey;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class SubscriberRedis extends Thread {
	
	private boolean running = true;

	private boolean pong = false;
	private RedisClientThread clientThread;

	@Override
	public void run()
	{
		if(ConfigSubscriberRedis.isSubscriberRedisEnable())
		{
			
			this.connect();
			do 
			{
				long sleep = ConfigSubscriberRedis.getSubscriberWsReconnectDelay();
				boolean ret = true;
				if(this.isConnected())
				{
					ret = this.ping(1000);		
				}
				if(sleep == 0)
				{
					sleep = 10000;
				}
				if(this.running && (!this.isConnected() || !ret))
				{
					this.disconnect();
					this.connect();
					this.delay(500);
					this.ping(100);
				}
				this.delay(sleep);
				
			}
			while(this.running);
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
			this.clientThread.flagDisconnected();
		}
		return this.pong;
	}
	public void evtOnPong(String pattern)
	{
		this.pong = true;
		this.clientThread.flagConnected();
	}
	
	public void evtOnUnsubscribe(String topic, int subscribedChannels) {
		this.clientThread.flagDisconnected();
	}
	
	public void evtOnSubscribe(String topic, int subscribedChannels) {
		this.clientThread.flagConnected();
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
			jedisSubscriber.publish(callbackTopic.getBytes(), message.getBytes());				
		}
		catch(JedisConnectionException e)
		{
			/**
			 * Do nothing
			 */
		}
		finally 
		{
			jedisSubscriber.close();
		}		
	}

	public void stopService() {
		this.setRunning(false);	
		this.clientThread.flagDisconnected();
	}
	
	public boolean isRunning() {
		return this.running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public boolean isConnected() {
		return this.clientThread.isConnected();
	}

	
	
}
