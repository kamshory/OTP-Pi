package com.planetbiru.buzzer;

public class ToneThread extends Thread {
	private int frequency = 0;
	private boolean running;
	private GPIOLibrary gpioLibrary;
	private int pin;
	private long time;
	
	@Override
	public void run()
	{
		if(this.frequency > 0)
		{
			double itnv = 1000000000 / (double) (this.frequency * 2);
			do
			{
				this.gpioLibrary.writePin(this.pin, true);
				this.waitUntil((long) itnv);
				this.gpioLibrary.writePin(this.pin, false);
				this.waitUntil((long) itnv);
			}
			while(this.running);
		}
		else
		{
			this.gpioLibrary.writePin(this.pin, false);
			try 
			{
				Thread.sleep(this.time);
			} 
			catch (InterruptedException e) 
			{
				Thread.currentThread().interrupt();
				e.printStackTrace();
			}
		}
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(GPIOLibrary gpioLibrary, int pin, int frequency, long time) {
		this.gpioLibrary = gpioLibrary;
		this.pin = pin;
		this.frequency = frequency;
		this.time = time;
		this.running = true;
	}

	public void stopSound() {
		this.running = false;	
	}
	
	public void waitUntil(long interval)
	{
	    long start = System.nanoTime();
	    long end=0;
	    do
	    {
	        end = System.nanoTime();
	    }
	    while(start + interval >= end);
	}
	
}
