package com.planetbiru.device;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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
	private static boolean activated = false;
	private static String iv = "nUTRDRUijIJfDTYj";
	private static String secret = "kiIJujED";
	private static String algorithm = "AES/CBC/PKCS5Padding";
	private static String keyEnv = "CPUID";
	private static String defaultSalt = "IjYTWsrJ";
	
	private DeviceActivation()
	{
		
	}
	
	public static void verify()
	{
		String dataStr = System.getenv(keyEnv);
		if(dataStr != null && !dataStr.isEmpty())
		{
			String salt = ServerInfo.cpuSerialNumber();
			salt = DeviceActivation.fixSalt(salt);
			try {
				verify(dataStr, salt);
			} catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException
					| InvalidAlgorithmParameterException | BadPaddingException | IllegalBlockSizeException e) {
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
			System.out.println(data.toString());
			DeviceActivation.activated = data.getOrDefault("VR", "").equals("1");
		}
	}
	
	public static void activate(String tlv, String salt) throws InvalidKeyException, 
		NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, 
		InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, IllegalArgumentException
	{
		salt = DeviceActivation.fixSalt(salt);
		String value = DeviceActivation.buildHmacCPUID(tlv, salt); 
		String command = "echo 'export "+keyEnv+"=\""+value.replace("'", "\\'")+"\"' >> $HOME/.bashrc";
		System.out.println(command);
		CommandLineExecutor.exec(command);
	}

	public static String fixSalt(String salt) {
		if(salt == null || salt.isEmpty())
		{
			return defaultSalt;
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
	    throws NoSuchAlgorithmException, InvalidKeySpecException {
	    
	    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
	    KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
	    return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
	}
	
	public static IvParameterSpec generateIv() {
	    return new IvParameterSpec(iv.getBytes());
	}
	
	public static String encrypt(String algorithm, String input, SecretKey key,
	    IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException,
	    InvalidAlgorithmParameterException, InvalidKeyException,
	    BadPaddingException, IllegalBlockSizeException, IllegalArgumentException {
	    
	    Cipher cipher = Cipher.getInstance(algorithm);
	    cipher.init(Cipher.ENCRYPT_MODE, key, iv);
	    byte[] cipherText = cipher.doFinal(input.getBytes());
	    return Base64.getEncoder().encodeToString(cipherText);
	}
	
	public static String decrypt(String algorithm, String cipherText, SecretKey key,
	    IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException,
	    InvalidAlgorithmParameterException, InvalidKeyException,
	    BadPaddingException, IllegalBlockSizeException, IllegalArgumentException {
	    
	    Cipher cipher = Cipher.getInstance(algorithm);
	    cipher.init(Cipher.DECRYPT_MODE, key, iv);
	    byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText));
	    return new String(plainText);
	}
	
	public static String buildHmacCPUID(String dataStr, String salt) 
			throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, 
			NoSuchPaddingException, InvalidAlgorithmParameterException, BadPaddingException, 
			IllegalBlockSizeException, IllegalArgumentException
	{
	    SecretKey key = DeviceActivation.getKeyFromPassword(DeviceActivation.secret, salt);
	    IvParameterSpec ivParameterSpec = DeviceActivation.generateIv();
	    return DeviceActivation.encrypt(DeviceActivation.algorithm, dataStr, key, ivParameterSpec);
	}
	
	public static Map<String, Object> parseHmacCPUID(String dataStr, String salt) 
			throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, 
			NoSuchPaddingException, InvalidAlgorithmParameterException, BadPaddingException, 
			IllegalBlockSizeException, IllegalArgumentException
	{
	    SecretKey key = DeviceActivation.getKeyFromPassword(DeviceActivation.secret, salt);
	    IvParameterSpec ivParameterSpec = DeviceActivation.generateIv();
		String plainText = DeviceActivation.decrypt(DeviceActivation.algorithm, dataStr, key, ivParameterSpec);	  
		return TLV.parse(plainText);
	}
}
