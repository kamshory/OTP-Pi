package com.planetbiru.subscriber.amqp;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.ServerWebSocketAdmin;
import com.planetbiru.api.MessageAPI;
import com.planetbiru.buzzer.Buzzer;
import com.planetbiru.config.ConfigSubscriberAMQP;
import com.planetbiru.config.ConfigSubscriberStomp;
import com.planetbiru.constant.ConstantString;
import com.planetbiru.constant.JsonKey;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MalformedFrameException;
import com.rabbitmq.client.ShutdownSignalException;

public class RabbitMQSubV0 extends RabbitMQSubscriber {
	private Connection connection;
	private Channel channel;
	private ConnectionFactory factory;
	
	@Override
	public void run()
	{
		this.connect();
		this.running = true;
		while (this.running)
		{
			if(!this.isConnected() || !this.connection.isOpen())
			{
				this.delay(ConfigSubscriberStomp.getSubscriberStompReconnectDelay());
				this.connect();
			}
			this.delay(ConfigSubscriberStomp.getSubscriberStompReconnectDelay());
		}
	}
	
	@Override
	public void connect()
	{
		this.factory = new ConnectionFactory();
	    this.factory.setHost(ConfigSubscriberAMQP.getSubscriberAmqpAddress());
	    this.factory.setPort(ConfigSubscriberAMQP.getSubscriberAmqpPort());
	    this.factory.setUsername(ConfigSubscriberAMQP.getSubscriberAmqpUsername());
	    this.factory.setPassword(ConfigSubscriberAMQP.getSubscriberAmqpPassword());	  
	    int timeout = ConfigSubscriberAMQP.getSubscriberAmqpTimeout();
	    if(timeout > 0)
	    {
	    	this.factory.setConnectionTimeout(timeout);
	    }
        
	    if(ConfigSubscriberAMQP.isSubscriberAmqpSSL())
		{
    		try 
    		{
    			SSLContext sslContext = SSLContext.getInstance("TLS");
    			sslContext.init(null,null,null);
    			SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();    	  		
    			this.factory.setSocketFactory(sslSocketFactory); 		
    		} 
    		catch(NoSuchAlgorithmException | KeyManagementException e) 
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
		    this.flagConnected();
			this.channel.basicConsume(topic, true, consumer);
		} 
	    catch (ConnectException e)
	    {
			this.evtIError(e);
	    }
		catch (TimeoutException e) 
		{
			this.evtTimeout(e);
		}     
	    catch (MalformedFrameException e)
	    {
	    	this.evtTimeout(e);
	    }
	    catch (IOException e) {
			this.evtIError(e);
		}
	}

	@Override
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

		this.connect();
		if(this.connected)
		{
			this.flagConnected();
		}
		else
		{
			this.flagDisconnected();
		}
	}
	
	@Override
	public void stopService() {
		this.running = false;
		this.flagDisconnected();		
		this.connection = null;
		this.factory = null;
		this.channel = null;
	}

	private void evtTimeout(TimeoutException e) // NOSONAR
	{
		this.flagDisconnected();
	}

	private void evtIError(ConnectException e) // NOSONAR
	{
		Buzzer.toneDisconnectAmqp();
		this.flagDisconnected();
	}
	
	private void evtIError(IOException e) // NOSONAR
	{
		Buzzer.toneDisconnectAmqp();
		this.flagDisconnected();
	}
	
	private void evtTimeout(MalformedFrameException e) // NOSONAR
	{
		Buzzer.toneDisconnectAmqp();
		this.flagDisconnected();
	}
	
	private void evtOnClose(String message, ShutdownSignalException e) // NOSONAR
	{
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
	
	public void evtOnMessage(byte[] payload, String topic) 
	{		
        if(payload != null)
		{
			String message = new String(payload, StandardCharsets.UTF_8);
			try
			{
			    MessageAPI api = new MessageAPI();
			    JSONObject response = api.processRequest(message, topic);
			   	JSONObject requestJSON = new JSONObject(message);
			    
			    String callbackTopic = requestJSON.optString(JsonKey.CALLBACK_TOPIC, "");
		        long callbackDelay = Math.abs(requestJSON.optLong(JsonKey.CALLBACK_DELAY, 10));
		        String command = requestJSON.optString(JsonKey.COMMAND, "");
		   		if(!callbackTopic.isEmpty() && (command.equals(ConstantString.ECHO) || command.equals(ConstantString.REQUEST_USSD) || command.equals(ConstantString.GET_MODEM_LIST)))
			    {
			    	this.delay(callbackDelay);
			    	this.sendMessage(response.toString(), callbackTopic);
			    }
			}
			catch(JSONException e)
			{
				/**
				 * Do nothing
				 */
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
    		catch(NoSuchAlgorithmException | NullPointerException | KeyManagementException e) 
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
	    	e.printStackTrace();
	    }
	}
	
	@Override
	public void flagDisconnected() {
		this.connected = false;
		this.updateConnectionStatus();
	}
	
	@Override
	public void flagConnected() {
		this.connected = true;
		this.updateConnectionStatus();
	}
	
	private void updateConnectionStatus() {
		ConfigSubscriberAMQP.setConnected(this.connected);
		if(this.connected != this.lastConnected)
		{
			ServerWebSocketAdmin.broadcastServerInfo(ConstantString.SERVICE_AMQP);		
		}
		this.lastConnected = this.connected;	
	}

	@Override
	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}
	
}
