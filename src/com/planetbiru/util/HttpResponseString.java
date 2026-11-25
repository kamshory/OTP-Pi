package com.planetbiru.util;

import com.sun.net.httpserver.Headers;

public class HttpResponseString {

	private String responseBody = "";
	private int status = 0;
	private Headers responseHeaders = null;

	public HttpResponseString(String responseBody, int status, Headers headers) {
		this.responseBody = responseBody;
		this.status = status;
		this.responseHeaders = headers;
	}

	public String body() {
		return responseBody;
	}

	public int statusCode() {
		return status;
	}

	public Headers headers() {
		return responseHeaders;
	}
	
	@Override
	public String toString()
	{
		return this.body();
	}

}
