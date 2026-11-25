package com.planetbiru.cookie;

import java.util.Date;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;

import com.planetbiru.util.Utility;

public class CookieItem {

	private String name = "";
	private String value = "";
	private String domain = null;
	private String path = "/";
	private Date expires = null;
	private boolean secure = false;
	private boolean httpOnly = false;

	public CookieItem(String cookieName, String cookieValue) {
		this.name = cookieName;
		this.value = cookieValue;
	}
	public CookieItem(String cookieName, String cookieValue, String path) {
		this.name = cookieName;
		this.value = cookieValue;
		this.path = path;
	}
	public CookieItem(String cookieName, String cookieValue, String path, Date expires) {
		this.name = cookieName;
		this.value = cookieValue;
		this.path = path;
		this.expires = expires;
	}
	public CookieItem(String cookieName, String cookieValue, String path, Date expires, boolean secure) {
		this.name = cookieName;
		this.value = cookieValue;
		this.path = path;
		this.expires = expires;
		this.secure = secure;
	}
	public CookieItem(String cookieName, String cookieValue, String path, Date expires, boolean secure, boolean httpOnly) {
		this.name = cookieName;
		this.value = cookieValue;
		this.path = path;
		this.expires = expires;
		this.secure = secure;
		this.httpOnly = httpOnly;
	}
	
	public CookieItem() {
		/**
		 * Do nothing
		 */
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public Date getExpires() {
		return expires;
	}
	public void setExpires(Date expires) {
		this.expires = expires;
	}
	public boolean isSecure() {
		return secure;
	}
	public void setSecure(boolean secure) {
		this.secure = secure;
	}
	public boolean isHttpOnly() {
		return httpOnly;
	}
	public void setHttpOnly(boolean httpOnly) {
		this.httpOnly = httpOnly;
	}
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		URLCodec urlCodec = new URLCodec();
		String encodedValue = "";
		try 
		{
			encodedValue = urlCodec.encode(this.value);
		} 
		catch (EncoderException e) 
		{
			/**
			 * Do nothing
			 */
		}
		builder.append(this.name+"="+encodedValue);
		if(this.domain != null && !domain.isEmpty())
		{
			builder.append("; Domain="+this.domain);
		}
		if(this.path != null && !path.equals("/"))
		{
			builder.append("; Path="+this.path);
		}
		if(this.expires != null)
		{
			String format = "EEE, dd MMM yyyy HH:mm:ss";
			builder.append("; Expires="+Utility.date(format, this.expires, "UTC")+" GMT");
		}
		/**
		 * builder.append("; SameSite=None");
		 */
		if(this.secure)
		{
			builder.append("; Secure");
		}
		if(this.httpOnly)
		{
			builder.append("; HttpOnly");
		}	
		return builder.toString();
	}
}
