package com.planetbiru.gsm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.planetbiru.ServerWebSocketAdmin;
import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigModem;
import com.planetbiru.config.ConfigSMS;
import com.planetbiru.config.DataModem;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.util.Utility;

public class GSMUtil {
	
	private static final String NO_DEVICE_CONNECTED = "No device connected";
	private static final String MODEM_ID = "modemID";
	private static final String RESULT = "result";
	private static final String SENDER_TYPE = "senderType";
	private static final String MODEM_NAME = "modemName";
	private static final String MODEM_IMEI = "modemIMEI";
	private static final String RECEIVER = "receiver";
	private static final String MONITOR_PATH = "monitor.html";
	private static final String SMS_TRAFFIC = "sms-traffic";
	
	private static long lastDelete = 0;

	private static boolean initialized = false;
	private static List<GSMInstance> gsmInstance = new ArrayList<>();
	private static List<Integer> connectedDevices = new ArrayList<>();
	private static List<Integer> connectedDefaultDevices = new ArrayList<>();
	private static int counter = -1;
	private static int defaultCcounter = -1;
	private static Map<String, String> callerType = new HashMap<>();
	private static boolean hasPrefix = false;
	private static Map<String, ModemRouter> modemRouterList = new HashMap<>();
	private static boolean hasDefaultModem = false;
	private static boolean eventListener = true;
	private static Logger logger = Logger.getLogger(GSMUtil.class);
	

	private GSMUtil()
	{
		/**
		 * Do nothing
		 */
	}
	
	public static void start()
	{
		ConfigModem.load(Config.getModemSettingPath());
		GSMUtil.gsmInstance = new ArrayList<>();
		Map<String, DataModem> modemData = ConfigModem.getModemData();		
		for (Map.Entry<String, DataModem> entry : modemData.entrySet())
		{
			DataModem modem = entry.getValue();
			if(modem.isActive() && !modem.isInternetAccess())
			{
				GSMInstance instance = new GSMInstance(modem, eventListener);
				try 
				{
					instance.connect();
					GSMUtil.getGsmInstance().add(instance);
				} 
				catch (GSMException | InvalidPortException e) 
				{
					logger.error(e.getMessage());
				}
			}
		}
		GSMUtil.initialized = true;
		GSMUtil.updateConnectedDevice();
	}

	public static void connect(String modemID) throws GSMException, InvalidPortException
	{
		DataModem modem = ConfigModem.getModemData(modemID);
		boolean found = false;
		GSMInstance instance = new GSMInstance(modem, eventListener);
		for(int i = 0; i<GSMUtil.getGsmInstance().size(); i++)
		{
			instance =  GSMUtil.getGsmInstance().get(i);
			if(instance.getId().equals(modemID))
			{
				found = true;
				break;
			}
		}
		if(!found)
		{
			GSMUtil.getGsmInstance().add(instance);
		}
		instance.connect();	
		GSMUtil.updateConnectedDevice();
	}
	
	public static void disconnect(String modemID) throws GSMException 
	{
		try 
		{
			GSMUtil.get(modemID).disconnect();
			GSMUtil.updateConnectedDevice();		
		} 
		catch (GSMException e) 
		{
			logger.error(e.getMessage());
			GSMUtil.updateConnectedDevice();		
			throw new GSMException(e);
		}
	}
	
	public static List<SMS> readSMS(String modemID) throws GSMException
	{
		return GSMUtil.get(modemID).readSMS();
	}
	
	public static JSONArray readSMSJSON(String modemID) throws GSMException
	{
		JSONArray arr = new JSONArray();
		List<SMS> sms = GSMUtil.get(modemID).readSMS();
		for(int i = 0; i<sms.size(); i++)
		{
			arr.put(sms.get(i).toJSONObject());
		}
		return arr;
	}
	
	/**
	 * Send SMS with modem ID
	 * @param receiver
	 * @param message
	 * @param modemID
	 * @return
	 * @throws GSMException
	 */
	public static JSONObject sendSMS(String receiver, String message, String modemID) throws GSMException 
	{
		StackTraceElement ste = Thread.currentThread().getStackTrace()[3];
		if(GSMUtil.getGsmInstance().isEmpty())
		{
			GSMUtil.sendTraffic(receiver, ste);
			throw new GSMException(GSMUtil.NO_DEVICE_CONNECTED);
		}				
		DataModem modemData = ConfigModem.getModemData(modemID);
		if(ConfigSMS.isMonitorSMS())
		{
			GSMUtil.sendTraffic(receiver, ste, modemData);
		}		
		String result = GSMUtil.get(modemID).sendSMS(receiver, message, modemData);			
		JSONObject response = new JSONObject();
		response.put(GSMUtil.MODEM_ID, modemData.getId());
		response.put(GSMUtil.RESULT, result);
		return response;
	}
	
