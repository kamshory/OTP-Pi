package com.planetbiru.web;

import java.io.IOException;
import com.planetbiru.config.PropertyLoader;
import com.planetbiru.util.Utility;
import com.sun.net.httpserver.Headers; //NOSONAR
import com.sun.net.httpserver.HttpExchange;

public class HttpUtil {
	private HttpUtil()
	{
		
	}
	public static byte[] getRequestBody(HttpExchange httpExchange)
	{
        Headers requestHeaders = httpExchange.getRequestHeaders();
		String cl = requestHeaders.getFirst("Content-length");
		byte[] requestBody = "".getBytes();
        if(cl != null)
        {
            try
            {
	            int contentLength = Utility.atoi(cl);	
	            requestBody = new byte[contentLength];
	            for(int j = 0; j < contentLength; j++)
	            {
	            	requestBody[j] = (byte) httpExchange.getRequestBody().read();
	            }
            }
            catch(NumberFormatException | IOException e)
            {
            	/**
            	 * Do nothing
            	 */
            }
            return requestBody;
        }
        else
        {
        	return "".getBytes();
        }
	}
	public static void broardcastWebSocket(String message) {
		HttpUtil.broardcastWebSocket(message, false, 0);
		
	}
	
	public static void broardcastWebSocket(String message, boolean async, long delay) 
	{
		if(async)
		{
			HttpBroadcaster bc = new HttpBroadcaster(message, delay);
			bc.start();
		}
		else
		{
			HttpBroadcaster bc = new HttpBroadcaster(message);
			bc.broadcast();
		}
	}

	public static String getMIMEType(String fileName) 
	{
		String[] arr = fileName.split("\\.");	
		String ext = arr[arr.length - 1];
		return PropertyLoader.getString("MIME", ext, "");
	}
	
	public static String getBaseName(String fileName) 
	{
		String[] arr = fileName.split("\\/");	
		if(arr.length > 1)
		{
			return arr[arr.length - 1];
		}
		else
		{
			return fileName;
		}
	}
	
	public static int getCacheLifetime(String fileName) {
		int lifetime = 0;
		if(fileName.contains("."))
		{
			String[] arr = fileName.split("\\.");
			String ext = arr[arr.length - 1];
			String lt = PropertyLoader.getString("CACHE", ext, "0");
			lifetime = Utility.atoi(lt);
		}
		return lifetime;
	}
	
}
