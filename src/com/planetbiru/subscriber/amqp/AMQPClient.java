package com.planetbiru.subscriber.amqp;

public interface AMQPClient {
	
	public boolean connect();

	public boolean isConnected();

	public void restart();

	public void flagConnected();

	public void flagDisconnected();

	public void stopService();
	
}
