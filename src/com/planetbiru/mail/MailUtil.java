package com.planetbiru.mail;

import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;

import com.planetbiru.config.ConfigEmail;
import com.planetbiru.config.DataEmail;

public class MailUtil {
	private static int counter = -1;
	private static List<Integer> activeAccounts = new ArrayList<>();
	private MailUtil()
	{
		
	}
	public static void send(String receiver, String subject, String message, String id) throws MessagingException, NoEmailAccountException {
		if(id == null || id.isEmpty())
		{
			MailUtil.send(receiver, subject, message);
		}
		if(MailUtil.activeAccounts.isEmpty())
		{
			throw new NoEmailAccountException("No email account active");
		}
		DataEmail mailer = ConfigEmail.getAccount(id);
		mailer.send(receiver, subject, message);
	}
	public static void send(String receiver, String subject, String message) throws MessagingException, NoEmailAccountException {
		if(MailUtil.activeAccounts.isEmpty())
		{
			throw new NoEmailAccountException("No email account active");
		}
		int index = MailUtil.getIndex();
		DataEmail mailer = ConfigEmail.getAccounts().get(index);
		mailer.send(receiver, subject, message);
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
