package com.planetbiru.buzzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class GPIOLibrary
{
	private ToneThread toneThread = new ToneThread();
	private int pin = 0;
	private boolean[] used;
	
	/**
	 * Constructor, needed to add the shutdown hook to prevent locked gpio pins
	 */
	public GPIOLibrary(int pin)
	{
		this.pin = pin;
		this.used = new boolean[Stat.PINS.length];
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    @Override 
		    public void run() 
		    {
		    	for(int i = 0; i<used.length; i++)
		    	{
		    		if(used[i])
		    		{
		    			unExport(Stat.PINS[i]);
		    		}
		    	}		    			
		    }
		 });
	}

	/**
	 * Lets the user export a pin.
	 * @param in true for a "in" pin and false for a "out" pin
	 * @param n pin number. If invalid the functions does nothing
	 */
	public void exportPin(boolean in)
	{		
		if (!validPin(this.pin) || inUse(this.pin))
		{
			/**
			 * Do nothing
			 */
		}
		else
		{
			try
			{
				this.toneThread.stopSound();
				this.toneThread = null;
				this.toneThread = new ToneThread();
				PrintWriter export = new PrintWriter(new FileWriter(Stat.EXP + "export", true));
				export.write(String.format("%d", this.pin));
				export.close();
				PrintWriter dir = new PrintWriter(new FileWriter(Stat.EXP + "gpio" + this.pin + "/direction", true));
				dir.write(in ? "in" : "out");
				dir.close();
				setUsed(this.pin, true);
			}
			catch (IOException e)
			{
				/**
				 * Do nothing
				 */
			}
		}
	}
	
	/**
	 * Lets the user unlock the GPIO pins for later use
	 * @param n pin number. If invalid the function does nothing
	 */
	public void unExport()
	{
		this.unExport(this.pin);
	}
	
	public void unExport(int pin)
	{
		if (!validPin(pin))
		{
			/**
			 * Do nothing
			 */
		}
		else
		{
			try
			{
				PrintWriter unexport = new PrintWriter(new FileWriter(Stat.EXP + "unexport", true));
				unexport.write(String.format("%d", pin));
				unexport.close();
				setUsed(pin, false);
			}
			catch (IOException e)
			{
				/**
				 * Do nothing
				 */
			}
		}
	}
	
	/**
	 * Returns the value of pin
	 * @param pin pin number. 
	 * @return pin value(0, 1). If invalid the function returns -1
	 */
	public int readPin(int pin)
	{
		if (!validPin(pin))
		{
			return -1;
		}
		else
		{
			try(FileInputStream in = new FileInputStream(new File(Stat.EXP + "/gpio" + pin + "/value")))
			{				
				int i = in.read();
				return i==30 ? 0 : 1;
			}
			catch (IOException e)
			{
				return -1;
			}
		}
	}
	
	/**
	 * Writes a value to the pin
	 * @param n pin number. If invalid the function does nothing
	 * @param out true for 1 and false for 0
	 */
	public void writePin(boolean out)
	{
		if (!validPin(this.pin))
		{
			/**
			 * Do nothing
			 */
		}
		else
		{
			try(PrintWriter pinWriter = new PrintWriter(new FileWriter(Stat.EXP + "gpio" + this.pin + "/value")))
			{
				pinWriter.write(out ? "1" : "0");
			}
			catch (IOException e)
			{
				/**
				 * Do nothing
				 */
			}	
		}
	}

	/**
	 * Help function to check if the pin number is valid
	 * @param pin pin number
	 * @return valid
	 */
	private boolean validPin(int pin)
	{
		for (int i = 0; i < Stat.PINS.length; i++)
		{
			if (pin == Stat.PINS[i])
			{
				return true;
			}
		}			
		return false;
	}
	
	/**
	 * Help function to check if the pin is in use
	 * @param pin pin number
	 * @return in use
	 */
	private boolean inUse(int pin)
	{
		for (int i = 0; i < Stat.PINS.length; i++)
		{
			if (pin == Stat.PINS[i])
			{
				return this.used[i];
			}
		}
		return false;
	}
	
	/**
	 * Help function to set the pin as used or unused
	 * @param pin pin number
	 * @param used used or unused
	 */
	private void setUsed(int pin, boolean used)
	{
		for (int i = 0; i < Stat.PINS.length; i++)
		{
			if (pin == Stat.PINS[i])
			{
				this.used[i] = used;
			}
		}
	}

	public ToneThread getToneThread() {
		return toneThread;
	}

	public void setToneThread(ToneThread toneThread) {
		this.toneThread = toneThread;
	}

	public void removeThread() {
		this.toneThread = null;		
	}

	public void newThread() {
		this.toneThread = new ToneThread();		
	}
}
