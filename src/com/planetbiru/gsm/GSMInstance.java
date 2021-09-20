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
	private boolean eventListener = true;
	private boolean pinValid = true;
	public GSMInstance(DataModem modem, boolean eventListener)
	{
		this.port = modem.getPort();
		this.eventListener = eventListener;
		this.id = modem.getId();
		this.gsm = new GSM();
	}
	
	public GSMInstance(String port, boolean eventListener) {
		this.port = port;
		this.eventListener =eventListener;
		this.gsm = new GSM();
	}

	public boolean connect(String pin) throws GSMException, InvalidPortException 
	{
		if(!this.gsm.isConnected())
		{
			try
			{
				boolean isConnected = this.gsm.connect(this.port, this.eventListener);
				if(pin != null && !pin.trim().isEmpty())
				{
					pin = pin.trim();
					String atCommand = "AT+CPIN=\""+pin+"\"";
					String response = this.executeATCommand(atCommand);
					if(response.contains("ERROR"))
					{
						this.pinValid = false;
					}
				}
				return isConnected;
			}
			catch(SerialPortInvalidPortException e)
			{
				throw new GSMException(e);
			}
		}
		else
		{
			return true;
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

	
	public String sendSMS(String receiver, String message, DataModem modemData) throws GSMException {
		Date date = new Date();
		String sender = modemData.getMsisdn();
		this.logSendSMS(sender, Utility.maskMSISDN(receiver), date, message.length());
		return this.sendSMS(receiver, message, modemData.isDeleteSentSMS());
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
		return this.gsm.readSMS(null, null);
	}
	public List<SMS> readSMS(String storage, String smsStatus) throws GSMException
	{
		if(storage == null)
		{
			return this.readSMS();
		}
		else
		{
			if(this.gsm.getSerialPort() == null)
			{
				throw new GSMException(ConstantString.SERIAL_PORT_NULL);
			}
			this.waitUntilReady();
			return this.gsm.readSMS(storage, smsStatus);
		}
	}
	
	public void deleteSMS(int smsID, String storage) throws GSMException {
		this.gsm.deleteSMS(smsID, storage);		
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
    public String getManufacturer() throws GSMException {
		return this.gsm.getManufacturer();
	}

	public String getModel() throws GSMException {
		return this.gsm.getModel();
	}

	public String getRevision() throws GSMException {
		return this.gsm.getRevision();
	}
	public String getSMSCenter() throws GSMException {
		return this.gsm.getSMSCenter();
	}
	public String getOperatorSelect() throws GSMException {
		return this.gsm.getOperatorSelect();
	}
	public String getNetworkRegistration() throws GSMException {
		return this.gsm.getNetworkRegistration();
	}
	private void waitUntilReady() {
		long maxWait = Config.getMaxWaitModemReady();
		long ellapsed = 0;
		long startTime = System.currentTimeMillis();
		while(!this.gsm.isReady() && ellapsed < maxWait)
		{
			this.sleep(Config.getWaithModemReady());
			ellapsed = System.currentTimeMillis() - startTime;
		}
	}
	
	public USSDParser executeUSSD(String ussd) throws GSMException {
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

	public boolean isPinValid() {
		return pinValid;
	}

	public void setPinValid(boolean pinValid) {
		this.pinValid = pinValid;
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

	public String executeATCommand(String command) throws GSMException {
		return this.gsm.executeATCommand(command);		
	}

	

}
