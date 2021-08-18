/*
 * $Id: MessageHandler.java 447 2011-10-22 13:22:38Z hontvari.jozsef.levente@gmail.com $
 * $URL: https://subethasmtp.googlecode.com/svn/trunk/src/main/java/org/subethamail/smtp/MessageHandler.java $
 */
package org.subethamail.smtp;

import java.io.IOException;
import java.io.InputStream;

/**
 * The interface that defines the conversational exchange of a single message on
 * an SMTP connection. Using the term "mail transaction", as defined by RFC 
 * 5321, implementing classes of this interface track a single mail transaction. 
 * The methods will be called in the following order:
 *
 * <ol>
 * <li><code>from()</code></li>
 * <li><code>recipient()</code> (possibly more than once)</li>
 * <li><code>data()</code></li>
 * <li><code>done()</code></li>
 * </ol>
 *
 * If multiple messages are delivered on a single connection (ie, using the RSET command)
 * then multiple message handlers will be instantiated.  Each handler services one
 * and only one message.
 *
 * @author Jeff Schnitzer
 */
public interface MessageHandler
{
	/**
	 * Called first, after the MAIL FROM during a SMTP exchange. A 
	 * MessageHandler is created after the MAIL command is received, so this 
	 * function is always called, even if the mail transaction is aborted later.
	 *
	 * @param from is the sender as specified by the client.  It will
	 *  be a rfc822-compliant email address, already validated by
	 *  the server.
	 * @throws RejectException if the sender should be denied.
	 * @throws DropConnectionException if the connection should be dropped
	 */
	public void from(String from) throws RejectException;

	/**
	 * Called once for every RCPT TO during a SMTP exchange.
	 * This will occur after a from() call.
	 *
	 * @param recipient is a rfc822-compliant email address,
	 *  validated by the server.
	 * @throws RejectException if the recipient should be denied.
	 * @throws DropConnectionException if the connection should be dropped
	 */
	public void recipient(String recipient) throws RejectException;

	/**
	 * Called when the DATA part of the SMTP exchange begins.  This
	 * will occur after all recipient() calls are complete.
	 *
	 * Note: If you do not read all the data, it will be read for you
	 * after this method completes.
	 *
	 * @param data will be the smtp data stream, stripped of any extra '.' chars.  The
	 * 			data stream will be valid only for the duration of the call.
	 *
	 * @throws RejectException if at any point the data should be rejected.
	 * @throws DropConnectionException if the connection should be dropped
	 * @throws TooMuchDataException if the listener can't handle that much data.
	 *         An error will be reported to the client.
	 * @throws IOException if there is an IO error reading the input data.
	 */
	public void data(InputStream data) throws RejectException, TooMuchDataException, IOException;

	/**
	 * Called after all other methods are completed.  Note that this method
	 * will be called even if the mail transaction is aborted at some point 
	 * after the initial from() call. 
	 */
	public void done();
}
