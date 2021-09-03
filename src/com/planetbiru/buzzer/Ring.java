package com.planetbiru.buzzer;


public class Ring extends Thread {
	
	public static final int MODE_INPUT_PULL_UP     = 1;
	public static final int MODE_INPUT_PULL_DOWN   = 2;
	public static final int MODE_OUTPUT_PUSH_PULL  = 4;
	public static final int MODE_OUTPUT_OPEN_DRAIN = 8;

	private int pin = 1;
	private String song = "";
	private int octave = 0;
	private int tempo = 80;

	@Override
	public void run()
	{
		Music.play(this.pin, this.song, this.octave, this.tempo);
	}

	public void stopService() 
	{
		Music.stop(this.pin);
	}

}
