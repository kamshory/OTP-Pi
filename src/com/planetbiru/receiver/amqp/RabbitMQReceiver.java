package com.planetbiru.receiver.amqp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import com.planetbiru.api.MessageAPI;
import com.planetbiru.config.ConfigFeederAMQP;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

public class RabbitMQReceiver{
	private Connection connection;
	private Channel channel;
	private ConnectionFactory factory;
	private boolean reconnect = false;
	public void connect()
	{
		this.factory = new ConnectionFactory();
	    this.factory.setHost(ConfigFeederAMQP.getFeederAmqpAddress());
	    this.factory.setPort(ConfigFeederAMQP.getFeederAmqpPort());
	    this.factory.setUsername(ConfigFeederAMQP.getFeederAmqpUsername());
	    this.factory.setPassword(ConfigFeederAMQP.getFeederAmqpPassword());	  
	    this.factory.setConnectionTimeout(ConfigFeederAMQP.getFeederAmqpTimeout());
	    
	    if(ConfigFeederAMQP.isFeederAmqpSSL())
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
		    this.channel.queueDeclare(ConfigFeederAMQP.getFeederAmqpTopic(), false, false, false, null);		    
		    DefaultConsumer consumer = new DefaultConsumer(channel) {
		        @Override
		        public void handleDelivery(
		            String consumerTag,
		            Envelope envelope, 
		            AMQP.BasicProperties properties, 
		            byte[] body) throws IOException {		     
		                // process the message
		                evtOnMessage(body);
		        }
		        @Override
		        public void handleShutdownSignal(String msg, ShutdownSignalException e)
		        {
		        	evtOnClose(msg, e);
		        }
				
		    };
		    ConfigFeederAMQP.setConnected(true);
		    this.channel.basicConsume(ConfigFeederAMQP.getFeederAmqpTopic(), true, consumer);
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
			Thread.sleep(ConfigFeederAMQP.getFeederAmqpTimeout() * 2);
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
		ConfigFeederAMQP.setConnected(false);
		if(!this.reconnect)
		{
			this.reconnect = true;
			this.restart();
		}
		this.reconnect = false;
	}

	private void evtIError(IOException e) {
		ConfigFeederAMQP.setConnected(false);
		if(!this.reconnect)
		{
			this.reconnect = true;
			this.restart();
		}
		this.reconnect = false;
	}

	private void evtOnClose(String message, ShutdownSignalException e) {
		ConfigFeederAMQP.setConnected(false);
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
