package com.planetbiru.constant;

public class ResponseCode {
	private ResponseCode()
	{
		
	}
	public static final String SUCCESS              = "0000";
	public static final String SERIAL_PORT_NULL     = "1000";
	public static final String UNAUTHORIZED         = "1100";
	public static final String NO_DEVICE_CONNECTED  = "1101";
	public static final String FAILED               = "1102";
	public static final String DUPLICATED           = "1201";
	public static final String INVALID_OTP          = "1202";
	public static final String EXPIRED              = "1203";

}
