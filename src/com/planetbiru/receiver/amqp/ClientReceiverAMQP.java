package com.planetbiru.receiver.amqp;

import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigFeederAMQP;


public class ClientReceiverAMQP {
	RabbitMQReceiver amqp = new RabbitMQReceiver();
	private boolean running = false;
	public void start()
	{
		Config.setFeederAMQPSettingPath(Config.getFeederAMQPSettingPath());
		ConfigFeederAMQP.load(Config.getFeederAMQPSettingPath());
		if(ConfigFeederAMQP.isFeederAmqpEnable())
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

