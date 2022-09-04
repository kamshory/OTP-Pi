package com.planetbiru.config;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.json.JSONObject;

public class DataEmail {
	private String id = "";
	private String senderAddress = "";
	private String senderPassword;
	private String senderName = "";
	private boolean auth = true;	
	private String host = "";
	private int port = 587;
	private boolean startTLS = true;
	private boolean ssl = false;
	private boolean active = true;
	private Session session = null;
	private boolean debug = false;
	private String name = "";
	
	
	
	
	public void set(DataEmail newData) {
		this.id = newData.id;
		this.name = newData.name;
		this.senderAddress = newData.senderAddress;
		this.senderPassword = newData.senderPassword;
		this.senderName = newData.senderName;
		this.auth = newData.auth;
		this.host = newData.host;
		this.port = newData.port;
		this.startTLS = newData.startTLS;
		this.ssl = newData.ssl;
		this.active = newData.active;
	}
	
	public DataEmail() {
		/**
		 * Do nothing
		 */
	}
	public String toString()
	{
		return this.toJSONObject().toString();
	}
	public JSONObject toJSONObject() {
		JSONObject config = new JSONObject();
		config.put("id", this.id);
		config.put("name", this.name);
		config.put("auth", this.auth);
		config.put("host", this.host);
		config.put("port", this.port);
		config.put("senderAddress", this.senderAddress);
		config.put("senderPassword", this.senderPassword);
		config.put("ssl", this.ssl);
		config.put("startTLS", this.startTLS);
		config.put("active", this.active);
		return config;
	}
	
	public void init()
	{
		Properties properties = new Properties();
 		if(this.active)
        {
 			properties.put("mail.smtp.auth", Boolean.toString(this.auth));
	        if(this.startTLS)
 			{
	        	properties.put("mail.smtp.starttls.enable", Boolean.toString(this.startTLS));
 			}
	        if(this.ssl)
 			{
	        	properties.put("mail.smtp.ssl.enable", Boolean.toString(this.ssl));
 			}
 			properties.put("mail.smtp.host", this.host);
 			properties.put("mail.smtp.port", Integer.toString(this.port));	
	        properties.put("mail.smtp.socketFactory.port", this.port+"");
        }
 		
        String localSmtpUser = this.senderAddress;
        String localSmtpPassword = this.senderPassword;
        
        this.session = Session.getInstance(properties, new Authenticator() {
        	@Override
			protected PasswordAuthentication getPasswordAuthentication() 
        	{
                return new PasswordAuthentication(localSmtpUser, localSmtpPassword);
            }
        });
        this.session.setDebug(debug );
	}
	public boolean send(String to, String subject, String message, StackTraceElement ste) throws MessagingException //NOSONAR
	{
 		return this.send(to, subject, message, this.senderAddress);
	}
	
	
	public boolean send(String to, String subject, String message, String from) throws MessagingException
	{
		if(this.session == null)
		{
			this.init();
		}
		boolean sent = false;

        // Create a default MimeMessage object.
        MimeMessage mimeMessage = new MimeMessage(session);

		mimeMessage.setFrom(new InternetAddress(from, false));

        // Set From: header field of the header.
        mimeMessage.setFrom(new InternetAddress(this.senderAddress));

        // Set To: header field of the header.
        mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

        // Set Subject: header field
        mimeMessage.setSubject(subject);

        // Now set the actual message
        mimeMessage.setText(message, "utf-8", "html");
        
        mimeMessage.setSentDate(new Date());

        // Send message
        Transport.send(mimeMessage);
        sent = true;
        return sent;
	}
	
	
	public void sendmail(String to, String subject, String message, String contentType, String from, List<String> files) throws MessagingException, IOException 
	{
		
		Message msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress(from, false));
		
		msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
		msg.setSubject(subject);
		msg.setContent(message, contentType);
		msg.setSentDate(new Date());
		
		MimeBodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setContent(message, contentType);
		
		Multipart multipart = new MimeMultipart();
		multipart.addBodyPart(messageBodyPart);
		
		if(files != null)
		{
			for(int i = 0; i<files.size(); i++)
			{
				String path = files.get(i);
				MimeBodyPart attachPart = new MimeBodyPart();
				attachPart.attachFile(path);
				multipart.addBodyPart(attachPart);
				msg.setContent(multipart);
				
			}
		}
		
		Transport.send(msg);  
	}
	
	public boolean send(String to, String subject, String message, String from, String contentType, List<String> files) throws MessagingException, IOException
	{
 		boolean sent = false;

 		Message mimeMessage = new MimeMessage(session);
		mimeMessage.setFrom(new InternetAddress(from, false));
		
		mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
		mimeMessage.setSubject(subject);
		mimeMessage.setContent(message, contentType);
		mimeMessage.setSentDate(new Date());
		
		MimeBodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setContent(message, contentType);
		
		if(files != null)
		{
			for(int i = 0; i<files.size(); i++)
			{
				String path = files.get(i);
				Multipart multipart = new MimeMultipart();
				multipart.addBodyPart(messageBodyPart);
		
				MimeBodyPart attachPart = new MimeBodyPart();
				attachPart.attachFile(path);
				multipart.addBodyPart(attachPart);
				mimeMessage.setContent(multipart);
				
			}
		}

		Transport.send(mimeMessage);  
		sent = true;
		return sent;
	}

	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSenderAddress() {
		return senderAddress;
	}


	public void setSenderAddress(String senderAddress) {
		this.senderAddress = senderAddress;
	}


	public String getSenderPassword() {
		return senderPassword;
	}


	public void setSenderPassword(String senderPassword) {
		this.senderPassword = senderPassword;
	}


	public String getSenderName() {
		return senderName;
	}


	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}


	public boolean isAuth() {
		return auth;
	}


	public void setAuth(boolean auth) {
		this.auth = auth;
	}


	public String getHost() {
		return host;
	}


	public void setHost(String host) {
		this.host = host;
	}


	public int getPort() {
		return port;
	}


	public void setPort(int port) {
		this.port = port;
	}


	public boolean isStartTLS() {
		return startTLS;
	}


	public void setStartTLS(boolean startTLS) {
		this.startTLS = startTLS;
	}


	public boolean isSsl() {
		return ssl;
	}


	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}


	public boolean isActive() {
		return active;
	}


	public void setActive(boolean active) {
		this.active = active;
	}

	
	
	
	
	
	
}
