package com.planetbiru.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import com.planetbiru.web.HttpMethod;
import com.sun.net.httpserver.Headers; //NOSONAR

import sun.net.www.protocol.http.HttpURLConnection;

public class CustomHttpClient {
	
	private CustomHttpClient()
	{
		
	}
	
	public static HttpResponseString httpExchange(String method, String url, Map<String, String> parameters, Headers requestHeaders, String body, int timeout) throws HttpRequestException
	{
		Map<String, List<String>> params = new HashMap<>();
		if(parameters != null)
		{
	        for (Map.Entry<String, String> entry : parameters.entrySet())
	        {
	        	String key = entry.getKey();
	        	String value = entry.getValue();
	        	List<String> list = new ArrayList<>();
	        	list.add(value);
				params.put(key, list);
	        }
		}
		try {
			ResponseEntityCustom response = CustomHttpClient.sendRequestHttps(method, url, params, requestHeaders, body, timeout);
			return new HttpResponseString(response.getBody(), response.getStatusCode(), response.getResponseHeaders());
			
		} catch (IOException e) {
			throw new HttpRequestException(e.getMessage());
		}
	}
	
	public static void setParameters(HttpURLConnection con, Map<String, List<String>> parameters)
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
	public static void setParameters(HttpsURLConnection con, Map<String, List<String>> parameters)
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
	
	public static ResponseEntityCustom sendRequestHttps(String method, String url, Map<String, List<String>> parameters, Headers requestHeaders, String body, int timeout) throws IOException  //NOSONAR
, HttpRequestException
	{
		
		ResponseEntityCustom result = new ResponseEntityCustom();
        byte[] postData = null;
        StringBuilder content = new StringBuilder();
             
		int statusCode = 200;
		Map<String, List<String>> responseHeader = new HashMap<>();
		URLConnection con = null;
		try 
		{
			
        	URL myurl = new URL(url);
			HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() //NOSONAR
			{
				public boolean verify(String hostname, SSLSession session) 
				{
					return true; //NOSONAR
				}
			});
			
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, new X509TrustManager[]{new X509TrustManager(){
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {} //NOSONAR
			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {} //NOSONAR
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}}}, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(
			context.getSocketFactory());
			
 			con = myurl.openConnection();
			if(con.getClass().toString().contains("HttpsURLConnection")) 
			{
	            con.setDoOutput(true);
	            ((java.net.HttpURLConnection) con).setRequestMethod(method);
	            con.setConnectTimeout(timeout);
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
	            if(method.equalsIgnoreCase(HttpMethod.POST) || method.equalsIgnoreCase(HttpMethod.PUT))
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
	            
	            statusCode = ((java.net.HttpURLConnection) con).getResponseCode();
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
	            
	            ((java.net.HttpURLConnection) con).disconnect();
	  
			} 
			else if(con.getClass().toString().contains("HttpURLConnection")) 
			{
	            con.setDoOutput(true);
	            ((java.net.HttpURLConnection) con).setRequestMethod(method);
	            con.setConnectTimeout(timeout);
	            
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
	            
	            if(method.equalsIgnoreCase(HttpMethod.POST) || method.equalsIgnoreCase(HttpMethod.PUT))
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
	            statusCode = ((java.net.HttpURLConnection) con).getResponseCode();
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
        	
        } 
		catch (NoSuchAlgorithmException | KeyManagementException e) //NOSONAR
		{
			throw new HttpRequestException(e.getMessage());
		}
		catch(IOException e) //NOSONAR
		{
			throw new HttpRequestException(e.getMessage());
		}
		catch(Exception e) //NOSONAR
		{
			throw new HttpRequestException(e.getMessage());
		}
		result = new ResponseEntityCustom(content.toString(), statusCode, responseHeader);
		return result;
	}

	public static String buildQuery(Map<String, List<String>> params) 
	{
		return Utility.buildQuery(params);
	}

}
