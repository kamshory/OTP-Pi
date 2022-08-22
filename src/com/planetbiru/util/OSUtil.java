package com.planetbiru.util;

public class OSUtil {     
    public enum OS {
        WINDOWS, LINUX, MAC, SOLARIS
    }

	private static OS os = null;
	
	public static OS getOS() {
	    if (os == null) {
	        String operSys = System.getProperty("os.name").toLowerCase();
	        if (operSys.contains("win")) {
	            os = OS.WINDOWS;
	        } else if (operSys.contains("nix") || operSys.contains("nux")
	                || operSys.contains("aix")) {
	            os = OS.LINUX;
	        } else if (operSys.contains("mac")) {
	            os = OS.MAC;
	        } else if (operSys.contains("sunos")) {
	            os = OS.SOLARIS;
	        }
	    }
	    return os;
	}

	public static boolean isWindows() {
		OS os = OSUtil.getOS();
		return (os != null && os.equals(OS.WINDOWS));
	}
	
	public static boolean isLinux() {
		OS os = OSUtil.getOS();
		return (os != null && os.equals(OS.LINUX));
	}
	
	public static boolean isMac() {
		OS os = OSUtil.getOS();
		return (os != null && os.equals(OS.MAC));
	}
	
	public static boolean isSunOS() {
		OS os = OSUtil.getOS();
		return (os != null && os.equals(OS.SOLARIS));
	}
}