package com.planetbiru.subscriber.activemq;

import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigSubscriberActiveMQ;

public class SubscriberActiveMQ extends Thread{
	
	private ActiveMQInstance instance = null;
	
	public SubscriberActiveMQ()
	{
		ConfigSubscriberActiveMQ.load(Config.getSubscriberActiveMQSettingPath());
	}

	@Override
	public void run()
	{
		this.instance = new ActiveMQInstance();
		this.instance.start();
	}

	public void stopService() {
		if(this.instance != null)
		{
			this.instance.stopService();
		}
	}

	public boolean isRunning() {
		return this.instance != null && this.instance.isRunning();
	}
}
