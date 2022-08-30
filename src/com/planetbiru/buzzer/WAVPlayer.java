package com.planetbiru.buzzer;

import java.io.File;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import org.apache.log4j.Logger;

public class WAVPlayer {
	
	private Clip clip;

	private boolean playing = false;

	private long length = 0;

	private String path = "";
	
	private static Logger logger = Logger.getLogger(WAVPlayer.class);

	public WAVPlayer(String path)
	{
		this.path = path;
		try
	    {
		    File lol = new File(path);
	        this.clip = AudioSystem.getClip();
	        this.clip.open(AudioSystem.getAudioInputStream(lol));
	        this.length  = this.clip.getMicrosecondLength();
	        
	    } 
	    catch (Exception e)
	    {	    	
	        logger.error(e.getMessage());
	    }
	}
	
	public void play()
	{
		if(!this.playing)
        {
        	Buzzer.setPlaying(path, true);
        	this.clip.start();
        	try {
				Thread.sleep((long) Math.ceil((double)this.length/1000));
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
        	this.playing = false;
        }
	}
	
	public void stop()
	{
		if(this.clip != null && this.playing)
		{
			this.clip.stop();
		}
	}

}
