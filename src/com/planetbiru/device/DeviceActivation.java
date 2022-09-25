package com.planetbiru.device;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;

import com.planetbiru.util.CommandLineExecutor;
import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.OSUtil;
import com.planetbiru.util.ServerInfo;
import com.planetbiru.web.HttpUtil;

public class DeviceActivation {
	private static boolean activated = false;
	private static String secret = "kiIJujED";
	private static String algorithm = "AES/CBC/PKCS5Padding";
	private static String keyAlgorithm = "AES";
	private static String keyEnv = "CPUID";
	private static String defaultSalt = "IjYTWsrJ";
	
	private static Logger logger = Logger.getLogger(DeviceActivation.class);
	
	private DeviceActivation()
	{
		
	}
	
	private static String getSecret()
	{
		String sec = DeviceActivation.secret;
		if(sec.length() > 8)
		{
			sec = sec.substring(sec.length() - 8);
		}
		else
		{
			sec = String.format("%8s", sec);
		}
		return sec;
	}
	
	public static void verify()
	{
		String dataStr = System.getenv(keyEnv);
		if(dataStr != null && !dataStr.isEmpty()) 
		{
			String salt = ServerInfo.cpuSerialNumber();
			try 
			{
				verify(dataStr, salt);
			} 
			catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException
					| InvalidAlgorithmParameterException | BadPaddingException | IllegalBlockSizeException e) 
			{
				DeviceActivation.activated = false;
			}
		}
	}

	public static void verify(String dataStr, String salt) throws InvalidKeyException, 
		NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, 
		InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, IllegalArgumentException 
	{
		if(dataStr != null && !dataStr.isEmpty())
		{
			salt = DeviceActivation.fixSalt(salt);
			Map<String, Object> data = DeviceActivation.parseHmacCPUID(dataStr, salt);
			DeviceActivation.activated = data.getOrDefault("VR", "").equals("1");
		}
	}

	public static void storeToEnv(String activationCode) {
		if(OSUtil.isWindows())
		{
			DeviceActivation.storeToEnvWindows(activationCode);
		}
		else if(OSUtil.isLinux())
		{
			DeviceActivation.storeToEnvLinux(activationCode);
		}
		
	}

	public static void storeToEnvWindows(String activationCode) {
		String commandExport = "setx "+keyEnv+" \""+activationCode.replace("'", "\\'")+"\"";
		CommandLineExecutor.exec(commandExport);	
	}

	public static void storeToEnvLinux(String activationCode) {
		try 
		{
			String home = System.getenv("HOME");
			if(home == null)
			{
				home = "";
			}
			String path = FileConfigUtil.fixFileName(home + "/.bashrc");
			byte[] content = FileConfigUtil.read(path);
			String contentStr = new String(content);			
			String commandExport = "echo 'export "+keyEnv+"=\""+activationCode.replace("'", "\\'")+"\"' >> $HOME/.bashrc";
			logger.info(commandExport);
			if(!contentStr.contains("export "+keyEnv+"="))
			{
				CommandLineExecutor.exec(commandExport);	
				logger.info("Execute Command "+commandExport);
			}
		} 
		catch (FileNotFoundException e) 
		{
			logger.info(e.getMessage());
		}		
	}
	
	public static String activate(Map<String, Object> tlv, String salt) throws InvalidKeyException, 
		NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, 
		InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, IllegalArgumentException {
		salt = DeviceActivation.fixSalt(salt);
		String activationCode = DeviceActivation.buildHmacCPUID(tlv, salt); 	
		DeviceActivation.storeToEnv(activationCode);	
		DeviceActivation.activate(activationCode);
		return activationCode;		
	}

	public static void activate(String activationCode) {
		String commandEnv = "";
		if(OSUtil.isWindows())
		{
			commandEnv = "setx "+keyEnv+" \""+activationCode.replace("'", "\\'")+"\"";
			HttpUtil.broardcastWebSocket("Reboot your operating system", true, 2000);
		}
		else if(OSUtil.isLinux())
		{
			commandEnv = "export "+keyEnv+"=\""+activationCode.replace("'", "\\'")+"\"";
		}
		logger.info(commandEnv);
		CommandLineExecutor.exec(commandEnv);	
	}

	public static String fixSalt(String salt) {
		if(salt == null || salt.isEmpty())
		{
			return defaultSalt;
		}
		else
		{
			if(salt.length() > 8)
			{
				salt = salt.substring(salt.length() - 8);
			}
			else
			{
				salt = String.format("%8s", salt);
			}
		}
		return salt;
	}

	public static boolean isActivated() {
		return activated;
	}

	public static void setActivated(boolean activated) {
		DeviceActivation.activated = activated;
	}
	
	public static SecretKey getKeyFromPassword(String password, String salt)
	    throws NoSuchAlgorithmException, InvalidKeySpecException 
	{
	    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
	    KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
	    byte[] secureKey = factory.generateSecret(spec).getEncoded();
		return new SecretKeySpec(secureKey, "AES");
	}

	public static byte[] randomIv(String algorithm) throws NoSuchAlgorithmException, NoSuchPaddingException
	{
		SecureRandom random = SecureRandom.getInstanceStrong();
	    byte[] iv = new byte[Cipher.getInstance(algorithm).getBlockSize()];		
	    random.nextBytes(iv);
	    return iv;
	}
	
	private static IvParameterSpec generateIv(byte[] iv) {
	    return new IvParameterSpec(iv); //NOSONAR
	}

	private static String encrypt(String algorithm, String dataStr, String salt) throws NoSuchAlgorithmException, 
		InvalidKeySpecException, InvalidKeyException, NoSuchPaddingException, InvalidAlgorithmParameterException, 
		BadPaddingException, IllegalBlockSizeException, IllegalArgumentException 
	{
	    SecretKey key = DeviceActivation.getKeyFromPassword(DeviceActivation.getSecret(), salt);
	    byte[] iv = DeviceActivation.randomIv(DeviceActivation.keyAlgorithm);
	    return DeviceActivation.encrypt(algorithm, dataStr, key, iv);
	}

	public static String encrypt(String algorithm, String input, SecretKey key,
	    byte[] iv) throws NoSuchPaddingException, NoSuchAlgorithmException,
	    InvalidAlgorithmParameterException, InvalidKeyException,
	    BadPaddingException, IllegalBlockSizeException, IllegalArgumentException 
	{
	    IvParameterSpec ivParameterSpec = DeviceActivation.generateIv(iv);
	    Cipher cipher = Cipher.getInstance(algorithm);
	    cipher.init(Cipher.ENCRYPT_MODE, key, ivParameterSpec);
	    byte[] cipherText = cipher.doFinal(input.getBytes());    
	    ByteArrayOutputStream b = new ByteArrayOutputStream();
        try {
			b.write(iv);
			b.write( cipherText );
			cipherText = b.toByteArray();
		} catch (IOException e) 
        {
			logger.error(e.getMessage());
		}	    
	    return Base64.getEncoder().encodeToString(cipherText);
	}
	
	public static String decrypt(String algorithm, String dataStr, String salt) throws InvalidKeyException, 
	NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, 
	BadPaddingException, IllegalBlockSizeException, IllegalArgumentException, InvalidKeySpecException
	{
	    SecretKey key = DeviceActivation.getKeyFromPassword(DeviceActivation.getSecret(), salt);
		return DeviceActivation.decrypt(algorithm, dataStr, key);	  
	}
	
	public static String decrypt(String algorithm, String cipherText, SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException,
	    InvalidAlgorithmParameterException, InvalidKeyException,
	    BadPaddingException, IllegalBlockSizeException, IllegalArgumentException 
	{	    
	    Cipher cipher = Cipher.getInstance(algorithm);	    
	    byte[] cipherByte = Base64.getDecoder().decode(cipherText);    
	    byte[] iv;
	    if(cipherByte.length >= 8)
	    {
	    	iv = Arrays.copyOfRange(cipherByte , 0, 16);
	    }
	    else
	    {
	    	iv = DeviceActivation.randomIv(DeviceActivation.keyAlgorithm);
	    }    
	    cipherByte = Arrays.copyOfRange( cipherByte, 16, cipherByte.length);    
	    IvParameterSpec ivParameterSpec = DeviceActivation.generateIv(iv);
	    cipher.init(Cipher.DECRYPT_MODE, key, ivParameterSpec);
	    byte[] plainText = cipher.doFinal(cipherByte);
	    return new String(plainText);
	}
	
	public static String buildHmacCPUID(Map<String, Object> data, String salt) 
			throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, 
			NoSuchPaddingException, InvalidAlgorithmParameterException, BadPaddingException, 
			IllegalBlockSizeException, IllegalArgumentException
	{
		String dataStr = TLV.build(data);
	    return DeviceActivation.encrypt(DeviceActivation.algorithm, dataStr, salt);
	}	

	public static Map<String, Object> parseHmacCPUID(String dataStr, String salt) 
			throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, 
			NoSuchPaddingException, InvalidAlgorithmParameterException, BadPaddingException, 
			IllegalBlockSizeException, IllegalArgumentException
	{
	    String plainText = DeviceActivation.decrypt(DeviceActivation.algorithm, dataStr, salt);
		return TLV.parse(plainText);
	}

	
}
