package com.planetbiru.api;

public class OTPExpireException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Default constructor
	 */
	public OTPExpireException() 
	{ 
		super(); 
	}
	/**
	 * Constructor with the message
	 * @param message Message
	 */
	public OTPExpireException(String message) 
	{ 
		super(message); 
	}
	/**
	 * Constructor with the message and cause
	 * @param message Message
	 * @param cause Cause
	 */
	public OTPExpireException(String message, Throwable cause) 
	{ 
		super(message, cause); 
	}
	/**
	 * Constructor with cause
	 * @param cause Cause
	 */
	public OTPExpireException(Throwable cause) 
	{ 
		super(cause); 
	}
}
