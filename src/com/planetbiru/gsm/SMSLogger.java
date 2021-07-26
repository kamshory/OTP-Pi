package com.planetbiru.gsm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.Utility;

public class SMSLogger {
	private static String fileName = "";
	private static String baseName = "";
	private static String path = "";

	private SMSLogger()
	{
		
	}
	public static void setPath(String path)
	{
		SMSLogger.path = path;
		Date date = new Date();
		if(!dirCreated(date))
		{
			prepareFile(date);
		}	
	}
	
	private static void prepareDir(String fileName) {
		File file = new File(fileName);
		String directory1 = file.getParent();
		File file2 = new File(directory1);
		String directory2 = file2.getParent();
		File file3 = new File(directory2);
		String directory3 = file3.getParent();
		
		File d1 = new File(directory1);
		File d2 = new File(directory2);		
		File d3 = new File(directory3);		

		if(!d3.exists())
		{
			d3.mkdir();
		}
		if(!d2.exists())
		{
			d2.mkdir();
		}
		if(!d1.exists())
		{
			d1.mkdir();
		}		
	}
	
	public static void add(Date date, String id, String sender, String receiver, int length) {
		if(!dirCreated(date))
		{
			prepareFile(date);
		}
		if(!SMSLogger.fileName.isEmpty())
		{
			String data = date.getTime()+","+id+","+sender+","+receiver+","+length+"\r\n";
			try(FileOutputStream fos = new FileOutputStream(SMSLogger.fileName, true))
			{
				fos.write(data.getBytes());
			}
			catch(IOException e)
			{
				/**
				 * DO nothing
				 */
			}
		}
	}	

	private static void prepareFile(Date date) {
		String baseName = Utility.date("yyyy-MM-dd", date)+".csv";
		SMSLogger.baseName = baseName;
		SMSLogger.fileName = FileConfigUtil.fixFileName(SMSLogger.path + File.separatorChar + baseName);	
		SMSLogger.prepareDir(SMSLogger.fileName);
	}
	
	private static boolean dirCreated(Date date) {
		String dt = Utility.date("yyyy-MM-dd", date)+".csv";
		return SMSLogger.baseName.equals(dt);
	}
	
	

}
