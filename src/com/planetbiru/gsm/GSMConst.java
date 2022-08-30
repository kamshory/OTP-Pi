package com.planetbiru.gsm;

public class GSMConst {

	private static String[] smsStorage = new String[]
	{
		"SM",
   		"MT" 
	};
	
	private GSMConst()
	{
		
	}

	public static String[] getSmsStorage() {
		return smsStorage;
	}

	public static void setSmsStorage(String[] smsStorage) {
		GSMConst.smsStorage = smsStorage;
	}
	
}
