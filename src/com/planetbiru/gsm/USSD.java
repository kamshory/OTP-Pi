package com.planetbiru.gsm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Splitter;

public class USSD {

	private boolean replyable = false;
	private String contentRaw = "";
	private String content = "";
	private String header = "";
	private String footer = "";

	public USSD() {
	}

	public USSD(String contentRaw) {
		this.contentRaw = contentRaw;
		Map<String, String> parsed = this.parseUSSDResponse(contentRaw);
		this.replyable = parsed.getOrDefault("footer", "0").equals("0");
		String body = parsed.getOrDefault("body", "");
		this.header = parsed.getOrDefault("header", "");
		this.footer = parsed.getOrDefault("footer", "");
		if(this.isUCS2(body))
		{
	        Iterable<String> arr = Splitter.fixedLength(4).split(body);
	        StringBuilder builder = new StringBuilder();
	        for (String str : arr)
	        {
	            int hexVal = Integer.parseInt(str, 16);
	            builder.append((char) hexVal);
	        }
	        this.content = builder.toString();
		}
 		else
 		{
 			this.content = body; 			
 		}
	}
	
	public Map<String, String> parseUSSDResponse(String resp)
    {
    	String[] arr = resp.split("\\,");
    	String header = arr[0];
    	List<String> list = new ArrayList<>();
    	for(int i = 1; i<arr.length-1; i++)
    	{
    		list.add(arr[i]);
    	}
    	String body = String.join(",", list);
    	if(body.startsWith("\"") && body.endsWith("\""))
    	{
    		body = body.substring(1, body.length()-1);
    	}
    	String footer = arr[arr.length-1];
    	Map<String, String> result = new HashMap<>();
    	result.put("header", header);
    	result.put("body", body);
    	result.put("footer", footer);
    	return result;
    }

	public boolean isUCS2(String content)
	{
		String[] arr = content.split("(?<=\\G....)");
		if(content.length() % 4 != 0)
		{
			return false;
		}
		for(int k = 0; k < arr.length; k++)
		{
			boolean isHex = arr[k].matches("^[0-9A-F]+$");
			if(!isHex)
			{
				return false;
			}
		}
		return true;
	}

	public boolean isReplyable() {
		return replyable;
	}

	public void setReplyable(boolean replyable) {
		this.replyable = replyable;
	}

	public String getContentRaw() {
		return contentRaw;
	}

	public void setContentRaw(String contentRaw) {
		this.contentRaw = contentRaw;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getFooter() {
		return footer;
	}

	public void setFooter(String footer) {
		this.footer = footer;
	}

}
