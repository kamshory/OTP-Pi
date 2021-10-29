package com.planetbiru.subscriber.amqp;

public class RabbitMQSubscriber extends Thread implements AMQPClient {
	
	protected boolean connected = false;
	protected boolean lastConnected = false;

	@Override
	public void run()
	{
		/**
		 * Do nothing
		 */
	}

	@Override
	public void connect() {
		/**
		 * Do nothing
		 */
	}

	@Override
	public boolean isConnected() {
		return this.connected;
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
