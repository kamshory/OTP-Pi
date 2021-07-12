package com.planetbiru.util;

import java.util.ArrayList;
import java.util.List;

public class CommandLineResult {
	private boolean error = false;
	private List<String> lines = new ArrayList<>();
	private int exitValue = 0;
	private String errorMessage = "";
	public void addLine(String line) {
		lines.add(line);	
	}
	public int getExitValue() {
		return exitValue;
	}
	public void setExitValue(int exitValue) {
		this.exitValue = exitValue;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public boolean isError() {
		return error;
	}
	public void setError(boolean error) {
		this.error = error;
	}
	
	@Override
	public String toString()
	{
		return String.join("\r\n", this.lines);
	}

}
