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
import com.planetbiru.web.HttpUtil;

public class GSMUtil {
	
	private static final String NO_DEVICE_CONNECTED = "No device connected";
	private static final String MODEM_ID = "modemID";
	private static final String RESULT = JsonKey.RESULT;
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
	
	/**
	 * Private constructor. Prevent instance creation
	 */
	private GSMUtil()
	{
		/**
		 * Do nothing
		 */
	}
	
	/**
	 * Initialize GSMUtil
	 */
	public static void start()
	{
		ConfigModem.load(Config.getModemSettingPath());
		GSMUtil.gsmInstance = new ArrayList<>();
		Map<String, DataModem> modemData = ConfigModem.getModemData();		
		for (Map.Entry<String, DataModem> entry : modemData.entrySet())
		{
			DataModem dataModem = entry.getValue();
			if(dataModem.isActive() && !dataModem.isInternetAccess())
			{
				boolean exists = GSMUtil.hasGSMInstanceID(dataModem.getId());				
				GSMInstance instance = GSMUtil.getOrCreate(dataModem, GSMUtil.eventListener, exists);				
				try 
				{
					GSMUtil.connectIfRequired(dataModem, instance, exists);
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
	
	/**
	 * Connect to modem if required
	 * @param dataModem DataModem
	 * @param instance GSMInstance
	 * @param exists Flag if GSMInstance already exists or not 
	 * @throws GSMException If any GSMException
	 * @throws InvalidPortException if any InvalidPortException
	 */
	private static void connectIfRequired(DataModem dataModem, GSMInstance instance, boolean exists) throws GSMException, InvalidPortException
	{
		if(instance != null)
		{
			String pin = dataModem.getSimCardPIN();
			if(pin.isEmpty())
			{
				pin = null;
			}
			instance.connect(pin);
		}
		if(!exists)
		{
			GSMUtil.getGSMInstance().add(instance);
		}
	}

	private static GSMInstance getOrCreate(DataModem dataModem, boolean eventListener, boolean exists) {
		GSMInstance instance;
		if(exists)
		{
			instance = GSMUtil.getGSMIntance(dataModem.getId());
			
		}
		else
		{
			instance = new GSMInstance(dataModem, eventListener);
		}
		return instance;
	}

	public static void removeInstanceByPort(String port) {
		try 
		{
			GSMInstance instance = GSMUtil.getGSMInstanceByPort(port);
			GSMUtil.removeModem(instance);
		}
		catch (ModemNotFoundException e) 
		{
			logger.error(e.getMessage());
		}
		
	}

	private static void removeModem(GSMInstance instance) {
		if(instance != null)
		{
			GSMUtil.gsmInstance.remove(instance);
			GSMUtil.updateConnectedDevice();
		}
	}

	public static void reconnectModem(GSMInstance instance) throws GSMException {
		if(instance != null)
		{
			DataModem dataModem;
			try 
			{
				dataModem = (DataModem) instance.getModem().clone();
				instance.disconnect();
				String pin = dataModem.getSimCardPIN();				
				if(pin.isEmpty())
				{
					pin = null;
				}
				GSMUtil.gsmInstance.remove(instance);
				boolean connected = false;
				instance = new GSMInstance(dataModem, GSMUtil.eventListener);				
				connected = instance.connect(pin);
				if(connected)
				{
					GSMUtil.gsmInstance.add(instance);
				}
			} 
			catch (GSMException | InvalidPortException | CloneNotSupportedException e) 
			{
				logger.error(e.getMessage());
			}			
		}	
		GSMUtil.updateConnectedDevice();
	}
	
	public static void reconnectModem(String modemID) throws GSMException {
		GSMInstance instance = GSMUtil.getGSMIntance(modemID);
		GSMUtil.reconnectModem(instance);
	}
	
	public static void stop() {
		Map<String, DataModem> modemData = ConfigModem.getModemData();		
		for (Map.Entry<String, DataModem> entry : modemData.entrySet())
		{
			DataModem modem = entry.getValue();
			if(modem.isActive() && !modem.isInternetAccess())
			{
				GSMInstance instance = GSMUtil.getGSMIntance(modem.getId());
				if(instance != null && instance.isConnected())
				{
					try 
					{
						instance.disconnect();
					} 
					catch (GSMException e) 
					{
						logger.error(e.getMessage());
					}
				}
			}
		}
		GSMUtil.updateConnectedDevice();	
	}

	/**
	 * Connect specified modem
	 * @param modemID Modem ID
	 * @throws GSMException if any GSM errors
	 * @throws InvalidPortException if serial port is invalid
	 * @throws InvalidSIMPinException 
	 */
	public static void connect(String modemID) throws GSMException, InvalidPortException
	{
		DataModem dataModem = ConfigModem.getModemData(modemID);
		boolean found = false;
		GSMInstance instance = new GSMInstance(dataModem, GSMUtil.eventListener);
		for(int i = 0; i<GSMUtil.getGSMInstance().size(); i++)
		{
			instance =  GSMUtil.getGSMInstance().get(i);
			if(instance.getModemID().equals(modemID))
			{
				found = true;
				break;
			}
		}
		if(!found)
		{
			GSMUtil.getGSMInstance().add(instance);
		}
		String pin = dataModem.getSimCardPIN();
		if(pin.isEmpty())
		{
			pin = null;
		}
		instance.connect(pin);
		GSMUtil.updateConnectedDevice();
	}
	
	/**
	 * Disconnect specified modem
	 * @param modemID Modem ID
	 * @throws GSMException if any GSM errors
	 */
	public static void disconnect(String modemID) throws GSMException 
	{
		try 
		{
			GSMUtil.get(modemID).disconnect();
			GSMUtil.updateConnectedDevice();		
		} 
		catch (GSMException e) 
		{
			GSMUtil.updateConnectedDevice();		
			throw new GSMException(e);
		}
	}
	
	/**
	 * Read SMS from specified modem
	 * @param modemID Modem ID
	 * @return List of SMS
	 * @throws GSMException if any GSM errors
	 * @throws InvalidSIMPinException 
	 * @throws SerialPortConnectionException 
	 */
	public static List<SMS> readSMS(String modemID) throws GSMException, InvalidSIMPinException, SerialPortConnectionException
	{
		return GSMUtil.get(modemID).readSMS();
	}
	
	/**
	 * Read SMS from specified modem
	 * @param modemID Modem ID
	 * @return JSONArray contains SMS
	 * @throws GSMException if any GSM errors
	 * @throws InvalidSIMPinException 
	 * @throws SerialPortConnectionException 
	 */
	public static JSONArray readSMSJSON(String modemID) throws GSMException, InvalidSIMPinException, SerialPortConnectionException
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
	 * Send SMS with from specified modem
	 * @param receiver The SMS recipient
	 * @param message The text message
	 * @param modemID Modem ID
	 * @return JSONObject contains sending SMS response
	 * @throws GSMException if any GSM errors
	 * @throws InvalidSIMPinException 
	 * @throws SerialPortConnectionException 
	 */
	public static JSONObject sendSMS(String receiver, String message, String modemID) throws GSMException, InvalidSIMPinException
	{
		StackTraceElement ste = Thread.currentThread().getStackTrace()[3];
		if(GSMUtil.getGSMInstance().isEmpty())
		{
			GSMUtil.sendTraffic(receiver, ste);
			throw new GSMException(GSMUtil.NO_DEVICE_CONNECTED);
		}				
		DataModem modemData = ConfigModem.getModemData(modemID);
		if(ConfigSMS.isMonitorSMS())
		{
			GSMUtil.sendTraffic(receiver, ste, modemData);
		}		
		String result = "";
		try 
		{
			result = GSMUtil.get(modemID).sendSMS(receiver, message, modemData);
		} 
		catch (SerialPortConnectionException e) 
		{
			reconnectModem(modemID);
		}
		
		JSONObject response = new JSONObject();
		response.put(GSMUtil.MODEM_ID, modemData.getId());
		response.put(GSMUtil.RESULT, result);
		return response;
	}

	/**
	 * Send SMS without specified modem
	 * @param receiver The SMS recipient
	 * @param message The text message
	 * @return JSONObject contains sending SMS response
	 * @throws GSMException if any GSM errors
	 * @throws InvalidSIMPinException 
	 * @throws SerialPortConnectionException 
	 * @throws InvalidPortException 
	 */
	public static JSONObject sendSMS(String receiver, String message, StackTraceElement ste) throws GSMException, InvalidSIMPinException, SerialPortConnectionException
	{
		if(GSMUtil.getGSMInstance().isEmpty())
		{
			GSMUtil.sendTraffic(receiver, ste);
			throw new GSMException(GSMUtil.NO_DEVICE_CONNECTED);
		}
		int index = GSMUtil.getModemIndex(receiver);
		GSMInstance instance = GSMUtil.getGSMInstance().get(index);	
		
		DataModem modemData = ConfigModem.getModemData(instance.getModemID());      
		String result = "";
		try 
		{
			result = instance.sendSMS(receiver, message, modemData);
		} 
		catch (SerialPortConnectionException e) 
		{
			GSMUtil.reconnectModem(instance);
			throw new SerialPortConnectionException(e);
		}
		
		if(ConfigSMS.isMonitorSMS())
		{
			GSMUtil.sendTraffic(receiver, ste, modemData);
		}
		JSONObject response = new JSONObject();
		response.put(GSMUtil.MODEM_ID, modemData.getId());
		response.put(GSMUtil.RESULT, result);
		return response;
	}
	
	/**
	 * Send SMS traffic to administrator web
	 * @param receiver The SMS recipient
	 * @param ste
	 */
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
	
	public static void sendTraffic(String receiver, StackTraceElement ste, DataModem dataModem)
	{
		String modemID = dataModem.getId();
		String modemName = dataModem.getName();
		String modemIMEI = dataModem.getImei();

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
		if(GSMUtil.getGSMInstance().isEmpty())
		{
			throw new GSMException(GSMUtil.NO_DEVICE_CONNECTED);
		}
		GSMInstance instance = GSMUtil.get(modemID);
		try
		{
			if(instance.isConnected())
			{
				return instance.executeUSSD(ussd);
			}
			else
			{
				throw new GSMException("The selected device is not connected");
			}
		}
		catch(SerialPortConnectionException e)
		{
			GSMUtil.reconnectModem(instance);
			return new USSDParser();
		}
	}

	public static GSMInstance get(String modemID) throws GSMException 
	{
		for(int i = 0; i<GSMUtil.getGSMInstance().size(); i++)
		{
			GSMInstance instance =  GSMUtil.getGSMInstance().get(i);
			if(instance.getModemID().equals(modemID))
			{
				return instance;
			}
		}
		throw new GSMException(GSMUtil.NO_DEVICE_CONNECTED);
	}


	public static boolean isConnected() 
	{
		if(GSMUtil.getGSMInstance().isEmpty())
		{
			return false;
		}
		return GSMUtil.initialized && countConnected() > 0;
	}
	
	public static int countConnected()
	{
		int connected = 0;
		for(int i = 0; i < GSMUtil.getGSMInstance().size(); i++)
		{
			if(GSMUtil.getGSMInstance().get(i).isConnected())
			{
				connected++;
			}
		}
		return connected;
	}
	
	public static boolean isConnected(String modemID)
	{
		for(int i = 0; i < GSMUtil.getGSMInstance().size(); i++)
		{
			if(GSMUtil.getGSMInstance().get(i).getModemID().equals(modemID) && GSMUtil.getGSMInstance().get(i).isConnected())
			{
				return true;
			}
		}
		return false;
	}
	
	public static boolean isConnected(int index)
	{
		if(GSMUtil.getGSMInstance().isEmpty())
		{
			return false;
		}
		return GSMUtil.getGSMInstance().get(index).isConnected();
	}
	
	public static boolean isHasPrefix(int index)
	{
		if(GSMUtil.getGSMInstance().isEmpty())
		{
			return false;
		}
		String modemID = GSMUtil.getGSMInstance().get(index).getModemID();
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
		for(int i = 0; i<GSMUtil.getGSMInstance().size(); i++)
		{
			if(GSMUtil.isConnected(i))
			{
				
				String modemID = GSMUtil.getGSMInstance().get(i).getModemID();
				DataModem dataModem = ConfigModem.getModemData(modemID);
				
				if(dataModem.isSmsAPI())
				{
					connectedDev.add(i);
				}
				
				if(dataModem.isDefaultModem() && dataModem.isSmsAPI())
				{
					connectedDefaultDev.add(i);
				}
				if(dataModem.getRecipientPrefix().length() > 0)
				{
					List<String> prefixes = dataModem.getRecipientPrefixList();
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
			catch (GSMException | ModemNotFoudException e) 
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

	private static int getRouterIndex(String receiver) throws GSMException, ModemNotFoudException {
		String prefix = GSMUtil.getPrefix(receiver);
		if(GSMUtil.modemRouterList.containsKey(prefix))
		{
			try 
			{
				return GSMUtil.modemRouterList.get(prefix).getIndex();
			} 
			catch (ModemNotFoudException e) 
			{
				return GSMUtil.getModemIndex();
			}
		}
		else
		{
			return GSMUtil.getModemIndex();
		}
	}

	private static String getPrefix(String receiver) throws ModemNotFoudException 
	{
		int length = ConfigSMS.getRecipientPrefixLength();
		if(length <= 0)
		{
			throw new ModemNotFoudException("Recipient prefix length must be greater than zero");
		}
		if(receiver.isEmpty())
		{
			throw new ModemNotFoudException("Recipient can not be empty");
		}
		String recv;
		try 
		{
			recv = Utility.canonicalMSISDN(receiver);
		} 
		catch (GSMException e) 
		{
			throw new ModemNotFoudException(e.getMessage());
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
		DataModem dataModem = ConfigModem.getModemData(modemID);
		return dataModem.getName();
	}

	public static Map<String, String> getCallerType() {
		return callerType;
	}

	public static void setCallerType(Map<String, String> callerType) {
		GSMUtil.callerType = callerType;
	}

	private static void reindexInstantce() {
		for(int i = 0; i < GSMUtil.getGSMInstance().size(); i++)
		{
			if(GSMUtil.getGSMInstance().get(i).isConnected() && !ConfigModem.getModemData(GSMUtil.getGSMInstance().get(i).getModemID()).isActive())
			{
				try 
				{
					GSMUtil.getGSMInstance().get(i).disconnect();
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

	public static List<GSMInstance> getGSMInstance() {
		return gsmInstance;
	}
	
	private static GSMInstance getGSMIntance(String id) {
		for(int i = 0; i < GSMUtil.getGSMInstance().size(); i++)
		{
			if(GSMUtil.getGSMInstance().get(i).getModemID().equals(id))
			{
				return GSMUtil.getGSMInstance().get(i);
			}
		}
		return null;
	}

	private static boolean hasGSMInstanceID(String id) {
		for(int i = 0; i < GSMUtil.getGSMInstance().size(); i++)
		{
			if(GSMUtil.getGSMInstance().get(i).getModemID().equals(id))
			{
				return true;
			}
		}
		return false;
	}

	public static long getLastDelete() {
		return lastDelete;
	}

	public static void setLastDelete(long lastDelete) {
		GSMUtil.lastDelete = lastDelete;
	}

	public static JSONObject getInstalledModemInfo(String port) throws SerialPortConnectionException {
		JSONObject info = new JSONObject();
		try 
		{
			info = getModemInfo(port);
		} 
		catch (SerialPortConnectionException e) 
		{
			logger.error(e.getMessage());
			
			GSMInstance instance;
			try 
			{
				instance = GSMUtil.getGSMInstanceByPort(port);
				
				GSMUtil.reconnectModem(instance);
			} 
			catch (ModemNotFoundException | GSMException e2) 
			{
				logger.error(e2.getMessage());
			}	
			
			throw new SerialPortConnectionException(e);
		}
		return info;
	}
	
	private static JSONObject getModemInfo(String port) throws SerialPortConnectionException {
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
				instance.connect(null);
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
			String copsOperator = instance.getCopsOperator();			
			info.put("copsOperator", copsOperator);
			String smsCenter = instance.getSMSCenter();		
			info.put("smsCenter", smsCenter);
		    
			if(addHock)
			{
				instance.disconnect();
			}
			
		} 
		catch (GSMException | InvalidPortException e) 
		{
			logger.error(e.getMessage());
			/**
			 * Do nothing
			 */
		}
		return info;
	}

	public static JSONObject changeIMEI(String port, String currentValue, String newValue) {
		GSMInstance instance;
		boolean addHock = false;
		JSONObject info = new JSONObject();
		boolean connected = false;
		try 
		{
			instance = GSMUtil.getGSMInstanceByPort(port);	
			connected = instance.isConnected();
		} 
		catch (ModemNotFoundException e) 
		{
			instance = new GSMInstance(port, eventListener);
			addHock = true;
		}		
		try 
		{
			if(!connected)
			{
				instance.connect(null);
				connected = instance.isConnected();
			}
			if(connected && currentValue != null && newValue != null && !currentValue.equals(newValue))
			{
				newValue = newValue.trim();
				String command = "AT+EGMR=1,7,\""+newValue+"\"";
				String response = instance.executeATCommand(command);
				info.put(JsonKey.RESPONSE, response);
				info.put(JsonKey.COMMAND, command);
			}
		    
			if(connected && addHock)
			{
				instance.disconnect();
			}
			
		} 
		catch (GSMException | InvalidPortException e) 
		{
			/**
			 * Do nothing
			 */
		}
		return info;
	}
	
	public static JSONObject addPIN(String port, String currentPIN, String pin1) {
		GSMInstance instance;
		boolean addHock = false;
		JSONObject info = new JSONObject();
		boolean connected = false;
		try 
		{
			instance = GSMUtil.getGSMInstanceByPort(port);	
			connected = instance.isConnected();
		} 
		catch (ModemNotFoundException e) 
		{
			instance = new GSMInstance(port, eventListener);
			addHock = true;
		}		
		try 
		{
			if(!connected)
			{
				if(currentPIN.isEmpty())
				{
					currentPIN = null;
				}
				connected = instance.connect(currentPIN);
			}
			if(connected && pin1 != null && !pin1.isEmpty())
			{
				pin1 = pin1.trim();
				String command = "AT+CLCK=\"SC\",1,\"" + pin1 + "\"";
				String response = instance.executeATCommand(command);
				info.put(JsonKey.RESPONSE, response);
				info.put(JsonKey.COMMAND, command);
			}
		    
			if(connected && addHock)
			{
				instance.disconnect();
			}
			
		} 
		catch (GSMException | InvalidPortException e) 
		{
			/**
			 * Do nothing
			 */
			HttpUtil.broardcastWebSocket(e.getMessage());
		}
		return info;
	}
	
	public static JSONObject removePIN(String port, String currentPIN) {
		GSMInstance instance;
		boolean addHock = false;
		JSONObject info = new JSONObject();
		boolean connected = false;
		try 
		{
			instance = GSMUtil.getGSMInstanceByPort(port);	
			connected = instance.isConnected();
		} 
		catch (ModemNotFoundException e) 
		{
			instance = new GSMInstance(port, eventListener);
			addHock = true;
		}		
		try 
		{
			if(!connected)
			{
				if(currentPIN.isEmpty())
				{
					currentPIN = null;
				}
				connected = instance.connect(currentPIN);
			}
			if(connected)
			{
				String command = "AT+CLCK=\"SC\",0,\""+currentPIN+"\"";
				String response = instance.executeATCommand(command);
				info.put(JsonKey.RESPONSE, response);
				info.put(JsonKey.COMMAND, command);
			    
				if(addHock)
				{
					instance.disconnect();
				}
			}
			else
			{
				HttpUtil.broardcastWebSocket("The selected device is not connected");
			}
		} 
		catch (GSMException | InvalidPortException e) 
		{
			/**
			 * Do nothing
			 */
			HttpUtil.broardcastWebSocket(e.getMessage());
		}
		return info;
	}

	public static GSMInstance getGSMInstanceByPort(String port) throws ModemNotFoundException {
		for(int i = 0; i < GSMUtil.getGSMInstance().size(); i++)
		{
			if(GSMUtil.getGSMInstance().get(i).getPort().equals(port))
			{
				return GSMUtil.getGSMInstance().get(i);
			}
		}
		throw new ModemNotFoundException("No modem use port "+port);
	}
	public static GSMInstance getGSMInstanceByModemID(String id) throws ModemNotFoundException {
		for(int i = 0; i < GSMUtil.getGSMInstance().size(); i++)
		{
			if(GSMUtil.getGSMInstance().get(i).getModemID().equals(id))
			{
				return GSMUtil.getGSMInstance().get(i);
			}
		}
		throw new ModemNotFoundException("No modem with ID  "+id);
	}


	public static JSONObject testAT(String modemID) throws GSMException, SerialPortConnectionException {
		GSMInstance instance = GSMUtil.getGSMIntance(modemID);
		JSONObject result = new JSONObject();
		if(instance != null)
		{
			String atResult = instance.testAT();
			result.put(JsonKey.RESULT, atResult);
			if(atResult.contains("\r\nOK"))
			{
				result.put(JsonKey.STATUS, "OK");
			}
			else
			{
				result.put(JsonKey.STATUS, "ERROR");
			}
		}
		return result;
	}

	public static JSONObject getSignalStrength(String modemID) throws GSMException {
		GSMInstance instance = GSMUtil.getGSMIntance(modemID);
		JSONObject result = new JSONObject();
		if(instance != null)
		{
			result = instance.getGsm().getSignalStrength();
		}
		return result;
	}

}




