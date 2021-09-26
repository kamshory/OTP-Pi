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
	private DataModem modem = new DataModem();
	public GSMInstance(DataModem modem, boolean eventListener)
	{
		this.modem = modem;
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
	private void processPIN(String pin)
	{
		if(pin != null && !pin.trim().isEmpty())
		{
			pin = pin.trim();
			String atCommand1 = "AT+CPIN?";
			String response1;
			String atCommand2 = "AT+CPIN=\""+pin+"\"";
			String response2;
			try 
			{
				response1 = this.executeATCommand(atCommand1);
				if(response1.contains("READY"))
				{
					this.pinValid = true;
				}
				else
				{
					response2 = this.executeATCommand(atCommand2);
					if(response2.contains("ERROR"))
					{
						this.pinValid = false;
					}
				}
			} 
			catch (GSMException e) 
			{
				e.printStackTrace();
			}
		}
	}
	public boolean connect(String pin) throws GSMException, InvalidPortException 
	{
		if(!this.gsm.isConnected())
		{
			try
			{
				boolean isConnected = this.gsm.connect(this.port, this.eventListener);
				this.processPIN(pin);
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
	
	public String sendSMS(String receiver, String message, boolean deleteSent) throws GSMException, InvalidSIMPinException, SerialPortConnectionException
	{
		if(!this.pinValid)
		{
			throw new InvalidSIMPinException(ConstantString.INVALID_SIM_CARD_PIN);
		}
		if(this.gsm.getSerialPort() == null)
		{
			throw new GSMException(ConstantString.SERIAL_PORT_NULL);
		}
		this.waitUntilReady();
		return this.gsm.sendSMS(receiver, message, deleteSent);
	}

	public String sendSMS(String receiver, String message) throws GSMException, InvalidSIMPinException, SerialPortConnectionException {
		return this.sendSMS(receiver, message, true);
	}

	
	public String sendSMS(String receiver, String message, DataModem modemData) throws GSMException, InvalidSIMPinException, SerialPortConnectionException {
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
	
	public List<SMS> readSMS() throws GSMException, InvalidSIMPinException, SerialPortConnectionException
	{
		if(!this.pinValid)
		{
			throw new InvalidSIMPinException(ConstantString.INVALID_SIM_CARD_PIN);
		}
		if(this.gsm.getSerialPort() == null)
		{
			throw new GSMException(ConstantString.SERIAL_PORT_NULL);
		}
		this.waitUntilReady();
		return this.gsm.readSMS(null, null);
	}
	public List<SMS> readSMS(String storage, String smsStatus) throws GSMException, InvalidSIMPinException, SerialPortConnectionException
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
	
	public void deleteSMS(int smsID, String storage) throws GSMException, InvalidSIMPinException, SerialPortConnectionException {
		if(!this.pinValid)
		{
			throw new InvalidSIMPinException(ConstantString.INVALID_SIM_CARD_PIN);
		}
		this.gsm.deleteSMS(smsID, storage);		
	}
    
    public void deleteAllSentSMS() throws GSMException, InvalidSIMPinException, SerialPortConnectionException 
    { 	
		if(!this.pinValid)
		{
			throw new InvalidSIMPinException(ConstantString.INVALID_SIM_CARD_PIN);
		}
		this.gsm.deleteAllSentSMS();
 	}
    
    public String getIMEI() throws GSMException, SerialPortConnectionException 
    {
        return this.gsm.getIMEI();
    }
    
    public String getIMSI() throws GSMException, SerialPortConnectionException 
    {
    	return this.gsm.getIMSI();
    }
    
    public String getICCID() throws GSMException, SerialPortConnectionException 
    {
    	return this.gsm.getICCID();
    }

    public String getMSISDN() throws GSMException, SerialPortConnectionException 
    {
    	return this.gsm.getMSISDN();
    }
    public String getManufacturer() throws GSMException, SerialPortConnectionException {
		return this.gsm.getManufacturer();
	}

	public String getModel() throws GSMException, SerialPortConnectionException {
		return this.gsm.getModel();
	}

	public String getRevision() throws GSMException, SerialPortConnectionException {
		return this.gsm.getRevision();
	}
	public String getSMSCenter() throws GSMException, SerialPortConnectionException {
		return this.gsm.getSMSCenter();
	}
	public String getOperatorSelect() throws GSMException, SerialPortConnectionException {
		return this.gsm.getOperatorSelect();
	}
	public String getNetworkRegistration() throws GSMException, SerialPortConnectionException {
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
	
	public USSDParser executeUSSD(String ussd) throws GSMException, SerialPortConnectionException {
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

	public DataModem getModem() {
		return modem;
	}

	public void setModem(DataModem modem) {
		this.modem = modem;
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

	public String testAT() throws GSMException, SerialPortConnectionException {
		return this.gsm.executeAT("AT", 1, eventListener);
	}

	

}
