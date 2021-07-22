package com.planetbiru.receiver.amqp;

import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigFeederAMQP;


public class ClientReceiverAMQP {
	RabbitMQReceiver amqp = new RabbitMQReceiver();
	public void start()
	{
		Config.setFeederAMQPSettingPath(Config.getFeederAMQPSettingPath());
		ConfigFeederAMQP.load(Config.getFeederAMQPSettingPath());
		if(ConfigFeederAMQP.isFeederAmqpEnable())
		{
			this.amqp = new RabbitMQReceiver();
			this.amqp.connect();
		}		
	}

	public void stopService() {
		this.amqp.stopService();	
	}
}

