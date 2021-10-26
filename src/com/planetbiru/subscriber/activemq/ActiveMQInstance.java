package com.planetbiru.subscriber.activemq;

import java.util.Arrays;

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.json.JSONObject;

import com.planetbiru.ServerWebSocketAdmin;
import com.planetbiru.api.MessageAPI;
import com.planetbiru.config.ConfigSubscriberActiveMQ;
import com.planetbiru.constant.ConstantString;
import com.planetbiru.constant.JsonKey;

public class ActiveMQInstance extends Thread implements ExceptionListener {
	
	protected ActiveMQConnectionFactory connectionFactory = null;
	private MessageConsumer consumer = null;
	private Session session = null;
	private ActiveMQConnection connection = null;
	private boolean running = false;
	private boolean connected = false;
	private long interval = 5000;
	private long timeout = 10000;
	private String topic = "sms";
	private long timeToLeave = 60000;
	
	public ActiveMQInstance()
	{
		try 
		{
			this.connected = this.connect();
			this.updateConnectionStatus();
		} 
		catch (JMSException e) 
		{
			this.flagDisconnected();
		}
	}

	public boolean connect() throws JMSException
	{
		this.running = true;
		this.timeout = ConfigSubscriberActiveMQ.getSubscriberActiveMQTimeout();
		this.interval = ConfigSubscriberActiveMQ.getSubscriberActiveMQReconnectDelay();
		this.timeToLeave = ConfigSubscriberActiveMQ.getSubscriberTimeToLeave();
		
		if(this.timeout <= 0)
		{
			this.timeout = 10000;
		}
		if(this.interval <= 0)
		{
			this.interval = 5000;
		}
		if(this.timeToLeave <= 0)
		{
			this.timeToLeave = 60000;
		}		
		String host = ConfigSubscriberActiveMQ.getSubscriberActiveMQAddress();
		int port = ConfigSubscriberActiveMQ.getSubscriberActiveMQPort();
		if(host.isEmpty())
		{
			return false;
		}
		if(host.isEmpty() || port == 0)
		{
			return false;
		}
		String url = String.format("tcp://%s:%d", host, port);
		String username = ConfigSubscriberActiveMQ.getSubscriberActiveMQUsername();
		String password = ConfigSubscriberActiveMQ.getSubscriberActiveMQPassword();
		String clientID = ConfigSubscriberActiveMQ.getSubscriberActiveMQClientID();
		this.topic = ConfigSubscriberActiveMQ.getSubscriberActiveMQTopic();		
		if(this.topic.isEmpty())
		{
			return false;
		}
		
		try
		{		
			if(!username.isEmpty())
			{
				this.connectionFactory = new ActiveMQConnectionFactory(username, password, url);
			}
			else
			{
				this.connectionFactory = new ActiveMQConnectionFactory(url);
			}
			this.connectionFactory.setClientID(clientID);
			this.connectionFactory.setTrustedPackages(Arrays.asList("com.planetbiru.subscriber.activemq"));
			this.connection = (ActiveMQConnection) connectionFactory.createConnection();
			
	        if(!this.connection.isClosed())
	        {
	        	this.connection.start();
	        	this.connection.setExceptionListener(this);
	        	this.session = this.connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	            Destination destination = this.session.createTopic(this.topic);
	            this.consumer = this.session.createConsumer(destination);
	            return true;
	        }
			else
			{
				return false;
			}
		}
		catch(Exception e)
		{
			this.connected = false;
		}
		return false;
 	}
	
	public void disconnect() throws JMSException
	{
		if(this.consumer != null)
		{
			this.consumer.close();
		}
		if(this.session != null)
        {
			this.session.close();
        }
		if(this.connection != null && !this.connection.isClosed())
        {
			this.connection.close();
        }
		this.connected = false;
	}
	
	public String processMessage(String message)
	{
		MessageAPI api = new MessageAPI();
        JSONObject response = api.processRequest(message, this.topic);
        JSONObject requestJSON = new JSONObject(message); 
        String callbackTopic = requestJSON.optString(JsonKey.CALLBACK_TOPIC, "");
        long callbackDelay = requestJSON.optLong(JsonKey.CALLBACK_DELAY, 10);
   		if(!callbackTopic.isEmpty() 
        		&& (requestJSON.optString(JsonKey.COMMAND, "").equals(ConstantString.REQUEST_USSD) 
        				|| requestJSON.optString(JsonKey.COMMAND, "").equals(ConstantString.GET_MODEM_LIST)))
        {
        	this.delay(callbackDelay);
        	try 
        	{
 				this.sendMessage(callbackTopic, response.toString());
			} 
        	catch (JMSException e) 
        	{
        		this.flagDisconnected();
			}
        }	
		return "";
	}
	
	private void sendMessage(String callbackTopic, String message) throws JMSException {
		Destination destination = this.session.createTopic(callbackTopic);
		MessageProducer producer = this.session.createProducer(destination);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        producer.setTimeToLive(this.timeToLeave);
        TextMessage textMessage = this.session.createTextMessage(message);
        producer.send(textMessage);		
	}

	public void loop()
	{
		if(this.consumer != null)
		{
			try 
			{
	            Message message = this.consumer.receive(this.timeout);
	            if(message != null)
	            {
	            	if(message instanceof TextMessage) 
		            {
		                TextMessage textMessage = (TextMessage) message;
		                this.processMessage(textMessage.getText());
		            } 
		            else 
		            {
		            	this.connected = true;
		            }
	            }	            
	        } 
			catch (JMSException e) 
			{
				this.flagDisconnected();
	        }
		}
	}

	@Override
	public void run() {
		do {
			while(!this.connected)
			{
				this.reconnect();
				if(!this.connected)
				{
					this.delay(this.interval);
				}
			}
			this.loop();
		}
		while(this.isRunning());       
    }

	private void delay(long sleep) {
		try 
		{
			Thread.sleep(sleep);
		} 
		catch (InterruptedException e) 
		{
			Thread.currentThread().interrupt();
		}
	}
	
	private void reconnect() {
		try 
		{
			this.disconnect();
			this.connected = this.connect();
			this.updateConnectionStatus();
		} 
		catch (JMSException e) 
		{
			this.flagDisconnected();
		}		
	}

	private void flagDisconnected() {
		this.connected = false;
		this.updateConnectionStatus();
		
	}

	private void updateConnectionStatus() {
		ConfigSubscriberActiveMQ.setConnected(this.connected);
		ServerWebSocketAdmin.broadcastServerInfo(ConstantString.SERVICE_ACTIVEMQ);		
	}

	@Override
	public void onException(JMSException exception) {
		this.flagDisconnected();
	}

	public void stopService() {
		this.running = false;
		try 
		{
			this.disconnect();			
		} 
		catch (JMSException e) {
			/**
			 * Do nothing
			 */
		}
		this.flagDisconnected();
	}

	public boolean isRunning() {
		return running;
	}	
}
