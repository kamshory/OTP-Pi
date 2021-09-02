package com.planetbiru.buzzer;

import com.planetbiru.config.ConfigBell;

public class Buzzer {
	
	public static final int SMS_FAILED = 1;
	private static Ring ringTone = new Ring();



	private Buzzer()
	{
		
	}

	public static void ringing(long duration) {
		Buzzer.ringTone.stopService();
		Buzzer.ringTone = new Ring(duration);
		Buzzer.ringTone.start();
	}



	public static void alert(int code) {
		if(code == SMS_FAILED && ConfigBell.isSmsFailure())
		{
			Buzzer.ringing(1000);
		}
		
	}

}
