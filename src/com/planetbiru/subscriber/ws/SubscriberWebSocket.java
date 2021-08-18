package com.planetbiru.subscriber.ws;

public class SubscriberWebSocket extends Thread{

	private boolean running;
	private WebSocketClientImpl ws;
	private long reconnectDelay = 5000;
	private long waitLoopParent = 1000;
	private long waitLoopChild = 30000;
	public SubscriberWebSocket(long reconnectDelay, long waitLoopParent, long waitLoopChild) {
		this.reconnectDelay = reconnectDelay;
		this.waitLoopParent = waitLoopParent;
		this.waitLoopChild = waitLoopChild;
	}
	
	@Override
	public void run()
	{
		this.startThread(true);
		do 
		{
			this.delay(this.waitLoopParent);
		}
		while(this.isRunning());
	}
	private void startThread(boolean reconnect) {
		this.ws = null;
		this.ws = new WebSocketClientImpl(this.reconnectDelay, this.waitLoopChild, this, reconnect);
		this.ws.start();	
	}
	public void restartThread() {
		this.ws = null;
		this.startThread(true);	
	}
	public void delay(long sleep)
	{
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
	public boolean isRunning() {
		return running;
	}

}
