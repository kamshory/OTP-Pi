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
	
	public static String execXXX(String command)
	{
		Config.setSshEnable(false);
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
			    String command = "/bin/hwclock --set --date \""+currentTime+"\"";
			    CommandLineExecutor.exec(command);	
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
		CommandLineExecutor.exec(Config.getRebootCommand());
	}

	public static void restart()
	{
		Application.restartService();
	}

	public static void setTimeZone(String timeZone) {
		String command1 = "/bin/timedatectl set-timezone " + timeZone;
		String command2 = "rm -rf /etc/localtime";
		String command3 = "ln -s /usr/share/zoneinfo/"+timeZone+" /etc/localtime";
		
		CommandLineExecutor.exec(command1);
		CommandLineExecutor.exec(command2);
		CommandLineExecutor.exec(command3);		
	}
	
	public static void setHardwareClock(Date date)
	{
		String command = "/bin/hwclock --set --date \""+Utility.date("MM/dd/yyyy HH:mm:ss", date)+"\"";
		CommandLineExecutor.exec(command);
	}

	public static void cleanup() 
	{
		CommandLineExecutor.exec(Config.getCleanupCommand());		
	}
	
}
