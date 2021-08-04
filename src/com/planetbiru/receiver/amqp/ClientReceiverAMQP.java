package com.planetbiru.receiver.amqp;

import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigSubscriberAMQP;


public class ClientReceiverAMQP {
	RabbitMQReceiver amqp = new RabbitMQReceiver();
	private boolean running = false;
	public void start()
	{
		Config.setSubscriberAMQPSettingPath(Config.getSubscriberAMQPSettingPath());
		ConfigSubscriberAMQP.load(Config.getSubscriberAMQPSettingPath());
		if(ConfigSubscriberAMQP.isSubscriberAmqpEnable())
		{
			this.amqp = new RabbitMQReceiver();
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

