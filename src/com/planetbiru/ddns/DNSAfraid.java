package com.planetbiru.ddns;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.planetbiru.config.Config;
import com.planetbiru.util.ResponseEntityCustom;
import com.planetbiru.util.Utility;
import com.planetbiru.web.HttpMethod;
import com.sun.net.httpserver.Headers;

public class DNSAfraid extends DNS{
	
	private String email = "";
	private String username = "";
	private String endpoint = "https://freedns.afraid.org/nic/update";
	private String password = "";
	private String company = "";
	private boolean active = false;
	
	@Override
	public JSONObject update(DDNSRecord ddnsRecord) throws IOException  
	{
		JSONObject res = new JSONObject();
		String method = HttpMethod.GET;
		Map<String, String> params = new HashMap<>();
		
		String ip = this.getIP();
		
		params.put("hostname", ddnsRecord.getRecordName());
		params.put("myip", ip);
		this.request(method, endpoint, params);
		return res;
	}
	
	/**
	* Issues an HTTPS request and returns the result
	*
	* @param string String method
	* @param string String endpoint
	* @param array  String params
	*
	* @throws Exception
	*
	* @return mixed
	 * @throws IOException 
	*/
	public ResponseEntityCustom request(String method, String endpoint, Map<String, String> params) throws IOException
	{
		int timeout = Config.getDdnsTimeout();
		Headers headers = this.createRequestHeader();
		String body = null;
		return this.httpExchange(method, endpoint, params, headers, body, timeout);
	}

	public Headers createRequestHeader() {
		Headers requestHeaders = new Headers();
		requestHeaders.add(DDNSKey.HEADER_AUTHORIZATION, Utility.basicAuth(this.username, this.password));
		requestHeaders.add(DDNSKey.HEADER_USER_AGENT, this.createUserAgent());
		requestHeaders.add(DDNSKey.HEADER_CONNECTION, "close");
		return requestHeaders;
	}
	
	private String createUserAgent()
	{
		return String.format("%s %s %s", this.company, Config.getNoIPDevice(), this.email);
	}

	public void setConfig(String endpoint, String username, String password, String email, String company, boolean active) {
		this.endpoint = endpoint;
		this.username = username;
		this.password = password;
		this.email = email;		
		this.company = company;
		this.active = active;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}


}
