package com.planetbiru.gsm;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortInvalidPortException;
import com.planetbiru.config.Config;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class GSM {
    private SerialPort serialPort;
    private boolean connected = false;
	private boolean ready = false;
	private boolean gcRunning = false; 
    
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
     * @return true if port was opened successfully
     * @throws GSMException 
     */
    public boolean connect(String portName) throws InvalidPortException
    {
    	this.setReady(false);
    	boolean isOpen = false;
    	
    	try
    	{
	   		setSerialPort(SerialPort.getCommPort(portName));
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
	                	if(event.getEventType() == SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
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
    	logger.info("AT Command : "+command);
    	if(getSerialPort() == null)
    	{
    		throw new GSMException("GSM is not initilized yet");
    	}
        command = command + "\r\n";
        String result = "";
        int i = 0;
        byte[] bytes = command.getBytes();
        int written = this.serialPort.writeBytes(bytes, bytes.length);
        if(written > 0)
        {
        	do 
        	{
	        	byte[] msg = new byte[getSerialPort().bytesAvailable()];
	            getSerialPort().readBytes(msg, msg.length);
	            result = new String(msg);
	            if(requireResult && (result.trim().equals("") || result.trim().equals("\n")))
	            {
	            	this.sleep(500);
	                i++;			        
	            }
	        }
        	while(requireResult && (result.trim().equals("") || result.trim().equals("\n")) && i < waitingTime);
        }
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
		if(result.startsWith("ERROR"))
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
    
    public String fixingRawData(String result)
	{
		result = result.replace("\n", "\r\n");
		result = result.replace("\r\r\n", "\r\n");
		result = result.replace("\r", "\r\n");
		result = result.replace("\r\n\n", "\r\n");
		return result;
	}

    /**
     * Read the SMS stored in the sim card
     *
     * @return ArrayList contains the SMS
     * @throws GSMException 
     */
    public List<SMS> readSMS() throws GSMException 
    {
    	this.setReady(false);
        this.executeAT("ATE0", 1);
        this.executeAT(this.selectProtocol("GSM"), 1);
        this.executeAT(this.selectOperator("1"), 1);
        List<SMS> smsList = new ArrayList<>();
		for (String storage : GSMConst.getSmsStorage()) 
        {
        	this.loadSMS(storage, smsList);
        }
        this.setReady(true);
        return smsList;
    }

	public List<SMS> readSMS(String storage) throws GSMException 
    {
    	this.setReady(false);
        this.executeAT("ATE0", 1);
        this.executeAT(this.selectProtocol("GSM"), 1);
        this.executeAT(this.selectOperator("1"), 1);
        List<SMS> smsList = new ArrayList<>();
       	this.loadSMS(storage, smsList);
        this.setReady(true);
        return smsList;
    }
    
    private void loadSMS(String storage, List<SMS> smsList) throws GSMException
    {
    	this.executeAT(this.selectStorage(storage), 1);
		
    	String result = this.executeAT("AT+CMGL=\"ALL\"", 5, true);		
    	if(Config.isDebugReadSMS())
    	{
			result = "+CMGL: 1,\"REC READ\",\"+85291234567\",,\"07/02/18,00:05:10+32\"\r\n"
					+ "Reading text messages \r\n"
					+ "is easy.\r\n"
					+ "+CMGL: 2,\"REC READ\",\"+85291234567\",,\"07/02/18,00:07:22+32\"\r\n"
					+ "A simple demo of SMS text messaging.\r\n"
					+ "+CMGL: 3,\"REC READ\",\"+85291234567\",,\"07/02/18,00:12:05+32\"\r\n"
					+ "Hello, welcome to our SMS tutorial.\r\n"
					+ "+CMGL: 4,\"REC READ\",\"+85291234567\",,\"07/02/18,00:12:11+32\"\r\n"
					+ "OTP tidak dapat digunakan\r\n"
					+ "OK";
    	}
    	
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
					String content = smsList.get(smsList.size()-1).getContent();
					smsList.get(smsList.size()-1).appendContent(content);
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
    	this.executeAT("ATE0", 1);
    	this.executeAT(this.selectProtocol("GSM"), 1);
    	this.executeAT(this.selectOperator("1"), 1);
    	this.executeAT("AT+CMGS=\"" + recipient + "\"", 2);
    	this.executeAT(message, 2);
    	this.executeAT(Character.toString((char) 26), 10);
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
        this.executeAT(this.selectOperator("1"), 1);
		for (String storage : GSMConst.getSmsStorage()) 
        {
			this.executeAT(this.selectStorage(storage), 1);
	    	this.executeAT(GSMConst.CREATE_DELETE_SMS_ALL, 1);
        }
        this.setReady(true);
 	}
    
    public String getIMEI() throws GSMException 
    {
    	this.setReady(false);
    	String result = this.executeAT("AT+CGSN", 1, true);
    	this.setReady(true);
        return result;
    }
    
    public String getIMSI() throws GSMException 
    {
    	this.setReady(false);
    	String result = this.executeAT("AT+CIMI", 1, true);
    	this.setReady(true);
        return result;
    }
    
    public String getICCID() throws GSMException 
    {
    	this.setReady(false);
    	String result = this.executeAT("AT+CCID", 1, true);
    	this.setReady(true);    	
        return result;
    }

    public String getMSISDN() throws GSMException 
    {
    	this.setReady(false);
    	String result = this.executeAT("AT+CNUM", 1, true);
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

    private String selectOperator(String operator) 
    {
		return "AT+CMGF="+operator;
	}

	private String selectProtocol(String protocol) 
    {
		return "AT+CSCS=\""+protocol+"\"";
	}
	
	public void onReceiveData(String message)
    {
    	logger.info("Receive Message " + message);
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
	}

	public boolean isGcRunning() {
		return gcRunning;
	}

	public void setGcRunning(boolean gcRunning) {
		this.gcRunning = gcRunning;
	}

	
	
}