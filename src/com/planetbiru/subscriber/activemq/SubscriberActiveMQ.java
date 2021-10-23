package com.planetbiru.subscriber.activemq;

import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigSubscriberActiveMQ;

public class SubscriberActiveMQ extends Thread{
	
	private ActiveMQInstance iinstance = null;
	
	public SubscriberActiveMQ()
	{
		ConfigSubscriberActiveMQ.load(Config.getSubscriberActiveMQSettingPath());
	}

	@Override
	public void run()
	{
		this.iinstance = new ActiveMQInstance();
		this.iinstance.start();
	}

	public void stopService() {
		if(this.iinstance != null)
		{
			this.iinstance.stopService();
		}
	}
}
