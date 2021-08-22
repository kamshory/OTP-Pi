package com.planetbiru.gsm;

public class CGSMConst {

	public static final String CREATE_DELETE_SMS_READ             = "AT+CMGD=0,1";
	public static final String CREATE_DELETE_SMS_READ_SENT        = "AT+CMGD=0,2";
	public static final String CREATE_DELETE_SMS_READ_SENT_UNSENT = "AT+CMGD=0,3";
	public static final String CREATE_DELETE_SMS_ALL              = "AT+CMGD=0,4";
	private static String[] smsStorage = new String[]{
    		"MT", 
    		"SM"
    	};
	
	private CGSMConst()
	{
		
	}

	public static String[] getSmsStorage() {
		return smsStorage;
	}

	public static void setSmsStorage(String[] smsStorage) {
		CGSMConst.smsStorage = smsStorage;
	}
	
}
