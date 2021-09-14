package com.planetbiru.buzzer;

import java.util.HashMap;
import java.util.Map;

public class SoftTone {
	private static Map<Integer, GPIOLibrary> gpio = new HashMap<>();
	private SoftTone()
	{
		
	}
	public static void softToneCreate(int pin) {
		if(!SoftTone.gpio.containsKey(pin))
		{
			SoftTone.gpio.put(pin, new GPIOLibrary());
		}	
		SoftTone.gpio.get(pin).exportPin(false, pin);	
	}
	public static void softToneWrite(int pin, int frequency, long time) {
		if(SoftTone.gpio.containsKey(pin))
		{
			SoftTone.gpio.get(pin).getToneThread().stopSound();
			SoftTone.gpio.get(pin).removeThread();
			SoftTone.gpio.get(pin).newThread();
			SoftTone.gpio.get(pin).getToneThread().setFrequency(SoftTone.gpio.get(pin), pin, frequency, time);
			SoftTone.gpio.get(pin).getToneThread().start();
		}
		
	}
	public static void softToneStop(int pin) {
		SoftTone.gpio.get(pin).getToneThread().stopSound();
		
	}

}
