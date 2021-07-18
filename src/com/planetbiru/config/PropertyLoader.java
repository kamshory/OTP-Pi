package com.planetbiru.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.json.JSONException;

import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.Utility;

public class PropertyLoader {
	private static Pattern mSection = Pattern.compile("\\s*\\[([^]]*)\\]\\s*");
	private static Pattern mKeyValue = Pattern.compile("\\s*([^=]*)=(.*)");
	private static Map <String, Map<String, String>> mEntries = new HashMap<>();
	private static boolean loaded = false;

	private static Logger logger = Logger.getLogger(PropertyLoader.class);
	
	private PropertyLoader()
	{
		
	}
	public static void load(String path) {
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		
		try 
		{
			byte[] data = FileConfigUtil.read(fileName);
			if(data != null)
			{
				String text = new String(data);
				text = fixingRawData(text);
				String[] lines = text.split("\\r?\\n");
				List<String> list = Arrays.asList(lines);
				PropertyLoader.load(list);
				
			}
		} 
		catch (FileNotFoundException | JSONException e) 
		{
			/**
			 * Do nothing
			 */
			if(Config.isLogConfigNotFound())
			{
				logger.error(e.getMessage(), e);
			}
		}
		PropertyLoader.setLoaded(true);
	}
	
	public static String fixingRawData(String result)
	{
		result = result.replace("\n", "\r\n");
		result = result.replace("\r\r\n", "\r\n");
		result = result.replace("\r", "\r\n");
		result = result.replace("\r\n\n", "\r\n");
		return result;
	}

	public static void load(List<String> lines) {
        String section = null;
		for(int i = 0; i<lines.size(); i++)
		{
			String line = lines.get(i);
            Matcher m = mSection.matcher(line);
            if(m.matches()) 
            {
                section = m.group(1).trim();
            } 
            else if (section != null) 
            {
            	m = mKeyValue.matcher(line);
                if(m.matches()) 
                {
                    String key = m.group(1).trim();
                    String value = m.group(2).trim();
                    Map<String, String> kv = mEntries.get(section);
                    if (kv == null) 
                    {
                    	kv = new HashMap<>();
                        mEntries.put(section, kv);
                    }
                    kv.put(key, value);
                }
            }
		}
	}

	public static String getString(String section, String key, String defaultvalue) {
	    Map<String, String> kv = mEntries.get(section);
	    if (kv == null) {
	        return defaultvalue;
	    }
	    return kv.get(key);
	}

	public static int getInt(String section, String key, int defaultvalue) {
	    Map<String, String> kv = mEntries.get(section);
	    if (kv == null) {
	        return defaultvalue;
	    }
	    return Integer.parseInt(kv.get(key));
	}

	public static float getFloat(String section, String key, float defaultvalue) {
	    Map<String, String> kv = mEntries.get(section);
	    if (kv == null) {
	        return defaultvalue;
	    }
	    return Float.parseFloat(kv.get(key));
	}

	public static double getDouble(String section, String key, double defaultvalue) {
	    Map<String, String> kv = mEntries.get(section);
	    if (kv == null) {
	        return defaultvalue;
	    }
	    return Double.parseDouble(kv.get(key));
	}
	public static boolean isLoaded() {
		return loaded;
	}
	public static void setLoaded(boolean loaded) {
		PropertyLoader.loaded = loaded;
	}

	
}
