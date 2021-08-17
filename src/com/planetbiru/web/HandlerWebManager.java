package com.planetbiru.web;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.planetbiru.DeviceAPI;
import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigAPI;
import com.planetbiru.config.ConfigAPIUser;
import com.planetbiru.config.ConfigBlocking;
import com.planetbiru.config.ConfigDDNS;
import com.planetbiru.config.ConfigEmail;
import com.planetbiru.config.ConfigSubscriberAMQP;
import com.planetbiru.config.ConfigSubscriberMQTT;
import com.planetbiru.config.ConfigSubscriberWS;
import com.planetbiru.config.ConfigFirewall;
import com.planetbiru.config.ConfigGeneral;
import com.planetbiru.config.ConfigKeystore;
import com.planetbiru.config.ConfigModem;
import com.planetbiru.config.ConfigNetDHCP;
import com.planetbiru.config.ConfigNetEthernet;
import com.planetbiru.config.ConfigNetWLAN;
import com.planetbiru.config.ConfigSMS;
import com.planetbiru.config.ConfigSMTP;
import com.planetbiru.config.ConfigVendorAfraid;
import com.planetbiru.config.ConfigVendorCloudflare;
import com.planetbiru.config.ConfigVendorDynu;
import com.planetbiru.config.ConfigVendorNoIP;
import com.planetbiru.config.DataEmail;
import com.planetbiru.config.DataModem;
import com.planetbiru.constant.ConstantString;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.cookie.CookieServer;
import com.planetbiru.ddns.DDNSRecord;
import com.planetbiru.gsm.GSMException;
import com.planetbiru.gsm.GSMUtil;
import com.planetbiru.mail.MailUtil;
import com.planetbiru.mail.NoEmailAccountException;
import com.planetbiru.user.NoUserRegisteredException;
import com.planetbiru.user.User;
import com.planetbiru.user.WebUserAccount;
import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.FileUtil;
import com.planetbiru.util.Utility;
import com.planetbiru.util.WebManagerContent;
import com.planetbiru.util.WebManagerTool;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class HandlerWebManager implements HttpHandler {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(HandlerWebManager.class);
	
	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		String path = httpExchange.getRequestURI().getPath();
		String method = httpExchange.getRequestMethod();
		if(path.endsWith(".html") && method.equals("POST"))
		{
			this.handlePost(httpExchange, path);
		}
		WebResponse response = this.handleGet(httpExchange, path);
		long length = 0;
		if(response.getResponseBody() != null)
		{
			length = response.getResponseBody().length;
		}
		httpExchange.sendResponseHeaders(response.getStatusCode(), length);	
		if(response.getResponseBody() != null)
		{
			httpExchange.getResponseBody().write(response.getResponseBody());
		}
		httpExchange.close();
	}

	private WebResponse handleGet(HttpExchange httpExchange, String path) {
		if(!Config.isValidDevice() && (path.contains(".html") || path.equals("/")))
		{
			return this.invalidDevice();
		}
		else
		{
			return this.serveDocumentRoot(httpExchange, path);
		}		
	}

	private WebResponse invalidDevice() {
		String fileName = WebManagerTool.getFileName("/invalid-device.html");
		byte[] responseBody = "".getBytes();
		int statusCode = HttpStatus.OK;
		try 
		{
			responseBody = FileUtil.readResource(fileName);
		} 
		catch (FileNotFoundException e) 
		{
			statusCode = HttpStatus.NOT_FOUND;
			if(fileName.endsWith(ConstantString.EXT_HTML))
			{
				try 
				{
					responseBody = FileUtil.readResource(WebManagerTool.getFileName("/404.html"));
				} 
				catch (FileNotFoundException e1) 
				{
					e1.printStackTrace();
				}
			}
		}
		Headers responseHeaders = new Headers();
		responseHeaders.add("Content-type", "text/html");
		return new WebResponse(statusCode, responseHeaders, responseBody);
	}

	private WebResponse serveDocumentRoot(HttpExchange httpExchange, String path) {
		if(path.equals("/"))
		{
			path = "/index.html";
		}
		WebResponse response = new WebResponse();
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		int statusCode = HttpStatus.OK;		
		String fileName = WebManagerTool.getFileName(path);
		byte[] responseBody = "".getBytes();
		try 
		{
			responseBody = FileUtil.readResource(fileName);
		} 
		catch (FileNotFoundException e) 
		{
			statusCode = HttpStatus.NOT_FOUND;
			if(fileName.endsWith(ConstantString.EXT_HTML))
			{
				try 
				{
					responseBody = FileUtil.readResource(WebManagerTool.getFileName("/404.html"));
				} 
				catch (FileNotFoundException e1) 
				{
					/**
					 * Do nothing
					 */
					responseBody = e.getMessage().getBytes();
				}
			}
		}
		CookieServer cookie = new CookieServer(requestHeaders, Config.getSessionName(), Config.getSessionLifetime());		
		WebManagerContent newContent = this.updateContent(fileName, responseHeaders, responseBody, statusCode, cookie);	
		responseBody = newContent.getResponseBody();
		responseHeaders = newContent.getResponseHeaders();
		statusCode = newContent.getStatusCode();
		String contentType = HttpUtil.getMIMEType(fileName);
		
		responseHeaders.add(ConstantString.CONTENT_TYPE, contentType);
		
		if(fileName.endsWith(ConstantString.EXT_HTML))
		{
			cookie.saveSessionData();
		}
		else
		{
			int lifetime = HttpUtil.getCacheLifetime(fileName);
			if(lifetime > 0)
			{
				responseHeaders.add(ConstantString.CACHE_CONTROL, "public, max-age="+lifetime+", immutable");				
			}
		}
		response.setResponseHeaders(responseHeaders);
		response.setStatusCode(statusCode);
		response.setResponseBody(responseBody);
		
		return response;
	}

	private WebManagerContent updateContent(String fileName, Headers responseHeaders, byte[] responseBody, int statusCode, CookieServer cookie) {
		String contentType = HttpUtil.getMIMEType(fileName);
		WebManagerContent webContent = new WebManagerContent(fileName, responseHeaders, responseBody, statusCode, cookie, contentType);
		boolean requireLogin = false;
		String fileSub = "";
		
		if(fileName.toLowerCase().endsWith(ConstantString.EXT_HTML))
		{
			JSONObject authFileInfo = WebManagerTool.processAuthFile(responseBody);
			requireLogin = authFileInfo.optBoolean(JsonKey.CONTENT, false);
			fileSub = WebManagerTool.getFileName(authFileInfo.optString("data-file", ""));
		}
		
		String username = cookie.getSessionValue(JsonKey.USERNAME, "");
		String password = cookie.getSessionValue(JsonKey.PASSWORD, "");
		
		if(requireLogin)
		{
			responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
			webContent.setResponseHeaders(responseHeaders);
			try
			{
				if(!WebUserAccount.checkUserAuth(username, password))	
				{
					try 
					{
						responseBody = FileUtil.readResource(fileSub);
						return this.updateContent(fileSub, responseHeaders, responseBody, statusCode, cookie);
					} 
					catch (FileNotFoundException e) 
					{
						statusCode = HttpStatus.NOT_FOUND;
						webContent.setStatusCode(statusCode);
					}	
				}
				responseBody = WebManagerTool.removeMeta(responseBody);
			}
			catch(NoUserRegisteredException e)
			{
				/**
				 * Do nothing
				 */
				statusCode = HttpStatus.PERMANENT_REDIRECT;
				webContent.setStatusCode(statusCode);
				
				responseHeaders.add(ConstantString.LOCATION, ConstantString.ADMIN_INIT);
				webContent.setResponseHeaders(responseHeaders);
				
				responseBody = "".getBytes();
			}
			webContent.setResponseBody(responseBody);
		}
		return webContent;
	}

	private void handlePost(HttpExchange httpExchange, String path) {
		Headers headers = httpExchange.getRequestHeaders();
		byte[] req = HttpUtil.getRequestBody(httpExchange);
		String requestBody = "";
		if(req != null)
		{
			requestBody = new String(req);
		}
		try 
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
				if(path.equals("/keystore.html"))
				{
					this.processKeystore(requestBody);
				}
				else if(path.equals("/keystore-update.html"))
				{
					this.processKeystore(requestBody);
				}
				else if(path.equals("/admin.html"))
				{
					this.processAdmin(requestBody, cookie);
				}
				else if(path.equals("/account-update.html"))
				{
					this.processAccount(requestBody, cookie);
				}
				else if(path.equals("/ddns-record.html"))
				{
					this.processDDNS(requestBody, cookie);
				}
				else if(path.equals("/ddns-record-update.html"))
				{
					this.processDDNS(requestBody, cookie);
				}
				else if(path.equals("/api-user.html"))
				{
					this.processAPIUser(requestBody);
				}
				else if(path.equals("/api-user-update.html"))
				{
					this.processAPIUser(requestBody);
				}
				else if(path.equals("/subscriber-setting.html"))
				{
					this.processSubscriberSetting(requestBody);
				}
				else if(path.equals("/sms-setting.html"))
				{
					this.processSMSSetting(requestBody);
				}
				else if(path.equals("/modem.html") || path.equals("/modem-add.html") || path.equals("/modem-update.html"))
				{
					this.processModemSetting(requestBody);
				}
				else if(path.equals("/email-account.html"))
				{
					this.processEmailAccount(requestBody);
				}
				else if(path.equals("/sms.html"))
				{
					this.processSMS(requestBody);
				}
				else if(path.equals("/api-setting.html"))
				{
					this.processAPISetting(requestBody);
				}
				else if(path.equals("/general-setting.html"))
				{
					this.processGeneralSetting(requestBody);
				}
				else if(path.equals("/date-time-setting.html"))
				{
					this.processDateTimeSetting(requestBody);
				}
				else if(path.equals("/cloudflare.html"))
				{
					this.processCloudflareSetting(requestBody);
				}
				else if(path.equals("/noip.html"))
				{
					this.processNoIPSetting(requestBody);
				}
				else if(path.equals("/afraid.html"))
				{
					this.processAfraidSetting(requestBody);
				}
				else if(path.equals("/dynu.html"))
				{
					this.processDynuSetting(requestBody);
				}
				else if(path.equals("/network-setting.html"))
				{
					this.processNetworkSetting(requestBody);
				}
				else if(path.equals("/block-list.html"))
				{
					this.processBlockList(requestBody);
				}
				else if(path.equals("/firewall.html"))
				{
					this.processFirewall(requestBody);
				}
				else if(path.equals("/logs.html"))
				{
					this.processDeleteLog(requestBody);
				}
				else if(path.equals("/sms-report.html"))
				{
					this.processDeleteReport(requestBody);
				}
				else if(path.equals("/smtp-setting.html"))
				{
					this.processSMTPSetting(requestBody);
				}
				else if(path.equals("/forget-password.html"))
				{
					this.processForgetPassword(requestBody);
				}
			}
		} 
		catch (NoUserRegisteredException e) 
		{
			/**
			 * Do nothing
			 */
		}	
	}
	
	private void processForgetPassword(String requestBody) {
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];      
		Map<String, String> queryPairs = Utility.parseQueryPairs(requestBody);	
		String userID = queryPairs.getOrDefault("userid", "");	
		WebUserAccount.load(Config.getUserSettingPath());
		try 
		{
			User user = WebUserAccount.getUser(userID);
			if(user.getUsername().isEmpty())
			{
				/**
				 * User not found
				 */
				user = WebUserAccount.getUserByPhone(userID);
				if(user.getUsername().isEmpty())
				{
					user = WebUserAccount.getUserByEmail(userID);
				}
			}
			if(!user.getUsername().isEmpty())
			{
				String phone = user.getPhone();
				String email = user.getEmail();
				if(!email.isEmpty() && userID.equalsIgnoreCase(email))
				{
					String message = "Username : "+user.getUsername()+"\r\nPassword : "+user.getPassword();
					ConfigEmail.load(Config.getEmailSettingPath());
					try 
					{
						MailUtil.send(email, "Account Information", message, ste);
					} 
					catch (MessagingException | NoEmailAccountException e) 
					{
						/**
						 * Do nothing
						 */
					}
				}
				else if(!phone.isEmpty())
				{
					String message = "Username : "+user.getUsername()+"\r\nPassword : "+user.getPassword();
					try 
					{
						GSMUtil.sendSMS(phone, message, ste);
					} 
					catch (GSMException e) 
					{
						/**
						 * Do nothing
						 */
					}
				}
			}
		} 
		catch (NoUserRegisteredException e1) 
		{
		}
		
	}

	private void processSMTPSetting(String requestBody) {
		Map<String, String> queryPairs = Utility.parseQueryPairs(requestBody);
		if(queryPairs.containsKey("update"))
		{
			ConfigSMTP.load(Config.getSmtpSettingPath());
			String softwareName = queryPairs.getOrDefault("software_name", "").trim();
			String serverName = queryPairs.getOrDefault("server_name", "").trim();
			String serverAddress = queryPairs.getOrDefault("server_address", "").trim();
			String port = queryPairs.getOrDefault("server_port", "0").trim();
			int serverPort = Utility.atoi(port);
			boolean active = queryPairs.getOrDefault("active", "").trim().equals("1");
			ConfigSMTP.setSoftwareName(softwareName);
			ConfigSMTP.setServerName(serverName);
			ConfigSMTP.setServerAddress(serverAddress);
			ConfigSMTP.setServerPort(serverPort);
			ConfigSMTP.setActive(active);
			ConfigSMTP.save();
		}
	}
	
	private void processDeleteLog(String requestBody) {
		Map<String, String> queryPairs = Utility.parseQueryPairs(requestBody);
		if(queryPairs.containsKey(JsonKey.DELETE))
		{
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					String path = FileConfigUtil.removeParentWithDot("/"+value);
					String dir = Config.getLogDir();
					String fileName = FileConfigUtil.fixFileName(dir+path);
					File file = new File(fileName);
					try 
					{
						FileConfigUtil.deleteDirectoryWalkTree(file.toPath());
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private void processDeleteReport(String requestBody) {
		Map<String, String> queryPairs = Utility.parseQueryPairs(requestBody);
		if(queryPairs.containsKey(JsonKey.DELETE))
		{
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					String path = FileConfigUtil.removeParentWithDot("/"+value);
					String dir = Config.getSmsLogPath();
					String fileName = FileConfigUtil.fixFileName(dir+path);
					File file = new File(fileName);
					try 
					{
						FileConfigUtil.deleteDirectoryWalkTree(file.toPath());
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private void processFirewall(String requestBody) {
		Map<String, String> queryPairs = Utility.parseQueryPairs(requestBody);
		String portStr = queryPairs.getOrDefault("port", "0").trim();
		int port = Utility.atoi(portStr);
		String protocol = queryPairs.getOrDefault("protocol", "tcp").trim();
		if(queryPairs.containsKey(JsonKey.DELETE))
		{
			ConfigFirewall.load(Config.getFirewallSettingPath());
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigFirewall.remove(value);
				}
			}
			ConfigFirewall.save();
		}
		if(queryPairs.containsKey(JsonKey.ACTIVATE))
		{
			ConfigFirewall.load(Config.getFirewallSettingPath());
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigFirewall.activate(value);
				}
			}
			ConfigFirewall.save();
		}
		if(queryPairs.containsKey(JsonKey.DEACTIVATE))
		{
			ConfigFirewall.load(Config.getFirewallSettingPath());
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigFirewall.deactivate(value);
				}
			}
			ConfigFirewall.save();
		}		
		if(queryPairs.containsKey(JsonKey.ADD))
		{
			ConfigFirewall.load(Config.getFirewallSettingPath());
			ConfigFirewall.add(port, protocol);
			ConfigFirewall.save();
		}
	}
	
	private void processBlockList(String requestBody) {
		Map<String, String> queryPairs = Utility.parseQueryPairs(requestBody);
		String msisdn = queryPairs.getOrDefault("msisdn", "").trim();
		if(queryPairs.containsKey(JsonKey.DELETE))
		{
			ConfigBlocking.load(Config.getBlockingSettingPath());
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigBlocking.remove(value);
				}
			}
			ConfigBlocking.save();
		}
		if(queryPairs.containsKey(JsonKey.BLOCK))
		{
			ConfigBlocking.load(Config.getBlockingSettingPath());
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					try 
					{
						ConfigBlocking.block(value);
					} 
					catch (GSMException e) 
					{
						/**
						 * Do nothing
						 */
					}
				}
			}
			ConfigBlocking.save();
		}
		if(queryPairs.containsKey(JsonKey.UNBLOCK))
		{
			ConfigBlocking.load(Config.getBlockingSettingPath());
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					try 
					{
						ConfigBlocking.unblock(value);
					} 
					catch (GSMException e) 
					{
						/**
						 * Do nothing
						 */
					}
				}
			}
			ConfigBlocking.save();
		}		
		if(queryPairs.containsKey(JsonKey.ADD))
		{
			ConfigBlocking.load(Config.getBlockingSettingPath());
			try 
			{
				ConfigBlocking.block(msisdn);
			} 
			catch (GSMException e) 
			{
				/**
				 * Do nothing
				 */
			}
			ConfigBlocking.save();
		}
	}
	
	private void processKeystore(String requestBody) {
		Map<String, String> queryPairs = Utility.parseQueryPairs(requestBody);
		if(queryPairs.containsKey(JsonKey.DELETE))
		{
			ConfigKeystore.load(Config.getKeystoreSettingPath());
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigKeystore.remove(value);
				}
			}
			ConfigKeystore.save();
		}
		if(queryPairs.containsKey(JsonKey.DEACTIVATE))
		{
			ConfigKeystore.load(Config.getKeystoreSettingPath());
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigKeystore.deactivate(value);
				}
			}
			ConfigKeystore.save();
		}
		if(queryPairs.containsKey(JsonKey.ACTIVATE))
		{
			ConfigKeystore.load(Config.getKeystoreSettingPath());
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigKeystore.activate(value);
				}
			}
			ConfigKeystore.save();
		}
		if(queryPairs.containsKey(JsonKey.UPDATE))
		{
			String id = queryPairs.getOrDefault("id", "");
			if(!id.isEmpty())
			{
				ConfigKeystore.load(Config.getKeystoreSettingPath());
				String fileName = queryPairs.getOrDefault("file_name", "").trim();
				String filePassword = queryPairs.getOrDefault("file_password", "").trim();
				boolean active = queryPairs.getOrDefault(JsonKey.ACTIVE, "").trim().equals("1");
				JSONObject data = ConfigKeystore.get(id).toJSONObject();
				
				data.put("id", id);
				data.put("fileName", fileName);
				if(!filePassword.isEmpty())
				{
					data.put("filePassword", filePassword);
				}
				data.put(JsonKey.ACTIVE, active);
				
				ConfigKeystore.update(id, data);
				ConfigKeystore.save();
			}
		}
		
		if(queryPairs.containsKey(JsonKey.ADD))
		{
			String id = Utility.md5(String.format("%d", System.nanoTime()));
			String fileName = queryPairs.getOrDefault("file_name", "").trim();
			String filePassword = queryPairs.getOrDefault("file_password", "").trim();
			if(!fileName.isEmpty() && !filePassword.isEmpty())
			{
				boolean active = queryPairs.getOrDefault(JsonKey.ACTIVE, "").trim().equals("1");
				JSONObject data = new JSONObject();				
				String fileExtension = FileConfigUtil.getFileExtension(fileName);				
				data.put("id", id);
				data.put("fileName", fileName);
				data.put("fileExtension", fileExtension);
				data.put("filePassword", filePassword);
				data.put(JsonKey.ACTIVE, active);
				byte[] binaryData = Utility.base64DecodeRaw(queryPairs.getOrDefault(JsonKey.DATA, ""));
				data.put("fileSize", binaryData.length);
				
				String fn = id + "." + fileExtension;
				
				ConfigKeystore.writeFile(Config.getKeystoreDataSettingPath(), fn, binaryData);
				ConfigKeystore.load(Config.getKeystoreSettingPath());
				ConfigKeystore.add(data);
				ConfigKeystore.save();
			}
		}
	}

	private void processSMSSetting(String requestBody) {
		Map<String, String> queryPairs = Utility.parseQueryPairs(requestBody);
		if(queryPairs.containsKey("save_sms_setting"))
		{
			ConfigSMS.load(Config.getSmsSettingPath());
			boolean lmonitorSMS = queryPairs.getOrDefault("monitor_sms", "").trim().equals("1");
			String v1 = queryPairs.getOrDefault("incomming_interval", "0").trim();
			int lIncommingInterval = Utility.atoi(v1);
			
			String v2 = queryPairs.getOrDefault("time_range", "0").trim();
			int lTimeRange = Utility.atoi(v2);
			
			String v3 = queryPairs.getOrDefault("max_per_time_range", "0").trim();
			int lMaxPerTimeRange = Utility.atoi(v3);
			String countryCode = queryPairs.getOrDefault("country_code", "").trim();
			String prefix = queryPairs.getOrDefault("recipient_prefix_length", "0").trim();
			int recipientPrefixLength = Utility.atoi(prefix);
			boolean logSMS = queryPairs.getOrDefault("log_sms", "").trim().equals("1");
			
			ConfigSMS.setCountryCode(countryCode);
			ConfigSMS.setMonitorSMS(lmonitorSMS);
			ConfigSMS.setIncommingInterval(lIncommingInterval);
			ConfigSMS.setTimeRange(lTimeRange);
			ConfigSMS.setMaxPerTimeRange(lMaxPerTimeRange);
			ConfigSMS.setRecipientPrefixLength(recipientPrefixLength);
			ConfigSMS.setLogSMS(logSMS);
			
			ConfigSMS.save();
		}	
	}

	private void processDateTimeSetting(String requestBody) {
		Map<String, String> queryPairs = Utility.parseQueryPairs(requestBody);
		if(queryPairs.containsKey("save_date_time_setting"))
		{
			ConfigGeneral.load(Config.getGeneralSettingPath());
			
			String deviceTime = queryPairs.getOrDefault("device_time", "").trim();
			String deviceTimeZone = queryPairs.getOrDefault("device_time_zone", "").trim();
			String ntpServer = queryPairs.getOrDefault("ntp_server", "").trim();
			String ntpUpdateInterval = queryPairs.getOrDefault("ntp_update_interval", "").trim();
			
			ConfigGeneral.setDeviceTimeZone(deviceTimeZone);
			ConfigGeneral.setNtpServer(ntpServer);
			ConfigGeneral.setNtpUpdateInterval(ntpUpdateInterval);

			ConfigGeneral.save();
			DeviceAPI.setTimeZone(deviceTimeZone);
			try 
			{
				Date date = Utility.stringToTime(deviceTime, "yyyy-MM-dd HH:mm:ss");
				DeviceAPI.setHardwareClock(date);
			} 
			catch (ParseException e) {
				/**
				 * Do nothing
				 */
			}
		}
	}

	private void processGeneralSetting(String requestBody) {
		Map<String, String> queryPairs = Utility.parseQueryPairs(requestBody);
		if(queryPairs.containsKey("save_general_setting"))
		{
			ConfigGeneral.load(Config.getGeneralSettingPath());
			
			String deviceName2 = queryPairs.getOrDefault("device_name", "").trim();
			String deviceTimeZone = queryPairs.getOrDefault("device_time_zone", "").trim();
			String ntpServer = queryPairs.getOrDefault("ntp_server", "").trim();
			String ntpUpdateInterval = queryPairs.getOrDefault("ntp_update_interval", "").trim();
			String restartService = queryPairs.getOrDefault("restart_service", "").trim();
			String restartDevice = queryPairs.getOrDefault("restart_device", "").trim();
			
			ConfigGeneral.setDeviceName(deviceName2);
			ConfigGeneral.setDeviceTimeZone(deviceTimeZone);
			ConfigGeneral.setNtpServer(ntpServer);
			ConfigGeneral.setNtpUpdateInterval(ntpUpdateInterval);
			ConfigGeneral.setRestartService(restartService);
			ConfigGeneral.setRestartDevice(restartDevice);
			
			ConfigGeneral.save();
			DeviceAPI.setTimeZone(deviceTimeZone);
		}
	}

	private void processAPISetting(String requestBody) {
		Map<String, String> queryPairs = Utility.parseQueryPairs(requestBody);
		if(queryPairs.containsKey("save_api_setting"))
		{
			ConfigAPI.load(Config.getApiSettingPath());
			String v1 = queryPairs.getOrDefault("http_port", "0").trim();
			int lHttpPort = Utility.atoi(v1);
			
			String v2 = queryPairs.getOrDefault("https_port", "0").trim();
			int lHttpsPort = Utility.atoi(v2);

			boolean lHttpEnable = queryPairs.getOrDefault("http_enable", "").trim().equals("1");
			boolean lHttpsEnable = queryPairs.getOrDefault("https_enable", "").trim().equals("1");
			
			String lMessagePath = queryPairs.getOrDefault("message_path", "").trim();
			String lSMSPath = queryPairs.getOrDefault("sms_path", "").trim();
			String lEmailPath = queryPairs.getOrDefault("email_path", "").trim();
			String lBlockingPath = queryPairs.getOrDefault("blocking_path", "").trim();
			String lUnblockingPath = queryPairs.getOrDefault("unblocking_path", "").trim();
			
			JSONObject config = new JSONObject();			
			config.put("httpPort", lHttpPort);
			config.put("httpsPort", lHttpsPort);
			config.put("httpEnable", lHttpEnable);
			config.put("httpsEnable", lHttpsEnable);
			config.put("messagePath", lMessagePath);
			config.put("smsPath", lSMSPath);
			config.put("emailPath", lEmailPath);
			config.put("blockingPath", lBlockingPath);
			config.put("unblockingPath", lUnblockingPath);
			
			ConfigAPI.save(Config.getApiSettingPath(), config);
		}
	}

	private void processNetworkSetting(String requestBody) {
		Map<String, String> queryPairs = Utility.parseQueryPairs(requestBody);
		if(queryPairs.containsKey("save_dhcp"))
		{
			String domainName = queryPairs.getOrDefault("domainName", "").trim();
			String domainNameServersStr = queryPairs.getOrDefault("domainNameServers", "").trim();
			String ipRouter = queryPairs.getOrDefault("ipRouter", "").trim();
			String netmask = queryPairs.getOrDefault("netmask", "").trim();
			String subnetMask = queryPairs.getOrDefault("subnetMask", "").trim();
			String domainNameServersAddress = queryPairs.getOrDefault("domainNameServersAddress", "").trim();
			String defaultLeaseTime = queryPairs.getOrDefault("defaultLeaseTime", "").trim();
			String maxLeaseTime = queryPairs.getOrDefault("maxLeaseTime", "").trim();
			String ranges = queryPairs.getOrDefault("ranges", "").trim();
			
			JSONArray nsList = new JSONArray();
			
			String[] arr1 = domainNameServersStr.split("\\,");
			for(int i = 0; i<arr1.length; i++)
			{
				String str1 = arr1[i].trim();
				if(!str1.isEmpty())
				{
					nsList.put(str1);
				}
			}
			JSONArray rangeList = new JSONArray();
			String[] arr2 = ranges.split("\\,");
			for(int i = 0; i<arr2.length; i++)
			{
				String str2 = arr2[i].trim();
				if(!str2.isEmpty())
				{
					String[] arr3 = str2.split("\\-");
					String str3 = arr3[0].trim();
					String str4 = arr3[1].trim();
					if(!str3.isEmpty() && !str4.isEmpty())
					{
						JSONObject obj1 = new JSONObject();
						obj1.put("begin", str3);
						obj1.put("end", str4);
						rangeList.put(obj1);
					}
				}
			}
			
			ConfigNetDHCP.load(Config.getDhcpSettingPath());
			ConfigNetDHCP.setDomainName(domainName);
			ConfigNetDHCP.setIpRouter(ipRouter);
			ConfigNetDHCP.setNetmask(netmask);
			ConfigNetDHCP.setSubnetMask(subnetMask);
			ConfigNetDHCP.setDomainNameServersAddress(domainNameServersAddress);
			ConfigNetDHCP.setDefaultLeaseTime(defaultLeaseTime);
			ConfigNetDHCP.setMaxLeaseTime(maxLeaseTime);
			ConfigNetDHCP.setRanges(rangeList);
			ConfigNetDHCP.setDomainNameServers(nsList);
			ConfigNetDHCP.save();	
			ConfigNetDHCP.apply(Config.getOsDHCPConfigPath());
		}
		
		if(queryPairs.containsKey("save_wlan"))
		{
			ConfigNetWLAN.load(Config.getWlanSettingPath());
			ConfigNetWLAN.setEssid(queryPairs.getOrDefault("essid", "").trim());
			ConfigNetWLAN.setKey(queryPairs.getOrDefault("key", "").trim());
			ConfigNetWLAN.setKeyMgmt(queryPairs.getOrDefault("keyMgmt", "").trim());
			ConfigNetWLAN.setIpAddress(queryPairs.getOrDefault("ipAddress", "").trim());
			ConfigNetWLAN.setPrefix(queryPairs.getOrDefault("prefix", "").trim());
			ConfigNetWLAN.setNetmask(queryPairs.getOrDefault("netmask", "").trim());
			ConfigNetWLAN.setGateway(queryPairs.getOrDefault("gateway", "").trim());
			ConfigNetWLAN.setDns1(queryPairs.getOrDefault("dns1", "").trim());
			ConfigNetWLAN.save();
			ConfigNetWLAN.apply(Config.getOsWLANConfigPath(), Config.getOsSSIDKey());
		}

		if(queryPairs.containsKey("save_ethernet"))
		{
			ConfigNetEthernet.load(Config.getEthernetSettingPath());
			ConfigNetEthernet.setIpAddress(queryPairs.getOrDefault("ipAddress", "").trim());
			ConfigNetEthernet.setPrefix(queryPairs.getOrDefault("prefix", "").trim());
			ConfigNetEthernet.setNetmask(queryPairs.getOrDefault("netmask", "").trim());
			ConfigNetEthernet.setGateway(queryPairs.getOrDefault("gateway", "").trim());
			ConfigNetEthernet.setDns1(queryPairs.getOrDefault("dns1", "").trim());
			ConfigNetEthernet.setDns2(queryPairs.getOrDefault("dns2", "").trim());
			ConfigNetEthernet.save();
			ConfigNetEthernet.apply(Config.getOsEthernetConfigPath());
		}
	}

	private void processCloudflareSetting(String requestBody) {
		Map<String, String> queryPairs = Utility.parseQueryPairs(requestBody);
		String endpoint = queryPairs.getOrDefault("endpoint", "").trim();
		String accountId = queryPairs.getOrDefault("account_id", "").trim();
		String authEmail = queryPairs.getOrDefault("auth_email", "").trim();
		String authApiKey = queryPairs.getOrDefault("auth_api_key", "").trim();
		String authToken = queryPairs.getOrDefault("auth_token", "").trim();
		boolean active = queryPairs.getOrDefault("active", "").trim().equals("1");
		
		if(!endpoint.isEmpty())
		{
			ConfigVendorCloudflare.load(Config.getCloudflareSettingPath());
			ConfigVendorCloudflare.setEndpoint(endpoint);
			ConfigVendorCloudflare.setAccountId(accountId);
			ConfigVendorCloudflare.setAuthEmail(authEmail);
			ConfigVendorCloudflare.setAuthApiKey(authApiKey);
			ConfigVendorCloudflare.setAuthToken(authToken);
			ConfigVendorCloudflare.setActive(active);
			ConfigVendorCloudflare.save();
		}
	}
	
	private void processNoIPSetting(String requestBody) {
		Map<String, String> queryPairs = Utility.parseQueryPairs(requestBody);
		String endpoint = queryPairs.getOrDefault("endpoint", "").trim();
		String username = queryPairs.getOrDefault("username", "").trim();
		String email = queryPairs.getOrDefault("email", "").trim();
		String password = queryPairs.getOrDefault("password", "").trim();
		String company = queryPairs.getOrDefault("company", "").trim();
		boolean active = queryPairs.getOrDefault("active", "").trim().equals("1");
		
		if(!endpoint.isEmpty())
		{
			ConfigVendorNoIP.load(Config.getNoIPSettingPath());
			ConfigVendorNoIP.setEndpoint(endpoint);
			ConfigVendorNoIP.setUsername(username);
			if(!password.isEmpty())
			{
				ConfigVendorNoIP.setPassword(password);
			}
			ConfigVendorNoIP.setCompany(company);
			ConfigVendorNoIP.setEmail(email);		
			ConfigVendorNoIP.setActive(active);
			ConfigVendorNoIP.save();
		}
	}
	
	private void processAfraidSetting(String requestBody) {
		Map<String, String> queryPairs = Utility.parseQueryPairs(requestBody);
		String endpoint = queryPairs.getOrDefault("endpoint", "").trim();
		String username = queryPairs.getOrDefault("username", "").trim();
		String email = queryPairs.getOrDefault("email", "").trim();
		String password = queryPairs.getOrDefault("password", "").trim();
		String company = queryPairs.getOrDefault("company", "").trim();
		boolean active = queryPairs.getOrDefault("active", "").trim().equals("1");
		
		if(!endpoint.isEmpty())
		{
			ConfigVendorAfraid.load(Config.getAfraidSettingPath());
			ConfigVendorAfraid.setEndpoint(endpoint);
			ConfigVendorAfraid.setUsername(username);
			if(!password.isEmpty())
			{
				ConfigVendorAfraid.setPassword(password);
			}
			ConfigVendorAfraid.setCompany(company);
			ConfigVendorAfraid.setEmail(email);		
			ConfigVendorAfraid.setActive(active);
			ConfigVendorAfraid.save();
		}
	}
	
	private void processDynuSetting(String requestBody) {
		Map<String, String> queryPairs = Utility.parseQueryPairs(requestBody);
		String apiVersion = queryPairs.getOrDefault("api_version", "").trim();
		String apiKey = queryPairs.getOrDefault("api_key", "").trim();
		String endpoint = queryPairs.getOrDefault("endpoint", "").trim();
		String username = queryPairs.getOrDefault("username", "").trim();
		String email = queryPairs.getOrDefault("email", "").trim();
		String password = queryPairs.getOrDefault("password", "").trim();
		String company = queryPairs.getOrDefault("company", "").trim();
		boolean active = queryPairs.getOrDefault("active", "").trim().equals("1");
		
		if(!endpoint.isEmpty())
		{
			ConfigVendorDynu.load(Config.getDynuSettingPath());
			ConfigVendorDynu.setEndpoint(endpoint);
			ConfigVendorDynu.setUsername(username);
			ConfigVendorDynu.setApiVersion(apiVersion);
			ConfigVendorDynu.setApiKey(apiKey);
			if(!password.isEmpty())
			{
				ConfigVendorDynu.setPassword(password);
			}
			ConfigVendorDynu.setCompany(company);
			ConfigVendorDynu.setEmail(email);		
			ConfigVendorDynu.setActive(active);
			ConfigVendorDynu.save();
		}
	}
	
	private void processEmailAccount(String requestBody) {
		Map<String, String> queryPairs = Utility.parseQueryPairs(requestBody);
		if(queryPairs.containsKey(JsonKey.DELETE))
		{
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigEmail.deleteRecord(value);
				}
			}
			ConfigEmail.save();
		}
		if(queryPairs.containsKey(JsonKey.DEACTIVATE))
		{
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigEmail.deactivate(value);
				}
			}
			ConfigEmail.save();
		}
		if(queryPairs.containsKey(JsonKey.ACTIVATE))
		{
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigEmail.activate(value);
				}
			}
			ConfigEmail.save();
		}
		if(queryPairs.containsKey("update"))
		{
			ConfigEmail.load(Config.getEmailSettingPath());
			
			String id = queryPairs.getOrDefault("id", "").trim();
			
			DataEmail dataEmail = ConfigEmail.getAccount(id);
			if(dataEmail == null)
			{
				dataEmail = new DataEmail();
			}		
			
			boolean auth = queryPairs.getOrDefault("mail_auth", "").trim().equals("1");
			String host = queryPairs.getOrDefault("smtp_host", "").trim();
	
			String v1 = queryPairs.getOrDefault("smtp_port", "0").trim();
			int port = Utility.atoi(v1);
			String senderAddress = queryPairs.getOrDefault("sender_address", "").trim();
			String senderPassword = queryPairs.getOrDefault("sender_password", "").trim();
			if(senderPassword.isEmpty())
			{
				senderPassword = dataEmail.getSenderPassword();
			}
			boolean ssl = queryPairs.getOrDefault("ssl", "").trim().equals("1");
			boolean startTLS = queryPairs.getOrDefault("start_tls", "").trim().equals("1");
			boolean active = queryPairs.getOrDefault(JsonKey.ACTIVE, "").trim().equals("1");
			
			dataEmail.setId(id);
			dataEmail.setAuth(auth);;
			dataEmail.setHost(host);
			dataEmail.setPort(port);
			dataEmail.setSenderAddress(senderAddress);
			dataEmail.setSenderPassword(senderPassword);
			dataEmail.setSsl(ssl);
			dataEmail.setStartTLS(startTLS);
			dataEmail.setActive(active);
			
			ConfigEmail.put(dataEmail);
			ConfigEmail.save();
		}
		if(queryPairs.containsKey("add"))
		{
			ConfigEmail.load(Config.getEmailSettingPath());
			
			String id = Utility.md5(System.nanoTime()+"");
			
			DataEmail dataEmail = new DataEmail();
			
			boolean auth = queryPairs.getOrDefault("mail_auth", "").trim().equals("1");
			String host = queryPairs.getOrDefault("smtp_host", "").trim();
	
			String v1 = queryPairs.getOrDefault("smtp_port", "0").trim();
			int port = Utility.atoi(v1);
			String senderAddress = queryPairs.getOrDefault("sender_address", "").trim();
			String senderPassword = queryPairs.getOrDefault("sender_password", "").trim();

			boolean ssl = queryPairs.getOrDefault("ssl", "").trim().equals("1");
			boolean startTLS = queryPairs.getOrDefault("start_tls", "").trim().equals("1");
			boolean active = queryPairs.getOrDefault(JsonKey.ACTIVE, "").trim().equals("1");
			
			dataEmail.setId(id);
			dataEmail.setAuth(auth);;
			dataEmail.setHost(host);
			dataEmail.setPort(port);
			dataEmail.setSenderAddress(senderAddress);
			dataEmail.setSenderPassword(senderPassword);
			dataEmail.setSsl(ssl);
			dataEmail.setStartTLS(startTLS);
			dataEmail.setActive(active);
		
			ConfigEmail.add(dataEmail);
			ConfigEmail.save();
		}	
	}
	
	private void processModemSetting(String requestBody) {
		Map<String, String> queryPairs = Utility.parseQueryPairs(requestBody);
		ConfigModem.load(Config.getModemSettingPath());
		if(queryPairs.containsKey(JsonKey.DELETE))
		{
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigModem.deleteRecord(value);
				}
			}
			ConfigModem.save();
		}
		if(queryPairs.containsKey(JsonKey.DEACTIVATE))
		{
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigModem.deactivate(value);
				}
			}
			ConfigModem.save();
		}
		if(queryPairs.containsKey(JsonKey.ACTIVATE))
		{
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigModem.activate(value);
				}
			}
			ConfigModem.save();
		}
		
		if(queryPairs.containsKey(JsonKey.ADD))
		{
			this.processModemUpdate(queryPairs, JsonKey.ADD);
		}	
		if(queryPairs.containsKey(JsonKey.UPDATE))
		{
			this.processModemUpdate(queryPairs, JsonKey.UPDATE);
		}
		GSMUtil.updateConnectedDevice();
	}
	
	private void processModemUpdate(Map<String, String> queryPairs, String action) {		
		
		String id = queryPairs.getOrDefault("id", "").trim();		
		String port = queryPairs.getOrDefault("port", "").trim();
		boolean active = queryPairs.getOrDefault(JsonKey.ACTIVE, "").trim().equals("1");	
		boolean defaultModem = queryPairs.getOrDefault("default_modem", "").trim().equals("1");
		boolean smsAPI = queryPairs.getOrDefault("sms_api", "").trim().equals("1");
		boolean internetAccess = queryPairs.getOrDefault("internet_access", "").trim().equals("1");
		boolean autoreconnect = queryPairs.getOrDefault("autoreconnect", "").trim().equals("1");

		String smsCenter = queryPairs.getOrDefault("sms_center", "").trim();		
		
		String incommingIntervalS = queryPairs.getOrDefault("incomming_interval", "0").trim();		
		int incommingInterval = Utility.atoi(incommingIntervalS);	
		
		String timeRangeS = queryPairs.getOrDefault("time_range", "").trim();		
		int timeRange = Utility.atoi(timeRangeS);

		String maxPerTimeRangeS = queryPairs.getOrDefault("maxPer_time_range", "0").trim();
		int maxPerTimeRange = Utility.atoi(maxPerTimeRangeS);

		String baudRateS = queryPairs.getOrDefault("baud_rate", "0").trim();
		int baudRate = Utility.atoi(baudRateS);

		String imei = queryPairs.getOrDefault("imei", "").trim();
		String msisdn = queryPairs.getOrDefault("msisdn", "").trim();
		String imsi = queryPairs.getOrDefault("imsi", "").trim();
		String name = queryPairs.getOrDefault("name", "").trim();
		String recipientPrefix = queryPairs.getOrDefault("recipient_prefix", "").trim();
		
		if(!recipientPrefix.isEmpty())
		{
			recipientPrefix = recipientPrefix.replace("\n", "\r\n");
			recipientPrefix = recipientPrefix.replace("\r\r\n", "\r\n");
			recipientPrefix = recipientPrefix.replace("\r", "\r\n");
			recipientPrefix = recipientPrefix.replace("\r\n\n", "\r\n");
			recipientPrefix = recipientPrefix.replace(" ", "");
			String[] arr = recipientPrefix.split(",");
			List<String> pref = new ArrayList<>();
			for(int i = 0; i<arr.length; i++)
			{
				if(!arr[i].isEmpty())
				{
					try 
					{
						pref.add(Utility.canonicalMSISDN(arr[i]));
					} 
					catch (GSMException e) 
					{
						/**
						 * Do nothing
						 */
					}
					
				}
			}
			recipientPrefix = String.join(",", pref);
		}	
		
		String simCardPIN = queryPairs.getOrDefault("sim_card_pin", "").trim();

		String parityBit = queryPairs.getOrDefault("parity_bit", "").trim();
		String startBits = queryPairs.getOrDefault("start_bits", "").trim();
		String stopBits = queryPairs.getOrDefault("stop_bits", "").trim();

		String apn = queryPairs.getOrDefault("apn", "").trim();
		String apnUsername = queryPairs.getOrDefault("apn_username", "").trim();
		String apnPassword = queryPairs.getOrDefault("apn_password", "").trim();
		String dialNumner = queryPairs.getOrDefault("dial_number", "").trim();
		String initDial1 = queryPairs.getOrDefault("init_dial_1", "").trim();
		String initDial2 = queryPairs.getOrDefault("init_dial_2", "").trim();
		String initDial3 = queryPairs.getOrDefault("init_dial_3", "").trim();
		String initDial4 = queryPairs.getOrDefault("init_dial_4", "").trim();
		String initDial5 = queryPairs.getOrDefault("init_dial_5", "").trim();
		String dialCommand = queryPairs.getOrDefault("dial_command", "").trim();		

		DataModem modem = ConfigModem.getModemData(id);
		if(action.equals(JsonKey.ADD) || id.isEmpty())
		{
			id = Utility.md5(String.format("%d", System.nanoTime()));
			modem.setId(id);
		}
		
		modem.setName(name);
		modem.setPort(port);
		modem.setSmsCenter(smsCenter);
		modem.setIncommingInterval(incommingInterval);
		modem.setTimeRange(timeRange);
		modem.setMaxPerTimeRange(maxPerTimeRange);
		modem.setImei(imei);
		modem.setMsisdn(msisdn);
		modem.setImsi(imsi);
		modem.setRecipientPrefix(recipientPrefix);
		if(!simCardPIN.isEmpty())
		{
			modem.setSimCardPIN(simCardPIN);
		}
		modem.setBaudRate(baudRate);
		modem.setParityBit(parityBit);
		modem.setStartBits(startBits);
		modem.setStopBits(stopBits);
		modem.setInternetAccess(internetAccess);
		modem.setSmsAPI(smsAPI);
		modem.setDefaultModem(defaultModem);
		modem.setActive(active);
		
		modem.setApn(apn);
		modem.setApnUsername(apnUsername);
		modem.setApnPassword(apnPassword);
		modem.setDialNumner(dialNumner);
		modem.setInitDial1(initDial1);
		modem.setInitDial2(initDial2);
		modem.setInitDial3(initDial3);
		modem.setInitDial4(initDial4);
		modem.setInitDial5(initDial5);
		modem.setDialCommand(dialCommand);
		modem.setAutoreconnect(autoreconnect);

		ConfigModem.update(id, modem);
		ConfigModem.save();	
	}

	private void processSubscriberSetting(String requestBody) {
		Map<String, String> queryPairs = Utility.parseQueryPairs(requestBody);
		if(queryPairs.containsKey("save_subscriber_ws_setting"))
		{
			ConfigSubscriberWS.load(Config.getSubscriberWSSettingPath());
			boolean subscriberWsEnable = queryPairs.getOrDefault("subscriber_ws_enable", "").equals("1");		
			boolean subscriberWsSSL = queryPairs.getOrDefault("subscriber_ws_ssl", "").equals("1");		
			String subscriberWsAddress = queryPairs.getOrDefault("subscriber_ws_address", "");		
			String port = queryPairs.getOrDefault("subscriber_ws_port", "0");
			int subscriberWsPort = Utility.atoi(port);
			String subscriberWsPath = queryPairs.getOrDefault("subscriber_ws_path", "");		
			String subscriberWsUsername = queryPairs.getOrDefault("subscriber_ws_username", "");		
			String subscriberWsPassword = queryPairs.getOrDefault("subscriber_ws_password", "");		
			String subscriberWsTopic = queryPairs.getOrDefault("subscriber_ws_topic", "");
			
			String timeout = queryPairs.getOrDefault("subscriber_ws_timeout", "0");
			int subscriberWsTimeout = Utility.atoi(timeout);	
			String reconnect = queryPairs.getOrDefault("subscriber_ws_reconnect_delay", "0");
			int subscriberWsReconnectDelay = Utility.atoi(reconnect);
			String refresh = queryPairs.getOrDefault("subscriber_ws_refresh", "0");
			int subscriberWsRefresh = Utility.atoi(refresh);
			
			ConfigSubscriberWS.setSubscriberWsEnable(subscriberWsEnable);
			ConfigSubscriberWS.setSubscriberWsSSL(subscriberWsSSL);
			ConfigSubscriberWS.setSubscriberWsAddress(subscriberWsAddress);
			ConfigSubscriberWS.setSubscriberWsPort(subscriberWsPort);
			ConfigSubscriberWS.setSubscriberWsPath(subscriberWsPath);
			ConfigSubscriberWS.setSubscriberWsUsername(subscriberWsUsername);
			ConfigSubscriberWS.setSubscriberWsPassword(subscriberWsPassword);
			ConfigSubscriberWS.setSubscriberWsTopic(subscriberWsTopic);
			ConfigSubscriberWS.setSubscriberWsTimeout(subscriberWsTimeout);
			ConfigSubscriberWS.setSubscriberWsReconnectDelay(subscriberWsReconnectDelay);
			ConfigSubscriberWS.setSubscriberWsRefresh(subscriberWsRefresh);		
			
			ConfigSubscriberWS.save();
		}
		
		if(queryPairs.containsKey("save_subscriber_amqp_setting"))
		{
			ConfigSubscriberAMQP.load(Config.getSubscriberAMQPSettingPath());
			boolean subscriberAmqpEnable = queryPairs.getOrDefault("subscriber_amqp_enable", "").equals("1");		
			boolean subscriberAmqpSSL = queryPairs.getOrDefault("subscriber_amqp_ssl", "").equals("1");		
			String subscriberAmqpAddress = queryPairs.getOrDefault("subscriber_amqp_address", "");		
			String port = queryPairs.getOrDefault("subscriber_amqp_port", "0");
			int subscriberAmqpPort = Utility.atoi(port);
			String subscriberAmqpPath = queryPairs.getOrDefault("subscriber_amqp_path", "");		
			String subscriberAmqpUsername = queryPairs.getOrDefault("subscriber_amqp_username", "");		
			String subscriberAmqpPassword = queryPairs.getOrDefault("subscriber_amqp_password", "");		
			String subscriberAmqpTopic = queryPairs.getOrDefault("subscriber_amqp_topic", "");
			
			String timeout = queryPairs.getOrDefault("subscriber_amqp_timeout", "0");
			int subscriberAmqpTimeout = Utility.atoi(timeout);	
			String refresh = queryPairs.getOrDefault("subscriber_amqp_refresh", "0");
			int subscriberAmqpRefresh = Utility.atoi(refresh);
			
			ConfigSubscriberAMQP.setSubscriberAmqpEnable(subscriberAmqpEnable);
			ConfigSubscriberAMQP.setSubscriberAmqpSSL(subscriberAmqpSSL);
			ConfigSubscriberAMQP.setSubscriberAmqpAddress(subscriberAmqpAddress);
			ConfigSubscriberAMQP.setSubscriberAmqpPort(subscriberAmqpPort);
			ConfigSubscriberAMQP.setSubscriberAmqpPath(subscriberAmqpPath);
			ConfigSubscriberAMQP.setSubscriberAmqpUsername(subscriberAmqpUsername);
			ConfigSubscriberAMQP.setSubscriberAmqpPassword(subscriberAmqpPassword);
			ConfigSubscriberAMQP.setSubscriberAmqpTopic(subscriberAmqpTopic);
			ConfigSubscriberAMQP.setSubscriberAmqpTimeout(subscriberAmqpTimeout);
			ConfigSubscriberAMQP.setSubscriberAmqpRefresh(subscriberAmqpRefresh);		

			ConfigSubscriberAMQP.save();			
		}	
		
		if(queryPairs.containsKey("save_subscriber_amqp_setting"))
		{
			ConfigSubscriberMQTT.load(Config.getSubscriberMqttSettingPath());
			boolean subscriberMqttEnable = queryPairs.getOrDefault("subscriber_mqtt_enable", "").equals("1");		
			boolean subscriberMqttSSL = queryPairs.getOrDefault("subscriber_mqtt_ssl", "").equals("1");		
			String subscriberMqttAddress = queryPairs.getOrDefault("subscriber_mqtt_address", "");		
			String port = queryPairs.getOrDefault("subscriber_mqtt_port", "0");
			int subscriberMqttPort = Utility.atoi(port);
			String subscriberMqttPath = queryPairs.getOrDefault("subscriber_mqtt_path", "");		
			String subscriberMqttUsername = queryPairs.getOrDefault("subscriber_mqtt_username", "");		
			String subscriberMqttPassword = queryPairs.getOrDefault("subscriber_mqtt_password", "");		
			String subscriberMqttTopic = queryPairs.getOrDefault("subscriber_mqtt_topic", "");
			
			String timeout = queryPairs.getOrDefault("subscriber_mqtt_timeout", "0");
			int subscriberMqttTimeout = Utility.atoi(timeout);	
			String refresh = queryPairs.getOrDefault("subscriber_mqtt_refresh", "0");
			int subscriberMqttRefresh = Utility.atoi(refresh);
			
			ConfigSubscriberMQTT.setSubscriberMqttEnable(subscriberMqttEnable);
			ConfigSubscriberMQTT.setSubscriberMqttSSL(subscriberMqttSSL);
			ConfigSubscriberMQTT.setSubscriberMqttAddress(subscriberMqttAddress);
			ConfigSubscriberMQTT.setSubscriberMqttPort(subscriberMqttPort);
			ConfigSubscriberMQTT.setSubscriberMqttPath(subscriberMqttPath);
			ConfigSubscriberMQTT.setSubscriberMqttUsername(subscriberMqttUsername);
			ConfigSubscriberMQTT.setSubscriberMqttPassword(subscriberMqttPassword);
			ConfigSubscriberMQTT.setSubscriberMqttTopic(subscriberMqttTopic);
			ConfigSubscriberMQTT.setSubscriberMqttTimeout(subscriberMqttTimeout);
			ConfigSubscriberMQTT.setSubscriberMqttRefresh(subscriberMqttRefresh);		

			ConfigSubscriberMQTT.save();			
		}	
	}
	
	private void processSMS(String requestBody) {
		Map<String, String> queryPairs = Utility.parseQueryPairs(requestBody);
		if(queryPairs.containsKey("send"))
		{
			String receiver = queryPairs.getOrDefault(JsonKey.RECEIVER, "").trim();			
			String message = queryPairs.getOrDefault(JsonKey.MESSAGE, "").trim();	
			String modemID = queryPairs.getOrDefault("modem", "").trim();	
			if(!receiver.isEmpty() && !message.isEmpty())
			{
				try 
				{
					GSMUtil.sendSMS(receiver, message, modemID);
					String modemName = GSMUtil.getModemName(modemID);
					HttpUtil.broardcastWebSocket("Sending a message to "+receiver+" via "+modemName);
				} 
				catch (GSMException e) 
				{
					HttpUtil.broardcastWebSocket(e.getMessage());
				}
			}
		}		
	}
	
	private void processAccount(String requestBody, CookieServer cookie) {
		Map<String, String> queryPairs = Utility.parseQueryPairs(requestBody);
		String loggedUsername = (String) cookie.getSessionValue(JsonKey.USERNAME, "");
		String phone = queryPairs.getOrDefault(JsonKey.PHONE, "");
		String password = queryPairs.getOrDefault(JsonKey.PASSWORD, "");
		String email = queryPairs.getOrDefault(JsonKey.EMAIL, "");
		String name = queryPairs.getOrDefault(JsonKey.NAME, "");
		if(queryPairs.containsKey(JsonKey.UPDATE))
		{
			User user;
			try 
			{
				user = WebUserAccount.getUser(loggedUsername);
				user.setName(name);
				user.setPhone(phone);
				user.setEmail(email);
				if(!password.isEmpty())
				{
					user.setPassword(password);
				}
				WebUserAccount.updateUser(user);
				WebUserAccount.save();
			} 
			catch (NoUserRegisteredException e) 
			{
				/**
				 * Do nothing
				 */
			}
		}		
	}
	
	private void processAdmin(String requestBody, CookieServer cookie) {
		Map<String, String> queryPairs = Utility.parseQueryPairs(requestBody);
		String loggedUsername = (String) cookie.getSessionValue(JsonKey.USERNAME, "");
		if(queryPairs.containsKey(JsonKey.DELETE))
		{
			/**
			 * Delete
			 */
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id[") && !value.equals(loggedUsername))
				{
					WebUserAccount.deleteUser(value);
				}
			}
			WebUserAccount.save();
		}
		else if(queryPairs.containsKey(JsonKey.DEACTIVATE))
		{
			/**
			 * Deactivate
			 */
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id[") && !value.equals(loggedUsername))
				{
					try 
					{
						WebUserAccount.deactivate(value);
					} 
					catch (NoUserRegisteredException e) 
					{
						/**
						 * Do nothing
						 */
					}
				}
			}
			WebUserAccount.save();
		}
		else if(queryPairs.containsKey(JsonKey.ACTIVATE))
		{
			/**
			 * Activate
			 */
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					try 
					{
						WebUserAccount.activate(value);
					} 
					catch (NoUserRegisteredException e) 
					{
						/**
						 * Do nothing
						 */
					}
				}
			}
			WebUserAccount.save();
		}
		else if(queryPairs.containsKey("block"))
		{
			/**
			 * Block
			 */
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id[") && !value.equals(loggedUsername))
				{
					try 
					{
						WebUserAccount.block(value);
					} 
					catch (NoUserRegisteredException e) 
					{
						/**
						 * Do nothing
						 */
					}
				}
			}
			WebUserAccount.save();		
		}
		else if(queryPairs.containsKey("unblock"))
		{
			/**
			 * Unblock
			 */
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					try 
					{
						WebUserAccount.unblock(value);
					} 
					catch (NoUserRegisteredException e) 
					{
						/**
						 * Do nothing
						 */
					}
				}
			}
			WebUserAccount.save();
		}
		else if(queryPairs.containsKey("update-data"))
		{
			String pkID = queryPairs.getOrDefault("pk_id", "");
			String field = queryPairs.getOrDefault("field", "");
			String value = queryPairs.getOrDefault("value", "");
			if(!field.equals(JsonKey.USERNAME))
			{
				User user;
				try 
				{
					user = WebUserAccount.getUser(pkID);
					if(field.equals(JsonKey.PHONE))
					{
						user.setPhone(value);
					}
					if(field.equals(JsonKey.NAME))
					{
						user.setName(value);
					}
					WebUserAccount.updateUser(user);
					WebUserAccount.save();
				} 
				catch (NoUserRegisteredException e) 
				{
					/**
					 * Do nothing
					 */
				}
			}
		}
		else if(queryPairs.containsKey(JsonKey.UPDATE))
		{
			String username = queryPairs.getOrDefault(JsonKey.USERNAME, "").trim();
			String name = queryPairs.getOrDefault(JsonKey.NAME, "").trim();
			String phone = queryPairs.getOrDefault(JsonKey.PHONE, "").trim();
			String email = queryPairs.getOrDefault(JsonKey.EMAIL, "").trim();
			String password = queryPairs.getOrDefault(JsonKey.PASSWORD, "").trim();
			boolean blocked = queryPairs.getOrDefault(JsonKey.BLOCKED, "").equals("1");
			boolean active = queryPairs.getOrDefault(JsonKey.ACTIVE, "").equals("1");

			if(!username.isEmpty())
			{
				User user;
				try 
				{
					user = WebUserAccount.getUser(username);
					if(!username.equals(loggedUsername) && !user.getUsername().isEmpty())
					{
						user.setUsername(username);
					}
					if(!name.isEmpty())
					{
						user.setName(name);
					}
					user.setPhone(phone);
					user.setEmail(email);
					if(!password.isEmpty())
					{
						user.setPassword(password);
					}
					if(!username.equals(loggedUsername))
					{
						user.setBlocked(blocked);
					}
					if(!username.equals(loggedUsername))
					{
						user.setActive(active);
					}
					WebUserAccount.updateUser(user);
					WebUserAccount.save();
				} 
				catch (NoUserRegisteredException e) 
				{
					/**
					 * Do nothing
					 */
				}
			}
		}
		else if(queryPairs.containsKey(JsonKey.ADD))
		{
			String username = queryPairs.getOrDefault(JsonKey.USERNAME, "");
		    String password = queryPairs.getOrDefault(JsonKey.PASSWORD, "");
		    String email = queryPairs.getOrDefault(JsonKey.EMAIL, "");
		    String name = queryPairs.getOrDefault(JsonKey.NAME, "");
		    String phone = queryPairs.getOrDefault(JsonKey.PHONE, "");
	
		    JSONObject jsonObject = new JSONObject();
			jsonObject.put(JsonKey.USERNAME, username);
			jsonObject.put(JsonKey.NAME, name);
			jsonObject.put(JsonKey.EMAIL, email);
			jsonObject.put(JsonKey.PASSWORD, password);
			jsonObject.put(JsonKey.PHONE, phone);
			jsonObject.put(JsonKey.BLOCKED, false);
			jsonObject.put(JsonKey.ACTIVE, true);
			
			if(!username.isEmpty())
			{
				WebUserAccount.addUser(new User(jsonObject));		
				WebUserAccount.save();
			}		
		}
	}
	private void processAPIUser(String requestBody) {
		Map<String, String> queryPairs = Utility.parseQueryPairs(requestBody);
		if(queryPairs.containsKey(JsonKey.DELETE))
		{
			/**
			 * Delete
			 */
			ConfigAPIUser.load(Config.getUserAPISettingPath());
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String value = entry.getValue();
				ConfigAPIUser.deleteUser(value);
			}
			ConfigAPIUser.save();
		}
		else if(queryPairs.containsKey(JsonKey.DEACTIVATE))
		{
			/**
			 * Deactivate
			 */
			ConfigAPIUser.load(Config.getUserAPISettingPath());
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigAPIUser.deactivate(value);
				}
			}
			ConfigAPIUser.save();
		}
		else if(queryPairs.containsKey(JsonKey.ACTIVATE))
		{
			/**
			 * Activate
			 */
			ConfigAPIUser.load(Config.getUserAPISettingPath());
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigAPIUser.activate(value);
				}
			}
			ConfigAPIUser.save();
		}
		else if(queryPairs.containsKey("block"))
		{
			/**
			 * Block
			 */
			ConfigAPIUser.load(Config.getUserAPISettingPath());
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigAPIUser.block(value);
				}
			}
			ConfigAPIUser.save();
			
		}
		else if(queryPairs.containsKey("unblock"))
		{
			/**
			 * Unblock
			 */
			ConfigAPIUser.load(Config.getUserAPISettingPath());
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigAPIUser.unblock(value);
				}
			}
			ConfigAPIUser.save();
		}
		else if(queryPairs.containsKey(JsonKey.UPDATE))
		{
			String username = queryPairs.getOrDefault(JsonKey.USERNAME, "").trim();
			String name = queryPairs.getOrDefault(JsonKey.NAME, "").trim();
			String phone = queryPairs.getOrDefault(JsonKey.PHONE, "").trim();
			String email = queryPairs.getOrDefault(JsonKey.EMAIL, "").trim();
			String password = queryPairs.getOrDefault(JsonKey.PASSWORD, "").trim();
			boolean blocked = queryPairs.getOrDefault(JsonKey.BLOCKED, "").equals("1");
			boolean active = queryPairs.getOrDefault(JsonKey.ACTIVE, "").equals("1");

			if(!username.isEmpty())
			{
				ConfigAPIUser.load(Config.getUserAPISettingPath());
		
			    JSONObject jsonObject = new JSONObject();
				jsonObject.put(JsonKey.USERNAME, username);
				jsonObject.put(JsonKey.NAME, name);
				jsonObject.put(JsonKey.EMAIL, email);
				jsonObject.put(JsonKey.PHONE, phone);
				jsonObject.put(JsonKey.BLOCKED, blocked);
				jsonObject.put(JsonKey.ACTIVE, active);
				if(!username.isEmpty())
				{
					jsonObject.put(JsonKey.USERNAME, username);
				}
				if(!password.isEmpty())
				{
					jsonObject.put(JsonKey.PASSWORD, password);
				}
				ConfigAPIUser.updateUser(new User(jsonObject));		
				ConfigAPIUser.save();	
			}
		}
		else if(queryPairs.containsKey(JsonKey.ADD))
		{
			ConfigAPIUser.load(Config.getUserAPISettingPath());
		    String username = queryPairs.getOrDefault(JsonKey.USERNAME, "");
		    String password = queryPairs.getOrDefault(JsonKey.PASSWORD, "");
		    String email = queryPairs.getOrDefault(JsonKey.EMAIL, "");
		    String name = queryPairs.getOrDefault(JsonKey.NAME, "");
		    String phone = queryPairs.getOrDefault(JsonKey.PHONE, "");
	
		    JSONObject jsonObject = new JSONObject();
			jsonObject.put(JsonKey.USERNAME, username);
			jsonObject.put(JsonKey.NAME, name);
			jsonObject.put(JsonKey.EMAIL, email);
			jsonObject.put(JsonKey.PASSWORD, password);
			jsonObject.put(JsonKey.PHONE, phone);
			jsonObject.put(JsonKey.BLOCKED, false);
			jsonObject.put(JsonKey.ACTIVE, true);
			
			if(!username.isEmpty())
			{
				ConfigAPIUser.addUser(new User(jsonObject));		
				ConfigAPIUser.save();
			}		
		}
	}

	private void processDDNS(String requestBody, CookieServer cookie) {
		Map<String, String> queryPairs = Utility.parseQueryPairs(requestBody);
		if(queryPairs.containsKey(JsonKey.DELETE))
		{
			/**
			 * Delete
			 */
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigDDNS.deleteRecord(value);
				}
			}
			ConfigDDNS.save();
		}
		else if(queryPairs.containsKey(JsonKey.DEACTIVATE))
		{
			/**
			 * Deactivate
			 */
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigDDNS.deactivate(value);
				}
			}
			ConfigDDNS.save();
		}
		else if(queryPairs.containsKey(JsonKey.ACTIVATE))
		{
			/**
			 * Activate
			 */
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigDDNS.activate(value);
				}
			}
			ConfigDDNS.save();
		}
		else if(queryPairs.containsKey(JsonKey.PROXIED))
		{
			/**
			 * Proxied
			 */
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigDDNS.proxied(value);
				}
			}
			ConfigDDNS.save();
		}
		else if(queryPairs.containsKey(JsonKey.UNPROXIED))
		{
			/**
			 * Unproxied
			 */
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigDDNS.unproxied(value);
				}
			}
			ConfigDDNS.save();
		}
		else if(queryPairs.containsKey(JsonKey.UPDATE))
		{
			String id = queryPairs.getOrDefault(JsonKey.ID, "").trim();
			String provider = queryPairs.getOrDefault(JsonKey.PROVIDER, "").trim();
			String zone = queryPairs.getOrDefault(JsonKey.ZONE, "").trim();
			String recordName = queryPairs.getOrDefault(JsonKey.RECORD_NAME, "").trim();
			String ttls = queryPairs.getOrDefault(JsonKey.TTL, "").trim();
			String cronExpression = queryPairs.getOrDefault(JsonKey.CRON_EXPRESSION, "").trim();
			boolean proxied = queryPairs.getOrDefault(JsonKey.PROXIED, "").equals("1");
			boolean forceCreateZone = queryPairs.getOrDefault(JsonKey.FORCE_CREATE_ZONE, "").equals("1");
			boolean active = queryPairs.getOrDefault(JsonKey.ACTIVE, "").equals("1");
			String type = queryPairs.getOrDefault(JsonKey.TYPE, "0");
			int ttl = Utility.atoi(ttls);
			
			if(!id.isEmpty())
			{
				DDNSRecord record = ConfigDDNS.getRecords().getOrDefault(id, new DDNSRecord());
				if(!id.isEmpty())
				{
					record.setId(id);
				}
				if(!zone.isEmpty())
				{
					record.setZone(zone);
				}
				if(!recordName.isEmpty())
				{
					record.setRecordName(recordName);
				}
				record.setProvider(provider);
				record.setProxied(proxied);
				record.setForceCreateZone(forceCreateZone);
				record.setCronExpression(cronExpression);
				record.setTtl(ttl);
				record.setActive(active);		
				record.setType(type);
				ConfigDDNS.updateRecord(record);
				ConfigDDNS.save();
			}
		}
		else if(queryPairs.containsKey(JsonKey.ADD))
		{
			String provider = queryPairs.getOrDefault(JsonKey.PROVIDER, "").trim();
			String zone = queryPairs.getOrDefault(JsonKey.ZONE, "").trim();
			String recordName = queryPairs.getOrDefault(JsonKey.RECORD_NAME, "").trim();
			String cronExpression = queryPairs.getOrDefault("cron_expression", "").trim();
			boolean proxied = queryPairs.getOrDefault(JsonKey.PROXIED, "").trim().equals("1");
			boolean forceCreateZone = queryPairs.getOrDefault(JsonKey.FORCE_CREATE_ZONE, "").trim().equals("1");
			boolean active = queryPairs.getOrDefault(JsonKey.ACTIVE, "").trim().equals("1");
			
			String ttls = queryPairs.getOrDefault(JsonKey.TTL, "0");
			int ttl = Utility.atoi(ttls);
			String type = queryPairs.getOrDefault(JsonKey.TYPE, "0");
			String id = Utility.md5(zone+":"+recordName);
			DDNSRecord record = new DDNSRecord(id, zone, recordName, type, proxied, ttl, forceCreateZone, provider, active, cronExpression);
			if(!zone.isEmpty() && !recordName.isEmpty())
			{
				ConfigDDNS.getRecords().put(id, record);	
				ConfigDDNS.save();
			}
		}
	}	
}
