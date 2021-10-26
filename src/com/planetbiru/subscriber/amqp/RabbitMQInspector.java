package com.planetbiru.subscriber.amqp;

import com.planetbiru.config.ConfigSubscriberAMQP;

public class RabbitMQInspector extends Thread {

	private RabbitMQSubscriber amqp = new RabbitMQSubscriber();
	private boolean running = true;
	
	public RabbitMQInspector() {
		/**
		 * Do nothing
		 */
	}
	
	@Override
	public void run()
	{
		while(this.running)
		{
			this.delay(ConfigSubscriberAMQP.getSubscriberAmqpReconnectDelay());
			if(!this.amqp.isConnected())
			{
				this.amqp.restart();
			}
		}
	}

	private void delay(long sleep) {
		try 
		{
			Thread.sleep(sleep);
		} 
		catch (InterruptedException e) 
		{
			Thread.currentThread().interrupt();
		}		
	}

	public RabbitMQInspector(RabbitMQSubscriber amqp) {
		this.amqp = amqp;
	}

	

	public void stopService() {
		this.running = false;	
	}

}

