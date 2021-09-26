package com.planetbiru.gsm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GSMSMSCleaner extends Thread {

	private static final Logger logger = LogManager.getLogger(GSMSMSCleaner.class);
	
	private GSM gsm;

	public GSMSMSCleaner(GSM gsm) {
		this.gsm = gsm;
	}

	@Override
	public void run()
	{
		this.waitUntil(10000);		
		if(!this.gsm.isGcRunning())
		{
			this.gsm.setGcRunning(true);
			this.waitUntil(5000);
			try 
			{
				this.gsm.deleteAllSentSMS();
			} 
			catch (GSMException | SerialPortConnectionException e) 
			{
				logger.error(e.getMessage());
			}
			this.gsm.setGcRunning(false);
		}
	}

	private void waitUntil(int sleep) {
		try {
			Thread.sleep(sleep);
		} catch (InterruptedException e1) {
			Thread.currentThread().interrupt();
		}
		
	}
	
	
}