	/**
	 * Send SMS without modem ID
	 * @param receiver
	 * @param message
	 * @return
	 * @throws GSMException
	 */
	public static JSONObject sendSMS(String receiver, String message, StackTraceElement ste) throws GSMException 
	{
		if(GSMUtil.getGsmInstance().isEmpty())
		{
			GSMUtil.sendTraffic(receiver, ste);
			throw new GSMException(GSMUtil.NO_DEVICE_CONNECTED);
		}
		int index = GSMUtil.getModemIndex(receiver);
		GSMInstance instance = GSMUtil.getGsmInstance().get(index);	
		
		DataModem modemData = ConfigModem.getModemData(instance.getId());      
		String result = instance.sendSMS(receiver, message, modemData);
		if(ConfigSMS.isMonitorSMS())
		{
			GSMUtil.sendTraffic(receiver, ste, modemData);
		}
		JSONObject response = new JSONObject();
		response.put(GSMUtil.MODEM_ID, modemData.getId());
		response.put(GSMUtil.RESULT, result);
		return response;
	}
	
	public static void sendTraffic(String receiver, StackTraceElement ste)
	{
		String callerClass = ste.getClassName();
        JSONObject monitor = new JSONObject();
        JSONObject data = new JSONObject();
        data.put(GSMUtil.SENDER_TYPE, GSMUtil.getSenderType(callerClass));
        data.put(GSMUtil.RECEIVER, receiver);
               
        monitor.put(JsonKey.COMMAND, GSMUtil.SMS_TRAFFIC);
        monitor.put(JsonKey.DATA, data);      
        ServerWebSocketAdmin.broadcastMessage(monitor.toString(), GSMUtil.MONITOR_PATH);
	}
	
	public static void sendTraffic(String receiver, StackTraceElement ste, DataModem modemData)
	{
		String modemID = modemData.getId();
		String modemName = modemData.getName();
		String modemIMEI = modemData.getImei();

		String callerClass = ste.getClassName();
        JSONObject monitor = new JSONObject();
        JSONObject data = new JSONObject();
        data.put(GSMUtil.MODEM_ID, modemID);
        data.put(GSMUtil.MODEM_NAME, modemName);
        data.put(GSMUtil.MODEM_IMEI, modemIMEI);
        data.put(GSMUtil.SENDER_TYPE, GSMUtil.getSenderType(callerClass));
        data.put(GSMUtil.RECEIVER, Utility.maskMSISDN(receiver));
        monitor.put(JsonKey.COMMAND, GSMUtil.SMS_TRAFFIC);
        monitor.put(JsonKey.DATA, data);      
        ServerWebSocketAdmin.broadcastMessage(monitor.toString(), GSMUtil.MONITOR_PATH);
	}
	
	public static String getSenderType(String callerClass) 
	{
		String key = Utility.getClassName(callerClass);
		return GSMUtil.getCallerType().getOrDefault(key, "");
	}

	public static USSDParser executeUSSD(String ussd, String modemID) throws GSMException 
	{
		if(GSMUtil.getGsmInstance().isEmpty())
		{
			throw new GSMException(GSMUtil.NO_DEVICE_CONNECTED);
		}
		GSMInstance instance = GSMUtil.get(modemID);		
		if(instance.isConnected())
		{
			return instance.executeUSSD(ussd);
		}
		else
		{
			throw new GSMException("The selected device is not connected");
		}
	}

	public static GSMInstance get(String modemID) throws GSMException 
	{
		for(int i = 0; i<GSMUtil.getGsmInstance().size(); i++)
		{
			GSMInstance instance =  GSMUtil.getGsmInstance().get(i);
			if(instance.getId().equals(modemID))
			{
				return instance;
			}
		}
		throw new GSMException(GSMUtil.NO_DEVICE_CONNECTED);
	}


	public static boolean isConnected() 
	{
		if(GSMUtil.getGsmInstance().isEmpty())
		{
			return false;
		}
		return GSMUtil.initialized && countConnected() > 0;
	}
	
