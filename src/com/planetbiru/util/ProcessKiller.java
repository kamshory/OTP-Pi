package com.planetbiru.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProcessKiller {

	/**
	 * Image name of this application
	 */
	private String path = "";	
	/**
	 * Flag to keep. false to kill all processes. true to kill all processes except the current process
	 */
	private boolean exceptThis = false;
	
	/**
	 * Default constructor 
	 */
	public ProcessKiller()
	{		
	}
	/**
	 * Constructor with path initialization
	 * @param path Application path to be killed
	 */
	public ProcessKiller(String path)
	{
		this.path = path;
	}
	/**
	 * Constructor with path initialization
	 * @param path Application path to be killed
	 * @param exceptThis Except this process
	 */
	public ProcessKiller(String path, boolean exceptThis) 
	{
		this.path = path;
		this.exceptThis = exceptThis;
	}
	/**
	 * Get base name of specified path
	 * @param path Path name
	 * @return Base name
	 */ 
	public String baseName(String path)
	{
		path = path.replace("\\", "/");		
		String name = "";
		if(!path.contains("/"))
		{
			name = path;
		}
		else
		{	
			String [] arr = path.split("/");
			name = arr[arr.length-1];
		}
		return name;
	}
	/**
	 * Stop process
	 * @return true if success and false if failed
	 */ 
	public boolean stop()
	{
		String operatingSystem = System.getProperty("os.name").toLowerCase();
		String commandLine = "";
		if(operatingSystem.contains("windows"))
		{
			commandLine = "wmic process get processid,creationdate,name,commandline /format:csv";
			killProcessWindow(commandLine);
		}
		else
		{
			commandLine = "ps -ef --sort=start_time";
			killProcessLinux(commandLine);
		}
		return true;
	}
	private void killProcessLinux(String commandLine) {
		List<String> processList = new ArrayList<>();
		String pid = "";
        int i = 0;
        int prcosessCount = 0;
        int processToKill = 0;
		try 
		{
			try
	        {            
				Runtime rt = Runtime.getRuntime();
		        Process proc = rt.exec(commandLine);
		        proc.waitFor();
	            InputStream simpuStream = proc.getInputStream();
	            InputStreamReader simpuStreamReader = new InputStreamReader(simpuStream);
	            BufferedReader bufferedReader = new BufferedReader(simpuStreamReader);
				String line = null;
				while ((line = bufferedReader.readLine()) != null)
	            {
	            	if(line.toLowerCase().contains("java ") && line.toLowerCase().contains("-jar ") && line.toLowerCase().contains(this.baseName(this.path).toLowerCase()))
	            	{
	            		line = line.replace("\t", " ");
	            		line = line.replace("  ", " ").trim();
	            		line = line.replace("  ", " ").trim();
	            		line = line.replace("  ", " ").trim();
	            		line = line.replace("  ", " ").trim();
	            		line = line.replace("  ", " ").trim();
	            		String[] arr = line.split(" ");
	            		if(arr.length > 1)
	            		{
	            			pid = arr[1];
	            			processList.add(pid.trim());	            				            			

	            		}
	            	}
	            }
	            bufferedReader.close();
		        prcosessCount = processList.size();
		        if(this.exceptThis)
		        {
		        	processToKill = prcosessCount - 1;
		        }
		        else
		        {
		        	processToKill = prcosessCount;
		        }
	        	for(i = 0; i < processToKill; i++)
	        	{
	        		pid = processList.get(i).toString();
        			String commandLine2 = "kill -9 "+pid+"";
        			Runtime rt2 = Runtime.getRuntime();
    		        rt2.exec(commandLine2);
 	        	}
	        } 
			catch (Throwable t)
	        {
	        }
		} 
		catch (Exception e) 
		{
		}		
	}
	private void killProcessWindow(String commandLine) {
		List<String> processListWindows = new ArrayList<>();
		List<String> processList = new ArrayList<>();
		String datetime = "";
		String pid = "";
        int i = 0;
        int prcosessCount = 0;
        int processToKill = 0;
		try
        {            
			Runtime rt = Runtime.getRuntime();
	        Process proc = rt.exec(commandLine);
	        //proc.waitFor();
            InputStream simpuStream = proc.getInputStream();
            InputStreamReader simpuStreamReader = new InputStreamReader(simpuStream);
            BufferedReader bufferedReader = new BufferedReader(simpuStreamReader);
			String line = null;
            while ((line = bufferedReader.readLine()) != null)
            {
            	String ret = kill(line, datetime);
            	if(ret != null)
            	{
            		processListWindows.add(ret);
            	}
            	
            }
            System.out.println(processListWindows);
            bufferedReader.close();	 
            // Sort by date and time
            Collections.sort(processListWindows);
	        prcosessCount = processListWindows.size();
            
            for(i = 0; i<prcosessCount; i++)
            {
            	line = processListWindows.get(i);
        		String[] arr = line.split(",");
    			pid = arr[1];
    			processList.add(pid.trim());	            				            				            	
            }
            
	        prcosessCount = processList.size();
	        if(this.exceptThis)
	        {
	        	processToKill = prcosessCount - 1;
	        }
	        else
	        {
	        	processToKill = prcosessCount;
	        }
        	for(i = 0; i < processToKill; i++)
        	{
        		pid = processList.get(i);
	            String commandLine2 = "taskkill /PID "+pid+" /F";
    			Runtime rt2 = Runtime.getRuntime();
		        rt2.exec(commandLine2);
        	}
        } 
		catch (Throwable t)
        {
        }
		
	}
	private String kill(String line, String datetime) {
		String pid = "";
		String proceeeFlag = null;
		if(line.toLowerCase().contains("java") && line.toLowerCase().contains(this.baseName(this.path).toLowerCase()))
    	{
    		String[] arr = line.split(",");
    		if(arr.length > 4)
    		{
    			if(arr[1].toLowerCase().contains("java") && arr[1].toLowerCase().contains(this.baseName(this.path).toLowerCase()) && (arr[3].toLowerCase().contains("java.exe") || arr[3].toLowerCase().contains("javaw.exe")))
    			{
    				datetime = arr[2];
        			pid = arr[4];
        			proceeeFlag = datetime+","+pid;	            				            			
    			}
    		}
    	}
		return proceeeFlag;
		
	}
}
