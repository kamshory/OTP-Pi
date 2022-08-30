package com.planetbiru.buzzer;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigBell;

public class Buzzer {
	
	private static Map<String, Boolean> playingStatus = new HashMap<>();
	private static Map<String, WAVPlayer> playlist = new HashMap<>();
	
	private static Logger logger = Logger.getLogger(Buzzer.class);
	
	private Buzzer()
	{
		
	}
	
	public static void setPlaying(String path, boolean status)
	{
		Buzzer.playingStatus.put(path, Boolean.valueOf(status));
	}
	
	public static boolean isPlaying(String path)
	{
		return Buzzer.playingStatus.getOrDefault(path, Boolean.FALSE).booleanValue();
	}
	
	public static void toneDisconnectAmqp() {
		if(ConfigBell.isAmqpDisconnected() == 1)
		{
			logger.info("toneDisconnectAmqp");
			Music.play(Config.getSoundPIN(), Config.getSoundDisconnectTone(), Config.getSoundDisconnectOctave(), Config.getSoundDisconnectTempo());
		}		
	}

	public static void toneDisconnectMqtt() {
		if(ConfigBell.isMqttDisconnected() == 1)
		{
			logger.info("toneDisconnectMqtt");
			Music.play(Config.getSoundPIN(), Config.getSoundDisconnectTone(), Config.getSoundDisconnectOctave(), Config.getSoundDisconnectTempo());
		}		
	}
	
	public static void toneDisconnectActiveMQ() {
		if(ConfigBell.isActiveMQDisconnected() == 1)
		{
			logger.info("toneDisconnectActiveMQ");
			Music.play(Config.getSoundPIN(), Config.getSoundDisconnectTone(), Config.getSoundDisconnectOctave(), Config.getSoundDisconnectTempo());
		}		
	}
	
	public static void toneDisconnectStomp() {
		if(ConfigBell.isStompDisconnected() == 1)
		{
			logger.info("toneDisconnectStomp");
			Music.play(Config.getSoundPIN(), Config.getSoundDisconnectTone(), Config.getSoundDisconnectOctave(), Config.getSoundDisconnectTempo());
		}		
	}

	public static void toneDisconnectWs() {
		if(ConfigBell.isWsDisconnected() == 1)
		{
			logger.info("toneDisconnectWs");
			Music.play(Config.getSoundPIN(), Config.getSoundDisconnectTone(), Config.getSoundDisconnectOctave(), Config.getSoundDisconnectTempo());
		}		
	}

	public static void toneSMSFailed() {
		if(ConfigBell.isSmsFailure() == 1)
		{
			logger.info("toneSMSFailed");
			Music.play(Config.getSoundPIN(), Config.getSoundAlertTone(), Config.getSoundAlertOctave(), Config.getSoundAlertTempo());
		}
	}

	public static void toneDisconnectRedis() {
		if(ConfigBell.isRedisDisconnected() == 1)
		{
			logger.info("toneDisconnectRedis");
			Music.play(Config.getSoundPIN(), Config.getSoundDisconnectTone(), Config.getSoundDisconnectOctave(), Config.getSoundDisconnectTempo());
		}				
	}
	
	public static void playSound(String path) {
		if(!Buzzer.playlist.containsKey(path))
		{
			Buzzer.playlist.put(path, new WAVPlayer(path));
			Buzzer.playlist.get(path).play();
		}	    
	}

	

	

}
