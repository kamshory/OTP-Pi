package com.planetbiru.buzzer;

public class Tone {

	private long duration = 0;
	private int octav = 1;
	private String note = "";

	public Tone(String note, long duration, int octav) {
		this.note = note;
		this.duration = duration;
		this.octav = octav;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public int getOctav() {
		return octav;
	}

	public void setOctav(int octav) {
		this.octav = octav;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

}
