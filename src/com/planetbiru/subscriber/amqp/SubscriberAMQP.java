package com.planetbiru.subscriber.amqp;

import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigSubscriberAMQP;


public class SubscriberAMQP {
	RabbitMQSubscriber amqp = new RabbitMQSubscriber();
	private boolean running = false;
	
	public void start()
	{
		Config.setSubscriberAMQPSettingPath(Config.getSubscriberAMQPSettingPath());
		ConfigSubscriberAMQP.load(Config.getSubscriberAMQPSettingPath());
		if(ConfigSubscriberAMQP.isSubscriberAmqpEnable())
		{
			this.amqp = new RabbitMQSubscriber();
			this.amqp.connect();
			this.running = true;
		}		
	}

	public void stopService() {
		this.amqp.stopService();	
		this.running = false;
	}

	public boolean isRunning() {
		return running;
	}
	
}

