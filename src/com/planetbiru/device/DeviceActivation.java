package com.planetbiru.device;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
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

import com.planetbiru.util.CommandLineExecutor;
import com.planetbiru.util.ServerInfo;

public class DeviceActivation {
	private static boolean activated = true;
	private static String secret = "paFyufyuYTDU57778934yuw327";
	private static String algorithm = "AES/CBC/PKCS5Padding";
	private static String keyEnv = "CPUID";
	
	private DeviceActivation()
	{
		
	}
	
	public static void verify()
	{
		String dataStr = System.getenv(keyEnv);
		if(dataStr != null && !dataStr.isEmpty())
		{
			String cpuSerialNumber = ServerInfo.cpuSerialNumber();
			try {
				verify(dataStr, cpuSerialNumber);
			} catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException
					| InvalidAlgorithmParameterException | BadPaddingException | IllegalBlockSizeException e) {
				DeviceActivation.activated = false;
				e.printStackTrace();
			}
		}
	}

	public static void verify(String dataStr, String salt) throws InvalidKeyException, 
	NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, 
	InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException {
		if(dataStr != null && !dataStr.isEmpty())
		{
			Map<String, String> data = DeviceActivation.parseHmacCPUID(dataStr, salt);
			DeviceActivation.activated = data.getOrDefault("VR", "").equals("1") && data.getOrDefault("PI", "").equals(salt);
		}
	}
	
	public static void activate(Map<String, String> data, String salt) throws InvalidKeyException, 
	NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, 
	InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException
	{
		String value = DeviceActivation.buildHmacCPUID(data, salt); 
		String command = "echo 'export "+keyEnv+"=\""+value.replace("'", "\\'")+"\"' >> $HOME/.bashrc";
		CommandLineExecutor.exec(command);
	}

	public static boolean isActivated() {
		return activated;
	}

	public static void setActivated(boolean activated) {
		DeviceActivation.activated = activated;
	}
	
	public static SecretKey getKeyFromPassword(String password, String salt)
	    throws NoSuchAlgorithmException, InvalidKeySpecException {
	    
	    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
	    KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
	    return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
	}
	
	public static IvParameterSpec generateIv() {
	    byte[] iv = new byte[16];
	    new SecureRandom().nextBytes(iv);
	    return new IvParameterSpec(iv);
	}
	
	public static String encrypt(String algorithm, String input, SecretKey key,
	    IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException,
	    InvalidAlgorithmParameterException, InvalidKeyException,
	    BadPaddingException, IllegalBlockSizeException {
	    
	    Cipher cipher = Cipher.getInstance(algorithm);
	    cipher.init(Cipher.ENCRYPT_MODE, key, iv);
	    byte[] cipherText = cipher.doFinal(input.getBytes());
	    return Base64.getEncoder().encodeToString(cipherText);
	}
	
	public static String decrypt(String algorithm, String cipherText, SecretKey key,
	    IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException,
	    InvalidAlgorithmParameterException, InvalidKeyException,
	    BadPaddingException, IllegalBlockSizeException {
	    
	    Cipher cipher = Cipher.getInstance(algorithm);
	    cipher.init(Cipher.DECRYPT_MODE, key, iv);
	    byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText));
	    return new String(plainText);
	}
	
	public static String buildHmacCPUID(Map<String, String> data, String salt) 
			throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, 
			NoSuchPaddingException, InvalidAlgorithmParameterException, BadPaddingException, 
			IllegalBlockSizeException
	{
	    SecretKey key = DeviceActivation.getKeyFromPassword(DeviceActivation.secret, salt);
	    IvParameterSpec ivParameterSpec = DeviceActivation.generateIv();
	    String input = TLV.build(data);
	    return DeviceActivation.encrypt(DeviceActivation.algorithm, input, key, ivParameterSpec);
	}
	
	public static Map<String, String> parseHmacCPUID(String dataStr, String salt) 
			throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, 
			NoSuchPaddingException, InvalidAlgorithmParameterException, BadPaddingException, 
			IllegalBlockSizeException
	{
	    SecretKey key = DeviceActivation.getKeyFromPassword(DeviceActivation.secret, salt);
	    IvParameterSpec ivParameterSpec = DeviceActivation.generateIv();
		String plainText = DeviceActivation.decrypt(DeviceActivation.algorithm, dataStr, key, ivParameterSpec);	  
		return TLV.parse(plainText);
	}
}
