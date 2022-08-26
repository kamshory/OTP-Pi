package com.planetbiru.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
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
		return CustomHttpClient.sendRequest(method, url, parameters2, requestHeaders, body, timeout);
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
	
	public static HttpResponseString sendRequest(String method, String url, Map<String, List<String>> parameters, Headers requestHeaders, String body, int timeout) throws HttpRequestException 
	{      
       return CustomHttpClient.sendRequestHttp(method, url, parameters, requestHeaders, body, timeout);
	}
	
	private static HttpResponseString sendRequestHttp(String method, String url, Map<String, List<String>> parameters, Headers requestHeaders, String body, int timeout) throws HttpRequestException {
		
		Builder builder = HttpRequest.newBuilder();
		builder.uri(URI.create(url));
		builder.timeout(Duration.ofMillis(timeout));

		
		if(method.equalsIgnoreCase(HttpMethod.POST) ||
				method.equalsIgnoreCase(HttpMethod.PUT) ||
				method.equalsIgnoreCase(HttpMethod.PATCH)
				)
        {
           	if(body == null && parameters != null)
        	{
           		body = Utility.buildQuery(parameters);
        	}   	 
           	if(body != null)
           	{
        		builder.method(method, BodyPublishers.ofByteArray(body.getBytes()));         		
           	}
        }
		else if(method.equalsIgnoreCase(HttpMethod.DELETE))
		{
			builder.DELETE();
		}
		else if(method.equalsIgnoreCase(HttpMethod.GET))
		{
			builder.GET();
		}
		
		for(Map.Entry<String, List<String>> entry : requestHeaders.entrySet()) {
			String key = entry.getKey();
			for (String value : entry.getValue()) 
			{
				builder.header(key, value);
			}
		}			
		
		HttpRequest request = builder.build();			
		
		HttpClient client = HttpClient.newBuilder()
		        .version(Version.HTTP_1_1)
		        .followRedirects(Redirect.NORMAL)
		        .connectTimeout(Duration.ofMillis(timeout))
		        .build();
		
		try {
			HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
			return new HttpResponseString(response.body(), response.statusCode(), response.headers());
		} 
		catch (InterruptedException e) //NOSONAR
		{
			throw new HttpRequestException(e.getMessage());
		}
		catch(ConnectException e) //NOSONAR
		{
			throw new HttpRequestException(e.getMessage());
		}
		catch(IOException e) //NOSONAR
		{
			throw new HttpRequestException(e.getMessage());
		}
	}
	
	

	public static ResponseEntityCustom sendRequestHttps(String method, String url, Map<String, List<String>> parameters, Headers requestHeaders, String body, int timeout) throws IOException  //NOSONAR
	{
        HttpsURLConnection con = null;
		ResponseEntityCustom result = new ResponseEntityCustom();
        byte[] postData = null;
        StringBuilder content = new StringBuilder();
             
		int statusCode = 200;
		Map<String, List<String>> responseHeader = new HashMap<>();
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
        	
        	con = (HttpsURLConnection) myurl.openConnection();
     	 
            con.setDoOutput(true);
            con.setRequestMethod(method);
            con.setConnectTimeout(timeout);


           

        	if(parameters != null)
            {
            	setParameters(con, parameters);	            
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

        } catch (NoSuchAlgorithmException | KeyManagementException e) {
			e.printStackTrace();
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

	public static ResponseEntityCustom sendRequestHttpX(String method, String endpoint, Map<String, List<String>> parameters, Headers requestHeaders, String body, int timeout) throws IOException //NOSONAR
	{
		ResponseEntityCustom result = new ResponseEntityCustom();
        byte[] postData = null;
        StringBuilder content = new StringBuilder();
             
		int statusCode = 200;
		Map<String, List<String>> responseHeader = new HashMap<>();
		HttpURLConnection con = null;
 		try 
		{
        	  	
 	    	URL url = new URL(endpoint);
 	    	con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setRequestMethod(method);
            con.setConnectTimeout(timeout);
            if(parameters != null)
            {
            	setParameters(con, parameters);	            
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

	public static String buildQuery(Map<String, List<String>> params) 
	{
		return Utility.buildQuery(params);
	}

}
