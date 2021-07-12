package com.planetbiru;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Date;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;

import com.planetbiru.config.Config;
import com.planetbiru.util.CommandLineExecutor;
import com.planetbiru.util.Utility;

public class DeviceAPI {
	
	private DeviceAPI()
	{
		
	}
	
	public static String exec(String command)
	{
		String result = "";
		if(Config.isSshEnable())
		{
			try 
			{
				result = CommandLineExecutor.execSSH(
						command, 
						Config.getSshSleep(),
						Config.getSshHost(), 
						Config.getSshPort(), 
						Config.getSshUsername(), 
						Config.getSshPassword() 
					);
			} 
			catch (IOException e) 
			{
				String[] commands = new String[1];
				commands[0] = command;
				result = CommandLineExecutor.exec(commands).toString();
			}
		}
		else
		{
			String[] commands = new String[1];
			commands[0] = command;
			result = CommandLineExecutor.exec(commands).toString();
		}
		return result;
	}

	public static void syncTime(String ntpServer) {
		NTPUDPClient client = new NTPUDPClient();
	    /**
	     * We want to timeout if a response takes longer than 10 seconds
	     */
	    client.setDefaultTimeout(10000);

		try 
		{
			InetAddress inetAddress = InetAddress.getByName(ntpServer);
		    TimeInfo timeInfo = client.getTime(inetAddress);
		    timeInfo.computeDetails();
		    Long offset;
			if (timeInfo.getOffset() != null) 
		    {
		        offset  = timeInfo.getOffset();
		        /**
			     * Calculate the remote server NTP time
			     */
			    long currentTimeMils = System.currentTimeMillis();
			    TimeStamp atomicNtpTime = TimeStamp.getNtpTime(currentTimeMils + offset);

			    Date date = new Date(atomicNtpTime.getTime());
			
			    /**
			     * URL : https://www.thegeekstuff.com/2013/08/hwclock-examples/
			     */
				String currentTime = Utility.date("MM/dd/yyyy HH:mm:ss", date);
			    String command = "hwclock --set --date \""+currentTime+"\"";
			    DeviceAPI.exec(command);	
		    }
		} 
		catch (IOException e) 
		{
			/**
			 * 
			 */
		}
	}
	
	public static void reboot()
	{
		DeviceAPI.exec(Config.getRebootCommand());
	}

	public static void restart()
	{
		DeviceAPI.exec(Config.getRestartCommand());
	}

	public static void setTimeZone(String timeZone) {
		String command1 = "sudo timedatectl set-timezone " + timeZone;
		String command2 = "sudo rm -rf /etc/localtime";
		String command3 = "sudo ln -s /usr/share/zoneinfo/"+timeZone+" /etc/localtime";
		
		DeviceAPI.exec(command1);
		DeviceAPI.exec(command2);
		DeviceAPI.exec(command3);		
	}
	
	public static void setHardwareClock(Date date)
	{
		String command = "hwclock --set --date \""+Utility.date("MM/dd/yyyy HH:mm:ss", date)+"\"";
		DeviceAPI.exec(command);
	}

	public static void cleanup() 
	{
		DeviceAPI.exec(Config.getCleanupCommand());		
	}
	
}
