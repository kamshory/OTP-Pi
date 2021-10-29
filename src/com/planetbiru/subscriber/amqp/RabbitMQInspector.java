package com.planetbiru.subscriber.amqp;

import com.planetbiru.config.ConfigSubscriberAMQP;

public class RabbitMQInspector extends Thread {

	private RabbitMQSubscriber amqp = new RabbitMQSubscriber();
	private boolean running = false;
	
	public RabbitMQInspector() {
	}
	
	public RabbitMQInspector(RabbitMQSubscriber amqp) {
		/**
		 * Do nothing
		 */
		this.amqp = amqp;
	}
	
	@Override
	public void run()
	{
		this.running = true;
		while(this.running)
		{
			this.delay(ConfigSubscriberAMQP.getSubscriberAmqpReconnectDelay());
			if(!this.amqp.isConnected() && this.running)
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

	public void stopService() {
		this.running = false;	
	}

}

