package com.planetbiru.buzzer;

import java.io.IOException;

import com.pi4j.wiringpi.SoftTone;


public class Ring extends Thread {
	
	public static final int MODE_INPUT_PULL_UP     = 1;
	public static final int MODE_INPUT_PULL_DOWN   = 2;
	public static final int MODE_OUTPUT_PUSH_PULL  = 4;
	public static final int MODE_OUTPUT_OPEN_DRAIN = 8;

	private long duration = 0;
	private int pin = 1;
	private int frequency = 200;

	public Ring(long duration) {
		this.duration = duration;
		this.setup();
	}

	
	public Ring() {
		this.setup();
	}

	private void setup() {
		SoftTone.softToneCreate(this.pin);
		
	}


	@Override
	public void run()
	{
		System.out.println("Start Ring");
		try 
		{
			this.on();
			this.waitUntil(this.duration);
			this.off();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		System.out.println("Stop Ring");
	}

	private void waitUntil(long duration) {
		try 
		{
			Thread.sleep(duration);
		} 
		catch (InterruptedException e) 
		{
			Thread.currentThread().interrupt();
		}
		
	}

	private void on() throws IOException {
		SoftTone.softToneWrite(this.pin, this.frequency);
	}

	private void off() throws IOException {
		SoftTone.softToneStop(this.pin);
	}

	public void stopService() {	
	}

}
