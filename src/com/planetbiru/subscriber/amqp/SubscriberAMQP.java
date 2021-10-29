package com.planetbiru.subscriber.amqp;

import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigSubscriberAMQP;


public class SubscriberAMQP {
	RabbitMQSubscriber amqp = new RabbitMQSubscriber();
	RabbitMQInspector inspector = new RabbitMQInspector();
	private boolean running = false;
	private int version = 0;
	
	public void start()
	{
		ConfigSubscriberAMQP.load(Config.getSubscriberAMQPSettingPath());
		this.version = ConfigSubscriberAMQP.getSubscriberAmqpVersion();
		if(ConfigSubscriberAMQP.isSubscriberAmqpEnable())
		{
			if(this.version == 0)
			{
				this.amqp = new RabbitMQSubV0();
			}
			else
			{
				this.amqp = new RabbitMQSubV1();
			}
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

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public void stopService() {
		this.inspector.stopService();	
		this.amqp.stopService();	
		this.amqp.flagDisconnected();
		this.amqp = new RabbitMQSubV0();
		this.inspector = new RabbitMQInspector();
		this.running = false;
	}

	public boolean isRunning() {
		return running;
	}
	
}

