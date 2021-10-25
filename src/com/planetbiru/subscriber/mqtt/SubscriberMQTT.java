package com.planetbiru.subscriber.mqtt;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

import com.planetbiru.ServerWebSocketAdmin;
import com.planetbiru.api.MessageAPI;
import com.planetbiru.buzzer.Buzzer;
import com.planetbiru.config.ConfigSubscriberMQTT;
import com.planetbiru.constant.ConstantString;
import com.planetbiru.constant.JsonKey;

public class SubscriberMQTT extends Thread{
	private MqttClient subscriber;
	private boolean connected = false;
	private boolean running = true;
	private MqttCallback callback;

	@Override
	public void run()
	{
		if(ConfigSubscriberMQTT.isSubscriberMqttEnable())
		{
			long sleep = ConfigSubscriberMQTT.getsubscriberMqttReconnectDelay();
			if(sleep == 0)
			{
				sleep = 10000;
			}
			ConfigSubscriberMQTT.setConnected(true);
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
					this.connect();
				}
			}
			while(this.running);
		}
	}
	
	private void flagDisconnected()
	{
		Buzzer.toneDisconnectMqtt();
		this.callback = null;
		try 
		{
			this.subscriber.disconnect();
		} 
		catch (MqttException e) 
		{
			/**
			 * Do nothing
			 */
		}
		this.connected = false;
		this.subscriber = null;
		ConfigSubscriberMQTT.setConnected(false);
		ServerWebSocketAdmin.broadcastServerInfo(ConstantString.SERVICE_MQTT);
	}
	
	private void connect() {
		try 
		{
			this.subscriber = null;
			String uri = "tcp://"+ConfigSubscriberMQTT.getSubscriberMqttAddress()+":"+ConfigSubscriberMQTT.getSubscriberMqttPort();
			String clientID = UUID.randomUUID().toString();
			MemoryPersistence persistence = new MemoryPersistence();
			this.subscriber = new MqttClient(uri, clientID, persistence);
		
			MqttConnectOptions options = new MqttConnectOptions();
			options.setAutomaticReconnect(false);
			options.setCleanSession(true);
			options.setConnectionTimeout(ConfigSubscriberMQTT.getSubscriberMqttTimeout());
			String username = ConfigSubscriberMQTT.getSubscriberMqttUsername();
			String password = ConfigSubscriberMQTT.getSubscriberMqttPassword();
			if(!username.isEmpty())
			{
				options.setUserName(username);
			}
			if(!password.isEmpty())
			{
				options.setPassword(password.toCharArray());
			}
			CountDownLatch latch = new CountDownLatch(10);
			this.callback = null;
			this.callback = new MqttCallback() {
			    @Override
				public void messageArrived(String topic, MqttMessage payload) throws Exception {
			        latch.countDown(); 
			        evtOnMessage(topic, payload);
			    }			    

				@Override
			    public void connectionLost(Throwable cause) {
			        latch.countDown();			        
			        flagDisconnected();
			    }
			    
			    @Override
			    public void deliveryComplete(IMqttDeliveryToken token) 
			    {
			    	/**
			    	 * Do nothing
			    	 */
			    }	
			};
			
			this.subscriber.connect(options);
			this.subscriber.setCallback(this.callback);
			this.connected = true;
			this.subscriber.subscribe(ConfigSubscriberMQTT.getSubscriberMqttTopic(), 0);
			ConfigSubscriberMQTT.setConnected(true);
			ServerWebSocketAdmin.broadcastServerInfo(ConstantString.SERVICE_MQTT);
		} 
		catch (MqttException e) 
		{
			flagDisconnected();
		}		
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
	
	public void evtOnMessage(String topic, MqttMessage payload) {
		String message = new String(payload.getPayload());
        MessageAPI api = new MessageAPI();
        JSONObject response = api.processRequest(message, topic);
        JSONObject requestJSON = new JSONObject(message); 
        String callbackTopic = requestJSON.optString(JsonKey.CALLBACK_TOPIC, "");
        long callbackDelay = requestJSON.optLong(JsonKey.CALLBACK_DELAY, 10);
        if(requestJSON.optString(JsonKey.COMMAND, "").equals(ConstantString.REQUEST_USSD) || requestJSON.optString(JsonKey.COMMAND, "").equals(ConstantString.GET_MODEM_LIST))
        {
        	this.delay(callbackDelay);
        	this.sendMessage(callbackTopic, response.toString());
        }		
	}
	
	private void sendMessage(String callbackTopic, String message) {
		String uri = "tcp://"+ConfigSubscriberMQTT.getSubscriberMqttAddress()+":"+ConfigSubscriberMQTT.getSubscriberMqttPort();
		String clientID = UUID.randomUUID().toString();
		MemoryPersistence persistence = new MemoryPersistence();
		try {
			MqttClient mqttSubscriber = new MqttClient(uri, clientID, persistence);
			MqttConnectOptions options = new MqttConnectOptions();
			options.setAutomaticReconnect(false);
			options.setCleanSession(true);
			options.setConnectionTimeout(ConfigSubscriberMQTT.getSubscriberMqttTimeout());
			String username = ConfigSubscriberMQTT.getSubscriberMqttUsername();
			String password = ConfigSubscriberMQTT.getSubscriberMqttPassword();
			if(!username.isEmpty())
			{
				options.setUserName(username);
			}
			if(!password.isEmpty())
			{
				options.setPassword(password.toCharArray());
			}
			
			mqttSubscriber.connect(options);
			MqttMessage mqttMessage = new MqttMessage(message.getBytes());
			mqttSubscriber.publish(callbackTopic, mqttMessage);
			mqttSubscriber.close();
			
		} catch (MqttException e) {
			/**
			 * Do nothing
			 */
		}
	}

	public boolean isRunning() {
		return running;
	}
	
	public void stopService() {
		this.running = false;	
		this.flagDisconnected();
	}
	
	
}
