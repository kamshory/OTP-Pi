package com.planetbiru.subscriber.amqp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.json.JSONObject;

import com.planetbiru.api.MessageAPI;
import com.planetbiru.buzzer.Buzzer;
import com.planetbiru.config.ConfigSubscriberAMQP;
import com.planetbiru.constant.ConstantString;
import com.planetbiru.constant.JsonKey;
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
			this.connection = this.factory.newConnection();
			this.channel = this.connection.createChannel();
		    String topic = ConfigSubscriberAMQP.getSubscriberAmqpTopic();
			this.channel.queueDeclare(topic, false, false, false, null);		    
		    DefaultConsumer consumer = new DefaultConsumer(this.channel) {
		        @Override
		        public void handleDelivery(
		            String consumerTag,
		            Envelope envelope, 
		            AMQP.BasicProperties properties, 
		            byte[] body) throws IOException {		     
		                evtOnMessage(body, topic);
		        }
		        @Override
		        public void handleShutdownSignal(String message, ShutdownSignalException e)
		        {
		        	evtOnClose(message, e);
		        }
				
		    };
		    ConfigSubscriberAMQP.setConnected(true);
		    this.channel.basicConsume(topic, true, consumer);
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
	public void delay(long sleep)
	{
		try 
		{
			Thread.sleep(sleep);
		} 
		catch (InterruptedException e) 
		{
			Thread.currentThread().interrupt();
		}
	}
	public void evtOnMessage(byte[] body, String topic) 
	{		
        if(body != null)
		{
			String message = new String(body, StandardCharsets.UTF_8);
            MessageAPI api = new MessageAPI();
            JSONObject response = api.processRequest(message, topic); 
            JSONObject requestJSON = new JSONObject(message);
            String command = requestJSON.optString(JsonKey.COMMAND, "");
            String callbackTopic = requestJSON.optString(JsonKey.CALLBACK_TOPIC, "");
            if(command.equals(ConstantString.REQUEST_USSD) || command.equals(ConstantString.GET_MODEM_LIST))
            {
            	this.delay(50);
            	this.sendMessage(callbackTopic, response.toString());
            }
		}
	}
	
	private void sendMessage(String callbackTopic, String message) {
		try(Channel replyChannel = this.connection.createChannel()) 
		{
			replyChannel.basicPublish("", callbackTopic, null, message.getBytes());
		} 
		catch (IOException | TimeoutException e) 
		{
			/**
			 * Do nothing
			 */
		}	
	}
	
	public void sendMessageX(String callbackTopic, String message) {
		ConnectionFactory connectionFactory = new ConnectionFactory();
	    connectionFactory.setHost(ConfigSubscriberAMQP.getSubscriberAmqpAddress());
	    connectionFactory.setPort(ConfigSubscriberAMQP.getSubscriberAmqpPort());
	    connectionFactory.setUsername(ConfigSubscriberAMQP.getSubscriberAmqpUsername());
	    connectionFactory.setPassword(ConfigSubscriberAMQP.getSubscriberAmqpPassword());	  
	    connectionFactory.setConnectionTimeout(ConfigSubscriberAMQP.getSubscriberAmqpTimeout());
	    
	    if(ConfigSubscriberAMQP.isSubscriberAmqpSSL())
		{
			SSLContext sslContext;		
    		try 
    		{
    			sslContext = SSLContext.getInstance("TLS");
    			sslContext.init(null,null,null);
    			SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();    	  		
    			connectionFactory.setSocketFactory(sslSocketFactory); 		
    		} 
    		catch(Exception e) 
    		{
    			/**
    			 * Do nothing
    			 */
    		}
		}	
	    
	    try(
	    		Connection clientConnection = connectionFactory.newConnection();
	    		Channel clientChannel = clientConnection.createChannel()
	    		) 
	    {
	    	clientChannel.basicPublish("", callbackTopic, null, message.getBytes());
	    }
	    catch (IOException | TimeoutException e) 
	    {
	    	/**
	    	 * Do nothing
	    	 */
	    }
	}
	
}
