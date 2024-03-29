package com.planetbiru.api;

import java.io.File;
import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Random;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.config.Config;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.Utility;

public class OTP {
	private static Random rand = new Random(); 
	private static JSONObject data = new JSONObject();
	private static String configPath = "";
	private static OTPGC otpGC;
	private static Logger logger = Logger.getLogger(OTP.class);

	/**
	 * Constructor
	 */
	private OTP()
	{
		
	}
	
	/**
	 * Initialize OTP service
	 * @param fileName
	 */
	public static void initialize(String fileName)
	{
		OTP.configPath = fileName;
		OTP.prepareDir(fileName);
		OTP.load(fileName);
		OTP.save();
		OTP.otpGC = new OTPGC();
	}
	
	/**
	 * Start OTP service
	 */
	public static void start()
	{
		OTP.otpGC.start();		
	}
	
	/**
	 * Garbage collection
	 */
	public static void gc()
	{
		Iterator<String> keys = OTP.data.keys();
		long currentTime = System.currentTimeMillis();
		try
		{
			while(keys.hasNext()) 
			{
			    String key = keys.next();
			    if(OTP.data.has(key) && OTP.data.get(key) instanceof JSONObject) 
			    {
			        JSONObject obj = OTP.data.optJSONObject(key);
			        if(obj.optLong(JsonKey.EXPIRATION, 0) < currentTime)
			        {
			        	OTP.data.remove(key);
			        }
			    }
			}
		}
		catch(ConcurrentModificationException e)
		{
			/**
			 * Do nothing
			 */
		}
	}
	
	/**
	 * Check if OTP of an OTP ID is exists
	 * @param otpID OTP ID
	 * @return
	 */
	public static boolean isExists(String otpID) {
		return OTP.data.has(otpID);
	}

	/**
	 * Create OTP
	 * @param otpID OTP ID
	 * @param receiver Receiver ID (email address or phone number)
	 * @param lifeTime OTP lifetime
	 * @param param1 Parameter 1
	 * @param param2 Parameter 2
	 * @param param3 Parameter 3
	 * @param param4 Parameter 4
	 * @return OTP message
	 */
	public static String createOTP(String otpID, String receiver, long lifeTime, String param1, String param2,
			String param3, String param4) {
		int length = Config.getOtpLength();
		String fmt = "%0"+length+"d";
		String plainOTP = String.format(fmt, OTP.generateRandomNumber(length));
		if(plainOTP.length() > length)
		{
			plainOTP = plainOTP.substring(0, length);
		}
		JSONObject otpRecord = new JSONObject();
		long expiration = System.currentTimeMillis() + lifeTime;	
		if(expiration < 0)
		{
			expiration = System.currentTimeMillis() + Config.getOtpLifetime();
		}
		String hash = OTP.createHash(otpID, plainOTP, receiver, param1, param2, param3, param4);
		otpRecord.put(JsonKey.HASH, hash);
		otpRecord.put(JsonKey.EXPIRATION, expiration);
		OTP.data.put(otpID, otpRecord);
		OTP.save();
		return plainOTP;
	}

	/**
	 * Generate random number
	 * @param length Random number length
	 * @return Random number
	 */
	private static int generateRandomNumber(int length) {
		int max = (int) (Math.pow(10, length));
		return rand.nextInt(max);
	}

	/**
	 * Create hash
	 * @param otpID OTP ID
	 * @param plainOTP Plain text
	 * @param receiver Receiver
	 * @param param1 Parameter 1
	 * @param param2 Parameter 2
	 * @param param3 Parameter 3
	 * @param param4 Parameter 4
	 * @return Hash
	 */
	private static String createHash(String otpID, String plainOTP, String receiver, String param1, String param2,
			String param3, String param4) {
		return Utility.sha512(otpID+":"+plainOTP+":"+receiver+":"+param1+":"+param2+":"+param3+":"+param4+":"+Config.getOtpSalt());
	}

	/**
	 * Validate OTP
	 * @param otpID OTP ID
	 * @param receiver Receiver
	 * @param param1 Parameter 1
	 * @param param2 Parameter 2
	 * @param param3 Parameter 3
	 * @param param4 Parameter 4
	 * @param plainOTP Plain OTP
	 * @return <strong>true</true> if OTP is valid and <strong>false</strong> if OTP is invalid
	 * @throws OTPExpireException if OTP expire
	 */
	public static boolean validateOTP(String otpID, String receiver, String param1, String param2,
			String param3, String param4, String plainOTP) throws OTPExpireException {
		String hash = OTP.createHash(otpID, plainOTP, receiver, param1, param2, param3, param4);
		if(OTP.isExists(otpID))
		{
			JSONObject otp = OTP.data.optJSONObject(otpID);
			if(otp == null || otp.optLong(JsonKey.EXPIRATION, 0) < System.currentTimeMillis())
			{
				throw new OTPExpireException("OTP has been expire");
			}
			if(otp.optString(JsonKey.HASH, "").equals(hash))
			{
				OTP.data.remove(otpID);
				OTP.save();
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Load file
	 * @param path File path
	 */
	public static void load(String path) {
		OTP.configPath = path;
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		fileName = FileConfigUtil.fixFileName(fileName);
		try 
		{
			byte[] data = FileConfigUtil.read(fileName);		
			if(data != null)
			{
				String text = new String(data);
				if(text != null && text.length() > 7)
				{
					JSONObject json = new JSONObject(text);
					OTP.data = json;
				}
			}
		} 
		catch (JSONException e) 
		{
			logger.error(e.getMessage(), e);
		}
		catch (FileNotFoundException e) 
		{
			if(Config.isLogConfigNotFound())
			{
				logger.error(e.getMessage(), e);
			}
		}		
	}
	
	/**
	 * Save configuration
	 */
	public static void save()
	{
		OTP.save(OTP.configPath);
	}
	
	/**
	 * Save to path
	 * @param path File path
	 */
	public static void save(String path) {
		JSONObject config = getJSONObject();
		save(path, config);
	}

	/**
	 * Save configuration to file
	 * @param path File path
	 * @param config Configuration
	 */
	public static void save(String path, JSONObject config) {		
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		fileName = FileConfigUtil.fixFileName(fileName);
		prepareDir(fileName);	
		try 
		{
			FileConfigUtil.write(fileName, config.toString().getBytes());
		}
		catch (IOException e) 
		{
			logger.error(e.getMessage(), e);
		}
	}
	
	/**
	 * Prepare directory before save a file
	 * @param fileName File path to be save after directory created
	 */
	private static void prepareDir(String fileName) {
		File file = new File(fileName);
		String directory1 = file.getParent();
		File file2 = new File(directory1);
		String directory2 = file2.getParent();
		
		File d1 = new File(directory1);
		File d2 = new File(directory2);		

		if(!d2.exists())
		{
			d2.mkdir();
		}
		if(!d1.exists())
		{
			d1.mkdir();
		}
		File file3 = new File(fileName);
		file3.getParentFile().mkdirs();
	}
	
	/**
	 * Get OTP data
	 * @return OTP data
	 */
	public static JSONObject getJSONObject() {
		return OTP.data;
	}

	/**
	 * Get OTP data
	 * @return OTP data
	 */
	public static JSONObject toJSONObject() {
		return getJSONObject();
	}

}
