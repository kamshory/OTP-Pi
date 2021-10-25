package com.planetbiru.subscriber.activemq;

import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigSubscriberActiveMQ;

public class SubscriberActiveMQ extends Thread{
	
	private ActiveMQInstance activeMQIstance = null;
	
	public SubscriberActiveMQ()
	{
		ConfigSubscriberActiveMQ.load(Config.getSubscriberActiveMQSettingPath());
	}

	@Override
	public void run()
	{
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
}