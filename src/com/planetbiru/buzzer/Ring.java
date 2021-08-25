package com.planetbiru.buzzer;

public class Ring extends Thread {

	private boolean running = false;
	private long duration = 0;

	public Ring(long duration) {
		this.duration = duration;
	}

	public void stopService() {
		this.running = false;	
	}
	
	public void run()
	{
		this.running = true;
		
	}

}
