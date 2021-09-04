package com.planetbiru.buzzer;

import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigBell;

public class Buzzer {
	
	private Buzzer()
	{
		
	}

	public static void toneDisconnectAmqp() {
		if(ConfigBell.isAmqpDisconnected())
		{
			System.out.println("toneDisconnectAmqp");
			Music.play(Config.getSoundPIN(), Config.getSoundDisconnectTone(), Config.getSoundDisconnectOctave(), Config.getSoundDisconnectTempo());
		}		
	}

	public static void toneDisconnectMqtt() {
		if(ConfigBell.isMqttDisconnected())
		{
			System.out.println("toneDisconnectMqtt");
			Music.play(Config.getSoundPIN(), Config.getSoundDisconnectTone(), Config.getSoundDisconnectOctave(), Config.getSoundDisconnectTempo());
		}		
	}

	public static void toneDisconnectWs() {
		if(ConfigBell.isWsDisconnected())
		{
			System.out.println("toneDisconnectWs");
			Music.play(Config.getSoundPIN(), Config.getSoundDisconnectTone(), Config.getSoundDisconnectOctave(), Config.getSoundDisconnectTempo());
		}		
	}

	public static void toneSMSFailed() {
		if(ConfigBell.isSmsFailure())
		{
			System.out.println("toneSMSFailed");
			Music.play(Config.getSoundPIN(), Config.getSoundAlertTone(), Config.getSoundAlertOctave(), Config.getSoundAlertTempo());
		}
	}

}
