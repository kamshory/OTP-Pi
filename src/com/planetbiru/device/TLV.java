// ============================================================================
// Copyright 2021 PT ALTO NETWORK, All Rights Reserved
// This source code is protected by Indonesian and International copyright laws.
// Any reproduction, modification, disclosure and/or distribution of the source
// code in any form is strictly prohibited and may be unlawful without
// PT ALTO Network's written consent.
// All other copyright or ALTO trademark, including but not limited to this
// source code, is PT ALTO NETWORK's property.
// ============================================================================

package com.planetbiru.device;

import java.util.HashMap;
import java.util.Map;

public class TLV {
	private TLV()
	{		
	}
	public static Map<String, String> parse(String data)
	{
		String remaining = data;
		Map<String, String> values = new HashMap<>();
		String tag = "";
		String len = "";
		String val = "";
		int itemLength = 0;
		while(remaining.length() > 3)
		{
			tag = remaining.substring(0, 2);
			len = remaining.substring(2, 4);
			try
			{
				itemLength = Integer.parseInt(len);
			}
			catch(NumberFormatException e)
			{
				itemLength = 0;
			}
			if(remaining.length() >= (4+itemLength))
			{
				val = remaining.substring(4, 4+itemLength);
				remaining = remaining.substring(4+itemLength);
				values.put(tag, val);
			}
			else
			{
				break;
			}
		}
		return values;		
	}
	public static String build(Map<String, String> data) 
	{
		String tag = "";
		String val = "";
		StringBuilder builder = new StringBuilder();	
		for (Map.Entry<String,String> entry : data.entrySet())
		{
			tag = entry.getKey();
			val = entry.getValue();			
			builder.append(String.format("%-2s%02d%s", tag.length()>2?tag.substring(0, 2):tag, tag.length(), val));
		}
		return builder.toString();
	}
}
