package com.planetbiru.device;

public class ConfigActivation {

	private static String username = "";
	private static String password = "";
	private static String method = "";
	private static String url = "";
	private static String contentType = "";
	private static String authorization = "";
	private static int requestTimeout = 30000;
	
	private ConfigActivation()
	{
		
	}

	public static String getUsername() {
		return username;
	}

	public static void setUsername(String username) {
		ConfigActivation.username = username;
	}

	public static String getPassword() {
		return password;
	}

	public static void setPassword(String password) {
		ConfigActivation.password = password;
	}

	public static String getMethod() {
		return method;
	}

	public static void setMethod(String method) {
		ConfigActivation.method = method;
	}

	public static String getUrl() {
		return url;
	}

	public static void setUrl(String url) {
		ConfigActivation.url = url;
	}

	public static String getContentType() {
		return contentType;
	}

	public static void setContentType(String contentType) {
		ConfigActivation.contentType = contentType;
	}

	public static String getAuthorization() {
		return authorization;
	}

	public static void setAuthorization(String authorization) {
		ConfigActivation.authorization = authorization;
	}

	public static int getRequestTimeout() {
		return requestTimeout;
	}

	public static void setRequestTimeout(int requestTimeout) {
		ConfigActivation.requestTimeout = requestTimeout;
	}

}
