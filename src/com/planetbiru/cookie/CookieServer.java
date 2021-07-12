package com.planetbiru.cookie;

import java.io.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.config.Config;
import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.Utility;
import com.sun.net.httpserver.Headers;

public class CookieServer {
	
	private String sessionName = "SMSSESSID";
	private String sessionID = "";
	private Map<String, CookieItem> cookieItem = new HashMap<>();
	private long sessionLifetime = 1440000;
	private JSONObject sessionData = new JSONObject();

	public CookieServer(Map<String, List<String>> headers) {
		this.sessionLifetime = Config.getSessionLifetime();
		this.sessionName = Config.getSessionName();
		this.parseCookie(headers);
		this.updateSessionID();
	}

	public CookieServer(Map<String, List<String>> headers, String sessionName, long sessionLifetime) {
		this.sessionLifetime = sessionLifetime;
		this.sessionName = sessionName;
		this.parseCookie(headers);
		this.updateSessionID();
	}
	
	public CookieServer(Headers headers, String sessionName, long sessionLifetime)
	{
		this.sessionLifetime = sessionLifetime;
		this.sessionName = sessionName;
		this.parseCookie(headers);
		this.updateSessionID();
	}
	
	public CookieServer(String rawCookie)
	{
		this.parseCookie(rawCookie);
		this.updateSessionID();
	}
	
	public CookieServer(String rawCookie, String sessionName)
	{
		this.sessionName = sessionName;
		this.parseCookie(rawCookie);
		this.updateSessionID();
	}
	
	private void updateSessionID() {
		if(!this.cookieItem.containsKey(this.sessionName))
		{
			this.generateSessionID();
			CookieItem cookie = new CookieItem(this.sessionName, this.sessionID);
			this.cookieItem.put(this.sessionName, cookie);
		}	
	}
	
	public void generateSessionID()
	{
		this.sessionID = Utility.sha1(System.currentTimeMillis()+"");
	}
	
	public void destroySession()
	{
		this.sessionData = new JSONObject();
		String sessionFile = this.getSessionFile();
		File file = new File(sessionFile);
		Path path = Paths.get(file.getPath());
		try 
		{
			Files.delete(path);
		} 
		catch (IOException e) 
		{
			/**
			 * Do nothing
			 */
		}
	}
	
	private void parseCookie(String rawCookie) {
		URLCodec urlCodec = new URLCodec();
		Map<String, CookieItem> list = new HashMap<>();
		String[] rawCookieParams = rawCookie.split("\\; ");
		
		for(int j = 0; j<rawCookieParams.length; j++)
		{
			String cookiePair = rawCookieParams[j];
			String[] arr = cookiePair.split("=");
	        String cookieName = arr[0];
	        String cookieValue = "";
	        try 
	        {
	        	cookieValue = urlCodec.decode(arr[1]);
	        }
	        catch (DecoderException e) 
	        {
	        	/**
				 * Do nothing
				 */
	        }
	        CookieItem cookie = new CookieItem(cookieName, cookieValue);
	        list.put(cookieName, cookie);
		}
		this.setCookie(list);
	}
	private void parseCookie(Map<String, List<String>> headers) {
		List<String> rawCookies = headers.get("cookie");
		this.parseCookie(rawCookies);
	}
	private void parseCookie(Headers headers)
	{
		List<String> rawCookies = headers.get("cookie");
		this.parseCookie(rawCookies);
	}
	private void parseCookie(List<String> rawCookies)
	{
		URLCodec urlCodec = new URLCodec();
		Map<String, CookieItem> list = new HashMap<>();
		if(rawCookies != null)
		{
			for(int i = 0; i<rawCookies.size(); i++)
			{
				String rawCookie = rawCookies.get(i);
				String[] rawCookieParams = rawCookie.split("\\; ");				
				for(int j = 0; j<rawCookieParams.length; j++)
				{
					String cookiePair = rawCookieParams[j];
					String[] arr = cookiePair.split("=");
			        String cookieName = arr[0];
			        String cookieValue = "";
			        try 
			        {
			        	cookieValue = urlCodec.decode(arr[1]);
			        }
			        catch (DecoderException e) 
			        {
			        	/**
						 * Do nothing
						 */
			        }
			        CookieItem cookie = new CookieItem(cookieName, cookieValue);
			        list.put(cookieName, cookie);
				}
			}
		}
		this.setCookie(list);
	}
	private void setCookie(Map<String, CookieItem> list)
	{
		this.cookieItem = list;
		if(this.cookieItem.containsKey(this.sessionName))
		{
			this.sessionID = this.cookieItem.get(this.sessionName).getValue();
		}
		this.setSessionData(this.readSessionData());		
	}

