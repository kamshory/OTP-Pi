package com.planetbiru.gsm;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortInvalidPortException;
import com.planetbiru.config.Config;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class GSM {
    private SerialPort serialPort;
    private boolean connected = false;
	private boolean ready = false;
	private boolean gcRunning = false;
	private StringBuilder incommingMessage = new StringBuilder(); 
	private boolean eventListener = true;
    
    private static Logger logger = Logger.getLogger(GSM.class);
    
    public GSM()
    {
    	/**
    	 * Default constructor
    	 */
    }
        
    /**
     * Initialize the connection
     *
     * @param portName the port name
     * @param eventListener 
     * @return true if port was opened successfully
     * @throws GSMException 
     */
    public boolean connect(String portName, boolean evtListener) throws InvalidPortException
    {
    	this.eventListener = evtListener;
    	logger.info("Connecting to "+portName);
    	this.setReady(false);
    	boolean isOpen = false;
    	try
    	{
    		SerialPort port = SerialPort.getCommPort(portName);
	   		this.setSerialPort(port);
	    	isOpen = this.serialPort.openPort();
	        if(isOpen)
	    	{		
	    		this.serialPort.addDataListener(new SerialPortDataListener() 
	            {
	                @Override
	                public int getListeningEvents() 
	                {
	                    return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
	                }
	
	                @Override
	                public void serialEvent(SerialPortEvent event) 
	                {
	                	if(eventListener && event.getEventType() == SerialPort.LISTENING_EVENT_DATA_AVAILABLE) 
	                	{
	                		byte[] msg = new byte[getSerialPort().bytesAvailable()];
	                		getSerialPort().readBytes(msg, msg.length);
	                		String result = new String(msg);
	                		onReceiveData(result);      
						}   
	                }
	            });
	    		this.connected = true;
	    		this.ready = true;
	    		isOpen = true;
	        } 
	        else 
	        {       	
	        	if(Config.isDebugModem())
	        	{
		        	this.connected = true;
		        	this.ready = true;
		            isOpen = true;
	        	}
	        	else
	        	{
	        		this.connected = false;
		        	this.ready = false;
		            isOpen = false;
	        	}
	        }
    	}
    	catch(SerialPortInvalidPortException e)
    	{
    		if(Config.isDebugModem())
        	{
	        	this.connected = true;
	        	this.ready = true;
	            isOpen = true;
        	}
        	else
        	{
        		this.connected = false;
	        	this.ready = false;
	       		throw new InvalidPortException(e.getMessage());
	       	}
    		logger.error(e.getMessage());
    	}
    	return isOpen;
    }
    
    private void executeAT(String command, int waitingTime) throws GSMException {
		this.executeAT(command, waitingTime, false);	
	}
    
    /**
     * Execute AT command
     *
     * @param command : the AT command
     * @param waitingTime
     * @return String contains the response
     * @throws GSMException 
     */
    public String executeAT(String command, int waitingTime, boolean requireResult) throws GSMException 
    {
    	this.setReady(false);
    	if(getSerialPort() == null)
    	{
    		throw new GSMException("GSM is not initilized yet");
    	}
    	logger.info("Execute AT Command : "+command);
        command = command + "\r\n";
        String result = "";
        int i = 0;
        byte[] bytes = command.getBytes();
        int written = this.serialPort.writeBytes(bytes, bytes.length);
        
        if(written > 0)
        {
        	do 
        	{
 	            this.sleep(100);
 	            i++;
	        }
        	while(
        			requireResult 
        			&& !this.incommingMessage.toString().trim().endsWith("\r\n\r\nOK") 
        			&& !this.incommingMessage.toString().trim().endsWith("\r\n\r\nERROR") 
        			&& i < waitingTime);
        }
        result = this.updateResult(result);
        this.setReady(true);
        return result;
    }
    
   
    private void sleep(int sleep)
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

    /**
     * Execute USSD command
     *
     * @param ussd The USSD command
     * @return String contains the response
     * @throws GSMException 
     */
    public USSDParser executeUSSD(String ussd) throws GSMException 
    {
    	this.setReady(false);
        String cmd = "AT+CUSD=1,\"" + ussd + "\",15";
        String result = "";
        result = this.executeAT(cmd, 2, true);
        USSDParser ussdParser;
		if(result.startsWith(ATCommand.ERROR))
        {
        	ussdParser = new USSDParser();
            return ussdParser;
        }
        int waiting = 0;
        while ((result.trim().equals("") || result.trim().equals("\n")) && waiting < 10) 
        {
        	waiting++;
        	this.sleep(1000);
            byte[] msg = new byte[getSerialPort().bytesAvailable()];
            getSerialPort().readBytes(msg, msg.length);
            result = new String(msg);
        }     
        if(result.startsWith("+CUSD")) 
        {
            ussdParser = new USSDParser(result);
        }
        else
        {
        	ussdParser = new USSDParser();
        }
        this.setReady(true);
        return ussdParser;
    }
    

    /**
     * Read the SMS stored in the sim card
     *
     * @return ArrayList contains the SMS
     * @throws GSMException 
     */
    public List<SMS> readSMS(String storage, String smsStatus) throws GSMException 
    {
    	this.setReady(false);
        this.executeAT("ATE0", 1, true);
        this.executeAT(this.selectProtocol("GSM"), 1, true);
        this.executeAT(this.selectDataMode(ATCommand.DATA_MODE_TEXT), 1, true);
        List<SMS> smsList = new ArrayList<>();
        if(smsStatus == null)
        {
        	smsStatus = "ALL";
        }
        if(storage == null)
        {
			for (String strg : GSMConst.getSmsStorage()) 
	        {
	        	this.loadSMS(strg, "ALL", smsList);
	        }
        }
        else
        {
        	this.loadSMS(storage, smsStatus, smsList);
        }
        this.setReady(true);
        return smsList;
    }
    private void loadSMS(String storage, String smsStatus, List<SMS> smsList) throws GSMException
    {
    	this.executeAT(this.selectStorage(storage), 1, true);
    	smsStatus = this.fixArrayString(smsStatus);	
    	String result = this.executeAT("AT+CMGL="+smsStatus+"", 20, true);	  	
		result = this.fixingRawData(result);				
		String[] arr = result.split("\r\n");	
		int max = this.getMax(arr);				
		for(int i = 0; i<max; i++)
		{
			String csvLine = arr[i];		
			if(csvLine.startsWith("+CMGL: ")) 
			{
				this.parseSMSAttributes(csvLine, storage, smsList);	        
			}
			else
			{				
				if(!smsList.isEmpty())
				{
					smsList.get(smsList.size()-1).appendContent(csvLine);
				}				
			}
		}		
    }

    private void parseSMSAttributes(String csvLine, String storage, List<SMS> smsList) {
		List<String> allMatches = new ArrayList<>(); 
    	Pattern pattern = Pattern.compile("\"([^\"]*)\"|(?<=,|^)([^,]*)(?=,|$)");
		Matcher matcher = pattern.matcher(csvLine.substring(7));
        String match;
        while (matcher.find()) 
        {
            match = matcher.group(1);
            if (match!=null) 
            {
                allMatches.add(match);
            }
            else 
            {
                allMatches.add(matcher.group(2));
            }
        }
        int size = allMatches.size();        
        if(size > 4)
        {
        	String[] attrs = allMatches.toArray(new String[size]);
        	SMS sms = new SMS(storage, attrs[0], attrs[1], attrs[2], attrs[3], attrs[4]);
        	smsList.add(sms);
			allMatches.clear();
        }	
	}
    
    public List<String> parseCSVResponse(String csvLine)
    {
    	List<String> record = new ArrayList<>();
		List<String> allMatches = new ArrayList<>(); 
    	Pattern pattern = Pattern.compile("\"([^\"]*)\"|(?<=,|^)([^,]*)(?=,|$)");
		Matcher matcher = pattern.matcher(csvLine);
        String match;
        while (matcher.find()) 
        {
            match = matcher.group(1);
            if (match!=null) 
            {
                allMatches.add(match);
            }
            else 
            {
                allMatches.add(matcher.group(2));
            }
        }
        int size = allMatches.size();        
        if(size > 0)
        {
        	String[] attrs = allMatches.toArray(new String[size]);
        	for(int i = 0; i<attrs.length; i++)
        	{
        		record.add(attrs[i]);
        	}
        }	
        return record;
    }

	private int getMax(String[] arr) {
    	int max = 0;
		if(arr.length > 2 && arr[arr.length - 1].equals("OK") && arr[arr.length - 2].isEmpty())
		{
			max = arr.length - 2;
		}
		else
		{
			max = arr.length;
		}
		return max;
	}

    public String fixingRawData(String result)
	{
		result = result.replace("\n", "\r\n");
		result = result.replace("\r\r\n", "\r\n");
		result = result.replace("\r", "\r\n");
		result = result.replace("\r\n\n", "\r\n");
		return result;
	}
    private String fixArrayString(String input)
    {
    	String[] stts = input.split(",");
    	for(int i = 0; i < stts.length; i++)
    	{
    		stts[i] = "\""+stts[i]+"\"";
    	}
    	List<String> lst = Arrays.asList(stts);
    	return String.join(",", lst);    	
    }
    private String fixResultByOK(String result) 
    {
    	if(result.contains("\r\nOK"))
		{
			result = result.substring(0, result.indexOf("\r\nOK"));
		}
		return result;
	}

    private String removeError(String result) 
    {
    	if(result.contains(ATCommand.ERROR))
		{
			result = result.replace(ATCommand.ERROR, "");
		}
		return result;
	}

    private String updateResult(String result)
    {
        if(result.isEmpty() && !this.incommingMessage.toString().isEmpty())
        {
        	result = this.incommingMessage.toString();
        }
		return result; 	
    }
 
	/**
     * Send an SMS
     *
     * @param recipient the destination number
     * @param message the body of the SMS
	 * @param deleteSent 
     * @return ?
     * @throws GSMException 
     */
    public String sendSMS(String recipient, String message, boolean deleteSent) throws GSMException 
    {
    	this.setReady(false);
    	String result = "";
    	recipient = recipient.trim();
    	message = message.trim();
    	String msg1 = this.executeAT("ATE0", 1, true);
    	logger.info("msg1 = "+msg1);
    	String msg2 = this.executeAT(this.selectProtocol("GSM"), 1, true);
    	logger.info("msg2 = "+msg2);
    	String msg3 = this.executeAT(this.selectDataMode(ATCommand.DATA_MODE_TEXT), 1, true);
    	logger.info("msg3 = "+msg3);
    	String msg4 = this.executeAT("AT+CMGW=\"" + recipient + "\"", 2, true);
    	logger.info("msg4 = "+msg4);
    	String msg5 = this.executeAT(message, 2, true);
    	logger.info("msg5 = "+msg5);
    	String msg6 = this.executeAT(Character.toString((char) 26), 2, true);
    	logger.info("msg6 = "+msg6);
    	this.setReady(true);
    	
    	if(deleteSent)
    	{
    		this.gcDeleteSent();
    	}
        return result;
    }

	private void gcDeleteSent() {
		GSMSMSCleaner gc = new GSMSMSCleaner(this);
		gc.start();		
	}

	public String deleteSMS(int smsID, String storage) throws GSMException 
    {
    	this.setReady(false);
    	String result = "";
    	this.executeAT(this.selectStorage(storage), 1);
    	this.executeAT(this.createDeleteSMS(smsID), 1);
    	this.setReady(true);
        return result;
    }

    public String deleteAllSMS(String storage) throws GSMException 
    {
    	this.setReady(false);
    	String result = "";
    	this.executeAT(this.selectStorage(storage), 1);
    	this.executeAT(GSMConst.CREATE_DELETE_SMS_ALL, 1);
    	this.setReady(true);
        return result;
    }
    
    public void deleteAllSentSMS() throws GSMException 
    { 	
    	this.setReady(false);
        this.executeAT("ATE0", 1);
        this.executeAT(this.selectProtocol("GSM"), 1);
        this.executeAT(this.selectDataMode(ATCommand.DATA_MODE_TEXT), 1);
		for(String storage : GSMConst.getSmsStorage()) 
        {
			this.executeAT(this.selectStorage(storage), 1);
	    	this.executeAT(GSMConst.CREATE_DELETE_SMS_ALL, 1);
        }
        this.setReady(true);
 	}
    
    public String getIMEI() throws GSMException 
    {
    	this.setReady(false);
    	String result = this.executeAT(ATCommand.GET_IMEI, 1, true);
    	if(!result.isEmpty())
    	{
    		if(result.contains(ATCommand.GET_IMEI))
    		{
    			result = result.replace(ATCommand.GET_IMEI, "").trim();
    		}
    		result = this.fixResultByOK(result).trim();
    		result = this.removeError(result).trim();
    	}
    	this.setReady(true);
        return result;
    }

	public String getIMSI() throws GSMException 
    {
    	this.setReady(false);
    	String result = this.executeAT(ATCommand.GET_IMSI, 1, true);
       	if(!result.isEmpty())
    	{
       		if(result.contains(ATCommand.GET_IMSI))
    		{
       			result = result.replace(ATCommand.GET_IMSI, "").trim();
    		}
       		result = this.fixResultByOK(result).trim();
       		result = this.removeError(result).trim();
    	}
    	this.setReady(true);
        return result;
    }
    
    public String getICCID() throws GSMException 
    {
    	this.setReady(false);
    	String result = this.executeAT(ATCommand.GET_ICCID, 1, true);
       	if(!result.isEmpty())
    	{
       		if(result.contains(ATCommand.GET_ICCID))
    		{
       			result = result.replace(ATCommand.GET_ICCID, "").trim();
    		}
       		result = this.fixResultByOK(result).trim();
       		result = this.removeError(result).trim();
    	}
    	this.setReady(true);    	
        return result;
    }

    public String getMSISDN() throws GSMException 
    {
    	this.setReady(false);
    	String result = this.executeAT(ATCommand.GET_MSISDN, 1, true);
       	if(!result.isEmpty())
    	{
       		if(result.contains(ATCommand.GET_MSISDN))
    		{
       			result = result.replace(ATCommand.GET_MSISDN, "");
    		}
       		result = this.fixResultByOK(result).trim();
       		result = this.removeError(result).trim();
    	}
    	this.setReady(true);
        return result;
    }
    
    public String getManufacturer() throws GSMException {
    	this.setReady(false);
    	String result = this.executeAT(ATCommand.GET_MANUFACTURER, 1, true);
       	if(!result.isEmpty())
    	{
       		if(result.contains(ATCommand.GET_MANUFACTURER))
    		{
       			result = result.replace(ATCommand.GET_MANUFACTURER, "");
    		}
       		result = this.fixResultByOK(result).trim();
       		result = this.removeError(result).trim();
    	}
    	this.setReady(true);
        return result;
	}

	public String getModel() throws GSMException {
	   	this.setReady(false);
    	String result = this.executeAT(ATCommand.GET_MODEL, 1, true);
       	if(!result.isEmpty())
    	{
       		if(result.contains(ATCommand.GET_MODEL))
    		{
       			result = result.replace(ATCommand.GET_MODEL, "");
    		}
       		result = this.fixResultByOK(result).trim();
       		result = this.removeError(result).trim();
    	}
    	this.setReady(true);
        return result;
	}

	public String getRevision() throws GSMException {
	   	this.setReady(false);
    	String result = this.executeAT(ATCommand.GET_REVISION, 1, true);
       	if(!result.isEmpty())
    	{
       		if(result.contains(ATCommand.GET_REVISION))
    		{
       			result = result.replace(ATCommand.GET_REVISION, "");
    		}
       		result = this.fixResultByOK(result).trim();
       		result = this.removeError(result).trim();
    	}
    	this.setReady(true);
        return result;
	}

	public String getSMSCenter() throws GSMException {
	   	this.setReady(false);
    	String result = this.executeAT(ATCommand.GET_SMS_CENTER, 1, true);
       	if(!result.isEmpty())
    	{
       		if(result.contains(ATCommand.GET_SMS_CENTER))
    		{
       			result = result.replace(ATCommand.GET_SMS_CENTER, "");
    		}
       		result = this.fixResultByOK(result).trim();
       		result = this.removeError(result).trim();
    	}
    	this.setReady(true);
        return result;
	}
	
	public String getNetworkRegistration() throws GSMException {
		this.setReady(false);
    	String result = this.executeAT("AT+CREG?", 2, true);
       	if(!result.isEmpty() && result.contains("+CREG"))
    	{
      		result = result.replace("AT+CREG?", "");
      		result = this.fixResultByOK(result).trim();
       		result = this.fixingRawData(result);	
       		result = result.replace("+CREG:", "").trim();
    	}
    	this.setReady(true);
        return result;
	}
	
	public String getOperatorSelect() throws GSMException {
		this.setReady(false);
    	String result = this.executeAT("AT+COPS?", 2, true);
       	if(!result.isEmpty() && result.contains("+COPS"))
    	{
      		result = result.replace("AT+COPS?", "");
      		result = this.fixResultByOK(result).trim();
       		result = this.fixingRawData(result);	
       		result = result.replace("\r\n", "").trim();
       		List<String> record = this.parseCSVResponse(result);
       		if(record.size() > 2)
       		{
       			result = record.get(2);
       		}
       		else
       		{
       			result = "";
       		}
    	}
    	this.setReady(true);
        return result;
	}
	
    public String createDeleteSMS(int smsID)
    {
    	return "AT+CMGD=" + smsID;
    }
 
    public String selectStorage(String storage) {
		return "AT+CPMS=\"" + storage + "\"";
	}

    private String selectDataMode(String operator) 
    {
		return "AT+CMGF="+operator;
	}

	private String selectProtocol(String protocol) 
    {
		return "AT+CSCS=\""+protocol+"\"";
	}
	
	public void onReceiveData(String message)
    {
		this.incommingMessage.append(message);
    }

    /**
     * Return list of the available port
     *
     * @return list contains list of the available port
     */
    public String[] getSystemPorts() 
    {
        String[] systemPorts = new String[SerialPort.getCommPorts().length];
        for (int i = 0; i < systemPorts.length; i++) 
        {
            systemPorts[i] = SerialPort.getCommPorts()[i].getSystemPortName();
        }
        return systemPorts;
    }

    /**
     * Close the connection
     *
     * @return true if port was closed successfully
     */
    public boolean closePort() 
    {
    	this.connected = false;
        if(getSerialPort() != null)
        {
        	return getSerialPort().closePort();
        }
        else
        {
            return true;
        }
    }

	public SerialPort getSerialPort() {
		return serialPort;
	}

	public void setSerialPort(SerialPort serialPort) {
		this.serialPort = serialPort;
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	public boolean isReady() {
		return ready;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
		if(!ready)
		{
			this.incommingMessage = new StringBuilder();
		}
	}

	public boolean isGcRunning() {
		return gcRunning;
	}

	public void setGcRunning(boolean gcRunning) {
		this.gcRunning = gcRunning;
	}

	

}