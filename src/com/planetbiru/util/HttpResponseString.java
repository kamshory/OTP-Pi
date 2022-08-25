package com.planetbiru.util;

import java.net.http.HttpHeaders;

import org.json.JSONTokener;

public class HttpResponseString {

	private String body = "";
	private int statusCode = 0;
	private HttpHeaders headers = null;

	public HttpResponseString(String body, int statusCode, HttpHeaders headers) {
		this.setBody(body);
		this.statusCode = statusCode;
		this.headers = headers;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}


}
