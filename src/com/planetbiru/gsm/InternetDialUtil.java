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
			/**
			 * Do nothing
			 */
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
		boolean ret = false;
		CommandLineResult result = CommandLineExecutor.exec(InternetDialUtil.wvdialCommandConnect);
		String resultStr = result.toString();
		ret = resultStr.contains("started") || resultStr.contains("running");
		/**
		System.out.println("exec   = "+wvdialCommandConnect);
		System.out.println("result = "+result);
		ret = true;
		*/
		InternetDialUtil.internetAccess.put(modemID, ret);
		return ret;
	}
	
	public static boolean disconnect(String modemID)
	{
		InternetDialUtil.internetAccess.remove(modemID);
		CommandLineResult result = CommandLineExecutor.exec(InternetDialUtil.wvdialCommandDisconnect);
		String resultStr = result.toString();
		/**
		System.out.println("exec   = "+DialUtil.wvdialCommandDisconnect);
		System.out.println("result = "+result);
		*/
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
