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
			try {
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
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
