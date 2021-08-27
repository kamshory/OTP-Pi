package com.planetbiru.buzzer;

public class Buzzer {
	
	private static Ring ring = new Ring();



	private Buzzer()
	{
		
	}
	
	

	public static void ring(long duration) {
		Buzzer.ring.stopService();
		Buzzer.ring = new Ring(duration);
		Buzzer.ring.start();
	}

}
