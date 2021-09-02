package com.planetbiru.buzzer;

public class Music {
	private static Sound sound = new Sound();
	
	private Music()
	{
		
	}
	
	public static void play(int pin, String song, int octave, int tempo)
	{
		Music.sound = new Sound(pin, song, octave, tempo);
		Music.sound.start();
	}
	public static void stop(int pin)
	{
		Music.sound.stopSound(pin);
	}
	
}
