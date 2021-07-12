package com.planetbiru.ddns;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import com.planetbiru.util.ResponseEntityCustom;
import com.planetbiru.util.Utility;
import com.sun.net.httpserver.Headers;

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

	public String getIP(String protocol) throws DDNSException
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
		requestHeaders.add(DDNSKey.HEADER_USER_AGENT, "OTP Broker");
		int timeout = 10000;
		ResponseEntityCustom response = this.httpExchange("GET", url, requestHeaders, "", timeout);
		return response.getBody();		
	}

	/**
	* Request to HTTP server
	* @param method Request method
	* @param url URL
	* @param requestHeaders Request headers
	* @param body Request body
	* @return ResponseEntityCustom Custom response entity
	*/
	public ResponseEntityCustom httpExchange(String method, String url, Headers requestHeaders, String body, int timeout)
	{
		
		ResponseEntityCustom result = null;
		return result;
	}

	/**
	* Create customer rest template
	* @param timeout Request timeout
	* @return RestTemplate
	*/
	public RestTemplate customRestTemplate(int timeout)
	{
		
		RestTemplate restTemplate = null;
		return restTemplate;
	}

	public String buildQuery(Map<String, List<String>> params) 
	{
		return Utility.buildQuery(params);
	}


	public JSONObject update(DDNSRecord ddnsRecord) {
		return ddnsRecord.toJSONObject();
	}

	

}
