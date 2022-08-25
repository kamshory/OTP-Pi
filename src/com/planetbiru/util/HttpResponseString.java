package com.planetbiru.util;

import java.net.http.HttpHeaders;

public class HttpResponseString {

	private String responseBody = "";
	private int status = 0;
	private HttpHeaders responseHeaders = null;

	public HttpResponseString(String responseBody, int status, HttpHeaders responseHeaders) {
		this.responseBody = responseBody;
		this.status = status;
		this.responseHeaders = responseHeaders;
	}

	public String body() {
		return responseBody;
	}

	public int statusCode() {
		return status;
	}

	public HttpHeaders headers() {
		return responseHeaders;
	}

}
