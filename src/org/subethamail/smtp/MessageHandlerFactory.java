/*
 * $Id: MessageHandlerFactory.java 337 2009-06-29 19:20:58Z latchkey $
 * $URL: https://subethasmtp.googlecode.com/svn/trunk/src/main/java/org/subethamail/smtp/MessageHandlerFactory.java $
 */
package org.subethamail.smtp;



/**
 * The primary interface to be implemented by clients of the SMTP library.
 * This factory is called for every message to be exchanged in an SMTP
 * conversation.  If multiple messages are transmitted in a single connection
 * (via RSET), multiple handlers will be created from this factory.
 *
 * @author Jeff Schnitzer
 */
public interface MessageHandlerFactory
{
	/**
	 * Called for the exchange of a single message during an SMTP conversation.
	 *
	 * @param ctx provides information about the client.
	 */
	public MessageHandler create(MessageContext ctx);
}
