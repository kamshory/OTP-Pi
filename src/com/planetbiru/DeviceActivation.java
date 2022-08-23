package com.planetbiru;

public class DeviceActivation {
	private static boolean activated = false;
	
	private DeviceActivation()
	{
		
	}

	public static boolean isActivated() {
		return activated;
	}

	public static void setActivated(boolean activated) {
		DeviceActivation.activated = activated;
	}
}
