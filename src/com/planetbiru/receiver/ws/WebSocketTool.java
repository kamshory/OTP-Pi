package com.planetbiru.receiver.ws;

public class WebSocketTool extends Thread{

	private boolean running;
	private long reconnectDelay = 10000;
	public WebSocketTool(long reconnectDelay) {
		this.reconnectDelay = reconnectDelay;
	}
	private WebSocketClientImpl ws;
	
	@Override
	public void run()
	{
		this.startThread(true);
		do 
		{
			this.delay(1000);
		}
		while(this.running);
	}
	private void startThread(boolean reconnect) {
		this.ws = null;
		this.ws = new WebSocketClientImpl(this.reconnectDelay, this, reconnect);
		this.ws.start();
		
	}
	public void restartThread() {
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

}