	public static int countConnected()
	{
		int connected = 0;
		for(int i = 0; i < GSMUtil.getGsmInstance().size(); i++)
		{
			if(GSMUtil.getGsmInstance().get(i).isConnected())
			{
				connected++;
			}
		}
		return connected;
	}
	
	public static boolean isConnected(String modemID)
	{
		for(int i = 0; i < GSMUtil.getGsmInstance().size(); i++)
		{
			if(GSMUtil.getGsmInstance().get(i).getId().equals(modemID) && GSMUtil.getGsmInstance().get(i).isConnected())
			{
				return true;
			}
		}
		return false;
	}
	
	public static boolean isConnected(int index)
	{
		if(GSMUtil.getGsmInstance().isEmpty())
		{
			return false;
		}
		return GSMUtil.getGsmInstance().get(index).isConnected();
	}
	
	public static boolean isHasPrefix(int index)
	{
		if(GSMUtil.getGsmInstance().isEmpty())
		{
			return false;
		}
		String modemID = GSMUtil.getGsmInstance().get(index).getId();
		return ConfigModem.getModemData(modemID).getRecipientPrefix().length() > 0;
	}
	
	/**
	 * Update Connected Device
	 */
	public static void updateConnectedDevice() {
		GSMUtil.reindexInstantce();
		GSMUtil.hasPrefix = false;
		List<Integer> connectedDev = new ArrayList<>();
		List<Integer> connectedDefaultDev = new ArrayList<>();
		GSMUtil.modemRouterList = new HashMap<>();
		for(int i = 0; i<GSMUtil.getGsmInstance().size(); i++)
		{
			if(GSMUtil.isConnected(i))
			{
				
				String modemID = GSMUtil.getGsmInstance().get(i).getId();
				DataModem modemData = ConfigModem.getModemData(modemID);
				
				if(modemData.isSmsAPI())
				{
					connectedDev.add(i);
				}
				
				if(modemData.isDefaultModem() && modemData.isSmsAPI())
				{
					connectedDefaultDev.add(i);
				}
				if(modemData.getRecipientPrefix().length() > 0)
				{
					List<String> prefixes = modemData.getRecipientPrefixList();
					GSMUtil.addRecipientPrefix(prefixes, i);
					GSMUtil.hasPrefix = true;
				}
			}
		}
		GSMUtil.connectedDevices = connectedDev;
		GSMUtil.connectedDefaultDevices = connectedDefaultDev;
	}
	
	
	private static void addRecipientPrefix(List<String> prefixes, int index) {
		for(int i = 0; i<prefixes.size(); i++)
		{
			String prefix = prefixes.get(i);
			if(GSMUtil.modemRouterList.containsKey(prefix))
			{
				ModemRouter route = GSMUtil.modemRouterList.getOrDefault(prefix, new ModemRouter());
				route.addIndex(index);
			}
			else
			{
				GSMUtil.modemRouterList.put(prefix, new ModemRouter(index));
			}
		}
		
	}

	private static int getModemIndex(String receiver) throws GSMException {
		if(GSMUtil.hasPrefix)
		{
			try 
			{
				return GSMUtil.getRouterIndex(receiver);
			} 
			catch (GSMException | InvalidModemRouterException e) 
			{
				return GSMUtil.getModemIndex();
			}
		}
		else if(GSMUtil.hasDefaultModem)
		{
			return GSMUtil.getModemIndexDefault();
		}
		else
		{
			return GSMUtil.getModemIndex();
		}
	}

	private static int getRouterIndex(String receiver) throws GSMException, InvalidModemRouterException {
		String prefix = GSMUtil.getPrefix(receiver);
		if(GSMUtil.modemRouterList.containsKey(prefix))
		{
			try 
			{
				return GSMUtil.modemRouterList.get(prefix).getIndex();
			} 
			catch (InvalidModemRouterException e) 
			{
				return GSMUtil.getModemIndex();
			}
		}
		else
		{
			return GSMUtil.getModemIndex();
		}
	}

	private static String getPrefix(String receiver) throws InvalidModemRouterException 
	{
		int length = ConfigSMS.getRecipientPrefixLength();
		if(length <= 0)
		{
			throw new InvalidModemRouterException("Recipient prefix length must be greater than zero");
		}
		if(receiver.isEmpty())
		{
			throw new InvalidModemRouterException("Recipient can not be empty");
		}
		String recv;
		try 
		{
			recv = Utility.canonicalMSISDN(receiver);
		} 
		catch (GSMException e) 
		{
			throw new InvalidModemRouterException(e.getMessage());
		}
		if(recv.length() >= length)
		{
			return recv.substring(0, length);
		}
		return recv;
	}

