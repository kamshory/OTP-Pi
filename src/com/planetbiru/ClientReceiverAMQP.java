package com.planetbiru;

import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigFeederAMQP;
import com.planetbiru.receiver.amqp.RabbitMQReceiver;


public class ClientReceiverAMQP {

	void init()
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

