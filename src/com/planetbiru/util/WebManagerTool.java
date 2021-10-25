package com.planetbiru.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import com.planetbiru.config.Config;
import com.planetbiru.constant.ConstantString;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.util.OSUtil.OS;

public class WebManagerTool {
	
	private WebManagerTool()
	{
		
	}

	public static JSONObject processAuthFile(byte[] responseBody) 
	{
		String responseString = new String(responseBody);
		int start = 0;
		int end = 0;
		do 
		{
			start = responseString.toLowerCase().indexOf("<meta ", end);
			if(start > -1)
			{
				end = responseString.toLowerCase().indexOf(">", start);				
				if(start >-1 && end >-1 && end < responseString.length())
				{
					String meta = responseString.substring(start, end+1);
					meta = WebManagerTool.fixMeta(meta);
					try
					{
						JSONObject metaObj = XML.toJSONObject(meta);
						JSONObject metaObjFixed = WebManagerTool.lowerCaseJSONKey(metaObj);
						if(requireLogin(metaObjFixed))
						{
							return metaObjFixed.optJSONObject(JsonKey.META);
						}
					}
					catch(JSONException e)
					{
						/**
						 * Do nothing
						 */
					}
				}
			}
		}
		while(start > -1);
		return new JSONObject();
	}
	
	public static byte[] removeMeta(byte[] responseBody) 
	{
		String responseString = new String(responseBody);
		int start = 0;
		int end = 0;
		do 
		{
			start = responseString.toLowerCase().indexOf("<meta ", end);
			if(start > -1)
			{				
				end = responseString.toLowerCase().indexOf(">", start);
				if(start >-1 && end >-1 && end < responseString.length())
				{
					String metaOri = responseString.substring(start, end+1);
					String meta = WebManagerTool.fixMeta(metaOri);
					try
					{
						JSONObject metaObj = XML.toJSONObject(meta);
						JSONObject metaObjFixed = WebManagerTool.lowerCaseJSONKey(metaObj); 
						if(requireLogin(metaObjFixed))
						{
							String content = new String(responseBody);
							return content.replace(metaOri, "").getBytes();
						}
					}
					catch(JSONException e)
					{
						/**
						 * Do nothing
						 */
					}
				}
			}
		}
		while(start > -1);
		return responseBody;
	}

	public static boolean requireLogin(JSONObject metaObj) {
		if(metaObj != null && metaObj.has(JsonKey.META))
		{
			JSONObject metaData = metaObj.optJSONObject(JsonKey.META);
			if(metaData != null)
			{
				String name = metaData.optString(JsonKey.NAME, "");
				boolean content = metaData.optBoolean(JsonKey.CONTENT, false);
				if(name.equals(JsonKey.REQUIRE_LOGIN) && content)
				{
					return true;
				}
			}
		}
		return false;
	}

	public static String fixMeta(String input)
	{
		if(input.indexOf("</meta>") == -1 && input.indexOf("/>") == -1)
		{
			input = input.replace(">", "/>");
		}
		return input;
	}
	
	public static JSONObject lowerCaseJSONKey(Object object) 
	{
		JSONObject newMetaObj = new JSONObject();
		JSONArray keys = ((JSONObject) object).names();
		if(keys == null)
		{
			return newMetaObj;
		}
		for (int i = 0; i < keys.length (); ++i) 
		{
		   String key = keys.getString(i); 
		   if(((JSONObject) object).get(key) instanceof JSONObject)
		   {
			   newMetaObj.put(key.toLowerCase(), WebManagerTool.lowerCaseJSONKey(((JSONObject) object).get(key)));
		   }
		   else
		   {
			   newMetaObj.put(key.toLowerCase(), ((JSONObject) object).get(key));
		   }
		}
		return newMetaObj;
	}
	
	public static String getFileName(String path) 
	{
		String dir = Config.getDocumentRoot();
		if(!path.startsWith(ConstantString.DOCUMENT_PATH_SEPARATOR))
		{
			path = ConstantString.DOCUMENT_PATH_SEPARATOR+path;
		}
		if(dir.endsWith(ConstantString.DOCUMENT_PATH_SEPARATOR))
		{
			dir = dir.substring(0, dir.length() - 1);
		}	
		return WebManagerTool.fixFileName(dir+path);
	}
	public static String fixFileName(String fileName) {
		if(OSUtil.getOS().equals(OS.WINDOWS))
		{
			fileName = fileName.replace("/", "\\");
			fileName = fileName.replace("\\\\", "\\");
		}
		else
		{
			fileName = fileName.replace("\\", "/");		
			fileName = fileName.replace("//", "/");
		}
		return fileName;
	}
	
	public static String getFileExtension(String fileName) 
	{
		String extension = fileName;
		int index = fileName.lastIndexOf('.');
		if (index > 0) {
		      extension = fileName.substring(index + 1);
		}
		return extension;
	}
}
