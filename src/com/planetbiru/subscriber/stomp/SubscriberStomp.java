package com.planetbiru.subscriber.stomp;

import java.nio.charset.StandardCharsets;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.fusesource.stomp.jms.StompJmsConnectionFactory;
import org.json.JSONObject;

import com.planetbiru.ServerWebSocketAdmin;
import com.planetbiru.api.MessageAPI;
import com.planetbiru.config.ConfigSubscriberStomp;
import com.planetbiru.constant.ConstantString;
import com.planetbiru.constant.JsonKey;

public class SubscriberStomp extends Thread {
	
	private boolean running = false;
	private boolean connected = false;
	private boolean lastConnected = false;

	@Override
	public void run()
	{
		while (this.running)
		{
			this.connect();
			this.delay(ConfigSubscriberStomp.getSubscriberStompReconnectDelay());
		}
	}

	public void connect() {
		this.running = true;
		String user = ConfigSubscriberStomp.getSubscriberStompUsername();
        String password = ConfigSubscriberStomp.getSubscriberStompPassword();
        String host = ConfigSubscriberStomp.getSubscriberStompAddress();
        int port = ConfigSubscriberStomp.getSubscriberStompPort();
        String connectionURI = "tcp://" + host + ":" + port;
        String topic = ConfigSubscriberStomp.getSubscriberStompTopic();
        StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
        factory.setBrokerURI(connectionURI);
        try (Connection connection = factory.createConnection(user, password))
        {
        	connection.setExceptionListener(new ExceptionListener() {				
				@Override
				public void onException(JMSException exception) {
					setConnected(false);
					flagDisconnected();
					
				}
			});
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createTopic(topic);
            MessageConsumer consumer = session.createConsumer(destination);
            this.connected = true;
            this.updateConnectionStatus();
            do
            {
            	
            	Message msg = consumer.receive(ConfigSubscriberStomp.getSubscriberStompTimeout());
                
                if(msg != null)
                {
	                if (msg instanceof TextMessage) 
	                {
	                    String body = ((TextMessage) msg).getText();    
	                    this.evtOnMessage(body.getBytes(), topic);
	                }	                
	                else 
	                {
	                    /**
	                     * Do nothing
	                     */
	                }
                }  
            }
            while(this.isConnected());
        }
        catch(JMSException e)
        {
        	this.connected = false;
        	this.updateConnectionStatus();
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
		String user = ConfigSubscriberStomp.getSubscriberStompUsername();
        String password = ConfigSubscriberStomp.getSubscriberStompPassword();
        String host = ConfigSubscriberStomp.getSubscriberStompAddress();
        int port = ConfigSubscriberStomp.getSubscriberStompPort();
        String connectionURI = "tcp://" + host + ":" + port;
        String topic = callbackTopic;
        StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
        factory.setBrokerURI(connectionURI);
        try (Connection connection = factory.createConnection(user, password))
        {
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createTopic(topic);
            MessageProducer producer = session.createProducer(destination);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            TextMessage msg = session.createTextMessage(message);
            producer.send(msg);
        }
        catch(JMSException e)
        {
        	/**
        	 * Do nothing
        	 */
        }
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

	public boolean isConnected() {
		return this.connected;
	}

	public void restart() {
		this.flagDisconnected();
		this.connect();
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
		ConfigSubscriberStomp.setConnected(this.connected);
		if(this.connected != this.lastConnected)
		{
			ServerWebSocketAdmin.broadcastServerInfo(ConstantString.SERVICE_STOMP);		
		}
		this.lastConnected = this.connected;	
	}

	public void stopService() {
		this.running = false;
		this.connected = false;
		this.updateConnectionStatus();
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}	
	
	
}
