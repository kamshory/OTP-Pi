package com.planetbiru.buzzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class GPIOLibrary
{
	public static final String EXP = "/sys/class/gpio/";
	public final int[] pins = { 0, 1, 4, 7, 8, 9, 10, 11, 14, 15, 17, 18, 21, 22, 23, 24, 25, 26, 27 };
	private ToneThread toneThread = new ToneThread();
	private boolean[] used;
	
	/**
	 * Constructor, needed to add the shutdown hook to prevent locked gpio pins
	 */
	public GPIOLibrary()
	{
		this.used = new boolean[pins.length];
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    @Override public void run() 
		    {
		    	for(int i = 0; i<used.length; i++)
		    	{
		    		if(used[i])
		    		{
		    			unExport(pins[i]);
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
	public void exportPin(boolean in, int n)
	{
		if (!validPin(n) || inUse(n))
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
				PrintWriter export = new PrintWriter(new FileWriter(EXP + "export", true));
				export.write(String.format("%d", n));
				export.close();
				PrintWriter dir = new PrintWriter(new FileWriter(EXP + "gpio" + n + "/direction", true));
				dir.write(in ? "in" : "out");
				dir.close();
				setUsed(n, true);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Lets the user unlock the gpio pins for later use
	 * @param n pin number. If invalid the function does nothing
	 */
	public void unExport(int n)
	{
		if (!validPin(n))
		{
			/**
			 * Do nothing
			 */
		}
		else
		{
			try
			{
				PrintWriter unexport = new PrintWriter(new FileWriter(EXP + "unexport", true));
				unexport.write(String.format("%d", n));
				unexport.close();
				setUsed(n, false);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Returns the value of pin n
	 * @param n pin number. 
	 * @return pin value(0, 1). If invalid the function returns -1
	 */
	public int readPin(int n)
	{
		if (!validPin(n))
		{
			return -1;
		}
		else
		{
			try(FileInputStream in = new FileInputStream(new File(EXP + "/gpio" + n + "/value")))
			{				
				int i = in.read();
				return i==30 ? 0 : 1;
			}
			catch (IOException e)
			{
				e.printStackTrace();
				return -1;
			}
		}
	}
	
	/**
	 * Writes a value to the pin
	 * @param n pin number. If invalid the function does nothing
	 * @param out true for 1 and false for 0
	 */
	public void writePin(int n, boolean out)
	{
		if (!validPin(n))
		{
			/**
			 * Do nothing
			 */
		}
		else
		{
				try
				{
					PrintWriter pin;
					pin = new PrintWriter(new FileWriter(EXP + "gpio" + n + "/value"));
					pin.write(out ? "1" : "0");
					pin.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}	
		}
	}

	/**
	 * Help function to check if the pin number is valid
	 * @param n pin number
	 * @return valid
	 */
	private boolean validPin(int n)
	{
		for (int i = 0; i < pins.length; i++)
			if (n == pins[i])
				return true;
		return false;
	}
	
	/**
	 * Help function to check if the pin is in use
	 * @param n pin number
	 * @return in use
	 */
	private boolean inUse(int n)
	{
		for (int i = 0; i < pins.length; i++)
			if (n == pins[i])
				return used[i];
		return false;
	}
	
	/**
	 * Help function to set the pin as used or unused
	 * @param n pin number
	 * @param p used or unused
	 */
	private void setUsed(int n, boolean p)
	{
		for (int i = 0; i < pins.length; i++)
			if (n == pins[i])
				used[i] = p;
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