	private static int getModemIndex() throws GSMException {
		GSMUtil.counter++;
		if(GSMUtil.counter >= GSMUtil.connectedDevices.size())
		{
			GSMUtil.counter = 0;
		}
		if(!GSMUtil.connectedDevices.isEmpty() && GSMUtil.connectedDevices.size() >= (GSMUtil.counter - 1))
		{
			return GSMUtil.connectedDevices.get(GSMUtil.counter);
		}
		else
		{
			throw new GSMException(GSMUtil.NO_DEVICE_CONNECTED);
		}
	}
	
	private static int getModemIndexDefault() throws GSMException {
		GSMUtil.defaultCcounter++;
		if(GSMUtil.defaultCcounter >= GSMUtil.countConnected())
		{
			GSMUtil.defaultCcounter = 0;
		}
		if(!GSMUtil.connectedDefaultDevices.isEmpty() && GSMUtil.connectedDefaultDevices.size() >= (GSMUtil.defaultCcounter -1))
		{
			return GSMUtil.connectedDefaultDevices.get(GSMUtil.defaultCcounter);
		}
		else
		{
			throw new GSMException(GSMUtil.NO_DEVICE_CONNECTED);
		}
	}

	public static String getModemName(String modemID) {
		DataModem modemData = ConfigModem.getModemData(modemID);
		return modemData.getName();
	}

	public static Map<String, String> getCallerType() {
		return callerType;
	}

	public static void setCallerType(Map<String, String> callerType) {
		GSMUtil.callerType = callerType;
	}

	private static void reindexInstantce() {
		for(int i = 0; i < GSMUtil.getGsmInstance().size(); i++)
		{
			if(GSMUtil.getGsmInstance().get(i).isConnected() && !ConfigModem.getModemData(GSMUtil.getGsmInstance().get(i).getId()).isActive())
			{
				try 
				{
					GSMUtil.getGsmInstance().get(i).disconnect();
				} 
				catch (GSMException e) 
				{
					/**
					 * Do nothing
					 */
					logger.error(e.getMessage());
				}
			}
		}		
	}

	public static List<GSMInstance> getGsmInstance() {
		return gsmInstance;
	}

	public static long getLastDelete() {
		return lastDelete;
	}

	public static void setLastDelete(long lastDelete) {
		GSMUtil.lastDelete = lastDelete;
	}

	public static JSONObject getInstalledModemInfo(String port) {
		GSMInstance instance;
		boolean addHock = false;
		JSONObject info = new JSONObject();
		try 
		{
			instance = GSMUtil.getGSMInstanceByPort(port);	
		} 
		catch (ModemNotFoundException e) 
		{
			instance = new GSMInstance(port, eventListener);
			addHock = true;
		}		
		try 
		{
			if(!instance.isConnected())
			{
				instance.connect();
			}
			String manufacturer = instance.getManufacturer();
			info.put("manufacturer", manufacturer);
			String model = instance.getModel();
			info.put("model", model);
			String revision = instance.getRevision();
			info.put("revision", revision);
			String imei = instance.getIMEI();
			info.put("imei", imei);
			String imsi = instance.getIMSI();
			info.put("imsi", imsi);
			String iccid = instance.getICCID();
			info.put("iccid", iccid);
			String networkRegistration = instance.getNetworkRegistration();
			info.put("networkRegistration", networkRegistration);
			String msisdn = instance.getMSISDN();			
			info.put("msisdn", msisdn);
			String operatorSelect = instance.getOperatorSelect();			
			info.put("operatorSelect", operatorSelect);
			String smsCenter = instance.getSMSCenter();		
			info.put("smsCenter", smsCenter);
		   	logger.info("info = "+info.toString(4));
		    
			if(addHock)
			{
				instance.disconnect();
			}
			
		} 
		catch (GSMException | InvalidPortException e) 
		{
			e.printStackTrace();
			/**
			 * Do nothing
			 */
		}
		return info;
	}

	private static GSMInstance getGSMInstanceByPort(String port) throws ModemNotFoundException {
		for(int i = 0; i < GSMUtil.getGsmInstance().size(); i++)
		{
			if(GSMUtil.getGsmInstance().get(i).getPort().equals(port))
			{
				return GSMUtil.getGsmInstance().get(i);
			}
		}
		throw new ModemNotFoundException("No modem use port "+port);
	}

}




