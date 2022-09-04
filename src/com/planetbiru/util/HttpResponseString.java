package com.planetbiru.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.methods.HttpHead;

import io.netty.handler.codec.http.HttpHeaders;

public class HttpResponseString {

	private String responseBody = "";
	private int status = 0;
	private Map<String, List<String>> responseHeaders = new HashMap<>();

	public HttpResponseString() {
	}

	public HttpResponseString(String string, int statusCode, Map<String, List<String>> responseHeader) {
		this.responseBody = responseBody;
		this.status = status;
		this.responseHeaders = responseHeader;
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
	
	@Override
	public String toString()
	{
		return this.body();
	}

}
