package com.planetbiru.web;

import com.sun.net.httpserver.Headers; //NOSONAR

public class WebResponse {

	private int statusCode = 200;
	private byte[] responseBody = "".getBytes();
	private Headers responseHeaders = new Headers();
	
	public WebResponse(int statusCode, Headers responseHeaders, byte[] responseBody)
	{
		this.statusCode = statusCode;
		this.responseHeaders = responseHeaders;
		this.responseBody = responseBody;
	}

	public WebResponse() {
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public byte[] getResponseBody() {
		return responseBody;
	}

	public void setResponseBody(byte[] responseBody) {
		this.responseBody = responseBody;
	}

	public Headers getResponseHeaders() {
		return responseHeaders;
	}

	public void setResponseHeaders(Headers responseHeaders) {
		this.responseHeaders = responseHeaders;
	}

}
