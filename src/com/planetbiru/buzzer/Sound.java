package com.planetbiru.buzzer;

import com.pi4j.wiringpi.SoftTone;
import com.planetbiru.config.Config;
import com.planetbiru.util.Utility;

public class Sound extends Thread {

	private String song = "";
	private boolean running = true;
	private int pin = 26;
	private int octave = 0;
	private int tempo = 120;
	
	public Sound(int pin, String song, int octave, int tempo) 
	{
		this.pin = pin;
		this.song = song.replaceAll("/s+/", " ").trim();
		this.octave = octave;
		this.tempo = tempo;
		SoftTone.softToneCreate(this.pin);
	}
	
	public Sound() 
	{
	}
	
	@Override
	public void run()
	{
		this.running = true;
		this.play();
	}
	
	private void play()
	{
        String[] tones = this.song.split(" ");       

        for(int i = 0; i < tones.length && this.running; i++)
        {
            Tone parsed = parseTone(tones[i].trim());
            double timeDouble = (240000/((double) this.tempo * (double) parsed.getDuration()));
            
            int lOctave = parsed.getOctav();
            lOctave += this.octave;
            String tone = parsed.getNote();
            
            if(tone.indexOf('.') >=0)
            {
                tone = tone.replace(".", "");
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
            long time = (long) timeDouble;            
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
	
	private void softToneWrite(int pin, int frequency) 
	{
		if(Config.isSoundEnable())
		{
			SoftTone.softToneWrite(pin, frequency);
		}
		/**
		 * System.out.println(frequency);
		 */
	}
	
	private void softToneStop(int pin) 
	{
		if(Config.isSoundEnable())
		{
			SoftTone.softToneStop(pin);		
		}
	}
	
	private double createOscillation(String tone, int octav) 
	{
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
        long duration = Utility.atoi(tone.substring(0, i-1));
        int start = i-1;
        String note = "";
		int lOctave = 0;
		if(i < tone.length())
        {
	        do
	        {
	            chr = tone.substring(i, i+1);
	            i++;
	        }
	        while(Stat.getNumbers().indexOf(chr) == -1 && !chr.isEmpty());	        
	        note = tone.substring(start, i-1);
	        lOctave = Utility.atoi(tone.substring(i-1));
        }
        return new Tone(note, duration, lOctave);
    }

	public void stopSound(int pin) 
	{
		this.softToneStop(pin);
		this.running = false;
	}
}
