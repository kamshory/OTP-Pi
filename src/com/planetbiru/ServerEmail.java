package com.planetbiru;

import org.apache.log4j.Logger;
import org.subethamail.smtp.server.SMTPServer;

import com.planetbiru.config.ConfigSMTP;
import com.planetbiru.mail.PlanetMessageHandlerFactory;

public class ServerEmail {
	private static Logger logger = Logger.getLogger(ServerEmail.class);
	private PlanetMessageHandlerFactory handlerFactory = new PlanetMessageHandlerFactory();
	
	private String serverAddress = "localhost";
	private int port = 25;
	private String softwareName = "";
	
	private SMTPServer server = null;
	private boolean running = false;
	
	public void start()
	{
		if(ConfigSMTP.isActive())
		{
			this.port = ConfigSMTP.getServerPort();
			this.serverAddress = ConfigSMTP.getServerAddress();
			this.softwareName = ConfigSMTP.getSoftwareName();
			this.startService();
		}
	}
	
	public void stopService() 
	{
		if(this.running)
		{
			try 
			{
				if(this.server != null)
				{
					this.server.stop();
				}
				this.server = null;
			}
			catch (Exception e)
			{
				logger.error(e.getMessage(), e);
			}
			finally 
			{
				  this.server = null;
			}
		}
		else
		{
			logger.info("Mail server not running");
		}
	}
	
	private void startService()
    {
        this.server = new SMTPServer(this.handlerFactory);
        this.server.setHostName(this.serverAddress);
        this.server.setSoftwareName(this.softwareName);
        this.server.setPort(this.port);
        this.server.setLogger(false);
        try
        {
            this.server.start();
            this.running = true;
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
    }
}
