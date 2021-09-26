package com.planetbiru.gsm;

public class SerialPortConnectionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Default constructor
	 */
	public SerialPortConnectionException() 
	{ 
		super(); 
	}
	/**
	 * Constructor with the message
	 * @param message Message
	 */
	public SerialPortConnectionException(String message) 
	{ 
		super(message); 
	}
	/**
	 * Constructor with the message and cause
	 * @param message Message
	 * @param cause Cause
	 */
	public SerialPortConnectionException(String message, Throwable cause) 
	{ 
		super(message, cause); 
	}
	/**
	 * Constructor with cause
	 * @param cause Cause
	 */
	public SerialPortConnectionException(Throwable cause) 
	{ 
		super(cause); 
	}
	public SerialPortConnectionException(Exception e) 
	{ 
		super(e); 
	}

}
