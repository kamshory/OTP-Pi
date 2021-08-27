package com.planetbiru.buzzer;

import java.io.IOException;

import jdk.dio.ClosedDeviceException;
import jdk.dio.DeviceManager;
import jdk.dio.UnavailableDeviceException;
import jdk.dio.gpio.GPIOPin;

public class Ring extends Thread {
	
	public static final int MODE_INPUT_PULL_DOWN = 2;
	public static final int MODE_INPUT_PULL_UP = 1;
	public static final int MODE_OUTPUT_OPEN_DRAIN = 8;
	public static final int MODE_OUTPUT_PUSH_PULL = 4;

	private long duration = 0;
	private GPIOPin gpioPIN;
	private int pin = 1;

	public Ring(long duration) {
		this.duration = duration;
		this.createDevice();
	}

	private void createDevice() {
		try 
		{
			System.out.println("OPEN GPIO");
			this.gpioPIN = (GPIOPin) DeviceManager.open(this.pin, MODE_OUTPUT_OPEN_DRAIN);
			System.out.println("OPENED");
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
	}

	public Ring() {
		this.createDevice();
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

	private void on() throws UnavailableDeviceException, ClosedDeviceException, IOException {
		this.gpioPIN.setValue(true);
	}

	private void off() throws UnavailableDeviceException, ClosedDeviceException, IOException {
		this.gpioPIN.setValue(false);
	}

	public void stopService() {	
	}

}
