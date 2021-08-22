package com.planetbiru.gsm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GCDeleteSMS extends Thread {

	private static final Logger logger = LogManager.getLogger(GCDeleteSMS.class);
	
	private GSM gsm;

	public GCDeleteSMS(GSM gsm) {
		this.gsm = gsm;
	}

	@Override
	public void run()
	{
		long min = System.currentTimeMillis() - 5000;
		if(GSMUtil.getLastDelete() <= min)
		{
			try 
			{
				this.gsm.deleteAllSentSMS();
			} 
			catch (GSMException e) 
			{
				logger.error(e.getMessage());
			}
			GSMUtil.setLastDelete(System.currentTimeMillis());
		}
	}
}
