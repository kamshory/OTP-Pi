package com.planetbiru.buzzer;

public class Tone {

	public long duration = 0;
	public int octav = 1;
	public String note = "";

	public Tone(String note, long dur, int octav) {
		this.note = note;
		this.duration = dur;
		this.octav = octav;
	}

}
