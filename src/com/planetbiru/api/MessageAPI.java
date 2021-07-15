package com.planetbiru.api;

import javax.mail.MessagingException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.config.ConfigBlocking;
import com.planetbiru.constant.ConstantString;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.constant.ResponseCode;
import com.planetbiru.gsm.GSMException;
import com.planetbiru.gsm.GSMUtil;
import com.planetbiru.mail.MailUtil;
import com.planetbiru.mail.NoEmailAccountException;

public class MessageAPI {

	public JSONObject processRequest(String requestBody) {
		JSONObject requestJSON = new JSONObject();
		JSONObject responseJSON = new JSONObject();
		try
		{
			requestJSON = new JSONObject(requestBody);
			String command = requestJSON.optString(JsonKey.COMMAND, "");
			JSONObject data = requestJSON.optJSONObject(JsonKey.DATA);
			if(data != null)
			{
				if(command.equals(ConstantString.SEND_SMS))
				{
					responseJSON = this.sendSMS(command, data);		
				}
				else if(command.equals(ConstantString.SEND_MAIL))
				{
					responseJSON = this.sendEmail(command, data);					
				}
				else if(command.equals(ConstantString.SEND_MESSAGE))
				{
					responseJSON = this.sendMessage(command, data);					
				}
				else if(command.equals(ConstantString.BLOCK_MSISDN))
				{
					responseJSON = this.blockMSISDN(command, data.optString(JsonKey.RECEIVER, ""));					
				}
				else if(command.equals(ConstantString.UNBLOCK_MSISDN))
				{
					responseJSON = this.unblockMSISDN(command, data.optString(JsonKey.RECEIVER, ""));					
				}
			}
			
		}
		catch(JSONException | GSMException e)
		{
			/**
			 * Do nothing
			 */
		}
		return responseJSON;
	}
	
	public JSONObject processEmailRequest(String requestBody) 
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
						this.sendMail(data.getJSONObject(i));					
					}
				}
			}
		}
		catch(JSONException e)
		{
			/**
			 * Do nothing
			 */
		}
		return requestJSON;
	}
	
	private JSONObject sendMessage(String command, JSONObject data) {
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];      
		JSONObject responseJSON = new JSONObject();
		JSONObject jsonData = new JSONObject();
		if(data != null)
		{
			String receiver = data.optString(JsonKey.RECEIVER, "");
			String subject = data.optString(JsonKey.SUBJECT, "");
			String textMessage = data.optString(JsonKey.MESSAGE, "");
			try 
			{
				if(receiver.contains("@"))
				{
					MailUtil.send(receiver, subject, textMessage);
					responseJSON.put(JsonKey.RESPONSE_CODE, ResponseCode.SUCCESS);
				}
				else
				{
					jsonData = GSMUtil.sendSMS(receiver, textMessage, ste);
					responseJSON.put(JsonKey.RESPONSE_CODE, ResponseCode.SUCCESS);					
				}
				
			} 
			catch(MessagingException | NoEmailAccountException e)
			{
				e.printStackTrace();
				/**
				 * Do nothing
				 */
				responseJSON.put(JsonKey.RESPONSE_CODE, ResponseCode.FAILED);
				responseJSON.put("error", e.getMessage());				
			}
			catch (GSMException e) 
			{
				e.printStackTrace();
				/**
				 * Do nothing
				 */
				responseJSON.put(JsonKey.RESPONSE_CODE, ResponseCode.NO_DEVICE_CONNECTED);
				responseJSON.put("error", e.getMessage());
			}
		}
		responseJSON.put(JsonKey.COMMAND, command);
		responseJSON.put(JsonKey.DATA, jsonData);
		return responseJSON;		
	}
	
	public void sendMail(JSONObject data) 
	{
		if(data != null)
		{
			String receiver = data.optString(JsonKey.RECEIVER, "");
			String textMessage = data.optString(JsonKey.MESSAGE, "");
			String subject = data.optString(JsonKey.SUBJECT, "");
			try 
			{
				MailUtil.send(receiver, subject, textMessage);
			} 
			catch (MessagingException | NoEmailAccountException e) 
			{
				/**
				 * Do nothing
				 */
			}
		}	
	}

	public JSONObject sendSMS(String command, JSONObject data)
	{
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];      
		JSONObject responseJSON = new JSONObject();
		JSONObject jsonData = new JSONObject();
		if(data != null)
		{
			String receiver = data.optString(JsonKey.RECEIVER, "");
			String textMessage = data.optString(JsonKey.MESSAGE, "");
			try 
			{
				jsonData = GSMUtil.sendSMS(receiver, textMessage, ste);
				responseJSON.put(JsonKey.RESPONSE_CODE, ResponseCode.SUCCESS);
				
			} 
			catch (GSMException e) 
			{
				e.printStackTrace();
				/**
				 * Do nothing
				 */
				responseJSON.put(JsonKey.RESPONSE_CODE, ResponseCode.NO_DEVICE_CONNECTED);
				responseJSON.put("error", e.getMessage());
			}
		}
		responseJSON.put(JsonKey.COMMAND, command);
		responseJSON.put(JsonKey.DATA, jsonData);
		return responseJSON;		
	}
	private JSONObject sendEmail(String command, JSONObject data) {
		JSONObject responseJSON = new JSONObject();
		String to = data.optString("recipient", "");
		String subject = data.optString("subject", "");
		String message = data.optString("message", "");
		String result = "";
		try 
		{
			MailUtil.send(to, subject, message);
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
		responseJSON.put(JsonKey.COMMAND, command);
		return responseJSON;	
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
