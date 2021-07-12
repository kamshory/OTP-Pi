package com.planetbiru.web;

import com.sun.net.httpserver.Headers;

public class WebResponse {

	public int statusCode = 200;
	public byte[] responseBody = "".getBytes();
	public Headers responseHeaders = new Headers();
	
	public WebResponse(int statusCode, Headers responseHeaders, byte[] responseBody)
	{
		this.statusCode = statusCode;
		this.responseHeaders = responseHeaders;
		this.responseBody = responseBody;
	}

	public WebResponse() {
		// TODO Auto-generated constructor stub
	}

}
