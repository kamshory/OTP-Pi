package com.planetbiru.gsm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fazecast.jSerialComm.SerialPort;
import com.planetbiru.ServerWebSocketAdmin;
import com.planetbiru.config.ConfigGeneral;
import com.planetbiru.config.ConfigModem;
import com.planetbiru.config.DataModem;
import com.planetbiru.util.ServerInfo;

public class ModemInspector extends Thread {

	private boolean running = true;
	private String lastList = "";
	private Map<String, Boolean> lastConnection = new HashMap<>();
	private long delay;
	
	public ModemInspector(long delay) 
	{
		this.delay = delay;
	}

	@Override
	public void run()
	{
		this.initPort();
		if(this.delay > 0)
		{
			try 
			{
				Thread.sleep(this.delay);
			} 
			catch (InterruptedException e) 
			{
				Thread.currentThread().interrupt();
			}
		}
		long interval = ConfigGeneral.getInspectModemInterval();
		if(interval >= 1000)
		{		
			while(this.running)
			{
				try 
				{
					Thread.sleep(interval);
				} 
				catch (InterruptedException e) 
				{
					Thread.currentThread().interrupt();
				}
				this.inspectSerialPort(true);			
			}
		}
	}
	
	private void initPort() {
		this.inspectSerialPort(false);	
		String[] arr = this.lastList.split(",");
		for(int i = 0; i<arr.length; i++)
		{
			try 
			{
				String port = arr[i];				
				GSMInstance instance = GSMUtil.getGSMInstanceByPort(port);				
				this.lastConnection.put(port, instance.isConnected());
			} 
			catch (ModemNotFoundException e) 
			{
				/**
				 * Do nothing
				 */
			}
		}	
	}

	public void stopService()
	{
		this.running = false;
	}

	private void inspectSerialPort(boolean addToList) {
		SerialPort[] ports = SerialPort.getCommPorts();
        List<String> portList = new ArrayList<>();
        for (SerialPort port : ports) 
        {
        	portList.add(port.getSystemPortName());
        }       
        Collections.sort(portList);       
        String currentList = String.join(",", portList);      
        if(addToList && !this.lastList.equals(currentList))
        {
        	this.processUpdate(currentList);
        }
		this.lastList = currentList;
	}
	
	private int indexOf(String[] haystack, String item)
	{
		for(int i = 0; i<haystack.length; i++)
		{
			if(haystack[i] != null && haystack[i].equals(item))
			{
				return i;
			}
		}
		return -1;
	}

	private void processUpdate(String currentList) {
		String[] arr = currentList.split(",");
		this.removeInactivePort(arr);	
		this.addNewPort(arr);
		GSMUtil.updateConnectedDevice();
		ServerInfo.sendModemStatus();
	}

	private void addNewPort(String[] arr) {
		List<String> newList = new ArrayList<>();
		for (Map.Entry<String, Boolean> entry : this.lastConnection.entrySet())
		{
			newList.add(entry.getKey());
		}
		this.lastConnection = new HashMap<>();
		Map<String, DataModem> modemData = ConfigModem.getModemData();	
		boolean connecting = false;
		for(int i = 0; i<arr.length; i++)
		{
			connecting = false;			
			try 
			{
				String port = arr[i];				
				if(!newList.contains(port) && this.isUsed(modemData, port))
				{
					this.reconnectModem(modemData, port);
					connecting = true;				
				}			
				GSMInstance instance = GSMUtil.getGSMInstanceByPort(port);
				if(connecting)
				{
					Thread.sleep(100);
					String result = instance.testAT();
					if(result.contains("OK"))
					{
						this.sendNotification(port, true, null);
					}
					else
					{
						this.sendNotification(port, true, "Please check connection for "+instance.getModem().getName()+" on port "+port);
					}
				}			
				this.lastConnection.put(port, instance.isConnected());
			} 
			catch (ModemNotFoundException | GSMException | SerialPortConnectionException e) 
			{
				/**
				 * Do nothing
				 */
			} 
			catch (InterruptedException e) 
			{
				Thread.currentThread().interrupt();
			}
		}		
	}

	private void removeInactivePort(String[] arr) {
		/**
		 * Remove current list
		 */
		for (Map.Entry<String, Boolean> entry : this.lastConnection.entrySet())
		{
			String port = entry.getKey();
			boolean connected = entry.getValue().booleanValue();
			try {
				GSMInstance instance = GSMUtil.getGSMInstanceByPort(port);
				if(connected 
						&& this.indexOf(arr, port) == -1 
						&& instance != null 
						&& instance.getModem() != null 
						&& instance.getModem().isActive() 
						&& !instance.getModem().isInternetAccess())
				{
					this.disconnectModem(port);
					this.sendNotification(port, false, null);
				}
			} 
			catch (ModemNotFoundException e) 
			{
				/**
				 * Do nothing
				 */
			}
			this.lastConnection.remove(port);
			
		}	
	}
	
	private void disconnectModem(String port) {
		try 
		{
			GSMInstance instance = GSMUtil.getGSMInstanceByPort(port);
			instance.disconnect();
			ServerInfo.sendModemStatus();
		} 
		catch (ModemNotFoundException | GSMException e) 
		{
			/**
			 * Do nothing
			 */
		}		
	}

	private void sendNotification(String port, boolean connect, String notif) {
		String message = "";
		DataModem modemData = ConfigModem.getModemDataByPort(port);
		if(notif != null)
		{
			message = notif;
		}
		else
		{
			if(modemData == null)
			{
				if(connect)
				{
					message = "Connecting "+port;
				}
				else
				{
					message = "Disconnecting "+port;
				}
			}
			else
			{
				String fmt = "%s %s (%s)";
				if(connect)
				{
					message = String.format(fmt, "Connecting", modemData.getName(), modemData.getPort());
				}
				else
				{
					message = String.format(fmt, "Disconnecting", modemData.getName(), modemData.getPort());
				}
			}
		}
		JSONObject jsonMessage = new JSONObject();
		JSONObject item = new JSONObject();
		item.put("message", message);
		jsonMessage.put("command", "broadcast-message");
		JSONArray data = new JSONArray();
		data.put(item);
		jsonMessage.put("data", data);
		ServerWebSocketAdmin.broadcastMessage(jsonMessage.toString());
		
	}

	private void reconnectModem(Map<String, DataModem> modemData, String port) {
		for (Map.Entry<String, DataModem> entry : modemData.entrySet())
		{
			DataModem modem = entry.getValue();
			if(modem.getPort().equals(port))
			{
				try 
				{
					GSMUtil.reconnectModem(modem.getId());
				} 
				catch (GSMException e) 
				{
					/**
					 * Do nothing
					 */
				}
			}
		}		
	}

	private boolean isUsed(Map<String, DataModem> modemData, String port) {
		for (Map.Entry<String, DataModem> entry : modemData.entrySet())
		{
			DataModem modem = entry.getValue();
			if(modem.getPort().equals(port) && modem.isActive() && !modem.isInternetAccess())
			{
				return true;
			}
		}
		return false;
	}
}
