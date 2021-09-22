package com.planetbiru.subscriber.redis;

import java.util.concurrent.CountDownLatch;

import javax.net.ssl.SSLParameters;
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

	@Override
	public void run()
	{		
		if(ConfigSubscriberRedis.isSubscriberMqttEnable())
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
		
		String channel = ConfigSubscriberRedis.topic;
		String host = ConfigSubscriberRedis.address;
		int port = ConfigSubscriberRedis.port;
		boolean ssl = ConfigSubscriberRedis.ssl;
		String username = ConfigSubscriberRedis.username;
		String password = ConfigSubscriberRedis.password;
		
		if(ssl)
		{
			this.subscriber = new Jedis(host, port); 
		}
		else
		{
			SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		    SSLParameters sslParameters = new SSLParameters();		    
			this.subscriber = new Jedis(host, port, ssl, sslSocketFactory, sslParameters, null); 	
		}
		
		this.subscriber.clientSetname(username);
		this.subscriber.auth(password);
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
			
		}, channel);
	}
	
	private void evtOnUnsubscribe(String channel, int subscribedChannels) {
	}
	
	private void evtOnSubscribe(String channel, int subscribedChannels) {
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
	
}
