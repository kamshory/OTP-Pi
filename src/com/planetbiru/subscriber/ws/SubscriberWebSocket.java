package com.planetbiru.subscriber.ws;

public class SubscriberWebSocket extends Thread{

	private boolean running = true;
	private WebSocketClientImpl ws = null;
	private long reconnectDelay = 5000;
	private long waitLoopParent = 1000;
	private long waitLoopChild = 30000;
	public SubscriberWebSocket(long reconnectDelay, long waitLoopParent, long waitLoopChild) {
		this.reconnectDelay = reconnectDelay;
		this.waitLoopParent = waitLoopParent;
		this.waitLoopChild = waitLoopChild;
	}
	
	public SubscriberWebSocket() {
		/**
		 * Just default constructor
		 */
	}

	@Override
	public void run()
	{
		this.running = true;
		this.startThread(true);
		do 
		{
			this.delay(this.waitLoopParent);
		}
		while(this.running);
	}
	public boolean isConnected()
	{
		if(ws == null)
		{
			return false;
		}
		else
		{
			return this.ws.isConnected();
		}
	}
	private void startThread(boolean reconnect) {
		this.setWs(null);
		this.setWs(new WebSocketClientImpl(this.reconnectDelay, this.waitLoopChild, this, reconnect));
		this.getWs().start();	
	}
	public void restartThread() {
		this.setWs(null);
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
		if(this.getWs() != null)
		{
			this.getWs().stopService();
		}
	}
	public boolean isRunning() {
		return running;
	}

	public WebSocketClientImpl getWs() {
		return ws;
	}

	public void setWs(WebSocketClientImpl ws) {
		this.ws = ws;
	}

}
