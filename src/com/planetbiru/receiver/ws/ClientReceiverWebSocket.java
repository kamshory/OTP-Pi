package com.planetbiru.receiver.ws;

public class ClientReceiverWebSocket extends Thread{

	private boolean running;
	private WebSocketClientImpl ws;
	private long reconnectDelay = 5000;
	private long waitLoop = 1000;
	public ClientReceiverWebSocket(long reconnectDelay, long waitLoop) {
		this.reconnectDelay = reconnectDelay;
		this.waitLoop = waitLoop;
	}
	
	@Override
	public void run()
	{
		this.startThread(true);
		do 
		{
			this.delay(this.waitLoop);
		}
		while(this.isRunning());
	}
	private void startThread(boolean reconnect) {
		this.ws = null;
		this.ws = new WebSocketClientImpl(this.reconnectDelay, this, reconnect);
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
