package com.planetbiru.buzzer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import org.apache.log4j.Logger;

import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigBell;

public class Buzzer {
	
	private static Map<String, Boolean> playingStatus = new HashMap<>();
	
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
	
	public static void toneDisconnectActiveMQ() {
		if(ConfigBell.isActiveMQDisconnected())
		{
			logger.info("toneDisconnectActiveMQ");
			Music.play(Config.getSoundPIN(), Config.getSoundDisconnectTone(), Config.getSoundDisconnectOctave(), Config.getSoundDisconnectTempo());
		}		
	}
	
	public static void toneDisconnectStomp() {
		if(ConfigBell.isStompDisconnected())
		{
			makeSound();
			logger.info("toneDisconnectStomp");
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

	public static void toneDisconnectRedis() {
		if(ConfigBell.isRedisDisconnected())
		{
			logger.info("toneDisconnectRedis");
			Music.play(Config.getSoundPIN(), Config.getSoundDisconnectTone(), Config.getSoundDisconnectOctave(), Config.getSoundDisconnectTempo());
		}				
	}
	
	public static void makeSound(){

	    try
	    {
			String path = "C:/bitbucket/Ring10.wav";
		    File lol = new File(path);
	        Clip clip = AudioSystem.getClip();
	        clip.open(AudioSystem.getAudioInputStream(lol));
	        long len = clip.getMicrosecondLength();
	        if(!Buzzer.isPlaying(path))
	        {
	        	Buzzer.setPlaying(path, true);
	        	clip.start();
	        	Thread.sleep((long) Math.ceil((double)len/1000));
	        	Buzzer.setPlaying(path, false);
	        }
	    } 
	    catch(InterruptedException e)
	    {
	    	Thread.currentThread().interrupt();
	    }
	    catch (Exception e)
	    {	    	
	        logger.error(e.getMessage());
	    }
	}

	

	

}
