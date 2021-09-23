package com.planetbiru.subscriber.redis;

import com.planetbiru.ServerWebSocketAdmin;
import com.planetbiru.api.MessageAPI;
import com.planetbiru.buzzer.Buzzer;
import com.planetbiru.config.ConfigSubscriberRedis;

public class SubscriberRedis extends Thread {
	
	boolean connected = false;
	private boolean running = true;

	private boolean pong = false;
	private ClientThread clientThread;

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
					this.disconnect();
					this.connect();
				}
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
		this.clientThread = new ClientThread(this);
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
			this.connected = false;
		}
		return this.pong;
	}
	public void evtOnPong(String pattern)
	{
		this.pong = true;
		this.connected = true;
	}
	
	public void evtOnUnsubscribe(String topic, int subscribedChannels) {
		/**
		 * Do nothing
		 */
		this.connected = false;
	}
	
	public void evtOnSubscribe(String topic, int subscribedChannels) {
		/**
		 * Do nothing
		 */
		this.connected = true;
	}
	
	public void evtOnMessage(String topic, String message) {
        MessageAPI api = new MessageAPI();
        api.processRequest(message, topic);
	}
	
	public void stopService() {
		this.running = false;	
		this.flagDisconnected();
	}
	
	public void flagDisconnected()
	{
		System.out.println("flagDisconnected()");
		Buzzer.toneDisconnectRedis();
		this.clientThread.getSubscriber().disconnect();
		this.connected = false;
		this.clientThread.setSubscriberRedis(null);
		ConfigSubscriberRedis.setConnected(false);
		ServerWebSocketAdmin.broadcastServerInfo();
	}

	public boolean isRunning() {
		return this.running;
	}
	
}
