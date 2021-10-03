package com.planetbiru.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.mail.MailUtil;
import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.Utility;

public class ConfigEmail {
	private static String configPath = "";
	private static List<DataEmail> accounts = new ArrayList<>();
	
	private static Logger logger = Logger.getLogger(ConfigEmail.class);
	
	private ConfigEmail()
	{
		
	}
	
	public static DataEmail getAccount(String id) {
		DataEmail data = null;
		for(int i = 0; i<ConfigEmail.accounts.size(); i++)
		{
			data = ConfigEmail.accounts.get(i);
			if(data.getId().equals(id))
			{
				return data;
			}
		}
		return null;
	}
	
	public static void load(String path) {
		ConfigEmail.configPath = path;
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);	
		try 
		{
			byte[] data = FileConfigUtil.read(fileName);		
			if(data != null)
			{
				String text = new String(data);
				if(text.length() > 7)
				{
					JSONArray jsonArray = new JSONArray(text);
					ConfigEmail.setAccounts(new ArrayList<>());
					for(int i = 0; i<jsonArray.length(); i++)
					{
						JSONObject json = jsonArray.optJSONObject(i);
						String id = json.optString("id", "");
						String senderName = json.optString("senderName", "");
						String senderAddress = json.optString("senderAddress", "");
						String senderPassword = json.optString("senderPassword", "");
						boolean auth = json.optBoolean("auth", false);
						boolean startTLS  = json.optBoolean("startTLS", false);
						boolean ssl = json.optBoolean("ssl", false);
						String host = json.optString("host", "");
						int port = json.optInt("port", 0);
						boolean active = json.optBoolean("active", false);
						DataEmail dataEmail = new DataEmail();
						
						dataEmail.setId(id);
						dataEmail.setSenderAddress(senderAddress);
						dataEmail.setSenderPassword(senderPassword);
						dataEmail.setSenderName(senderName);
						dataEmail.setAuth(auth);
						dataEmail.setHost(host);
						dataEmail.setPort(port);
						dataEmail.setStartTLS(startTLS);
						dataEmail.setSsl(ssl);
						dataEmail.setActive(active);
						
						ConfigEmail.getAccounts().add(dataEmail);
					}
				}
			}
		} 
		catch (FileNotFoundException | JSONException e) 
		{
			if(Config.isLogConfigNotFound())
			{
				logger.error(e.getMessage(), e);
			}
		}
		MailUtil.updateIndex();	
	}	
	
	public static void save() {
		ConfigEmail.save(ConfigEmail.configPath);
	}
	private static void save(String path) {
		JSONArray config = toJSONArray();
		ConfigEmail.save(path, config);
	}

	public static JSONArray toJSONArray() {
		
		JSONArray arr = new JSONArray();
		for(int i = 0; i<ConfigEmail.getAccounts().size(); i++)
		{
			JSONObject account = ConfigEmail.getAccounts().get(i).toJSONObject();
			arr.put(account);
		}
		return arr;
	}

	private static void save(String path, JSONArray config) {		
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		prepareDir(fileName);		
		try 
		{
			FileConfigUtil.write(fileName, config.toString().getBytes());
		}
		catch (IOException e) 
		{
			logger.error(e.getMessage(), e);
		}
		MailUtil.updateIndex();
	}

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
	}

	public static List<DataEmail> getAccounts() {
		return accounts;
	}

	public static void setAccounts(List<DataEmail> accounts) {
		ConfigEmail.accounts = accounts;
	}

	public static void add(DataEmail newData) {
		if(newData != null && !ConfigEmail.accountExists(newData.getHost(), newData.getPort(), newData.getSenderAddress()))
		{
			ConfigEmail.accounts.add(newData);
		}		
	}
	
	public static void put(DataEmail newData) {
		if(newData != null)
		{
			DataEmail oldData = ConfigEmail.getAccount(newData.getId());
			if(oldData == null)
			{
				ConfigEmail.accounts.add(newData);
			}
			else
			{
				oldData.set(newData);
			}
		}		
	}

	private static boolean accountExists(String host, int port, String senderAddress) {
		for(int i = 0; i<ConfigEmail.accounts.size(); i++)
		{
			DataEmail data = ConfigEmail.accounts.get(i);
			if(data.getHost().equals(host) && data.getPort() == port && data.getSenderAddress().equals(senderAddress))
			{
				return true;
			}
		}
		return false;
	}

	public static void activate(String id) {
		DataEmail oldData = ConfigEmail.getAccount(id);
		if(oldData != null)
		{
			oldData.setActive(true);
		}		
	}

	public static void deactivate(String id) {
		DataEmail oldData = ConfigEmail.getAccount(id);
		if(oldData != null)
		{
			oldData.setActive(false);
		}	
		
	}

	public static void deleteRecord(String id) {
		List<DataEmail> newAccounts = new ArrayList<>();
		for(int i = 0; i<ConfigEmail.accounts.size(); i++)
		{
			DataEmail data = ConfigEmail.accounts.get(i);
			if(!data.getId().equals(id))
			{
				newAccounts.add(data);
			}
		}
		ConfigEmail.accounts = newAccounts;
	}

	public static void reset() {
		ConfigEmail.accounts = new ArrayList<>();	
	}

	public static void delete(Map<String, String> queryPairs) {
		for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
		{
			String key = entry.getKey();
			String value = entry.getValue();
			if(key.startsWith("id["))
			{
				ConfigEmail.deleteRecord(value);
			}
		}
	}

	public static void deactivate(Map<String, String> queryPairs) {
		for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
		{
			String key = entry.getKey();
			String value = entry.getValue();
			if(key.startsWith("id["))
			{
				ConfigEmail.deactivate(value);
			}
		}
		
	}

	public static void activate(Map<String, String> queryPairs) {
		for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
		{
			String key = entry.getKey();
			String value = entry.getValue();
			if(key.startsWith("id["))
			{
				ConfigEmail.activate(value);
			}
		}
	}
}
