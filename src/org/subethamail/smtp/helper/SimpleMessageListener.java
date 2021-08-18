/*
 * $Id: SimpleMessageListener.java 337 2009-06-29 19:20:58Z latchkey $
 * $URL: https://subethasmtp.googlecode.com/svn/trunk/src/main/java/org/subethamail/smtp/helper/SimpleMessageListener.java $
 */
package org.subethamail.smtp.helper;

import java.io.IOException;
import java.io.InputStream;

import org.subethamail.smtp.TooMuchDataException;

/**
 * This is an interface for processing the end-result messages that is
 * higher-level than the MessageHandler and related factory.
 *
 * While the SMTP message is being received, all listeners are asked if they
 * want to accept each recipient. After the message has arrived, the message is
 * handed off to all accepting listeners.
 *
 * @author Jeff Schnitzer
 */
public interface SimpleMessageListener
{
	/**
	 * Called once for every RCPT TO during a SMTP exchange.  Each accepted recipient
	 * will result in a separate deliver() call later.
	 *
	 * @param from is a rfc822-compliant email address.
	 * @param recipient is a rfc822-compliant email address.
	 *
	 * @return true if the listener wants delivery of the message, false if the
	 *         message is not for this listener.
	 */
	public boolean accept(String from, String recipient);

	/**
	 * When message data arrives, this method will be called for every recipient
	 * this listener accepted.
	 *
	 * @param from is the envelope sender in rfc822 form
	 * @param recipient will be an accepted recipient in rfc822 form
	 * @param data will be the smtp data stream, stripped of any extra '.' chars.  The
	 * 			data stream is only valid for the duration of this call.
	 *
	 * @throws TooMuchDataException if the listener can't handle that much data.
	 *         An error will be reported to the client.
	 * @throws IOException if there is an IO error reading the input data.
	 */
	public void deliver(String from, String recipient, InputStream data)
			throws TooMuchDataException, IOException;
}
