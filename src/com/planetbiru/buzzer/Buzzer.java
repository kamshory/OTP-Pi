package com.planetbiru.buzzer;

import org.apache.log4j.Logger;

import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigBell;

public class Buzzer {
	
	private static Logger logger = Logger.getLogger(Buzzer.class);
	private Buzzer()
	{
		
	}

	public static void toneDisconnectAmqp() {
		if(ConfigBell.isAmqpDisconnected())
		{
			logger.info("toneDisconnectAmqp");
			Music.play(Config.getSoundPIN(), Config.getSoundDisconnectTone(), Config.getSoundDisconnectOctave(), Config.getSoundDisconnectTempo());
		}		
	}

	public static void toneDisconnectMqtt() {
		if(ConfigBell.isMqttDisconnected())
		{
			logger.info("toneDisconnectMqtt");
			Music.play(Config.getSoundPIN(), Config.getSoundDisconnectTone(), Config.getSoundDisconnectOctave(), Config.getSoundDisconnectTempo());
		}		
	}

	public static void toneDisconnectWs() {
		if(ConfigBell.isWsDisconnected())
		{
			logger.info("toneDisconnectWs");
			Music.play(Config.getSoundPIN(), Config.getSoundDisconnectTone(), Config.getSoundDisconnectOctave(), Config.getSoundDisconnectTempo());
		}		
	}

	public static void toneSMSFailed() {
		if(ConfigBell.isSmsFailure())
		{
			logger.info("toneSMSFailed");
			Music.play(Config.getSoundPIN(), Config.getSoundAlertTone(), Config.getSoundAlertOctave(), Config.getSoundAlertTempo());
		}
	}

}
