package com.planetbiru.gsm;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigModem;
import com.planetbiru.config.DataModem;
import com.planetbiru.util.CommandLineExecutor;
import com.planetbiru.util.CommandLineResult;
import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.ServerInfo;

public class InternetDialUtil {
	
	
	private static Map<String, Boolean> internetAccess = new HashMap<>();
	private static String configPath = "";
	private static String wvdialCommandConnect = "";
	private static String wvdialCommandDisconnect = "";
	
	private static Logger logger = Logger.getLogger(InternetDialUtil.class);
	
	private InternetDialUtil()
	{
		
	}

	public static void start() {
		InternetDialUtil.configPath = Config.getWvdialSettingPath();
		InternetDialUtil.wvdialCommandConnect = Config.getWvdialCommandConnect();
		InternetDialUtil.wvdialCommandDisconnect = Config.getWvdialCommandDisconnect();
		for (Map.Entry<String, DataModem> entry : ConfigModem.getModemData().entrySet())
		{
			String modemID = entry.getKey();
			DataModem modemData = entry.getValue();
			if(modemData.isInternetAccess() && modemData.isActive())
			{
				boolean connected = InternetDialUtil.connect(modemID);
				if(connected)
				{
					break;
				}
			}
		}
		ServerInfo.sendModemStatus();
	}
	
	public static boolean connect(String modemID)
	{
		DataModem modemData = ConfigModem.getModemData(modemID);
		try 
		{
			if(GSMUtil.isConnected(modemID))
			{
				GSMUtil.disconnect(modemID);
			}
		} 
		catch (GSMException e) 
		{
			logger.error(e.getMessage(), e);
		}
		try 
		{
			InternetDialUtil.apply(modemData);
		} 
		catch (IOException e) 
		{
			logger.error(e.getMessage(), e);
		}
		CommandLineResult result = CommandLineExecutor.exec(InternetDialUtil.wvdialCommandConnect);
		String resultStr = result.toString();
		boolean success = isDialSuccess(resultStr);
		InternetDialUtil.internetAccess.put(modemID, success);
		return success;
	}
	
	private static boolean isDialSuccess(String resultStr) 
	{
		String[] lines = resultStr.split("\r\n");
		int status = 0;
		for(int i = 0; i<lines.length; i++)
		{
			String line = lines[i];		
			if(InternetDialUtil.lineContains(line, "local IP address".split(" ")) && status < 1)
			{
				status = 1;
			}
			if(InternetDialUtil.lineContains(line, "remote IP address".split(" ")) && status < 2)
			{
				status = 2;
			}
			if(InternetDialUtil.lineContains(line, "primary DNS address".split(" ")) && status < 3)
			{
				status = 3;
			}
			if(InternetDialUtil.lineContains(line, "secondary DNS address".split(" ")) && status < 4)
			{
				status = 4;
			}
		}
		return status >= 4;
	}

	private static boolean lineContains(String haystack, String[] search)
	{
		for(int i = 0; i<search.length; i++)
		{
			search[i] = search[i].replaceAll("\\s+", " ").trim();
			if(!haystack.contains(search[i]))
			{
				return false;
			}
		}
		return true;
	}
	
	public static boolean disconnect(String modemID)
	{
		InternetDialUtil.internetAccess.remove(modemID);
		CommandLineResult result = CommandLineExecutor.exec(InternetDialUtil.wvdialCommandDisconnect);
		String resultStr = result.toString();
		return resultStr.contains("stoped");
	}

	public static boolean isConnected(String modemID) {
		if(InternetDialUtil.internetAccess.isEmpty())
		{
			return false;
		}
		if(InternetDialUtil.internetAccess.containsKey(modemID))
		{
			return InternetDialUtil.internetAccess.get(modemID).booleanValue();
		}
		return false;
	}

	private static void apply(DataModem modemData) throws IOException {
		
		String configStr = ""
				+ "[Dialer Defaults]\r\n"
				+ "Modem = "+modemData.getPort()+"\r\n"
				+ "Baud = "+modemData.getBaudRate()+"\r\n"
				+ "Init = "+modemData.getInitDial1()+"\r\n"
				+ "Init2 = "+modemData.getInitDial2()+"\r\n"
				+ "Phone = "+modemData.getDialNumner()+"\r\n"
				+ "Username = "+modemData.getApnUsername()+"\r\n"
				+ "Password = "+modemData.getApnPassword()+"\r\n"
				+ "\r\n"
				+ "[Dialer phone2]\r\n"
				+ "Phone = "+modemData.getDialNumner()+"\r\n"
				+ "\r\n"
				+ "[Dialer shh]\r\n"
				+ "Init3 = "+modemData.getInitDial3()+"\r\n"
				+ "\r\n"
				+ "[Dialer pulse]\r\n"
				+ "Dial Command = "+modemData.getDialCommand()+"";
		
		String fileName = FileConfigUtil.fixFileName(InternetDialUtil.configPath);
		FileConfigUtil.write(fileName, configStr.getBytes());
	}
	

}
