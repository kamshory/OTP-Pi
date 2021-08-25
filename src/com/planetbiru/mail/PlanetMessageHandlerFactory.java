package com.planetbiru.mail;

import org.apache.log4j.Logger;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.RejectException;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.util.Properties;

public class PlanetMessageHandlerFactory implements MessageHandlerFactory
{

	private static Logger logger = Logger.getLogger(PlanetMessageHandlerFactory.class);
	
	@Override
    public MessageHandler create(MessageContext messageContext)
    {
        return new PlanetMailHandler(messageContext);
    }
	

    class PlanetMailHandler implements MessageHandler
    {
        MessageContext context;
        PlanetMail email;

        /**
         * Constructor
         * @param context MessageContext
         */
        public PlanetMailHandler(MessageContext context)
        {
            this.context = context;
            this.email = new PlanetMail();
            this.email.setId(System.currentTimeMillis());
        }

        /**
         * Called first, after the MAIL FROM during a SMTP exchange.
         * @param from String
         * @throws RejectException
         */
        @Override
        public void from(String from) throws RejectException
        {
            this.email.setFrom(from);
        }

        /**
         * Called once for every RCPT TO during a SMTP exchange.
         * This will occur after a from() call.
         * @param recipient String
         * @throws RejectException
         */
        @Override
        public void recipient(String recipient) throws RejectException
        {
            this.email.setTo(recipient);
        }

        /**
         * Called when the DATA part of the SMTP exchange begins.
         * @param data InputStream
         * @throws RejectException
         * @throws IOException
         */
        @Override
        public void data(InputStream data) throws RejectException, IOException
        {
            String rawMail = this.convertStreamToString(data);
            email.setRawMail(rawMail);

            Session session = Session.getDefaultInstance(new Properties());
            InputStream is = new ByteArrayInputStream(rawMail.getBytes());

            try
            {
                MimeMessage message = new MimeMessage(session, is);
                email.setSubject(message.getSubject());
                email.setMimeMessage(message);

                Object messageContent = message.getContent();
                if(messageContent instanceof Multipart)
                {
                    Multipart multipart = (Multipart) messageContent;
                    for (int i = 0; i < multipart.getCount(); i++)
                    {
                        BodyPart bodyPart = multipart.getBodyPart(i);
                        String contentType = bodyPart.getContentType();
                        if(contentType.matches("text/plain.*"))
                        {
                            email.setBody(convertStreamToString(bodyPart.getInputStream()));
                        }
                        else if(contentType.matches("text/html.*"))
                        {
                            email.setBodyHtml(convertStreamToString(bodyPart.getInputStream()));
                        }
                    }
                }
                else if(messageContent instanceof InputStream)
                {
                    InputStream mailContent = (InputStream) messageContent;
                    email.setBody(convertStreamToString(mailContent));
                }
                else if(messageContent instanceof String)
                {
                    String contentType = message.getContentType();
                    if(contentType.matches("text/plain.*"))
                    {
                        email.setBody(messageContent.toString());
                    }
                    else if(contentType.matches("text/html.*"))
                    {
                        email.setBodyHtml(messageContent.toString());
                    }
                }
            }
            catch (MessagingException e)
            {
                /**
                 * Do nothing
                 */
            }
        }

        @Override
        public void done()
        {
            // set the received date
        	
            email.setReceivedTime(System.currentTimeMillis());
           
        }

        /**
         * Converts given input stream to String
         * @param is InputStream
         * @return String
         */
        protected String convertStreamToString(InputStream is)
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder stringBuilder = new StringBuilder();

            String line;
            try
            {
                while ((line = reader.readLine()) != null)
                {
                    stringBuilder.append(line);
                    stringBuilder.append("\n");
                }
            }
            catch (IOException e)
            {
                logger.error(e.getMessage(), e);
            }

            return stringBuilder.toString();
        }
    }
}
