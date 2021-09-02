package com.planetbiru.buzzer;

public class Music {
	private static Sound sound = new Sound();
	
	private Music()
	{
		
	}
	
	public static void play(int pin, String song, int octave)
	{
		Music.sound = new Sound(pin, song, octave);
		Music.sound.start();
	}
	public static void stop()
	{
		Music.sound.stopSound();
	}
	
}
