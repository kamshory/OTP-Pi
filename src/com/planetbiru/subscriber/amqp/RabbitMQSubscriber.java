package com.planetbiru.subscriber.amqp;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.json.JSONObject;

import com.planetbiru.ServerWebSocketAdmin;
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
	private boolean connected;
	public boolean connect()
	{
		boolean con = false;
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
			con = true;
		} 
	    catch (ConnectException e)
	    {
			this.evtIError(e);
	    }
		catch (TimeoutException e) 
		{
			this.evtTimeout(e);
		} 
	    catch (IOException e) {
			this.evtIError(e);
		}
	    return con;
	}

	public void restart() 
	{
		this.connection = null;
		this.factory = null;
		this.channel = null;
		try 
		{
			Thread.sleep(ConfigSubscriberAMQP.getSubscriberAmqpReconnectDelay());
		} 
		catch (InterruptedException e) 
		{
			Thread.currentThread().interrupt();
		}

		if(this.connect())
		{
			this.flagConnected();
		}
		else
		{
			this.flagDisconnected();
		}
	}
	public void stopService() {
		this.connection = null;
		this.factory = null;
		this.channel = null;
	}

	private void evtTimeout(TimeoutException e) {
		this.flagDisconnected();
	}

	private void evtIError(ConnectException e) {
		Buzzer.toneDisconnectAmqp();
		this.flagDisconnected();
	}
	
	private void evtIError(IOException e) {
		Buzzer.toneDisconnectAmqp();
		this.flagDisconnected();
	}
	
	private void evtOnClose(String message, ShutdownSignalException e) {
		Buzzer.toneDisconnectAmqp();
		this.flagDisconnected();
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
            long callbackDelay = requestJSON.optLong(JsonKey.CALLBACK_DELAY, 10);
            if(command.equals(ConstantString.REQUEST_USSD) || command.equals(ConstantString.GET_MODEM_LIST))
            {
            	this.delay(callbackDelay);
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
	
	public void flagDisconnected() {
		this.connected = false;
		ConfigSubscriberAMQP.setConnected(this.connected);
		ServerWebSocketAdmin.broadcastServerInfo(ConstantString.SERVICE_AMQP);		
	}
	
	public void flagConnected() {
		this.connected = true;
		ConfigSubscriberAMQP.setConnected(this.connected);
		ServerWebSocketAdmin.broadcastServerInfo(ConstantString.SERVICE_AMQP);	
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}
	
}
