package com.planetbiru.util;

public class HttpRequestException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1597954710422707123L;

	public HttpRequestException(Exception e) {
		super(e);
	}

	public HttpRequestException(String e) {
		super(e);
	}

}
