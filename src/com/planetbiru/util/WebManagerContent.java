package com.planetbiru.util;

import com.planetbiru.cookie.CookieServer;
import com.sun.net.httpserver.Headers;

public class WebManagerContent {

	private String fileName;
	private Headers responseHeaders;
	private byte[] responseBody;
	private int statusCode;
	private CookieServer cookie;
	private String contentType;

	public WebManagerContent(String fileName, Headers responseHeaders, byte[] responseBody, int statusCode, CookieServer cookie, String contentType) {
		this.fileName = fileName;
		this.responseHeaders = responseHeaders;
		this.responseBody = responseBody;
		this.statusCode = statusCode;
		this.cookie = cookie;
		this.contentType = contentType;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Headers getResponseHeaders() {
		return responseHeaders;
	}

	public void setResponseHeaders(Headers responseHeaders) {
		this.responseHeaders = responseHeaders;
	}

	public byte[] getResponseBody() {
		return responseBody;
	}

	public void setResponseBody(byte[] responseBody) {
		this.responseBody = responseBody;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public CookieServer getCookie() {
		return cookie;
	}

	public void setCookie(CookieServer cookie) {
		this.cookie = cookie;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	
}
