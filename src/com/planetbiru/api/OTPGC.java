package com.planetbiru.api;

import com.planetbiru.config.Config;

public class OTPGC extends Thread {

	private boolean running = false;
	@Override
	public void run()
	{
		this.running = true;
		long interval = Config.getOtpGCInterval();
		while(this.running)
		{
			System.out.println("Waiting for "+interval);
			try {
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			System.out.println("Execute GC");
			OTP.gc();
			OTP.save();
		}
	}
	public boolean isRunning() {
		return running;
	}
	public void setRunning(boolean running) {
		this.running = running;
	}
	
}
