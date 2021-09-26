package com.planetbiru.gsm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fazecast.jSerialComm.SerialPort;
import com.planetbiru.config.ConfigGeneral;
import com.planetbiru.config.ConfigModem;
import com.planetbiru.config.DataModem;

public class ModemInspector extends Thread {

	private boolean running = true;
	private String lastList = "";
	private Map<String, Boolean> lastConnection = new HashMap<>();
	private long delay;
	public ModemInspector(long delay) {
		this.delay = delay;
	}

	@Override
	public void run()
	{
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
				this.inspectSerialPort();			
			}
		}
	}
	
	public void stopService()
	{
		this.running = false;
	}

	private void inspectSerialPort() {
		SerialPort[] ports = SerialPort.getCommPorts();
        List<String> portList = new ArrayList<>();
        for (SerialPort port : ports) 
        {
        	portList.add(port.getSystemPortName());
        }
        
        Collections.sort(portList);
        
        String currentList = String.join(",", portList);
        
        
        if(!this.lastList.equals(currentList))
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
	}

	private void addNewPort(String[] arr) {
		List<String> newList = new ArrayList<>();
		for (Map.Entry<String, Boolean> entry : this.lastConnection.entrySet())
		{
			newList.add(entry.getKey());
		}
		this.lastConnection = new HashMap<>();
		Map<String, DataModem> modemData = ConfigModem.getModemData();	
		for(int i = 0; i<arr.length; i++)
		{
			try 
			{
				String port = arr[i];				
				if(!newList.contains(port) && this.isUsed(modemData, port))
				{
					this.reconnectModem(modemData, port);
				}
				
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

	private void removeInactivePort(String[] arr) {
		/**
		 * Remove current list
		 */
		for (Map.Entry<String, Boolean> entry : this.lastConnection.entrySet())
		{
			String port = entry.getKey();
			boolean connected = entry.getValue().booleanValue();
			if(connected && this.indexOf(arr, port) == -1)
			{
				this.lastConnection.remove(port);
			}
		}	
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
			if(modem.getPort().equals(port))
			{
				return true;
			}
		}
		return false;
	}
}