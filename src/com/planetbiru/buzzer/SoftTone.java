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
			SoftTone.gpio.put(pin, new GPIOLibrary(pin));
			SoftTone.gpio.get(pin).exportPin(Stat.OUT);	
		}	
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
		if(SoftTone.gpio.get(pin).getToneThread() != null)
		{
			SoftTone.gpio.get(pin).getToneThread().stopSound();
		}		
	}

}