	public void setCookieItem(Map<String, CookieItem> cookieItem) 
	{
		this.cookieItem = cookieItem;	
	}
	public void setValue(String name, String value)
	{
		for(Map.Entry<String, CookieItem> entry : this.cookieItem.entrySet()) 
		{
			String key = entry.getKey();
			if(key.equals(name))
			{
				((CookieItem) entry.getValue()).setValue(value);
			}
		}
	}
	public void setDomain(String name, String domain)
	{
		for (Map.Entry<String, CookieItem> entry : this.cookieItem.entrySet()) {
			String key = entry.getKey();
			if(key.equals(name))
			{
				entry.getValue().setDomain(domain);
			}
		}
	}
	public void setPath(String name, String path)
	{
		for (Map.Entry<String, CookieItem> entry : this.cookieItem.entrySet()) {
			String key = entry.getKey();
			if(key.equals(name))
			{
				entry.getValue().setPath(path);
			}
		}
	}
	public void setExpires(String name, Date expires)
	{
		for (Map.Entry<String, CookieItem> entry : this.cookieItem.entrySet()) {
			String key = entry.getKey();
			if(key.equals(name))
			{
				entry.getValue().setExpires(expires);
			}
		}
	}

	public void setSecure(String name, boolean secure)
	{
		for (Map.Entry<String, CookieItem> entry : this.cookieItem.entrySet()) {
			String key = entry.getKey();
			if(key.equals(name))
			{
				entry.getValue().setSecure(secure);
			}
		}
	}

	public void setHttpOnly(String name, boolean httpOnly)
	{
		for (Map.Entry<String, CookieItem> entry : this.cookieItem.entrySet()) {
			String key = entry.getKey();
			if(key.equals(name))
			{
				entry.getValue().setHttpOnly(httpOnly);
			}
		}
	}
	
	public void putToHeaders(Headers responseHeaders) 
	{
		for (Map.Entry<String, CookieItem> entry : this.cookieItem.entrySet()) {
			String key = entry.getKey();
			if(key.equals(this.sessionName))
			{
				Date expirationDate = new Date(System.currentTimeMillis() + this.sessionLifetime);
				entry.getValue().setExpires(expirationDate);
			}
			responseHeaders.add("Set-Cookie", ((CookieItem) entry.getValue()).toString());
		}
	}
	
	public void clearFile(File directory)
    {
        File[] list = directory.listFiles();
        if(list != null)
        {
	        for(File file : list)
	        {
	            if(file.isDirectory())
	            {
	            	clearFile(file);
	            }
	            else 
	            {
	            	this.deleteFile(file);
	            }
	        }
        }
    }
	
	public void deleteFile(File file)
	{
		long lasModifued = file.lastModified();
    	if(lasModifued < (System.currentTimeMillis() - this.sessionLifetime))
    	{
    		Path path = Paths.get(file.getPath());
    		try 
    		{
				Files.delete(path);
			} 
    		catch (IOException e) 
    		{
    			/**
    			 * Do nothing
    			 */
			}
    	}
	}
	
	public void saveSessionData() {
		String fileName = FileConfigUtil.fixFileName(this.getSessionFile());
		try 
		{
			FileConfigUtil.write(fileName, this.getSessionData().toString().getBytes());
		} 
		catch (IOException e) 
		{
			/**
			 * Do nothing
			 */
		}
	}
	
	private JSONObject readSessionData() {
		JSONObject jsonData = new JSONObject();
		String fileName = FileConfigUtil.fixFileName(this.getSessionFile());		
		try 
		{
			byte[] data = FileConfigUtil.read(fileName);
			if(data != null)
			{
				String text = new String(data);
				jsonData = new JSONObject(text);
			}
		} 
		catch (JSONException | FileNotFoundException e) 
		{
			/**
			 * Do nothing
			 */
		}
		return jsonData;
	}
	
	private String getSessionFile() {
		return Utility.getBaseDir()+"/"+Config.getSessionFilePath()+"/"+this.sessionID;
	}
	
	public void setSessionValue(String sessionKey, Object sessionValue) {
		this.getSessionData().put(sessionKey, sessionValue);		
	}
	
	public Object getSessionValue(String sessionKey, Object defaultValue)
	{
		Object value = null;
		try 
		{
			value = this.getSessionData().get(sessionKey);
			if(value == null)
			{
				value = defaultValue;
			}
		}
		catch(JSONException e)
		{
			value = defaultValue;
		}
		return value;
	}
	
	public JSONObject getSessionData() {
		return sessionData;
	}
	
	public void setSessionData(JSONObject sessionData) {
		this.sessionData = sessionData;
	}
}
