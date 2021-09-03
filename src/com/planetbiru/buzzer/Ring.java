package com.planetbiru.buzzer;


public class Ring extends Thread {
	

	private int pin = 26;
	private String song = "";
	private int octave = 0;
	private int tempo = 120;

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
