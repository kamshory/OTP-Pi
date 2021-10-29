package com.planetbiru.subscriber.amqp;

public class RabbitMQSubscriber extends Thread implements AMQPClient {
	
	@Override
	public void run()
	{
		/**
		 * Do nothing
		 */
	}

	@Override
	public boolean connect() {
		return false;
	}

	@Override
	public boolean isConnected() {
		return false;
	}

	@Override
	public void restart() {
		/**
		 * Do nothing
		 */
	}

	@Override
	public void flagConnected() {
		/**
		 * Do nothing
		 */
	}

	@Override
	public void flagDisconnected() {
		/**
		 * Do nothing
		 */
	}

	@Override
	public void stopService() {
		/**
		 * Do nothing
		 */
	}

}
