package com.planetbiru;

import org.subethamail.smtp.server.SMTPServer;

import com.planetbiru.config.ConfigSMTP;
import com.planetbiru.mail.PlanetMessageHandlerFactory;

public class ServerEmail {
	private PlanetMessageHandlerFactory handlerFactory = new PlanetMessageHandlerFactory();
	
	private String serverAddress = "localhost";
	private int port = 25;
	private String softwareName = "";
	
	private SMTPServer server = null;
	
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
	
	public void destroy()
	{
		this.stop();
	}
	
	public void stop() 
	{
		try 
		{
			if(this.server != null)
			{
				this.server.stop();
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		finally 
		{
			  this.server = null;
		}
	}
	
	private void startService()
    {
        this.server = new SMTPServer(this.handlerFactory);
        this.server.setHostName(this.serverAddress);
        this.server.setSoftwareName(this.softwareName);
        this.server.setPort(this.port);
        try
        {
            this.server.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
