package com.planetbiru.gsm;

public class ATCommand {

	public static final String GET_IMEI         = "AT+CGSN";
	public static final String GET_IMSI         = "AT+CIMI";
	public static final String GET_ICCID        = "AT+CCID";
	public static final String GET_MSISDN       = "AT+CNUM";
	public static final String GET_MANUFACTURER = "AT+CGMI";
	public static final String GET_MODEL        = "AT+CGMM";
	public static final String GET_REVISION     = "AT+CGMR";
	public static final String GET_SMS_CENTER   = "AT+CSCA";
	public static final String ERROR            = "ERROR";
	public static final String DATA_MODE_TEXT   = "1";
	public static final String DATA_MODE_PDU    = "0";
	
	private ATCommand()
	{
		
	}

}
