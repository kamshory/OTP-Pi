package com.planetbiru.subscriber.redis;

import java.util.concurrent.CountDownLatch;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

import com.planetbiru.ServerWebSocketAdmin;
import com.planetbiru.api.MessageAPI;
import com.planetbiru.buzzer.Buzzer;
import com.planetbiru.config.ConfigSubscriberRedis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class SubscriberRedis extends Thread {
	
	private boolean connected = false;
	private boolean running = true;
	private Jedis subscriber = null;

	private boolean pong = false;

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
			ConfigSubscriberRedis.setConnected(true);
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
				if(!this.connected)
				{
					this.connect();
				}
			}
			while(this.running);
		}
	}

	private void connect() {
		String channel = ConfigSubscriberRedis.getSubscriberRedisTopic();
		String host = ConfigSubscriberRedis.getSubscriberRedisAddress();
		int port = ConfigSubscriberRedis.getSubscriberRedisPort();
		boolean ssl = ConfigSubscriberRedis.isSubscriberRedisSSL();
		String username = ConfigSubscriberRedis.getSubscriberRedisUsername();
		String password = ConfigSubscriberRedis.getSubscriberRedisPassword();
		
		if(ssl)
		{
			this.subscriber = new Jedis(host, port); 
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
			this.subscriber = new Jedis(host, port, ssl, sslSocketFactory, sslParameters, allHostsValid); 	
		}
		
		if(!username.isEmpty())
		{
			this.subscriber.clientSetname(username);
		}
		if(!password.isEmpty())
		{
			this.subscriber.auth(password);
		}
		this.subscriber.connect();
	
		CountDownLatch latch = new CountDownLatch(10);
		
		this.subscriber.subscribe(new JedisPubSub() {
		    
			@Override
		    public void onMessage(String channel, String message) {
		        evtOnMessage(channel, message);
		    }
		    
		    @Override
		    public void onSubscribe(String channel, int subscribedChannels) {
		        evtOnSubscribe(channel, subscribedChannels);
		    }	    

			@Override
		    public void onUnsubscribe(String channel, int subscribedChannels) {
		    	latch.countDown();			        
		    	evtOnUnsubscribe(channel, subscribedChannels);
		        flagDisconnected();
		    }
			
			@Override
			public void onPong(String pattern) {
				evtOnPong(pattern);
			}
			
		}, channel);
	}
	public boolean ping(long timeout)
	{
		this.pong = false;
		this.subscriber.ping();
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
			this.connected = false;
		}
		return this.pong;
	}
	private void evtOnPong(String pattern)
	{
		this.pong = true;
		this.connected = true;
	}
	
	private void evtOnUnsubscribe(String topic, int subscribedChannels) {
		/**
		 * Do nothing
		 */
		this.connected = false;
	}
	
	private void evtOnSubscribe(String topic, int subscribedChannels) {
		/**
		 * Do nothing
		 */
		this.connected = true;
	}
	
	private void evtOnMessage(String topic, String message) {
        MessageAPI api = new MessageAPI();
        api.processRequest(message, topic);
	}
	
	public void stopService() {
		this.running = false;	
		this.flagDisconnected();
	}
	
	private void flagDisconnected()
	{
		Buzzer.toneDisconnectRedis();
		this.subscriber.disconnect();
		this.connected = false;
		this.subscriber = null;
		ConfigSubscriberRedis.setConnected(false);
		ServerWebSocketAdmin.broadcastServerInfo();
	}

	public boolean isRunning() {
		return this.running;
	}
	
}
