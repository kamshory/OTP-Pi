package com.planetbiru.subscriber.activemq;

public class SubscriberActiveMQ extends Thread{
	
	private ActiveMQInstance iinstance = null;

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
