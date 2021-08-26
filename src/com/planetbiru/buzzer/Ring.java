package com.planetbiru.buzzer;

public class Ring extends Thread {

	private long duration = 0;

	public Ring(long duration) {
		this.duration = duration;
	}

	public void stopService() {	
	}
	
	@Override
	public void run()
	{
		this.on();
		this.waitUntil(this.duration);
		this.off();
	}

	private void waitUntil(long duration) {
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		
	}

	private void on() {
	}

	private void off() {
	}

}
