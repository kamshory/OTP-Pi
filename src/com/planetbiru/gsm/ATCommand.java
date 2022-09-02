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

	public static final String CREATE_DELETE_SMS_READ               = "AT+CMGD=0,1";
	public static final String CREATE_DELETE_SMS_READ_SENT          = "AT+CMGD=0,2";
	public static final String CREATE_DELETE_SMS_READ_SENT_UNSENT   = "AT+CMGD=0,3";
	public static final String CREATE_DELETE_SMS_ALL                = "AT+CMGD=0,4";
	public static final String SIGNAL_STRENGTH_REQUEST              = "AT+CSQ";
	public static final String SIGNAL_STRENGTH_RESPONSE             = "+CSQ:";
	public static final String NETWORK_REGISTRATION_REQUEST         = "AT+CREG?";
	public static final String NETWORK_REGISTRATION_RESPONSE_PREFIX = "+CREG:";
	public static final String TELCO_OPERATOR_REQUEST               = "AT+COPS?";
	public static final String TELCO_OPERATOR_RESPONSE              = "+COPS";
	public static final String NETWORK_REGISTRATION_RESPONSE        = "+CREG";

	private ATCommand()
	{
		
	}

}
