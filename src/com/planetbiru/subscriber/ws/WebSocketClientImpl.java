package com.planetbiru.subscriber.ws;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.api.MessageAPI;
import com.planetbiru.buzzer.Buzzer;
import com.planetbiru.config.ConfigSubscriberWS;
import com.planetbiru.constant.ConstantString;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.util.Utility;

public class WebSocketClientImpl extends Thread{
	private long reconnectDelay = 10000;
	private boolean stopend = false;
	private WebSocketClient wsClient;
	private SubscriberWebSocket webSocketTool;
	private boolean reconnect = false;
	private long waitLoop = 300000;
	
	public WebSocketClientImpl(long reconnectDelay, long waitLoop, SubscriberWebSocket webSocketTool, boolean reconnect)
	{
		this.reconnectDelay = reconnectDelay;
		this.waitLoop = waitLoop;
		this.webSocketTool = webSocketTool;
		this.reconnect = reconnect;
	}	

	@Override
	public void run()
	{
		if(ConfigSubscriberWS.isSubscriberWsEnable())
		{
			try 
			{
				this.initWSClient();
			} 
			catch (URISyntaxException e) 
			{
				/**
				 * Do nothing
				 */
			}
			
			do
			{			
				try 
				{
					Thread.sleep(this.waitLoop);
				} 
				catch (InterruptedException e) 
				{
					Thread.currentThread().interrupt();
				}
			}
			while(!this.stopend);
		}
	}
	
	public void restartThread()
	{
		this.stopend = true;
		try
		{
			if(this.wsClient != null)
			{
				this.wsClient.close();
			}
		}
		catch(Exception e)
		{
			/**
			 * Do nothing
			 */
		}
		this.wsClient = null;
		try 
		{
			Thread.sleep(this.reconnectDelay);
		} 
		catch (InterruptedException e) 
		{
			Thread.currentThread().interrupt();
		}
		this.webSocketTool.restartThread();
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
	public void evtOnMessage(String message, String topic) {
		try
		{
            MessageAPI api = new MessageAPI();
            JSONObject response = api.processRequest(message, topic);  
            JSONObject requestJSON = new JSONObject(message);
            if(requestJSON.optString(JsonKey.COMMAND, "").equals(ConstantString.REQUEST_USSD) || requestJSON.optString(JsonKey.COMMAND, "").equals(ConstantString.GET_MODEM_LIST))
            {
            	this.delay(50);
            	this.sendMessage(requestJSON.optString(JsonKey.CALLBACK_TOPIC, ""), response.toString());
            }
            
		}
		catch(JSONException e)
		{
			/**
			 * Do nothing
			 */
		}	
	}
	private void sendMessage(String callbackTopic, String message) {
		String endpoint = this.createWSEndpoint();
		endpoint = this.fixWSEndpoint(endpoint, callbackTopic);
		WebSocketClient localWSClient = null;
		try 
		{
			URI uri = new URI(endpoint);	
			Map<String, String> headers = new HashMap<>();
			headers.put("Authorization", Utility.basicAuth(ConfigSubscriberWS.getSubscriberWsUsername(), ConfigSubscriberWS.getSubscriberWsPassword()));
			localWSClient = new WebSocketClient(uri, headers) {
			    @Override
			    public void onOpen(ServerHandshake serverHandshake) {
			    	this.send(message);
			    	this.close();
			    }
	
			    @Override
			    public void onMessage(String message) {
			    	/**
			    	 * Do nothing
			    	 */
			    }
			    
				@Override
				public void onClose(int code, String reason, boolean remote) {					
					this.close();
				}

				@Override
			    public void onError(Exception e) {
					this.close();
			    }
			};
			localWSClient.connect();
		} 	
		catch (URISyntaxException e) 
		{
			/**
			 * Do nothing
			 */
		}
		
	}

	public void evtOnOpen(ServerHandshake serverHandshake)
	{
		if(serverHandshake.getHttpStatus() != 101 && this.reconnect)
		{
			this.reconnect = false;
			this.restartThread();
		}
		/**
		 * Do nothing
		 */
	}
	
	public void evtOnClose(int code, String reason, boolean remote)
	{
		Buzzer.toneDisconnectWs();
		if(this.reconnect)
		{
			this.reconnect = false;
			this.restartThread();
		}
	}
	public void evtOnError(Exception e)
	{
		Buzzer.toneDisconnectWs();
		if(this.reconnect)
		{
			this.reconnect = false;
			this.restartThread();
		}
	}

	public void initWSClient() throws URISyntaxException
	{
		String topic = ConfigSubscriberWS.getSubscriberWsTopic();
		String endpoint = this.createWSEndpoint();
		endpoint = this.fixWSEndpoint(endpoint, topic);
		try 
		{
			URI uri = new URI(endpoint);	
			Map<String, String> headers = new HashMap<>();
			headers.put("Authorization", Utility.basicAuth(ConfigSubscriberWS.getSubscriberWsUsername(), ConfigSubscriberWS.getSubscriberWsPassword()));
			this.wsClient = null;
			this.wsClient = new WebSocketClient(uri, headers) {
			    @Override
			    public void onOpen(ServerHandshake serverHandshake) {
			        evtOnOpen(serverHandshake);
			    }
	
			    @Override
			    public void onMessage(String message) {
			    	evtOnMessage(message, topic);
			    }
			    
				@Override
				public void onClose(int code, String reason, boolean remote) {
					evtOnClose(code, reason, remote);
					
				}

				@Override
			    public void onError(Exception e) {
			    	evtOnError(e);
			    }
			};
			this.wsClient.connect();
		} 	
		catch (URISyntaxException e) 
		{
			throw new URISyntaxException(e.getMessage(), e.getReason());
		}
	}
	
	private String fixWSEndpoint(String endpoint, String topic) {
		String path = endpoint;
		Map<String, List<String>> params = new HashMap<>();
		String query = "";
		if(endpoint.contains("?"))
		{
			String[] arr = endpoint.split("\\?", 2);
			if(arr.length > 1)
			{
				path = arr[0];
				try 
				{
					params = Utility.splitQuery(arr[1]);
					if(params.containsKey(JsonKey.TOPIC))
					{
						params.remove(JsonKey.TOPIC);
					}				
				} 
				catch (UnsupportedEncodingException e) 
				{
					/**
					 * Do nothing
					 */
				}				
			}
		}
		params.put(JsonKey.TOPIC, Utility.asList(topic));
		query = Utility.buildQuery(params);
		
		endpoint = path + "?"+query;
		return endpoint;	
	}

	private String createWSEndpoint() {
		String protocol = "";
		String host = ConfigSubscriberWS.getSubscriberWsAddress();
		String port = "";
		String contextPath = ConfigSubscriberWS.getSubscriberWsPath();
		if(!contextPath.startsWith("/"))
		{
			contextPath = "/"+contextPath;
		}
		if(ConfigSubscriberWS.isSubscriberWsSSL())
		{
			protocol = "wss://";
			if(ConfigSubscriberWS.getSubscriberWsPort() != 443)
			{
				port = ":"+ConfigSubscriberWS.getSubscriberWsPort();
			}
		}
		else
		{
			protocol = "ws://";
			if(ConfigSubscriberWS.getSubscriberWsPort() != 80)
			{
				port = ":"+ConfigSubscriberWS.getSubscriberWsPort();
			}
		}	
		return String.format("%s%s%s%s", protocol, host, port, contextPath);
	}
}

