package org.subethamail.smtp.server;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 */
public class InvalidCommandNameException extends CommandException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** */
	public InvalidCommandNameException()
	{
		super();
	}

	/** */
	public InvalidCommandNameException(String string)
	{
		super(string);
	}

	/** */
	public InvalidCommandNameException(String string, Throwable throwable)
	{
		super(string, throwable);
	}

	/** */
	public InvalidCommandNameException(Throwable throwable)
	{
		super(throwable);
	}
}
