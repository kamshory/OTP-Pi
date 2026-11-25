package com.planetbiru.subscriber.activemq;

import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigSubscriberActiveMQ;

public class SubscriberActiveMQ extends Thread{
	
	private ActiveMQInstance activeMQIstance = null;
	
	public SubscriberActiveMQ()
	{
		/**
		 * Just default constructor
		 */
	}

	@Override
	public void run()
	{
		ConfigSubscriberActiveMQ.load(Config.getSubscriberActiveMQSettingPath());
		this.activeMQIstance = new ActiveMQInstance();
		this.activeMQIstance.start();
	}

	public void stopService() {	
		if(this.activeMQIstance != null)
		{
			this.activeMQIstance.stopService();
		}
	}

	public boolean isRunning() {
		return this.activeMQIstance != null && this.activeMQIstance.isRunning();
	}
	public boolean isConnected()
	{
		if(this.activeMQIstance == null)
		{
			return false;
		}
		return this.activeMQIstance.isConnected();
	}
}