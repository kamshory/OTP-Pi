package com.planetbiru.receiver.amqp;

import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigFeederAMQP;


public class ClientReceiverAMQP {

	public void start()
	{
		Config.setFeederAMQPSettingPath(Config.getFeederAMQPSettingPath());
		ConfigFeederAMQP.load(Config.getFeederAMQPSettingPath());
		if(ConfigFeederAMQP.isFeederAmqpEnable())
		{
			RabbitMQReceiver amqp = new RabbitMQReceiver();
			amqp.connect();
		}		
	}
}

