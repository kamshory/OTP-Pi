package com.planetbiru.subscriber.redis;

import java.util.concurrent.CountDownLatch;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

import org.apache.log4j.Logger;

import com.planetbiru.config.ConfigSubscriberRedis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;

public class RedisClientThread extends Thread {
	
	private Jedis subscriber;
	private SubscriberRedis subscriberRedis;
	private boolean running = false;
	
	private static Logger logger = Logger.getLogger(RedisClientThread.class);

	public RedisClientThread(SubscriberRedis subscriberRedis) {
		this.setSubscriberRedis(subscriberRedis);
	}

	@Override
	public void run()
	{
		this.setRunning(true);
		String topic = ConfigSubscriberRedis.getSubscriberRedisTopic();
		String host = ConfigSubscriberRedis.getSubscriberRedisAddress();
		int port = ConfigSubscriberRedis.getSubscriberRedisPort();
		boolean ssl = ConfigSubscriberRedis.isSubscriberRedisSSL();
		String username = ConfigSubscriberRedis.getSubscriberRedisUsername();
		String password = ConfigSubscriberRedis.getSubscriberRedisPassword();
		
		if(ssl)
		{
			this.setSubscriber(new Jedis(host, port)); 
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
			this.setSubscriber(new Jedis(host, port, ssl, sslSocketFactory, sslParameters, allHostsValid)); 	
		}
		try
		{
			if(!username.isEmpty())
			{
				this.subscriber.clientSetname(username);
			}
			if(!password.isEmpty())
			{
				this.subscriber.auth(password);
			}
		}
		catch(JedisDataException e)
		{
			logger.error(e.getMessage());
		}
		
		
		try
		{
			this.subscriber.connect();	
			subscriberRedis.flagConnected(true);
			CountDownLatch latch = new CountDownLatch(10);			
			this.subscriber.subscribe(new JedisPubSub() {
				
				@Override
			    public void onMessage(String channel, String message) {
					subscriberRedis.evtOnMessage(channel, message);
			    }
			    
			    @Override
			    public void onSubscribe(String channel, int subscribedChannels) {
			    	subscriberRedis.evtOnSubscribe(channel, subscribedChannels);
			    	
			    }	    
	
				@Override
			    public void onUnsubscribe(String channel, int subscribedChannels) {
			    	latch.countDown();			        
			    	subscriberRedis.evtOnUnsubscribe(channel, subscribedChannels);
			    	subscriberRedis.flagDisconnected();
			    }
				
				@Override
				public void onPong(String pattern) {
					subscriberRedis.evtOnPong(pattern);
				}
				
			}, topic);
		}
		catch(JedisConnectionException e)
		{
			if(this.subscriberRedis != null)
			{
				this.subscriberRedis.setConnected(false);
			}
		}
		if(this.subscriberRedis != null)
		{
			this.subscriberRedis.setConnected(false);
		}
	}

	public void ping() {
		this.subscriber.ping();
	}

	public boolean isRunning() {
		return this.running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public Jedis getSubscriber() {
		return this.subscriber;
	}

	public void setSubscriber(Jedis subscriber) {
		this.subscriber = subscriber;
	}

	public SubscriberRedis getSubscriberRedis() {
		return this.subscriberRedis;
	}

	public void setSubscriberRedis(SubscriberRedis subscriberRedis) {
		this.subscriberRedis = subscriberRedis;
	}

}
