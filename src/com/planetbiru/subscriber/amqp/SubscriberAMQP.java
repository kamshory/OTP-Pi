package com.planetbiru.subscriber.amqp;

import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigSubscriberAMQP;


public class SubscriberAMQP {
	RabbitMQSubscriber amqp = new RabbitMQSubscriber();
	RabbitMQInspector inspector = new RabbitMQInspector();
	private boolean running = false;
	
	public void start()
	{
		ConfigSubscriberAMQP.load(Config.getSubscriberAMQPSettingPath());
		if(ConfigSubscriberAMQP.isSubscriberAmqpEnable())
		{
			this.amqp = new RabbitMQSubscriber();
			this.inspector = new RabbitMQInspector(this.amqp);
			this.inspector.start();
			boolean con = this.amqp.connect();
			if(con)
			{
				this.amqp.flagConnected();
			}
			else
			{
				this.amqp.flagDisconnected();
			}
			this.running = true;
		}		
	}

	public void stopService() {
		this.inspector.stopService();	
		this.amqp.stopService();	
		this.amqp.flagDisconnected();
		this.amqp = new RabbitMQSubscriber();
		this.inspector = new RabbitMQInspector();
		this.running = false;
	}

	public boolean isRunning() {
		return running;
	}
	
}

