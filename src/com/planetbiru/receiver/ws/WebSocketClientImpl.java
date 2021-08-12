package com.planetbiru.receiver.ws;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import com.planetbiru.api.MessageAPI;
import com.planetbiru.config.ConfigSubscriberWS;
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
	public void evtOnMessage(String message) {
		try
		{
            MessageAPI api = new MessageAPI();
            api.processRequest(message);            
		}
		catch(JSONException e)
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
		if(this.reconnect)
		{
			this.reconnect = false;
			this.restartThread();
		}
	}
	public void evtOnError(Exception e)
	{
		if(this.reconnect)
		{
			this.reconnect = false;
			this.restartThread();
		}
	}

	public void initWSClient() throws URISyntaxException
	{
		String endpoint = this.createWSEndpoint();
		endpoint = this.fixWSEndpoint(endpoint);
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
			    	evtOnMessage(message);
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
	
	private String fixWSEndpoint(String endpoint) {
		String topic = Utility.urlEncode(ConfigSubscriberWS.getSubscriberWsTopic());
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
					if(params.containsKey("topic"))
					{
						params.remove("topic");
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
		params.put("topic", Utility.asList(topic));
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

