package com.planetbiru.gsm;

import java.util.Date;
import java.util.List;

import com.fazecast.jSerialComm.SerialPortInvalidPortException;
import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigSMS;
import com.planetbiru.config.DataModem;
import com.planetbiru.constant.ConstantString;
import com.planetbiru.util.Utility;

public class GSMInstance {
	
	private GSM gsm;
	private String id = "";
	private String port = "";
	public GSMInstance(DataModem modem)
	{
		this.port = modem.getPort();
		this.id = modem.getId();
		this.gsm = new GSM();
	}
	
	public GSMInstance(String port) {
		this.port = port;
		this.gsm = new GSM();
	}

	public boolean connect() throws GSMException, InvalidPortException 
	{
		try
		{
			return this.gsm.connect(this.port);
		}
		catch(SerialPortInvalidPortException e)
		{
			throw new GSMException(e);
		}
	}
	
	public void disconnect() throws GSMException {
		if(this.gsm.getSerialPort() == null)
		{
			throw new GSMException(ConstantString.SERIAL_PORT_NULL);
		}
		this.gsm.closePort();
	}
	
	public String sendSMS(String receiver, String message, boolean deleteSent) throws GSMException
	{
		if(this.gsm.getSerialPort() == null)
		{
			throw new GSMException(ConstantString.SERIAL_PORT_NULL);
		}
		this.waitUntilReady();
		return this.gsm.sendSMS(receiver, message, deleteSent);
	}

	public String sendSMS(String receiver, String message) throws GSMException {
		return this.sendSMS(receiver, message, true);
	}

	public String sendSMS(String receiver, String message, DataModem modemData, boolean deleteSent) throws GSMException {
		Date date = new Date();
		String sender = modemData.getMsisdn();
		this.logSendSMS(sender, Utility.maskMSISDN(receiver), date, message.length());
		return this.sendSMS(receiver, message, deleteSent);
	}
	
	public String sendSMS(String receiver, String message, DataModem modemData) throws GSMException {
		return this.sendSMS(receiver, message, modemData, true);
	}
	
	private void logSendSMS(String sender, String receiver, Date date, int length) {
		if(ConfigSMS.isLogSMS())
		{
			SMSLogger.add(date, this.id, sender, Utility.maskMSISDN(receiver), length);	
		}
	}
	
	public List<SMS> readSMS() throws GSMException
	{
		if(this.gsm.getSerialPort() == null)
		{
			throw new GSMException(ConstantString.SERIAL_PORT_NULL);
		}
		this.waitUntilReady();
		return this.gsm.readSMS();
	}
	
	
    
    public void deleteAllSentSMS() throws GSMException 
    { 	
    	this.gsm.deleteAllSentSMS();
 	}
    
    public String getIMEI() throws GSMException 
    {
        return this.gsm.getIMEI();
    }
    
    public String getIMSI() throws GSMException 
    {
    	return this.gsm.getIMSI();
    }
    
    public String getICCID() throws GSMException 
    {
    	return this.gsm.getICCID();
    }

    public String getMSISDN() throws GSMException 
    {
    	return this.gsm.getMSISDN();
    }
	
	private void waitUntilReady() {
		long maxWaith = Config.getMaxWaitModemReady();
		long ellapsed = 0;
		long startTime = System.currentTimeMillis();
		while(!this.gsm.isReady() && ellapsed < maxWaith)
		{
			this.sleep(Config.getWaithModemReady());
			ellapsed = System.currentTimeMillis() - startTime;
		}
	}
	
	public USSD executeUSSD(String ussd) throws GSMException {
		if(this.gsm.getSerialPort() == null)
		{
			throw new GSMException(ConstantString.SERIAL_PORT_NULL);
		}
		this.waitUntilReady();
		return this.gsm.executeUSSD(ussd);	
	}
	
	public boolean isConnected()
	{
		return this.gsm.isConnected();
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public GSM getGsm() {
		return gsm;
	}

	public String getPort() {
		return port;
	}

	private void sleep(long sleep) 
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
	
	
}
