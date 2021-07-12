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
	
	public void init()
	{
		if(ConfigSMTP.isActive())
		{
			this.port = ConfigSMTP.getServerPort();
			this.serverAddress = ConfigSMTP.getServerAddress();
			this.softwareName = ConfigSMTP.getSoftwareName();
			this.start();
		}
	}
	
	public void destroy()
	{
		this.stop();
	}
	
	public void stop() 
	{
		this.server.stop();	
	}
	
	public void start()
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
