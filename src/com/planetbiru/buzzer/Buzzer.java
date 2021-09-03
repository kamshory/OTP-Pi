package com.planetbiru.buzzer;

import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigBell;

public class Buzzer {
	
	private static Ring ringTone = new Ring();

	private Buzzer()
	{
		
	}

	public static void ringing() {
		Buzzer.ringTone.stopService();
		Buzzer.ringTone = new Ring();
		Buzzer.ringTone.start();
	}

	public static void toneDisconnectAmqp() {
		if(ConfigBell.isAmqpDisconnected())
		{
			Music.play(Config.getSoundPIN(), Config.getSoundDisconnectTone(), Config.getSoundDisconnectOctave(), Config.getSoundDisconnectTempo());
		}		
	}

	public static void toneDisconnectMqtt() {
		if(ConfigBell.isMqttDisconnected())
		{
			Music.play(Config.getSoundPIN(), Config.getSoundDisconnectTone(), Config.getSoundDisconnectOctave(), Config.getSoundDisconnectTempo());
		}		
	}

	public static void toneDisconnectWs() {
		if(ConfigBell.isWsDisconnected())
		{
			Music.play(Config.getSoundPIN(), Config.getSoundDisconnectTone(), Config.getSoundDisconnectOctave(), Config.getSoundDisconnectTempo());
		}		
	}

	public static void toneSMSFailed() {
		if(ConfigBell.isSmsFailure())
		{
			Music.play(Config.getSoundPIN(), Config.getSoundAlertTone(), Config.getSoundAlertOctave(), Config.getSoundAlertTempo());
		}
	}

}
