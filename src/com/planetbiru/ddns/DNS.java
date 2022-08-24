package com.planetbiru.ddns;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

import com.planetbiru.util.CustomHttpClient;
import com.planetbiru.util.HttpRequestException;
import com.sun.net.httpserver.Headers; //NOSONAR

public class DNS {
	

	public String getIP() 
	{
		String ip = "";
		try 
		{
			ip = this.getIP("ipv4");
		} 
		catch (Exception e) 
		{
			/**
			 * 
			 */
		}
		return ip;
	}

	public String getIP(String protocol) throws DDNSException, HttpRequestException
	{
		List<String> allowedTypes = new ArrayList<>();
		allowedTypes.add("ipv4");
		allowedTypes.add("ipv6");
		allowedTypes.add("auto");
		if(!allowedTypes.contains(protocol))
		{
			throw new DDNSException("Invalid \"protocol\" config value. Allowed : " + allowedTypes.toString());
		}
		
		String prefix = protocol.equals("auto")?"":(protocol+".");
		String url = "http://"+prefix+"icanhazip.com/";
		
		Headers requestHeaders = new Headers();
		requestHeaders.add(DDNSKey.HEADER_USER_AGENT, "OTP Pi");
		int timeout = 10000;
		HttpResponse<String> response = CustomHttpClient.httpExchange("GET", url, null, requestHeaders, "", timeout);
		return response.body();		
	}

	public JSONObject update(DDNSRecord ddnsRecord) throws HttpRequestException  //NOSONAR
	{
		return ddnsRecord.toJSONObject();
	}
}
