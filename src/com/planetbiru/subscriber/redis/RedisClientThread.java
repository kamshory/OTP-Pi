package com.planetbiru.subscriber.redis;

import java.util.concurrent.CountDownLatch;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

import org.apache.log4j.Logger;

import com.planetbiru.ServerWebSocketAdmin;
import com.planetbiru.config.ConfigSubscriberRedis;
import com.planetbiru.constant.ConstantString;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisMonitor;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;

public class RedisClientThread extends Thread {
	
	private Jedis subscriber;
	private SubscriberRedis subscriberRedis;
	private boolean running = false;
	private boolean connected = false;
	private boolean lastComnnected = false;
	
	private static Logger logger = Logger.getLogger(RedisClientThread.class);

	public RedisClientThread(SubscriberRedis subscriberRedis) {
		this.setSubscriberRedis(subscriberRedis);
	}

	public RedisClientThread() {
		/**
		 * Just a default constructor
		 */
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
		    final HostnameVerifier allHostsValid = new HostnameVerifier() //NOSONAR
		    	{   
				public boolean verify(String hostname, SSLSession session) {   
					return true; //NOSONAR
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
			CountDownLatch latch = new CountDownLatch(10);
			
			this.subscriber.monitor(new JedisMonitor() {

				@Override
				public void onCommand(String command) {
					flagConnected();					
				}
				
			});
			
			this.subscriber.subscribe(new JedisPubSub() {
				
				@Override
			    public void onMessage(String channel, String message) {
					subscriberRedis.evtOnMessage(message.getBytes(), channel);
			    }
			    
			    @Override
			    public void onSubscribe(String channel, int subscribedChannels) {
			    	subscriberRedis.evtOnSubscribe(channel, subscribedChannels);
			    	
			    }	    
	
				@Override
			    public void onUnsubscribe(String channel, int subscribedChannels) {
			    	latch.countDown();			        
			    	subscriberRedis.evtOnUnsubscribe(channel, subscribedChannels);
			    	
			    }
				
				@Override
				public void onPong(String pattern) {
					subscriberRedis.evtOnPong(pattern);
				}
				
			}, topic);
		}
		catch(JedisConnectionException e)
		{
			this.flagDisconnected();
		}
	}
	public void flagConnected() {
		this.connected = true;	
		this.updateConnectionStatus();
	}
	public void flagDisconnected() {
		this.connected = false;		
		this.updateConnectionStatus();
	}

	
	private void updateConnectionStatus() {
		ConfigSubscriberRedis.setConnected(this.connected);
		if(this.connected != this.lastComnnected)
		{
			ServerWebSocketAdmin.broadcastServerInfo(ConstantString.SERVICE_REDIS);	
		}
		this.lastComnnected = this.connected;
	}

	public void ping() {
		try
		{
			this.subscriber.ping();
		}
		catch(Exception e)
		{
			this.flagDisconnected();
		}
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

	public boolean isConnected() {
		return this.connected && this.subscriber.isConnected();
	}

}
