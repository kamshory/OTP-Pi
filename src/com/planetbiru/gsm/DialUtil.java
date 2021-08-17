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

public class DialUtil {
	
	
	private static Map<String, Boolean> internetAccess = new HashMap<>();
	private static String configPath = "";
	private static String wvdialCommandConnect = "";
	private static String wvdialCommandDisconnect = "";
	
	private static Logger logger = Logger.getLogger(DialUtil.class);
	
	private DialUtil()
	{
		
	}

	public static void start() {
		DialUtil.configPath = Config.getWvdialSettingPath();
		DialUtil.wvdialCommandConnect = Config.getWvdialCommandConnect();
		DialUtil.wvdialCommandDisconnect = Config.getWvdialCommandDisconnect();
		for (Map.Entry<String, DataModem> entry : ConfigModem.getModemData().entrySet())
		{
			String modemID = entry.getKey();
			DataModem modemData = entry.getValue();
			if(modemData.isInternetAccess() && modemData.isActive())
			{
				boolean connected = DialUtil.connect(modemID);
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
			/**
			 * Do nothing
			 */
			logger.error(e.getMessage(), e);
		}
		try 
		{
			DialUtil.apply(modemData);
		} 
		catch (IOException e) 
		{
			logger.error(e.getMessage(), e);
		}
		boolean ret = false;
		CommandLineResult result = CommandLineExecutor.exec(wvdialCommandConnect);
		System.out.println("exec   = "+wvdialCommandConnect);
		System.out.println("result = "+result);
		ret = true;
		DialUtil.internetAccess.put(modemID, ret);
		return ret;
	}
	
	public static boolean disconnect(String modemID)
	{
		DialUtil.internetAccess.remove(modemID);
		CommandLineResult result = CommandLineExecutor.exec(wvdialCommandDisconnect);
		System.out.println("exec   = "+wvdialCommandDisconnect);
		System.out.println("result = "+result);
		return true;
	}

	public static boolean isConnected(String modemID) {
		if(DialUtil.internetAccess.isEmpty())
		{
			return false;
		}
		if(DialUtil.internetAccess.containsKey(modemID))
		{
			return DialUtil.internetAccess.get(modemID).booleanValue();
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
		
		String fileName = FileConfigUtil.fixFileName(DialUtil.configPath);
		FileConfigUtil.write(fileName, configStr.getBytes());
	}
	

}
