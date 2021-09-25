package com.planetbiru.ddns;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;

import com.planetbiru.util.ResponseEntityCustom;
import com.planetbiru.util.Utility;
import com.sun.net.httpserver.Headers;

import sun.net.www.protocol.http.HttpURLConnection;

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

	public String getIP(String protocol) throws DDNSException, IOException
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
		ResponseEntityCustom response = this.httpExchange("GET", url, null, requestHeaders, "", timeout);
		return response.getBody();		
	}
	
	public ResponseEntityCustom httpExchange(String method, String url, Map<String, String> parameters, Headers requestHeaders, String body, int timeout) throws IOException
	{
		Map<String, List<String>> parameters2 = new HashMap<>();
		if(parameters != null)
		{
	        for (Map.Entry<String, String> entry : parameters.entrySet())
	        {
	        	String key = entry.getKey();
	        	String value = entry.getValue();
	        	List<String> list = new ArrayList<>();
	        	list.add(value);
				parameters2.put(key, list);
	        }
		}
		return this.sendRequest(method, url, parameters2, requestHeaders, body, timeout);
	}
	public void setParameters(HttpURLConnection con, Map<String, List<String>> parameters)
	{
		if(parameters != null)
		{
			for (Map.Entry<String, List<String>> entry : parameters.entrySet())
	        {
	        	String key = entry.getKey();
	        	List<String> list = entry.getValue();
	        	for(int i = 0; i<list.size(); i++)
	        	{
	        		con.setRequestProperty(key, list.get(i));
	        	}
	        }
        }
	}
	public void setParameters(HttpsURLConnection con, Map<String, List<String>> parameters)
	{
		for (Map.Entry<String, List<String>> entry : parameters.entrySet())
        {
        	String key = entry.getKey();
        	List<String> list = entry.getValue();
        	for(int i = 0; i<list.size(); i++)
        	{
        		con.setRequestProperty(key, list.get(i));
        	}
        }
	}
	public ResponseEntityCustom sendRequest(String method, String url, Map<String, List<String>> parameters, Headers requestHeaders, String body, int timeout) throws IOException
	{      
        if(url.contains("https://"))
        {
        	return this.sendRequestHttps(method, url, parameters, requestHeaders, body, timeout);
        }
        else
        {
           	return this.sendRequestHttp(method, url, parameters, requestHeaders, body, timeout);                   	
        }
	}

	private ResponseEntityCustom sendRequestHttps(String method, String url, Map<String, List<String>> parameters, Headers requestHeaders, String body, int timeout) throws IOException {
        HttpsURLConnection con = null;
		ResponseEntityCustom result = new ResponseEntityCustom();
        byte[] postData = null;
        StringBuilder content = new StringBuilder();
             
		int statusCode = 200;
		Map<String, List<String>> responseHeader = new HashMap<>();
		try 
		{
        	URL myurl = new URL(url);
        	
        	con = (HttpsURLConnection) myurl.openConnection();
        	 
        	

            con.setDoOutput(true);
            con.setRequestMethod(method);
            con.setConnectTimeout(timeout);
            if(parameters != null)
            {
            	setParameters(con, parameters);	            
            }
            
            if(method.equalsIgnoreCase("POST"))
            {
               	if(body == null && parameters != null)
            	{
               		body = Utility.buildQuery(parameters);
            	}
         	 
               	if(body != null)
               	{
             		postData = body.getBytes(StandardCharsets.UTF_8);
	     	        try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {		
		                wr.write(postData);
		            }
               	}
            }
            
            statusCode = con.getResponseCode();
            responseHeader = con.getHeaderFields();
 
            try (
            		BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()))) 
            {

                String line;
                int count = 0;
                while ((line = br.readLine()) != null) {
                	if(count > 0)
                    {
                		content.append(System.lineSeparator());
                    }
                    content.append(line);
                    count++;
                }
            }

        } 
		finally 
		{
        	if(con != null)
            {
        		con.disconnect();
            }
        }
		result = new ResponseEntityCustom(content.toString(), statusCode, responseHeader);
		return result;
	}

	private ResponseEntityCustom sendRequestHttp(String method, String url, Map<String, List<String>> parameters, Headers requestHeaders, String body, int timeout) throws IOException {
        HttpURLConnection con = null;
		ResponseEntityCustom result = new ResponseEntityCustom();
        byte[] postData = null;
        StringBuilder content = new StringBuilder();
             
		int statusCode = 200;
		Map<String, List<String>> responseHeader = new HashMap<>();
		try 
		{
        	URL myurl = new URL(url);
        	
        	con = (HttpURLConnection) myurl.openConnection();
        	 
        	

            con.setDoOutput(true);
            con.setRequestMethod(method);
            con.setConnectTimeout(timeout);
            if(parameters != null)
            {
            	setParameters(con, parameters);	            
            }
            
            if(method.equalsIgnoreCase("POST"))
            {
               	if(body == null && parameters != null)
            	{
               		body = Utility.buildQuery(parameters);
            	}
         	 
               	if(body != null)
               	{
             		postData = body.getBytes(StandardCharsets.UTF_8);
	     	        try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {		
		                wr.write(postData);
		            }
               	}
            }
            
            statusCode = con.getResponseCode();
            responseHeader = con.getHeaderFields();
 
            try (
            		BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()))) 
            {

                String line;
                int count = 0;
                while ((line = br.readLine()) != null) {
                	if(count > 0)
                    {
                		content.append(System.lineSeparator());
                    }
                    content.append(line);
                    count++;
                }
            }

        } 
		finally 
		{
        	if(con != null)
            {
        		con.disconnect();
            }
        }
		result = new ResponseEntityCustom(content.toString(), statusCode, responseHeader);
		return result;
	}

	public String buildQuery(Map<String, List<String>> params) 
	{
		return Utility.buildQuery(params);
	}


	public JSONObject update(DDNSRecord ddnsRecord) throws IOException {
		return ddnsRecord.toJSONObject();
	}
}
