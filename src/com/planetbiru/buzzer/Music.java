package com.planetbiru.buzzer;

public class Music {
	private static Sound sound = new Sound();
	
	private Music()
	{
		
	}
	
	/**
	 * Play sound from a string
	 * @param pin GPIO pin
	 * @param song String contain sound code
	 * @param octave Octave
	 * @param tempo Tempo in bit per minute
	 */
	public static void play(int pin, String song, int octave, int tempo)
	{
		if(Music.sound.isRunning())
		{
			Music.sound.stopSound(pin);
		}
		Music.sound = new Sound(pin, song, octave, tempo);
		Music.sound.start();
	}
	
	/**
	 * Stop sound
	 * @param pin GPIO pin
	 */
	public static void stop(int pin)
	{
		Music.sound.stopSound(pin);
	}
	
}
