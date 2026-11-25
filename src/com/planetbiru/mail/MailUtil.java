package com.planetbiru.mail;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;

import com.planetbiru.config.ConfigEmail;
import com.planetbiru.config.DataEmail;
import com.planetbiru.util.SQLiteDataStore;

public class MailUtil {
	private static int counter = -1;
	private static List<Integer> activeAccounts = new ArrayList<>();
	private static Logger logger = Logger.getLogger(MailUtil.class);
	private MailUtil()
	{
		
	}
	
	public static void send(String receiver, String subject, String message, StackTraceElement ste, String id) throws MessagingException, NoEmailAccountException {
		if(id == null || id.isEmpty())
		{
			MailUtil.send(receiver, subject, message, ste);
			return;
		}
		if(MailUtil.activeAccounts.isEmpty())
		{
			NoEmailAccountException e = new NoEmailAccountException("No email account active");
			MailUtil.recordEmail("", "", receiver, subject, message, "failed", e.getClass().getSimpleName());
			throw e;
		}
		DataEmail mailer = ConfigEmail.getAccount(id);
		if(mailer == null)
		{
			NoEmailAccountException e = new NoEmailAccountException("Email account not found");
			MailUtil.recordEmail(id, "", receiver, subject, message, "failed", "ACCOUNT_NOT_FOUND");
			throw e;
		}
		MailUtil.send(mailer, receiver, subject, message, ste);
	}
	
	public static void send(String receiver, String subject, String message, StackTraceElement ste) throws MessagingException, NoEmailAccountException 
	{
		if(MailUtil.activeAccounts.isEmpty())
		{
			NoEmailAccountException e = new NoEmailAccountException("No email account active");
			MailUtil.recordEmail("", "", receiver, subject, message, "failed", e.getClass().getSimpleName());
			throw e;
		}
		int index = MailUtil.getIndex();
		DataEmail mailer = ConfigEmail.getAccounts().get(index);
		MailUtil.send(mailer, receiver, subject, message, ste);
	}

	private static void send(DataEmail mailer, String receiver, String subject, String message,
			StackTraceElement ste) throws MessagingException {
		long createdAt = System.currentTimeMillis();
		try
		{
			boolean sent = mailer.send(receiver, subject, message, ste);
			MailUtil.recordEmail(mailer.getId(), mailer.getSenderAddress(), receiver, subject, message,
					sent ? "sent" : "failed", sent ? "" : "SEND_FAILED", createdAt);
		}
		catch(MessagingException e)
		{
			MailUtil.recordEmail(mailer.getId(), mailer.getSenderAddress(), receiver, subject, message,
					"failed", e.getClass().getSimpleName(), createdAt);
			throw e;
		}
	}

	private static void recordEmail(String accountID, String sender, String receiver, String subject,
			String message, String status, String errorType) {
		MailUtil.recordEmail(accountID, sender, receiver, subject, message, status, errorType,
				System.currentTimeMillis());
	}

	private static void recordEmail(String accountID, String sender, String receiver, String subject,
			String message, String status, String errorType, long createdAt) {
		try
		{
			SQLiteDataStore.logEmail(createdAt, accountID, sender, receiver,
					subject == null ? 0 : subject.length(), message == null ? 0 : message.length(),
					status, errorType);
		}
		catch(SQLException e)
		{
			logger.error("Unable to record email event in SQLite", e);
		}
	}

	private static int getIndex() {
		MailUtil.counter++;
		if(MailUtil.counter >= MailUtil.activeAccounts.size())
		{
			MailUtil.counter = 0;
		}
		return MailUtil.activeAccounts.get(MailUtil.counter).intValue();
	}

	private static void reindex() {
		List<Integer> activeAccounts = new ArrayList<>();
		for(int i = 0; i<ConfigEmail.getAccounts().size(); i++)
		{
			if(ConfigEmail.getAccounts().get(i).isActive())
			{
				activeAccounts.add(i);
			}
		}
		MailUtil.activeAccounts = activeAccounts;
	}
	
	public static void updateIndex() {
		MailUtil.reindex();	
	}
	
}
