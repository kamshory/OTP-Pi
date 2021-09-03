package com.planetbiru.subscriber.amqp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import com.planetbiru.api.MessageAPI;
import com.planetbiru.buzzer.Buzzer;
import com.planetbiru.config.ConfigSubscriberAMQP;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

public class RabbitMQSubscriber{
	private Connection connection;
	private Channel channel;
	private ConnectionFactory factory;
	private boolean reconnect = false;
	public void connect()
	{
		this.factory = new ConnectionFactory();
	    this.factory.setHost(ConfigSubscriberAMQP.getSubscriberAmqpAddress());
	    this.factory.setPort(ConfigSubscriberAMQP.getSubscriberAmqpPort());
	    this.factory.setUsername(ConfigSubscriberAMQP.getSubscriberAmqpUsername());
	    this.factory.setPassword(ConfigSubscriberAMQP.getSubscriberAmqpPassword());	  
	    this.factory.setConnectionTimeout(ConfigSubscriberAMQP.getSubscriberAmqpTimeout());
	    
	    if(ConfigSubscriberAMQP.isSubscriberAmqpSSL())
		{
			SSLContext sslContext;		
    		try 
    		{
    			sslContext = SSLContext.getInstance("TLS");
    			sslContext.init(null,null,null);
    			SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();    	  		
    			this.factory.setSocketFactory(sslSocketFactory); 		
    		} 
    		catch(Exception e) 
    		{
    			/**
    			 * Do nothing
    			 */
    		}
		}	
		
	    try 
	    {
			this.connection = factory.newConnection();
			this.channel = connection.createChannel();
		    this.channel.queueDeclare(ConfigSubscriberAMQP.getSubscriberAmqpTopic(), false, false, false, null);		    
		    DefaultConsumer consumer = new DefaultConsumer(channel) {
		        @Override
		        public void handleDelivery(
		            String consumerTag,
		            Envelope envelope, 
		            AMQP.BasicProperties properties, 
		            byte[] body) throws IOException {		     
		                evtOnMessage(body);
		        }
		        @Override
		        public void handleShutdownSignal(String message, ShutdownSignalException e)
		        {
		        	evtOnClose(message, e);
		        }
				
		    };
		    ConfigSubscriberAMQP.setConnected(true);
		    this.channel.basicConsume(ConfigSubscriberAMQP.getSubscriberAmqpTopic(), true, consumer);
		} 
		catch (IOException e) 
		{
			this.evtIError(e);
		} 
		catch (TimeoutException e) 
		{
			this.evtTimeout(e);
		}
	}
	private void restart() 
	{
		this.connection = null;
		this.factory = null;
		this.channel = null;
		try 
		{
			Thread.sleep(ConfigSubscriberAMQP.getSubscriberAmqpTimeout() * 2L);
		} 
		catch (InterruptedException e) 
		{
			Thread.currentThread().interrupt();
		}
		this.connect();
		
	}
	public void stopService() {
		this.connection = null;
		this.factory = null;
		this.channel = null;
	}

	private void evtTimeout(TimeoutException e) {
		ConfigSubscriberAMQP.setConnected(false);
		if(!this.reconnect)
		{
			this.reconnect = true;
			this.restart();
		}
		this.reconnect = false;
	}

	private void evtIError(IOException e) {
		Buzzer.toneDisconnectAmqp();
		ConfigSubscriberAMQP.setConnected(false);
		if(!this.reconnect)
		{
			this.reconnect = true;
			this.restart();
		}
		this.reconnect = false;
	}

	private void evtOnClose(String message, ShutdownSignalException e) {
		Buzzer.toneDisconnectAmqp();
		ConfigSubscriberAMQP.setConnected(false);
		if(!this.reconnect)
		{
			this.reconnect = true;
			this.restart();
		}
		this.reconnect = false;	
	}
	
	public void evtOnMessage(byte[] body) 
	{		
        if(body != null)
		{
			String message = new String(body, StandardCharsets.UTF_8);
            MessageAPI api = new MessageAPI();
            api.processRequest(message);            
		}
	}
	
}
