package com.planetbiru.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import com.jcabi.ssh.Shell;
import com.jcabi.ssh.Ssh;
import com.planetbiru.config.Config;
import com.planetbiru.util.OSUtil.OS;

public class CommandLineExecutor {
	
	private CommandLineExecutor()
	{
		
	}

	public static CommandLineResult exec(String command)
	{	
		CommandLineResult result = new CommandLineResult();
		String line;
        Process process;
        try 
        {
        	String commandLine = CommandLineExecutor.fixCommand(command);
            process = Runtime.getRuntime().exec(commandLine);
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = br.readLine()) != null)
            {
            	result.addLine(line);
            }
            process.waitFor();
            result.setExitValue(process.exitValue());
            process.destroy();
        } 
        catch (Exception e) {
        	result.setError(true);
        	result.setErrorMessage(e.getMessage());
        }
        return result;
	}
	
	public static CommandLineResult exec(String[] command)
	{	
		CommandLineResult result = new CommandLineResult();
		String line;
        Process process;
        try 
        {
            process = Runtime.getRuntime().exec(command);
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = br.readLine()) != null)
            {
            	result.addLine(line);
            }
            process.waitFor();
            result.setExitValue(process.exitValue());
            process.destroy();
        } 
        catch (Exception e) {
        	result.setError(true);
        	result.setErrorMessage(e.getMessage());
        }
        return result;
	}

	private static String fixCommand(String command) 
	{
		if(OSUtil.getOS().equals(OS.WINDOWS))
		{
			return "cmd.exe /c "+command;
		}
		else
		{
			return command;
		}
	}
	public static String execSSH(String command) throws IOException
	{
		return CommandLineExecutor.execSSH(command, Config.getSshHost(), Config.getSshPort(), Config.getSshUsername(), Config.getSshPassword());
	}
	public static String execSSH(String command, String host, int port, String username, String password) throws IOException 
	{
		Shell shell = new Ssh(host, port, username, password);
		return new Shell.Plain(shell).exec(command);		
	}
}
