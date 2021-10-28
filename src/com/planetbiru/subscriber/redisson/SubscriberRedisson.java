package com.planetbiru.subscriber.redisson;


public class SubscriberRedisson {
	
	private boolean running = false;

	public void start()
	{
		/**
		 * Do nothing
		 */
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public void stopService() {
		this.running = false;		
	}	
}
