package com.planetbiru.buzzer;

import java.util.Arrays;
import java.util.List;

public class Stat {
	private static String numbers = "0123456789";
	private static List<String> list = Arrays.asList("C,C#,D,D#,E,F,F#,G,G#,A,A#,B".split(","));

	public static final boolean OUT = false;
	public static final boolean IN = true;
	public static final int MODE_INPUT_PULL_UP     = 1;
	public static final int MODE_INPUT_PULL_DOWN   = 2;
	public static final int MODE_OUTPUT_PUSH_PULL  = 4;
	public static final int MODE_OUTPUT_OPEN_DRAIN = 8;

	public static final String EXP = "/sys/class/gpio/";
	public static final int[] PINS = { 0, 1, 4, 7, 8, 9, 10, 11, 14, 15, 17, 18, 21, 22, 23, 24, 25, 26, 27 };
	private static boolean[] used;

	private Stat()
	{
		
	}
	
	public static boolean[] getUsed() {
		return used;
	}

	public static void setUsed(boolean[] used) {
		Stat.used = used;
	}

	public static String getNumbers() {
		return numbers;
	}

	public static void setNumbers(String numbers) {
		Stat.numbers = numbers;
	}

	public static List<String> getList() {
		return list;
	}

	public static void setList(List<String> list) {
		Stat.list = list;
	}
	

}
