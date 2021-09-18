package com.planetbiru.api;

import java.util.Random;

import org.json.JSONObject;

import com.planetbiru.config.Config;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.util.Utility;

public class OTP {
	private static Random rand = new Random(); 
	private static JSONObject data = new JSONObject();
	public static boolean isExists(String otpID) {
		return OTP.data.has(otpID);
	}

	public static String createOTP(String otpID, String receiver, long lifeTime, String param1, String param2,
			String param3, String param4) {
		int length = Config.getOtpLength();
		String fmt = "%0"+length+"d";
		String plainOTP = String.format(fmt, OTP.generateRandomNumber(length));
		if(plainOTP.length() > length)
		{
			plainOTP = plainOTP.substring(0, length);
		}
		JSONObject record = new JSONObject();
		long expiration = System.currentTimeMillis() + (lifeTime * 1000);		
		String hash = OTP.createHash(otpID, plainOTP, receiver, param1, param2, param3, param4);
		record.put(JsonKey.HASH, hash);
		record.put(JsonKey.EXPIRATION, expiration);
		OTP.data.put(otpID, record);		
		return plainOTP;
	}

	private static int generateRandomNumber(int length) {
		int max = (int) (Math.pow(10, length));
		int randomInt = rand.nextInt(max);
		return randomInt;
	}

	private static String createHash(String otpID, String plainOTP, String receiver, String param1, String param2,
			String param3, String param4) {
		return Utility.sha256(otpID+":"+plainOTP+":"+receiver+":"+param1+":"+param2+":"+param3+":"+param4+":"+Config.getOtpSalt());
	}

	public static boolean validateOTP(String otpID, String receiver, long lifeTime, String param1, String param2,
			String param3, String param4, String plainOTP) {
		String hash = OTP.createHash(otpID, plainOTP, receiver, param1, param2, param3, param4);
		if(OTP.isExists(otpID))
		{
			JSONObject otp = OTP.data.optJSONObject(otpID);
			if(otp != null && otp.optString("hash", "").equals(hash))
			{
				return true;
			}
		}
		return false;
	}

}
