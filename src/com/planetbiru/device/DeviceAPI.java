package com.planetbiru.device;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Date;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;

import com.planetbiru.Application;
import com.planetbiru.config.Config;
import com.planetbiru.util.CommandLineExecutor;
import com.planetbiru.util.Utility;

public class DeviceAPI {
	
	private static long lastRestart = 0;
	private static long lastReboot = 0;
	private static long lastUpdateNTP = 0;
	private static long lastCheckModem = 0;
	private static long lastCheckStatus = 0;
	private static long lastCheckAMQP = 0;
	private static long lastCheckRedis = 0;

	private DeviceAPI()
	{
		
	}
	
	public static String execSSH(String command)
	{
		Config.setSshEnable(false);
		String result = "";
		if(Config.isSshEnable())
		{
			try 
			{
				result = CommandLineExecutor.execSSH(
						command, 
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
			
			    DeviceAPI.updateServerTime(date);
		    }
		} 
		catch (IOException e) 
		{
			/**
			 * 
			 */
		}
	}
	
	public static void updateServerTime(Date date) {
		/**
	     * URL : https://www.thegeekstuff.com/2013/08/hwclock-examples/
	     */
		String currentTime = Utility.date("MM/dd/yyyy HH:mm:ss", date);
	    String command = "/bin/hwclock --set --date \""+currentTime+"\"";
	    CommandLineExecutor.exec(command);			
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
		String command3 = "ln -s /usr/share/zoneinfo/" + timeZone + " /etc/localtime";
		
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

	public static void expand() {
		String command = "/usr/bin/rootfs-expand";
		CommandLineExecutor.exec(command);
	}

	public static long getLastReboot() {
		return lastReboot;
	}

	public static void setLastReboot(long lastReboot) {
		DeviceAPI.lastReboot = lastReboot;
	}

	public static long getLastRestart() {
		return lastRestart;
	}

	public static void setLastRestart(long lastRestart) {
		DeviceAPI.lastRestart = lastRestart;
	}

	public static long getLastUpdateNTP() {
		return lastUpdateNTP;
	}

	public static void setLastUpdateNTP(long lastUpdateNTP) {
		DeviceAPI.lastUpdateNTP = lastUpdateNTP;
	}

	public static long getLastCheckModem() {
		return lastCheckModem;
	}

	public static void setLastCheckModem(long lastCheckModem) {
		DeviceAPI.lastCheckModem = lastCheckModem;
	}

	public static long getLastCheckStatus() {
		return lastCheckStatus;
	}

	public static void setLastCheckStatus(long lastCheckStatus) {
		DeviceAPI.lastCheckStatus = lastCheckStatus;
	}

	public static long getLastCheckAMQP() {
		return lastCheckAMQP;
	}

	public static void setLastCheckAMQP(long lastCheckAMQP) {
		DeviceAPI.lastCheckAMQP = lastCheckAMQP;
	}

	public static long getLastCheckRedis() {
		return lastCheckRedis;
	}

	public static void setLastCheckRedis(long lastCheckRedis) {
		DeviceAPI.lastCheckRedis = lastCheckRedis;
	}
	
}
