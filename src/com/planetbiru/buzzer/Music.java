package com.planetbiru.buzzer;

import com.planetbiru.util.Utility;

public class Music {
	public static void play(String song)
	{
		song = "16c2 16d2 6e2 8d2 8c2 4d2 4g2 32- 6c2 8c2 8b1 16a1 4b1 4e2 32- 6a1 6b1 6c2 6g1 6b1 8c2 6d2 6e2 8c2 4d2";
		
		song = song.replaceAll("/s+/", " ").trim();
        String[] tones = song.split(" ");
        long curtime = System.currentTimeMillis()+1 ;
        int offset = 3;

        for(int i = 0; i< tones.length; i++)
        {
            Tone parsed = parseTone(tones[i]);
            double tm = 3.2/parsed.duration;
            //console.log(parsed)
            
            int octav = parsed.octav;
            octav += offset;
            String tone = parsed.note;

            
            if(tone.indexOf('.') >=0)
            {
                tone = tone.replace(".", "");
                octav += 1;
            }
            if(tone.indexOf('#') >=0)
            {
                tone = tone.replace("#", "");
                tone += '#';
            }
            if(tone.indexOf('p') >=0 )
            {
                tone = "0";
                octav = 0;

            }   

        }          
		
	}
	public static Tone parseTone(String tone)
    {
        String numbers = "0123456789";
        int i = 0;
        String chr = "";
        do
        {
            chr = tone.substring(i, i+1);
            i++;
        }
        while(numbers.indexOf(chr) >= 0 && !chr.isEmpty());

        

        long dur = Utility.atoi(tone.substring(0, i-1));
        int start = i-1;
        

        do
        {
            chr = tone.substring(i, i+1);
            i++;
        }
        while(numbers.indexOf(chr) == -1 && !chr.isEmpty());
        String note = tone.substring(start, i-1);
        int octav = Utility.atoi(tone.substring(i-1));
        

        return new Tone(note, dur, octav);
    }
}
