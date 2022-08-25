package com.planetbiru.api;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.buzzer.Buzzer;
import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigBlocking;
import com.planetbiru.config.ConfigGeneral;
import com.planetbiru.config.ConfigModem;
import com.planetbiru.constant.ConstantString;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.constant.ResponseCode;
import com.planetbiru.gsm.GSMException;
import com.planetbiru.gsm.GSMUtil;
import com.planetbiru.gsm.InvalidSIMPinException;
import com.planetbiru.gsm.SerialPortConnectionException;
import com.planetbiru.gsm.USSDParser;
import com.planetbiru.mail.MailUtil;
import com.planetbiru.mail.NoEmailAccountException;

public class MessageAPI {

	private static Logger logger = Logger.getLogger(MessageAPI.class);
	
	public JSONObject processRequest(String requestBody) {
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2]; 
		return this.processRequest(requestBody, "", ste);	
	}
	
	public JSONObject processRequest(String requestBody, String topic) {
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
				logger.info("\r\n");
				logger.info("Topic     : " + topic);
				logger.info("Message   : " + requestJSON.toString(0));
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
				else if(command.equals(ConstantString.REQUEST_OTP))
				{
					logger.info("Request OTP");
					responseJSON = this.requestOTP(command, data, ste);					
				}
				else if(command.equals(ConstantString.VERIFY_OTP))
				{
					logger.info("Verify OTP");
					responseJSON = this.verifyOTP(command, data);					
				}
				else if(command.equals(ConstantString.REQUEST_USSD))
				{
					logger.info("Request USSD");
					responseJSON = this.requestUSSD(command, data);					
				}
				else if(command.equals(ConstantString.GET_MODEM_LIST))
				{
					logger.info("Get Modem List");
					responseJSON = this.getModemList(command, data);
				}
				else if(command.equals(ConstantString.ECHO))
				{
					responseJSON = new JSONObject(requestBody);
				}
			}		
		}
		catch(JSONException | GSMException | SerialPortConnectionException e)
		{
			logger.error(e.getMessage());
		}
		return responseJSON;
	}
	
	private JSONObject getModemList(String command, JSONObject data) {
		JSONObject responseJSON = new JSONObject();
		JSONObject jsonData = new JSONObject();
		String responseCode = ResponseCode.FAILED;
		if(data != null)
		{
			ConfigModem.load(Config.getModemSettingPath());
			JSONObject list = ConfigModem.getStatus(true);
			jsonData.put(JsonKey.MODEM_LIST, list);
			responseCode = ResponseCode.SUCCESS;
		}	
		responseJSON.put(JsonKey.COMMAND, command);
		responseJSON.put(JsonKey.DATA, jsonData);
		responseJSON.put(JsonKey.RESPONSE_CODE, responseCode);
		return responseJSON;
	}

	private JSONObject requestUSSD(String command, JSONObject data) {
		JSONObject responseJSON = new JSONObject();
		JSONObject jsonData = new JSONObject();
		String responseCode = ResponseCode.FAILED;
		if(data != null)
		{
			String ussdCode = data.optString(JsonKey.USSD_CODE, "");
			String modemID = data.optString(JsonKey.MODEM_ID, "");
			try 
			{
				USSDParser ussdParser = this.requestUSSD(ussdCode, modemID);
				jsonData.put(JsonKey.USSD_CONTENT, ussdParser.getContent());
				jsonData.put(JsonKey.USSD_CONTENT_RAW, ussdParser.getContentRaw());
				jsonData.put(JsonKey.REPLYABLE, ussdParser.isReplyable());
				responseCode = ResponseCode.SUCCESS;
			} 
			catch (GSMException e) 
			{
				/**
				 * Do nothing
				 */
				logger.error(e.getMessage());
			}
		}	
		responseJSON.put(JsonKey.COMMAND, command);
		responseJSON.put(JsonKey.DATA, jsonData);
		responseJSON.put(JsonKey.RESPONSE_CODE, responseCode);
		return responseJSON;
	}

	private USSDParser requestUSSD(String ussdCode, String modemID) throws GSMException {
		return GSMUtil.executeUSSD(ussdCode, modemID);	
	}

	private JSONObject requestOTP(String command, JSONObject data, StackTraceElement ste) throws SerialPortConnectionException {
		String dateTime = data.optString(JsonKey.DATE_TIME, "").trim();
		String otpID = data.optString(JsonKey.REFERENCE, "").trim();
		String receiver = data.optString(JsonKey.RECEIVER, "").trim();
		String subject = data.optString(JsonKey.SUBJECT, "").trim();
		String param1 = data.optString(JsonKey.PARAM1, "").trim();
		String param2 = data.optString(JsonKey.PARAM2, "").trim();
		String param3 = data.optString(JsonKey.PARAM3, "").trim();
		String param4 = data.optString(JsonKey.PARAM4, "").trim();
		String messageFormat = data.optString(JsonKey.MESSAGE, "").trim();
		long expiration = data.optLong(JsonKey.EXPIRATION, 0) * 1000; 
		long lifeTime = expiration - System.currentTimeMillis();
		String responseCode = ResponseCode.SUCCESS;
		JSONObject requestJSON = new JSONObject();
		JSONObject responseData = new JSONObject();
		try
		{
			if(OTP.isExists(otpID))
			{
				responseCode = ResponseCode.DUPLICATED;
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
				responseData.put(JsonKey.REFERENCE, otpID);
				responseData.put(JsonKey.RECEIVER, receiver);
				responseData.put(JsonKey.DATE_TIME, dateTime);
			}
		}
		catch(MessagingException | NoEmailAccountException | GSMException | InvalidSIMPinException e)
		{
			responseCode = ResponseCode.FAILED;
		}
		requestJSON.put(JsonKey.COMMAND, command);
		requestJSON.put(JsonKey.DATA, responseData);
		requestJSON.put(JsonKey.RESPONSE_CODE, responseCode);
		return requestJSON;
	}
	
	private JSONObject verifyOTP(String command, JSONObject data) {
		String dateTime = data.optString(JsonKey.DATE_TIME, "").trim();
		String otpID = data.optString(JsonKey.REFERENCE, "").trim();
		String receiver = data.optString(JsonKey.RECEIVER, "").trim();
		String param1 = data.optString(JsonKey.PARAM1, "").trim();
		String param2 = data.optString(JsonKey.PARAM2, "").trim();
		String param3 = data.optString(JsonKey.PARAM3, "").trim();
		String param4 = data.optString(JsonKey.PARAM4, "").trim();
		String clearOTP = data.optString("otp", "");
		String responseCode;
		JSONObject requestJSON = new JSONObject();
		JSONObject responseData = new JSONObject();		
		responseData.put(JsonKey.REFERENCE, otpID);
		responseData.put(JsonKey.RECEIVER, receiver);
		responseData.put(JsonKey.DATE_TIME, dateTime);
		boolean valid = false;
		try 
		{
			valid = OTP.validateOTP(otpID, receiver, param1, param2, param3, param4, clearOTP);
			if(valid)
			{
				responseCode = ResponseCode.SUCCESS;
			}
			else
			{
				responseCode = ResponseCode.INVALID_OTP;
			}
		} 
		catch (OTPExpireException e) 
		{
			responseCode = ResponseCode.EXPIRED;
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
			logger.error(e.getMessage());
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
				responseJSON = this.sendMail(command, data, ste);
				responseJSON.put(JsonKey.RESPONSE_CODE, ResponseCode.SUCCESS);
			}
			else
			{
				responseJSON = this.sendSMS(command, data, ste);
				responseJSON.put(JsonKey.RESPONSE_CODE, ResponseCode.SUCCESS);					
			}
		}
		responseJSON.put(JsonKey.COMMAND, command);
		responseJSON.put(JsonKey.DATA, jsonData);
		return responseJSON;		
	}
	
	public JSONObject sendMail(String command, JSONObject data, StackTraceElement ste) //NOSONAR
	{
		JSONObject responseJSON = new JSONObject();
		JSONObject jsonData = new JSONObject();
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
				logger.error(e.getMessage());
			}
		}	
		responseJSON.put(JsonKey.COMMAND, command);
		responseJSON.put(JsonKey.DATA, jsonData);
		return responseJSON;		
	}
	
	public boolean isExpire(JSONObject data)
	{
		boolean dropOTPExpire = ConfigGeneral.isDropExpireOTP();
		if(dropOTPExpire)
		{
			long currentTime = System.currentTimeMillis();
			long expiration = data.has(JsonKey.EXPIRATION) 
					? data.optLong(JsonKey.EXPIRATION, 0) 
					: ((data.optLong(JsonKey.DATE_TIME, 0) * 1000) + ConfigGeneral.getOtpExpirationOffset());
			return (currentTime > expiration);
		}
		return false;
	}

	public JSONObject sendSMS(String command, JSONObject data, StackTraceElement ste)
	{
		JSONObject responseJSON = new JSONObject();
		JSONObject jsonData = new JSONObject();
		if(data != null)
		{
			String receiver = data.optString(JsonKey.RECEIVER, "");
			String textMessage = data.optString(JsonKey.MESSAGE, "");		
			if(!this.isExpire(data))
			{
				try 
				{
					jsonData = GSMUtil.sendSMS(receiver, textMessage, ste);
					responseJSON.put(JsonKey.RESPONSE_CODE, ResponseCode.SUCCESS);			
				} 
				catch (GSMException | InvalidSIMPinException | SerialPortConnectionException e) 
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
	private JSONObject sendEmail(String command, JSONObject data, StackTraceElement ste) //NOSONAR
	{
		JSONObject responseJSON = new JSONObject();
		String to = data.optString(JsonKey.RECEIVER, "");
		String subject = data.optString(JsonKey.SUBJECT, "");
		String message = data.optString(JsonKey.MESSAGE, "");
		String result = "";
		if(!this.isExpire(data))
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
