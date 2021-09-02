package com.planetbiru.buzzer;

import java.util.Arrays;
import java.util.List;

public class Stat {
	private static String numbers = "0123456789";
	private static List<String> list = Arrays.asList("C,C#,D,D#,E,F,F#,G,G#,A,A#,B".split(","));
	
	private Stat()
	{
		
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
