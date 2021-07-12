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
import org.json.JSONObject;

import com.planetbiru.api.RESTAPI;
import com.planetbiru.config.ConfigFeederWS;
import com.planetbiru.gsm.GSMException;
import com.planetbiru.gsm.GSMUtil;
import com.planetbiru.util.Utility;

public class WebSocketClientImpl extends Thread{
	private long reconnectDelay = 10000;
	private boolean stopend = false;
	private WebSocketClient wsClient;
	private WebSocketTool webSocketTool;
	private boolean reconnect = false;
	
	public WebSocketClientImpl(long reconnectDelay, WebSocketTool webSocketTool, boolean reconnect)
	{
		this.reconnectDelay = reconnectDelay;
		this.webSocketTool = webSocketTool;
		this.reconnect = reconnect;
	}	

	@Override
	public void run()
	{
		if(ConfigFeederWS.isFeederWsEnable())
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
					Thread.sleep(300000);
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
			Thread.sleep(reconnectDelay);
		} 
		catch (InterruptedException e) 
		{
			Thread.currentThread().interrupt();
		}
		this.webSocketTool.restartThread();
	}
	public void evtOnMessage(String message) {
		System.out.println("Receive "+message);
		try
		{
			JSONObject jsonObj = new JSONObject(message);
			if(!jsonObj.isEmpty())
			{
				String command = jsonObj.optString("command", "");
				if(command.equals("send-sms"))
				{
					this.sendSMS(jsonObj);
				}
				else if(command.equals("block-msisdn"))
				{
					this.blockMSISDN(jsonObj);
				}
				else if(command.equals("unblock-msisdn"))
				{
					this.unblockMSISDN(jsonObj);
				}			
			}
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
		if(serverHandshake.getHttpStatus() != 101)
		{
			if(this.reconnect)
			{
				this.reconnect = false;
				this.restartThread();
			}
		}
		System.out.println("STATUS "+serverHandshake.getHttpStatus());
		System.out.println("onOpen()");
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
			headers.put("Authorization", Utility.basicAuth(ConfigFeederWS.getFeederWsUsername(), ConfigFeederWS.getFeederWsPassword()));
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
	
	

	private void blockMSISDN(JSONObject jsonObj) {
		String command = jsonObj.optString("command", "");
		JSONObject data = jsonObj.optJSONObject("data");
		String msisdn = data.optString("msisdn", "");
		try 
		{
			RESTAPI.blockMSISDN(command, msisdn);
		} 
		catch (GSMException e) 
		{
			e.printStackTrace();
		}
	}
	
	private void unblockMSISDN(JSONObject jsonObj) {
		String command = jsonObj.optString("command", "");
		JSONObject data = jsonObj.optJSONObject("data");
		String msisdn = data.optString("msisdn", "");
		try 
		{
			RESTAPI.unblockMSISDN(command, msisdn);
		} 
		catch (GSMException e) 
		{
			e.printStackTrace();
		}
	}
	
	private void sendSMS(JSONObject jsonObj) {
		JSONObject data = jsonObj.optJSONObject("data");
		String msisdn = data.optString("msisdn", "");
		String message = data.optString("message", "");
		try 
		{
			this.sendSMS(msisdn, message);
		} 
		catch (GSMException e) 
		{
			e.printStackTrace();
		}	
	}
	
	private void sendSMS(String receiver, String textMessage) throws GSMException 
	{
		GSMUtil.sendSMS(receiver, textMessage);	
	}
	
	private String fixWSEndpoint(String endpoint) {
		String channel = Utility.urlEncode(ConfigFeederWS.getFeederWsChannel());
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
					if(params.containsKey("channel"))
					{
						params.remove("channel");
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
		params.put("channel", Utility.asList(channel));
		query = Utility.buildQuery(params);
		
		endpoint = path + "?"+query;
		return endpoint;	
	}

	private String createWSEndpoint() {
		String protocol = "";
		String host = ConfigFeederWS.getFeederWsAddress();
		String port = "";
		String contextPath = ConfigFeederWS.getFeederWsPath();
		if(!contextPath.startsWith("/"))
		{
			contextPath = "/"+contextPath;
		}
		if(ConfigFeederWS.isFeederWsSSL())
		{
			protocol = "wss://";
			if(ConfigFeederWS.getFeederWsPort() != 443)
			{
				port = ":"+ConfigFeederWS.getFeederWsPort();
			}
		}
		else
		{
			protocol = "ws://";
			if(ConfigFeederWS.getFeederWsPort() != 80)
			{
				port = ":"+ConfigFeederWS.getFeederWsPort();
			}
		}	
		return String.format("%s%s%s%s", protocol, host, port, contextPath);
	}
}

