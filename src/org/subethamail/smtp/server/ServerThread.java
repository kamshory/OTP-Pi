package org.subethamail.smtp.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.GuardedBy;

import org.apache.log4j.Logger;
import org.slf4j.MDC;

/**
 * ServerThread accepts TCP connections to the server socket and starts a new
 * {@link Session} thread for each connection which will handle the connection.
 * On shutdown it terminates not only this thread, but the session threads too.
 */
class ServerThread extends Thread
{
	private final Logger log = Logger.getLogger(ServerThread.class);
	private final SMTPServer server;
	private final ServerSocket serverSocket;
	/**
	 * A semaphore which is used to prevent accepting new connections by
	 * blocking this thread if the allowed count of open connections is already
	 * reached.
	 */
	private final Semaphore connectionPermits;
	/**
	 * The list of currently running sessions.
	 */
	@GuardedBy("this")
	private final Set<Session> sessionThreads;
	/**
	 * A flag which indicates that this SMTP port and all of its open
	 * connections are being shut down.
	 */
	private volatile boolean shuttingDown;

	public ServerThread(SMTPServer server, ServerSocket serverSocket)
	{
		super(ServerThread.class.getName() + " " + server.getDisplayableLocalSocketAddress());
		this.server = server;
		this.serverSocket = serverSocket;
		// reserve a few places for graceful disconnects with informative
		// messages
		int countOfConnectionPermits = server.getMaxConnections() + 10;
		this.connectionPermits = new Semaphore(countOfConnectionPermits);
		this.sessionThreads = new HashSet<Session>(countOfConnectionPermits * 4 / 3 + 1);
	}

	/**
	 * This method is called by this thread when it starts up. To safely cause
	 * this to exit, call {@link #shutdown()}.
	 */
	@Override
	public void run()
	{
		MDC.put("smtpServerLocalSocketAddress", server.getDisplayableLocalSocketAddress());
		if(this.server.isLogger())
		{
			log.info("SMTP server "+server.getDisplayableLocalSocketAddress()+" started");
		}

		try
		{
			runAcceptLoop();
			log.info("SMTP server "+server.getDisplayableLocalSocketAddress()+" stopped");
		}
		catch (RuntimeException e)
		{
			log.error("Unexpected exception in server socket thread, server is stopped", e);
			throw e;
		}
		catch (Error e)
		{
			log.error("Unexpected error in server socket thread, server is stopped", e);
			throw e;
		}
		finally
		{
			MDC.remove("smtpServerLocalSocketAddress");
		}
	}

	/**
	 * Accept connections and run them in session threads until shutdown.
	 */
	private void runAcceptLoop()
	{
		while (!this.shuttingDown)
		{
			try
			{
				// block if too many connections are open
				connectionPermits.acquire();
			}
			catch (InterruptedException consumed)
			{
				continue; // exit or retry
			}

			Socket socket = null;
			try
			{
				socket = this.serverSocket.accept();
			}
			catch (IOException e)
			{
				connectionPermits.release();
				// it also happens during shutdown, when the socket is closed
				if (!this.shuttingDown)
				{
					log.error("Error accepting connection", e);
					// prevent a possible loop causing 100% processor usage
					try
					{
						Thread.sleep(1000);
					}
					catch (InterruptedException consumed)
					{
						// fall through
					}
				}
				continue;
			}

			Session session = null;
			try
			{
				session = new Session(server, this, socket);
			}
			catch (IOException e)
			{
				connectionPermits.release();
				log.error("Error while starting a connection", e);
				try
				{
					socket.close();
				}
				catch (IOException e1)
				{
					log.debug("Cannot close socket after exception", e1);
				}
				continue;
			}

			// add thread before starting it,
			// because it will check the count of sessions
			synchronized (this)
			{
				this.sessionThreads.add(session);
			}

			try {
				server.getExecutorService().execute(session);
			}
			catch (RejectedExecutionException e) {
				connectionPermits.release();
				synchronized (this)
				{
					this.sessionThreads.remove(session);
				}
				log.error("Error while executing a session", e);
				try
				{
					socket.close();
				}
				catch (IOException e1)
				{
					log.debug("Cannot close socket after exception", e1);
				}
				continue;
			}
		}
	}

	/**
	 * Closes the server socket and all client sockets.
	 */
	public void shutdown()
	{
		// First make sure we aren't accepting any new connections
		shutdownServerThread();
		// Shut down any open connections.
		shutdownSessions();
	}

	private void shutdownServerThread()
	{
		shuttingDown = true;
		closeServerSocket();
		interrupt();
		try {
			join();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Closes the serverSocket in an orderly way.
	 */
	private void closeServerSocket()
	{
		try
		{
			this.serverSocket.close();
			log.debug("SMTP Server socket shut down");
		}
		catch (IOException e)
		{
			log.error("Failed to close server socket.", e);
		}
	}

	private void shutdownSessions()
	{
		// Copy the sessionThreads collection so the guarding lock on this
		// instance can be released before calling the Session.shutdown methods.
		// This is necessary to avoid a deadlock, because the terminating
		// session threads call back the sessionEnded function in this instance,
		// which locks this instance.
		List<Session> sessionsToBeClosed;
		synchronized (this) {
			sessionsToBeClosed = new ArrayList<Session>(sessionThreads);
		}
		for (Session sessionThread : sessionsToBeClosed)
		{
			sessionThread.quit();
		}

		server.getExecutorService().shutdown();
		try {
			server.getExecutorService().awaitTermination(Long.MAX_VALUE,
					TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			log.warn("Interrupted waiting for termination of session threads",
					e);
			Thread.currentThread().interrupt();
		}
	}

	public synchronized boolean hasTooManyConnections()
	{
		return sessionThreads.size() > server.getMaxConnections();
	}

	public synchronized int getNumberOfConnections()
	{
		return sessionThreads.size();
	}

	/**
	 * Registers that the specified {@link Session} thread ended. Session
	 * threads must call this function.
	 */
	public void sessionEnded(Session session)
	{
		synchronized (this)
		{
			sessionThreads.remove(session);
		}
		connectionPermits.release();
	}
}
