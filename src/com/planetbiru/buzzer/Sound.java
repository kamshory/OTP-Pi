package com.planetbiru.buzzer;

import com.pi4j.wiringpi.SoftTone;
import com.planetbiru.config.Config;
import com.planetbiru.util.Utility;

public class Sound extends Thread {

	private String song = "";
	private boolean running = true;
	private int pin = 26;
	private int octave = 0;
	
	public Sound(int pin, String song, int octave) {
		this.pin = pin;
		this.song = song.replaceAll("/s+/", " ").trim();
		this.octave = octave;
	}
	
	public Sound() {
	}
	
	@Override
	public void run()
	{
		this.running = true;
		this.play();
	}
	
	private void play()
	{
		System.out.println("Sing");
        String[] tones = this.song.split(" ");
        

        for(int i = 0; i< tones.length && this.running; i++)
        {
            Tone parsed = parseTone(tones[i].trim());
            double tm = (3200/parsed.getDuration());
            
            int lOctave = parsed.getOctav();
            lOctave += this.octave;
            String tone = parsed.getNote();

            
            if(tone.indexOf('.') >=0)
            {
                tone = tone.replace(".", "");
                lOctave += 1;
            }
            if(tone.indexOf('#') >=0)
            {
                tone = tone.replace("#", "");
                tone += '#';
            }
            if(tone.indexOf('p') >=0 )
            {
                tone = "0";
                lOctave = 0;

            }  
            int frequency = (int) Math.round(createOscillation(tone, lOctave));
            long time = (long) tm;
            
            softToneWrite(this.pin, frequency);
            try 
            {
				Thread.sleep(time);
			} 
            catch (InterruptedException e) 
            {
				Thread.currentThread().interrupt();
			}
        }  
        softToneStop(this.pin);
	}
	private void softToneWrite(int i, int frequency) {
		if(Config.isSoundEnable())
		{
			SoftTone.softToneWrite(i, frequency);
		}
		System.out.println(frequency);
	}
	private void softToneStop(int i) {
		if(Config.isSoundEnable())
		{
			SoftTone.softToneStop(i);		
		}
	}
	private double createOscillation(String tone, int octav) {
		if(tone.equals("-") || tone.isEmpty())
		{
			return 0;
		}
		tone = tone.toUpperCase();
		
		double idx = (double) Stat.getList().indexOf(tone);
		idx-=9;
		
		idx += (octav * 12);
		
		return (440 * Math.pow(2, idx/12));
	}
	public Tone parseTone(String tone)
    {
        int i = 0;
        String chr = "";
        do
        {
            chr = tone.substring(i, i+1);
            i++;
        }
        while(Stat.getNumbers().indexOf(chr) >= 0 && !chr.isEmpty());     
        long dur = Utility.atoi(tone.substring(0, i-1));
        int start = i-1;
        String note = "";
		int octav = 0;
		if(i < tone.length())
        {
	        do
	        {
	            chr = tone.substring(i, i+1);
	            i++;
	        }
	        while(Stat.getNumbers().indexOf(chr) == -1 && !chr.isEmpty());
	        
	        note = tone.substring(start, i-1);
	        octav = Utility.atoi(tone.substring(i-1));
        }
        

        return new Tone(note, dur, octav);
    }

	public void stopSound() {
		this.running = false;
	}
}
