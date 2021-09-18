package com.planetbiru.api;


import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.buzzer.Buzzer;
import com.planetbiru.config.ConfigBlocking;
import com.planetbiru.config.ConfigGeneral;
import com.planetbiru.constant.ConstantString;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.constant.ResponseCode;
import com.planetbiru.gsm.GSMException;
import com.planetbiru.gsm.GSMUtil;
import com.planetbiru.mail.MailUtil;
import com.planetbiru.mail.NoEmailAccountException;

public class MessageAPI {

	private static Logger logger = Logger.getLogger(MessageAPI.class);
	public JSONObject processRequest(String requestBody) {
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2]; 
		return this.processRequest(requestBody, "", ste);	
	}
	public JSONObject processRequest(String requestBody, String topic)
	{
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2]; 
		return this.processRequest(requestBody, topic, ste);	
	}
	public JSONObject processRequest(String requestBody, String topic, StackTraceElement ste) {
		JSONObject requestJSON = new JSONObject();
		JSONObject responseJSON = new JSONObject();
		try
		{
			requestJSON = new JSONObject(requestBody);
			String command = requestJSON.optString(JsonKey.COMMAND, "");
			JSONObject data = requestJSON.optJSONObject(JsonKey.DATA);
			if(data != null)
			{
				logger.info("Topic     : " + topic);
				logger.info("Message   : " + requestJSON.toString(4));
				logger.info("Command   : " + command);
				logger.info("Date Time : " + data.optLong(JsonKey.DATE_TIME, 0));
				if(command.equals(ConstantString.SEND_SMS))
				{
					logger.info("Send SMS");
					responseJSON = this.sendSMS(command, data, ste);		
				}
				else if(command.equals(ConstantString.SEND_MAIL))
				{
					logger.info("Send Email");
					responseJSON = this.sendEmail(command, data, ste);					
				}
				else if(command.equals(ConstantString.SEND_MESSAGE))
				{
					logger.info("Send Message");
					responseJSON = this.sendMessage(command, data, ste);					
				}
				else if(command.equals(ConstantString.BLOCK_MSISDN))
				{
					logger.info("Block Number");
					responseJSON = this.blockMSISDN(command, data);					
				}
				else if(command.equals(ConstantString.UNBLOCK_MSISDN))
				{
					logger.info("Unblock Number");
					responseJSON = this.unblockMSISDN(command, data);					
				}
				else if(command.equals(ConstantString.CREATE_OTP))
				{
					logger.info("Create OTP");
					responseJSON = this.createOTP(command, data, ste);					
				}
				else if(command.equals(ConstantString.VALIDATE_OTP))
				{
					logger.info("Create OTP");
					responseJSON = this.validateOTP(command, data, ste);					
				}
			}		
		}
		catch(JSONException | GSMException e)
		{
			logger.error(e.getMessage(), e);
		}
		return responseJSON;
	}
	
	private JSONObject createOTP(String command, JSONObject data, StackTraceElement ste) {
		String dateTime = data.optString("date_time", "").trim();
		String otpID = data.optString("reference", "").trim();
		String receiver = data.optString("receiver", "").trim();
		String subject = data.optString("subject", "").trim();
		String param1 = data.optString("param1", "").trim();
		String param2 = data.optString("param2", "").trim();
		String param3 = data.optString("param3", "").trim();
		String param4 = data.optString("param4", "").trim();
		String messageFormat = data.optString("message", "").trim();
		long lifeTime = (data.optLong("expiration", 0) * 1000) - System.currentTimeMillis();
		String responseCode = ResponseCode.SUCCESS;
		JSONObject requestJSON = new JSONObject();
		JSONObject responseData = new JSONObject();
		try
		{
			if(OTP.isExists(otpID))
			{
				responseCode = ResponseCode.FAILED;
			}
			else
			{
				String clearOTP = OTP.createOTP(otpID, receiver, lifeTime, param1, param2, param3, param4);
				String message = String.format(messageFormat, clearOTP);
				
				if(receiver.contains("@"))
				{
					this.sendMail(receiver, subject, message, ste);
				}
				else
				{
					GSMUtil.sendSMS(receiver, message, ste);
				}
				responseData.put("reference", otpID);
				responseData.put("receiver", receiver);
				responseData.put("date_time", dateTime);
			}
		}
		catch(MessagingException | NoEmailAccountException | GSMException e)
		{
			responseCode = ResponseCode.FAILED;
		}
		requestJSON.put(JsonKey.COMMAND, command);
		requestJSON.put(JsonKey.DATA, responseData);
		requestJSON.put(JsonKey.RESPONSE_CODE, responseCode);
		return requestJSON;
	}
	
	private JSONObject validateOTP(String command, JSONObject data, StackTraceElement ste) {
		String dateTime = data.optString("date_time", "").trim();
		String otpID = data.optString("reference", "").trim();
		String receiver = data.optString("receiver", "").trim();
		String param1 = data.optString("param1", "").trim();
		String param2 = data.optString("param2", "").trim();
		String param3 = data.optString("param3", "").trim();
		String param4 = data.optString("param4", "").trim();
		String clearOTP = data.optString("otp", "");
		long lifeTime = (data.optLong("expiration", 0) * 1000) - System.currentTimeMillis();
		String responseCode = ResponseCode.SUCCESS;
		JSONObject requestJSON = new JSONObject();
		JSONObject responseData = new JSONObject();
		if(OTP.isExists(otpID))
		{
			responseCode = ResponseCode.FAILED;
		}
		else
		{
			boolean valid = OTP.validateOTP(otpID, receiver, lifeTime, param1, param2, param3, param4, clearOTP);
			responseData.put("reference", otpID);
			responseData.put("receiver", receiver);
			responseData.put("date_time", dateTime);
			if(valid)
			{
				responseCode = ResponseCode.SUCCESS;
			}
			else
			{
				responseCode = ResponseCode.FAILED;
			}
		}
		requestJSON.put(JsonKey.COMMAND, command);
		requestJSON.put(JsonKey.DATA, responseData);
		requestJSON.put(JsonKey.RESPONSE_CODE, responseCode);
		return requestJSON;
	}
	
	public JSONObject processEmailRequest(String requestBody, StackTraceElement ste) 
	{
		JSONObject requestJSON = new JSONObject();
		try
		{
			requestJSON = new JSONObject(requestBody);
			String command = requestJSON.optString(JsonKey.COMMAND, "");
			if(command.equals(ConstantString.SEND_EMAIL))
			{
				JSONArray data = requestJSON.optJSONArray(JsonKey.DATA);
				if(data != null && !data.isEmpty())
				{
					int length = data.length();
					int i;
					for(i = 0; i<length; i++)
					{
						this.sendMail(command, data.getJSONObject(i), ste);					
					}
				}
			}
		}
		catch(JSONException e)
		{
			/**
			 * Do nothing
			 */
			logger.error(e.getMessage(), e);
		}
		return requestJSON;
	}
	
	private JSONObject sendMessage(String command, JSONObject data, StackTraceElement ste) {
		JSONObject responseJSON = new JSONObject();
		JSONObject jsonData = new JSONObject();
		if(data != null)
		{
			String receiver = data.optString(JsonKey.RECEIVER, "");
			if(receiver.contains("@"))
			{
				this.sendMail(command, data, ste);
				responseJSON.put(JsonKey.RESPONSE_CODE, ResponseCode.SUCCESS);
			}
			else
			{
				jsonData = this.sendSMS(command, data, ste);
				responseJSON.put(JsonKey.RESPONSE_CODE, ResponseCode.SUCCESS);					
			}
		}
		responseJSON.put(JsonKey.COMMAND, command);
		responseJSON.put(JsonKey.DATA, jsonData);
		return responseJSON;		
	}
	
	public void sendMail(String command, JSONObject data, StackTraceElement ste) 
	{
		if(data != null)
		{
			String receiver = data.optString(JsonKey.RECEIVER, "");
			String textMessage = data.optString(JsonKey.MESSAGE, "");
			String subject = data.optString(JsonKey.SUBJECT, "");
			try 
			{
				this.sendMail(receiver, subject, textMessage, null);
			} 
			catch (MessagingException | NoEmailAccountException e) 
			{
				/**
				 * Do nothing
				 */
				logger.error(e.getMessage(), e);
			}
		}	
	}

	public JSONObject sendSMS(String command, JSONObject data, StackTraceElement ste)
	{
		JSONObject responseJSON = new JSONObject();
		JSONObject jsonData = new JSONObject();
		if(data != null)
		{
			String receiver = data.optString(JsonKey.RECEIVER, "");
			String textMessage = data.optString(JsonKey.MESSAGE, "");
			boolean dropOTPExpire = ConfigGeneral.isDropExpireOTP();
			boolean sendOTP = false;
			if(dropOTPExpire)
			{
				long currentTime = System.currentTimeMillis();
				long expiration = (data.optLong(JsonKey.DATE_TIME, 0) * 1000) + ConfigGeneral.getOtpExpirationOffset();
				if(currentTime < expiration)
				{
					sendOTP = true;
				}
			}
			else
			{
				sendOTP = true;
			}
			if(sendOTP)
			{
				try 
				{
					jsonData = GSMUtil.sendSMS(receiver, textMessage, ste);
					responseJSON.put(JsonKey.RESPONSE_CODE, ResponseCode.SUCCESS);			
				} 
				catch (GSMException e) 
				{
					Buzzer.toneSMSFailed();
					responseJSON.put(JsonKey.RESPONSE_CODE, ResponseCode.NO_DEVICE_CONNECTED);
					responseJSON.put(JsonKey.ERROR, e.getMessage());
				}
			}
		}
		responseJSON.put(JsonKey.COMMAND, command);
		responseJSON.put(JsonKey.DATA, jsonData);
		return responseJSON;		
	}
	private JSONObject sendEmail(String command, JSONObject data, StackTraceElement ste) {
		JSONObject responseJSON = new JSONObject();
		String to = data.optString(JsonKey.RECEIVER, "");
		String subject = data.optString("subject", "");
		String message = data.optString("message", "");
		String result = "";
		boolean dropOTPExpire = ConfigGeneral.isDropExpireOTP();
		boolean sendOTP = false;
		if(dropOTPExpire)
		{
			long currentTime = System.currentTimeMillis();
			long expiration = (data.optLong(JsonKey.DATE_TIME, 0) * 1000) + ConfigGeneral.getOtpExpirationOffset();
			if(currentTime < expiration)
			{
				sendOTP = true;
			}
		}
		else
		{
			sendOTP = true;
		}
		if(sendOTP)
		{
			try 
			{
				this.sendMail(to, subject, message, null);
				result = "The message was sent successfuly";
				responseJSON.put(JsonKey.RESPONSE_CODE, ResponseCode.SUCCESS);
				responseJSON.put(JsonKey.MESSAGE, result);
			} 
			catch (MessagingException | NoEmailAccountException e) 
			{
				result = e.getMessage();
				responseJSON.put(JsonKey.RESPONSE_CODE, ResponseCode.FAILED);
				responseJSON.put(JsonKey.MESSAGE, result);
			}	
		}
		responseJSON.put(JsonKey.COMMAND, command);
		return responseJSON;	
	}
	
	private void sendMail(String to, String subject, String message, StackTraceElement ste) throws MessagingException, NoEmailAccountException {
		MailUtil.send(to, subject, message, ste);
		
	}
	public JSONObject blockMSISDN(String command, JSONObject data) throws GSMException {
		return this.blockMSISDN(command, data.optString(JsonKey.RECEIVER, ""));	
	}
	
	public JSONObject unblockMSISDN(String command, JSONObject data) throws GSMException {
		return this.unblockMSISDN(command, data.optString(JsonKey.RECEIVER, ""));	
	}
	
	public JSONObject blockMSISDN(String command, String msisdn) throws GSMException {
		ConfigBlocking.block(msisdn);
		ConfigBlocking.save();
		JSONObject responseJSON = new JSONObject();
		responseJSON.put(JsonKey.COMMAND, command);
		responseJSON.put(JsonKey.RESPONSE_CODE, ResponseCode.SUCCESS);
		return responseJSON;	
	}
	
	public JSONObject unblockMSISDN(String command, String msisdn) throws GSMException {
		ConfigBlocking.unblock(msisdn);
		ConfigBlocking.save();
		JSONObject responseJSON = new JSONObject();
		responseJSON.put(JsonKey.COMMAND, command);
		responseJSON.put(JsonKey.RESPONSE_CODE, ResponseCode.SUCCESS);
		return responseJSON;		
	}

	
}
