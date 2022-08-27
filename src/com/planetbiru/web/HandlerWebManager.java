package com.planetbiru.web;


import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.App;
import com.planetbiru.ServerWebSocketAdmin;
import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigAPI;
import com.planetbiru.config.ConfigAPIUser;
import com.planetbiru.config.ConfigBell;
import com.planetbiru.config.ConfigBlocking;
import com.planetbiru.config.ConfigDDNS;
import com.planetbiru.config.ConfigEmail;
import com.planetbiru.config.ConfigSubscriberAMQP;
import com.planetbiru.config.ConfigSubscriberActiveMQ;
import com.planetbiru.config.ConfigSubscriberMQTT;
import com.planetbiru.config.ConfigSubscriberRedis;
import com.planetbiru.config.ConfigSubscriberStomp;
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
import com.planetbiru.device.ConfigActivation;
import com.planetbiru.device.DeviceAPI;
import com.planetbiru.device.DeviceActivation;
import com.planetbiru.gsm.GSMException;
import com.planetbiru.gsm.GSMUtil;
import com.planetbiru.gsm.InvalidSIMPinException;
import com.planetbiru.gsm.SerialPortConnectionException;
import com.planetbiru.mail.MailUtil;
import com.planetbiru.mail.NoEmailAccountException;
import com.planetbiru.user.NoUserRegisteredException;
import com.planetbiru.user.User;
import com.planetbiru.user.WebUserAccount;
import com.planetbiru.util.CustomHttpClient;
import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.FileUtil;
import com.planetbiru.util.HttpRequestException;
import com.planetbiru.util.HttpResponseString;
import com.planetbiru.util.ServerInfo;
import com.planetbiru.util.Utility;
import com.planetbiru.util.WebManagerContent;
import com.planetbiru.util.WebManagerTool;
import com.sun.net.httpserver.Headers; //NOSONAR
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class HandlerWebManager implements HttpHandler {

	private static Logger logger = Logger.getLogger(HandlerWebManager.class);
	
	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		String path = httpExchange.getRequestURI().getPath();
		String method = httpExchange.getRequestMethod();
		if(path.endsWith(".html") && method.equals(HttpMethod.POST))
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
		if(!Config.isValidDevice() && (path.contains(".html") || path.equals(ConstantString.DOCUMENT_PATH_SEPARATOR)))
		{
			return this.invalidDevice();
		}
		else
		{
			return this.serveDocumentRoot(httpExchange, path);
		}		
	}

	private WebResponse invalidDevice() {
		String fileName = WebManagerTool.getFileName(ConstantString.PATH_SEPARATOR+"invalid-device.html");
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
					responseBody = FileUtil.readResource(WebManagerTool.getFileName(ConstantString.PATH_SEPARATOR+"404.html"));
				} 
				catch (FileNotFoundException e1) 
				{
					logger.error(e1.getMessage(), e1);
				}
			}
		}
		Headers responseHeaders = new Headers();
		responseHeaders.add("Content-type", "text/html");
		return new WebResponse(statusCode, responseHeaders, responseBody);
	}

	private WebResponse serveDocumentRoot(HttpExchange httpExchange, String path) {
		if(path.equals(ConstantString.DOCUMENT_PATH_SEPARATOR))
		{
			path = ConstantString.PATH_SEPARATOR+"index.html";
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
					responseBody = FileUtil.readResource(WebManagerTool.getFileName(ConstantString.PATH_SEPARATOR+"404.html"));
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
		WebManagerContent newContent = this.updateContent(fileName, responseHeaders, responseBody, statusCode, cookie, DeviceActivation.isActivated());	
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

	private WebManagerContent updateContent(String fileName, Headers responseHeaders, byte[] responseBody, int statusCode, CookieServer cookie, boolean activated) {
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
					responseBody = FileUtil.readResource(fileSub);
					return this.updateContent(fileSub, responseHeaders, responseBody, statusCode, cookie, activated);
				}
				responseBody = WebManagerTool.removeMeta(responseBody, activated);
			}
			catch (FileNotFoundException e) 
			{
				statusCode = HttpStatus.NOT_FOUND;
				webContent.setStatusCode(statusCode);
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

	private void handlePost(HttpExchange httpExchange, String path) //NOSONAR
	{
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
				if(path.equals(ConstantString.PATH_SEPARATOR+"keystore.html"))
				{
					this.processKeystore(requestBody);
				}
				else if(path.equals(ConstantString.PATH_SEPARATOR+"keystore-update.html"))
				{
					this.processKeystore(requestBody);
				}
				else if(path.equals(ConstantString.PATH_SEPARATOR+"admin.html"))
				{
					this.processAdmin(requestBody, cookie);
				}
				else if(path.equals(ConstantString.PATH_SEPARATOR+"account-update.html"))
				{
					this.processAccount(requestBody, cookie);
				}
				else if(path.equals(ConstantString.PATH_SEPARATOR+"ddns-record.html"))
				{
					this.processDDNS(requestBody);
				}
				else if(path.equals(ConstantString.PATH_SEPARATOR+"ddns-record-update.html"))
				{
					this.processDDNS(requestBody);
				}
				else if(path.equals(ConstantString.PATH_SEPARATOR+"api-user.html"))
				{
					this.processAPIUser(requestBody);
				}
				else if(path.equals(ConstantString.PATH_SEPARATOR+"api-user-update.html"))
				{
					this.processAPIUser(requestBody);
				}
				else if(path.equals(ConstantString.PATH_SEPARATOR+"subscriber-setting.html"))
				{
					this.processSubscriberSetting(requestBody);
				}
				else if(path.equals(ConstantString.PATH_SEPARATOR+"sms-setting.html"))
				{
					this.processSMSSetting(requestBody);
				}
				else if(path.equals(ConstantString.PATH_SEPARATOR+"modem.html") 
						|| path.equals(ConstantString.PATH_SEPARATOR+"modem-add.html") 
						|| path.equals(ConstantString.PATH_SEPARATOR+"modem-update.html"))
				{
					this.processModemSetting(requestBody);
				}
				else if(path.equals(ConstantString.PATH_SEPARATOR+"email-account.html"))
				{
					this.processEmailAccount(requestBody);
				}
				else if(path.equals(ConstantString.PATH_SEPARATOR+"sms.html"))
				{
					this.processSMS(requestBody);
				}
				else if(path.equals(ConstantString.PATH_SEPARATOR+"api-setting.html"))
				{
					this.processAPISetting(requestBody);
				}
				else if(path.equals(ConstantString.PATH_SEPARATOR+"general-setting.html"))
				{
					this.processGeneralSetting(requestBody);
				}
				else if(path.equals(ConstantString.PATH_SEPARATOR+"bell-setting.html"))
				{
					this.processBellSetting(requestBody);
				}
				else if(path.equals(ConstantString.PATH_SEPARATOR+"date-time-setting.html"))
				{
					this.processDateTimeSetting(requestBody);
				}
				else if(path.equals(ConstantString.PATH_SEPARATOR+"cloudflare.html"))
				{
					this.processCloudflareSetting(requestBody);
				}
				else if(path.equals(ConstantString.PATH_SEPARATOR+"noip.html"))
				{
					this.processNoIPSetting(requestBody);
				}
				else if(path.equals(ConstantString.PATH_SEPARATOR+"afraid.html"))
				{
					this.processAfraidSetting(requestBody);
				}
				else if(path.equals(ConstantString.PATH_SEPARATOR+"dynu.html"))
				{
					this.processDynuSetting(requestBody);
				}
				else if(path.equals(ConstantString.PATH_SEPARATOR+"network-setting.html"))
				{
					this.processNetworkSetting(requestBody);
				}
				else if(path.equals(ConstantString.PATH_SEPARATOR+"block-list.html"))
				{
					this.processBlockList(requestBody);
				}
				else if(path.equals(ConstantString.PATH_SEPARATOR+"firewall.html"))
				{
					this.processFirewall(requestBody);
				}
				else if(path.equals(ConstantString.PATH_SEPARATOR+"logs.html"))
				{
					this.processDeleteLog(requestBody);
				}
				else if(path.equals(ConstantString.PATH_SEPARATOR+"sms-report.html"))
				{
					this.processDeleteReport(requestBody);
				}
				else if(path.equals(ConstantString.PATH_SEPARATOR+"smtp-setting.html"))
				{
					this.processSMTPSetting(requestBody);
				}
				else if(path.equals(ConstantString.PATH_SEPARATOR+"forget-password.html"))
				{
					this.processForgetPassword(requestBody);
				}
				else if(path.equals(ConstantString.PATH_SEPARATOR+"activation.html"))
				{
					this.processActivation(requestBody);
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
	
	private void processActivation(String requestBody) {
		String username = ConfigActivation.getUsername();
		String password = ConfigActivation.getPassword();
		String method = ConfigActivation.getMethod();
		String url = ConfigActivation.getUrl();
		String contentType = ConfigActivation.getContentType();
		String authorization = ConfigActivation.getAuthorization();
		int timeout = ConfigActivation.getRequestTimeout();		
		
		Map<String, String> queryPairs = Utility.parseQueryPairs(requestBody);					
		Map<String, String> parameters = new HashMap<>();

		Headers requestHeaders = new Headers();
		requestHeaders.add(ConstantString.ACCEPT, contentType);
		requestHeaders.add(ConstantString.AUTHORIZATION, authorization+" "+Utility.base64Encode(username+":"+password));
		String requestBody2 = "";
		
		if(method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT))
		{
			if(contentType.toLowerCase().contains("json"))
			{
				JSONObject requestJSON = new JSONObject(queryPairs);
				requestBody2 = requestJSON.toString(0);
			}
			else
			{
				requestBody2 = requestBody;
			}
			parameters = null;
		}
		else if(method.equals(HttpMethod.GET))
		{
			requestBody2 = null;			
			parameters = queryPairs;
		}
		
		JSONObject responseJSON = new JSONObject();
		try 
		{
			HttpResponseString response = CustomHttpClient.httpExchange(method, url, parameters, requestHeaders, requestBody2, timeout);
			responseJSON = new JSONObject(response.body());
			JSONObject data = responseJSON.optJSONObject(JsonKey.DATA);
			Map<String, Object> map = data.toMap();
			String cpuSN = ServerInfo.cpuSerialNumber();
			String activationCode = DeviceActivation.activate(map, cpuSN);
			DeviceActivation.verify(activationCode, cpuSN);
			
		} 
		catch (JSONException | HttpRequestException | InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidAlgorithmParameterException | BadPaddingException | IllegalBlockSizeException | IllegalArgumentException e) 
		{
			e.printStackTrace();
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
					MailUtil.send(email, "Account Information", message, ste);
				}
				else if(!phone.isEmpty())
				{
					String message = "Username : "+user.getUsername()+"\r\nPassword : "+user.getPassword();
					GSMUtil.sendSMS(phone, message, ste);
				}
			}
		} 
		catch (NoUserRegisteredException | MessagingException | NoEmailAccountException | GSMException | InvalidSIMPinException | SerialPortConnectionException e1) 
		{
			/**
			 * Do nothing
			 */
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
			boolean active = queryPairs.getOrDefault(JsonKey.ACTIVE, "").trim().equals("1");
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
					String path = FileConfigUtil.removeParentWithDot(ConstantString.DOCUMENT_PATH_SEPARATOR+value);
					String dir = Config.getLogDir();
					String fileName = FileConfigUtil.fixFileName(dir+path);
					File file = new File(fileName);
					try 
					{
						FileConfigUtil.deleteDirectoryWalkTree(file.toPath());
					} 
					catch (IOException e) 
					{
						logger.error(e.getMessage(), e);
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
					String path = FileConfigUtil.removeParentWithDot(ConstantString.DOCUMENT_PATH_SEPARATOR+value);
					String dir = Config.getSmsLogPath();
					String fileName = FileConfigUtil.fixFileName(dir+path);
					File file = new File(fileName);
					try 
					{
						FileConfigUtil.deleteDirectoryWalkTree(file.toPath());
					} 
					catch (IOException e) 
					{
						logger.error(e.getMessage(), e);
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
			ConfigFirewall.delete(queryPairs);
			ConfigFirewall.save();
		}
		if(queryPairs.containsKey(JsonKey.ACTIVATE))
		{
			ConfigFirewall.load(Config.getFirewallSettingPath());
			ConfigFirewall.activate(queryPairs);
			ConfigFirewall.save();
		}
		if(queryPairs.containsKey(JsonKey.DEACTIVATE))
		{
			ConfigFirewall.load(Config.getFirewallSettingPath());
			ConfigFirewall.deactivate(queryPairs);
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
			ConfigBlocking.delete(queryPairs);
			ConfigBlocking.save();
		}
		if(queryPairs.containsKey(JsonKey.BLOCK))
		{
			ConfigBlocking.load(Config.getBlockingSettingPath());
			ConfigBlocking.block(queryPairs);
			ConfigBlocking.save();
		}
		if(queryPairs.containsKey(JsonKey.UNBLOCK))
		{
			ConfigBlocking.load(Config.getBlockingSettingPath());
			ConfigBlocking.unblock(queryPairs);
			ConfigBlocking.save();
		}		
		if(queryPairs.containsKey(JsonKey.ADD))
		{
			ConfigBlocking.load(Config.getBlockingSettingPath());
			ConfigBlocking.addList(msisdn);
			ConfigBlocking.save();
		}
	}
	
	private void processKeystore(String requestBody) {
		Map<String, String> queryPairs = Utility.parseQueryPairs(requestBody);
		if(queryPairs.containsKey(JsonKey.DELETE))
		{
			ConfigKeystore.load(Config.getKeystoreSettingPath());
			ConfigKeystore.delete(queryPairs);
			ConfigKeystore.save();
		}
		if(queryPairs.containsKey(JsonKey.DEACTIVATE))
		{
			ConfigKeystore.load(Config.getKeystoreSettingPath());
			ConfigKeystore.deactivate(queryPairs);
			ConfigKeystore.save();
		}
		if(queryPairs.containsKey(JsonKey.ACTIVATE))
		{
			ConfigKeystore.load(Config.getKeystoreSettingPath());
			ConfigKeystore.activate(queryPairs);
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
			String exp = queryPairs.getOrDefault("otp_expiration_offset", "0").trim();
			String insp = queryPairs.getOrDefault("inspect_modem_interval", "0").trim();
			long otpExpirationOffset = Utility.atol(exp);
			long inspectModemInterval = Utility.atol(insp);
			boolean dropExpireOTP = queryPairs.getOrDefault("drop_expire_otp", "").trim().equals("1");
			
			ConfigGeneral.setDeviceName(deviceName2);
			ConfigGeneral.setDeviceTimeZone(deviceTimeZone);
			ConfigGeneral.setNtpServer(ntpServer);
			ConfigGeneral.setNtpUpdateInterval(ntpUpdateInterval);
			ConfigGeneral.setRestartService(restartService);
			ConfigGeneral.setRestartDevice(restartDevice);
			ConfigGeneral.setOtpExpirationOffset(otpExpirationOffset);
			ConfigGeneral.setDropExpireOTP(dropExpireOTP);
			ConfigGeneral.setInspectModemInterval(inspectModemInterval);
			
			ConfigGeneral.save();
			DeviceAPI.setTimeZone(deviceTimeZone);
		}
	}
	
	private void processBellSetting(String requestBody) {
		Map<String, String> queryPairs = Utility.parseQueryPairs(requestBody);
		if(queryPairs.containsKey("save_bell_setting"))
		{
			ConfigGeneral.load(Config.getGeneralSettingPath());
			
			boolean smsFailure = queryPairs.getOrDefault("sms_failure", "").trim().equals("1");
			boolean amqpDisconnected = queryPairs.getOrDefault("amqp_disconnected", "").trim().equals("1");
			boolean mqttDisconnected = queryPairs.getOrDefault("mqtt_disconnected", "").trim().equals("1");
			boolean redisDisconnected = queryPairs.getOrDefault("redis_disconnected", "").trim().equals("1");
			boolean wsDisconnected = queryPairs.getOrDefault("ws_disconnected", "").trim().equals("1");
			
			ConfigBell.setSmsFailure(smsFailure);
			ConfigBell.setAmqpDisconnected(amqpDisconnected);
			ConfigBell.setMqttDisconnected(mqttDisconnected);
			ConfigBell.setRedisDisconnected(redisDisconnected);
			ConfigBell.setWsDisconnected(wsDisconnected);
			
			ConfigBell.save();			
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
			
			String lOTPPath = queryPairs.getOrDefault("otp_path", "").trim();
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
			config.put("otpPath", lOTPPath);
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
			String netmask = queryPairs.getOrDefault(JsonKey.NETMASK, "").trim();
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
			JSONArray rangeList = this.createRange(ranges);			
			
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
			ConfigNetWLAN.setNetmask(queryPairs.getOrDefault(JsonKey.NETMASK, "").trim());
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
			ConfigNetEthernet.setNetmask(queryPairs.getOrDefault(JsonKey.NETMASK, "").trim());
			ConfigNetEthernet.setGateway(queryPairs.getOrDefault("gateway", "").trim());
			ConfigNetEthernet.setDns1(queryPairs.getOrDefault("dns1", "").trim());
			ConfigNetEthernet.setDns2(queryPairs.getOrDefault("dns2", "").trim());
			ConfigNetEthernet.save();
			ConfigNetEthernet.apply(Config.getOsEthernetConfigPath());
		}
	}

	private JSONArray createRange(String ranges) {
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
		return rangeList;
	}

	private void processCloudflareSetting(String requestBody) {
		Map<String, String> queryPairs = Utility.parseQueryPairs(requestBody);
		String endpoint = queryPairs.getOrDefault(JsonKey.ENDPOINT, "").trim();
		String accountId = queryPairs.getOrDefault("account_id", "").trim();
		String authEmail = queryPairs.getOrDefault("auth_email", "").trim();
		String authApiKey = queryPairs.getOrDefault("auth_api_key", "").trim();
		String authToken = queryPairs.getOrDefault("auth_token", "").trim();
		boolean active = queryPairs.getOrDefault(JsonKey.ACTIVE, "").trim().equals("1");
		
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
		String endpoint = queryPairs.getOrDefault(JsonKey.ENDPOINT, "").trim();
		String username = queryPairs.getOrDefault(JsonKey.USERNAME, "").trim();
		String email = queryPairs.getOrDefault(JsonKey.EMAIL, "").trim();
		String password = queryPairs.getOrDefault(JsonKey.PASSWORD, "").trim();
		String company = queryPairs.getOrDefault(JsonKey.COMPANY, "").trim();
		boolean active = queryPairs.getOrDefault(JsonKey.ACTIVE, "").trim().equals("1");
		
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
		String endpoint = queryPairs.getOrDefault(JsonKey.ENDPOINT, "").trim();
		String username = queryPairs.getOrDefault(JsonKey.USERNAME, "").trim();
		String email = queryPairs.getOrDefault(JsonKey.EMAIL, "").trim();
		String password = queryPairs.getOrDefault(JsonKey.PASSWORD, "").trim();
		String company = queryPairs.getOrDefault(JsonKey.COMPANY, "").trim();
		boolean active = queryPairs.getOrDefault(JsonKey.ACTIVE, "").trim().equals("1");
		
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
		String endpoint = queryPairs.getOrDefault(JsonKey.ENDPOINT, "").trim();
		String username = queryPairs.getOrDefault(JsonKey.USERNAME, "").trim();
		String email = queryPairs.getOrDefault(JsonKey.EMAIL, "").trim();
		String password = queryPairs.getOrDefault(JsonKey.PASSWORD, "").trim();
		String company = queryPairs.getOrDefault(JsonKey.COMPANY, "").trim();
		boolean active = queryPairs.getOrDefault(JsonKey.ACTIVE, "").trim().equals("1");
		
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
			ConfigEmail.delete(queryPairs);
			ConfigEmail.save();
		}
		if(queryPairs.containsKey(JsonKey.DEACTIVATE))
		{
			ConfigEmail.deactivate(queryPairs);
			ConfigEmail.save();
		}
		if(queryPairs.containsKey(JsonKey.ACTIVATE))
		{
			ConfigEmail.activate(queryPairs);
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
			dataEmail.setAuth(auth);
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
			dataEmail.setAuth(auth);
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
			ConfigModem.delete(queryPairs);
			ConfigModem.save();
		}
		if(queryPairs.containsKey(JsonKey.DEACTIVATE))
		{
			ConfigModem.deactivate(queryPairs);
			ConfigModem.save();
		}
		if(queryPairs.containsKey(JsonKey.ACTIVATE))
		{
			ConfigModem.activate(queryPairs);
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
		if(action.equals(JsonKey.ADD) || id.isEmpty())
		{
			id = Utility.md5(String.format("%d", System.nanoTime()));
		}
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

		String name = queryPairs.getOrDefault("name", "").trim();

		String manufacturer = queryPairs.getOrDefault("manufacturer", "").trim();
		String model = queryPairs.getOrDefault("model", "").trim();
		String revision = queryPairs.getOrDefault("revision", "").trim();
		String iccid = queryPairs.getOrDefault("iccid", "").trim();

		String imei = queryPairs.getOrDefault("imei", "").trim();
		String copsOperator = queryPairs.getOrDefault("cops_operator", "").trim();
		String imsi = queryPairs.getOrDefault("imsi", "").trim();
		String msisdn = queryPairs.getOrDefault("msisdn", "").trim();
		String recipientPrefix = queryPairs.getOrDefault("recipient_prefix", "").trim();
		boolean deleteSentSMS = queryPairs.getOrDefault("delete_sent_sms", "").trim().equals("1");
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
		modem.setId(id);
		modem.setManufacturer(manufacturer);
		modem.setModel(model);
		modem.setRevision(revision);
		modem.setIccid(iccid);
		modem.setName(name);
		modem.setPort(port);
		modem.setSmsCenter(smsCenter);
		modem.setIncommingInterval(incommingInterval);
		modem.setTimeRange(timeRange);
		modem.setMaxPerTimeRange(maxPerTimeRange);
		modem.setImei(imei);
		modem.setcopsOperator(copsOperator);
		modem.setMsisdn(msisdn);
		modem.setImsi(imsi);
		modem.setRecipientPrefix(recipientPrefix);
		modem.setSimCardPIN(simCardPIN);
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
		modem.setDeleteSentSMS(deleteSentSMS);
		
		boolean duplicated = false;
		if(action.equals(JsonKey.ADD))
		{
			duplicated = ConfigModem.isDuuplicated(port, null);
		}
		else
		{
			duplicated = ConfigModem.isDuuplicated(port, id);
		}	
		if(!duplicated)
		{
			ConfigModem.update(id, modem);
			ConfigModem.save();	
		}
	}

	private void processSubscriberSetting(String requestBody) //NOSONAR
	{
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
			
			if(ConfigSubscriberWS.isSubscriberWsEnable())
			{
				App.subscriberWSStart();
			}
			else
			{
				App.subscriberWSStop(true);
			}
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
			String version = queryPairs.getOrDefault("subscriber_amqp_version", "0");
			int subscriberAmqpVersion = Utility.atoi(version);
			
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
			
			boolean changeVerstion = (ConfigSubscriberAMQP.getSubscriberAmqpVersion() != subscriberAmqpVersion);
			
			ConfigSubscriberAMQP.setSubscriberAmqpVersion(subscriberAmqpVersion);
			ConfigSubscriberAMQP.save();	
			
			if(changeVerstion)
			{
				App.subscriberAMQPStop(true);
			}
			if(ConfigSubscriberAMQP.isSubscriberAmqpEnable())
			{
				App.subscriberAMQPStart();
			}
			else
			{
				App.subscriberAMQPStop(true);
			}
		}	
		
		if(queryPairs.containsKey("save_subscriber_active_mq_setting"))
		{
			ConfigSubscriberActiveMQ.load(Config.getSubscriberActiveMQSettingPath());
			boolean subscriberActiveMQEnable = queryPairs.getOrDefault("subscriber_active_mq_enable", "").equals("1");		
			boolean subscriberActiveMQSSL = queryPairs.getOrDefault("subscriber_active_mq_ssl", "").equals("1");		
			String subscriberActiveMQAddress = queryPairs.getOrDefault("subscriber_active_mq_address", "");		
			String port = queryPairs.getOrDefault("subscriber_active_mq_port", "0");
			int subscriberActiveMQPort = Utility.atoi(port);
			String subscriberActiveMQClientID = queryPairs.getOrDefault("subscriber_active_mq_client_id", "");		
			String subscriberActiveMQUsername = queryPairs.getOrDefault("subscriber_active_mq_username", "");		
			String subscriberActiveMQPassword = queryPairs.getOrDefault("subscriber_active_mq_password", "");		
			String subscriberActiveMQTopic = queryPairs.getOrDefault("subscriber_active_mq_topic", "");
			
			String timeout = queryPairs.getOrDefault("subscriber_active_mq_timeout", "0");
			int subscriberActiveMQTimeout = Utility.atoi(timeout);	
			String refresh = queryPairs.getOrDefault("subscriber_active_mq_refresh", "0");
			int subscriberActiveMQRefresh = Utility.atoi(refresh);

			String reconnect = queryPairs.getOrDefault("subscriber_active_mq_reconnect_delay", "0");
			int subscriberActiveMQReconnectDelay = Utility.atoi(reconnect);

			String ttl = queryPairs.getOrDefault("subscriber_active_mq_time_to_leave", "0");
			long subscriberActiveMQTimeToLeave = Utility.atoi(ttl);	

			ConfigSubscriberActiveMQ.setSubscriberActiveMQEnable(subscriberActiveMQEnable);
			ConfigSubscriberActiveMQ.setSubscriberActiveMQSSL(subscriberActiveMQSSL);
			ConfigSubscriberActiveMQ.setSubscriberActiveMQAddress(subscriberActiveMQAddress);
			ConfigSubscriberActiveMQ.setSubscriberActiveMQPort(subscriberActiveMQPort);
			ConfigSubscriberActiveMQ.setSubscriberActiveMQClientID(subscriberActiveMQClientID);
			ConfigSubscriberActiveMQ.setSubscriberActiveMQUsername(subscriberActiveMQUsername);
			ConfigSubscriberActiveMQ.setSubscriberActiveMQPassword(subscriberActiveMQPassword);
			ConfigSubscriberActiveMQ.setSubscriberActiveMQTopic(subscriberActiveMQTopic);
			ConfigSubscriberActiveMQ.setSubscriberActiveMQTimeout(subscriberActiveMQTimeout);
			ConfigSubscriberActiveMQ.setSubscriberActiveMQTimeToLeave(subscriberActiveMQTimeToLeave);
			ConfigSubscriberActiveMQ.setSubscriberActiveMQRefresh(subscriberActiveMQRefresh);		
			ConfigSubscriberActiveMQ.setSubscriberActiveMQReconnectDelay(subscriberActiveMQReconnectDelay);			

			ConfigSubscriberActiveMQ.save();	
			if(ConfigSubscriberActiveMQ.isSubscriberActiveMQEnable())
			{
				App.subscriberActiveMQStart();
			}
			else
			{
				App.subscriberActiveMQStop(true);
			}
		}	
		
		
		if(queryPairs.containsKey("save_subscriber_redis_setting"))
		{
			ConfigSubscriberRedis.load(Config.getSubscriberRedisSettingPath());
			boolean subscriberRedisEnable = queryPairs.getOrDefault("subscriber_redis_enable", "").equals("1");		
			boolean subscriberRedisSSL = queryPairs.getOrDefault("subscriber_redis_ssl", "").equals("1");		
			String subscriberRedisAddress = queryPairs.getOrDefault("subscriber_redis_address", "");		
			String port = queryPairs.getOrDefault("subscriber_redis_port", "0");
			int subscriberRedisPort = Utility.atoi(port);
			String subscriberRedisPath = queryPairs.getOrDefault("subscriber_redis_path", "");		
			String subscriberRedisUsername = queryPairs.getOrDefault("subscriber_redis_username", "");		
			String subscriberRedisPassword = queryPairs.getOrDefault("subscriber_redis_password", "");		
			String subscriberRedisTopic = queryPairs.getOrDefault("subscriber_redis_topic", "");
			
			String timeout = queryPairs.getOrDefault("subscriber_redis_timeout", "0");
			int subscriberRedisTimeout = Utility.atoi(timeout);	
			String reconnect = queryPairs.getOrDefault("subscriber_redis_reconnect_delay", "0");
			int subscriberRedisReconnectDelay = Utility.atoi(reconnect);	
			String refresh = queryPairs.getOrDefault("subscriber_redis_refresh", "0");
			int subscriberRedisRefresh = Utility.atoi(refresh);
			
			ConfigSubscriberRedis.setSubscriberRedisEnable(subscriberRedisEnable);
			ConfigSubscriberRedis.setSubscriberRedisSSL(subscriberRedisSSL);
			ConfigSubscriberRedis.setSubscriberRedisAddress(subscriberRedisAddress);
			ConfigSubscriberRedis.setSubscriberRedisPort(subscriberRedisPort);
			ConfigSubscriberRedis.setSubscriberRedisPath(subscriberRedisPath);
			ConfigSubscriberRedis.setSubscriberRedisUsername(subscriberRedisUsername);
			ConfigSubscriberRedis.setSubscriberRedisPassword(subscriberRedisPassword);
			ConfigSubscriberRedis.setSubscriberRedisTopic(subscriberRedisTopic);
			ConfigSubscriberRedis.setSubscriberRedisReconnectDelay(subscriberRedisReconnectDelay);
			ConfigSubscriberRedis.setSubscriberRedisTimeout(subscriberRedisTimeout);
			ConfigSubscriberRedis.setSubscriberRedisRefresh(subscriberRedisRefresh);		

			ConfigSubscriberRedis.save();	
			
			if(ConfigSubscriberRedis.isSubscriberRedisEnable())
			{
				App.subscriberRedisStart();
			}
			else
			{
				App.subscriberRedisStop(true);
			}
		}	
		
		if(queryPairs.containsKey("save_subscriber_stomp_setting"))
		{
			ConfigSubscriberStomp.load(Config.getSubscriberStompSettingPath());
			boolean subscriberStompEnable = queryPairs.getOrDefault("subscriber_stomp_enable", "").equals("1");		
			boolean subscriberStompSSL = queryPairs.getOrDefault("subscriber_stomp_ssl", "").equals("1");		
			String subscriberStompAddress = queryPairs.getOrDefault("subscriber_stomp_address", "");		
			String port = queryPairs.getOrDefault("subscriber_stomp_port", "0");
			int subscriberStompPort = Utility.atoi(port);
			String subscriberStompUsername = queryPairs.getOrDefault("subscriber_stomp_username", "");		
			String subscriberStompPassword = queryPairs.getOrDefault("subscriber_stomp_password", "");		
			String subscriberStompTopic = queryPairs.getOrDefault("subscriber_stomp_topic", "");
			
			String timeout = queryPairs.getOrDefault("subscriber_stomp_timeout", "0");
			int subscriberStompTimeout = Utility.atoi(timeout);	
			String reconnect = queryPairs.getOrDefault("subscriber_stomp_reconnect_delay", "0");
			int subscriberStompReconnectDelay = Utility.atoi(reconnect);	
			String refresh = queryPairs.getOrDefault("subscriber_stomp_refresh", "0");
			int subscriberStompRefresh = Utility.atoi(refresh);

			String database = queryPairs.getOrDefault("subscriber_stomp_database", "0");
			int subscriberStompDatabase = Utility.atoi(database);

			ConfigSubscriberStomp.setSubscriberStompEnable(subscriberStompEnable);
			ConfigSubscriberStomp.setSubscriberStompSSL(subscriberStompSSL);
			ConfigSubscriberStomp.setSubscriberStompAddress(subscriberStompAddress);
			ConfigSubscriberStomp.setSubscriberStompPort(subscriberStompPort);
			ConfigSubscriberStomp.setSubscriberStompDatabase(subscriberStompDatabase);
			ConfigSubscriberStomp.setSubscriberStompUsername(subscriberStompUsername);
			ConfigSubscriberStomp.setSubscriberStompPassword(subscriberStompPassword);
			ConfigSubscriberStomp.setSubscriberStompTopic(subscriberStompTopic);
			ConfigSubscriberStomp.setSubscriberStompReconnectDelay(subscriberStompReconnectDelay);
			ConfigSubscriberStomp.setSubscriberStompTimeout(subscriberStompTimeout);
			ConfigSubscriberStomp.setSubscriberStompRefresh(subscriberStompRefresh);		

			ConfigSubscriberStomp.save();	
			
			if(ConfigSubscriberStomp.isSubscriberStompEnable())
			{
				/**
				 * Do nothing
				 */
			}
		}	
		
		if(queryPairs.containsKey("save_subscriber_mqtt_setting"))
		{
			ConfigSubscriberMQTT.load(Config.getSubscriberMQTTSettingPath());
			boolean subscriberMqttEnable = queryPairs.getOrDefault("subscriber_mqtt_enable", "").equals("1");		
			boolean subscriberMqttSSL = queryPairs.getOrDefault("subscriber_mqtt_ssl", "").equals("1");		
			String subscriberMqttAddress = queryPairs.getOrDefault("subscriber_mqtt_address", "");		
			String port = queryPairs.getOrDefault("subscriber_mqtt_port", "0");
			int subscriberMqttPort = Utility.atoi(port);
			String subscriberMqttClientID = queryPairs.getOrDefault("subscriber_mqtt_client_id", "");		
			String subscriberMqttUsername = queryPairs.getOrDefault("subscriber_mqtt_username", "");		
			String subscriberMqttPassword = queryPairs.getOrDefault("subscriber_mqtt_password", "");		
			String subscriberMqttTopic = queryPairs.getOrDefault("subscriber_mqtt_topic", "");
			
			String timeout = queryPairs.getOrDefault("subscriber_mqtt_timeout", "0");
			int subscriberMqttTimeout = Utility.atoi(timeout);	
			String refresh = queryPairs.getOrDefault("subscriber_mqtt_refresh", "0");
			int subscriberMqttRefresh = Utility.atoi(refresh);
			String reconnectDelay = queryPairs.getOrDefault("subscriber_mqtt_reconnect_delay", "0");
			long subscriberMqttReconnectDelay = Utility.atol(reconnectDelay);
			String mqttQosStr = queryPairs.getOrDefault("subscriber_mqtt_qos", "0");
			long subscriberMqttQos = Utility.atol(mqttQosStr);
			
			ConfigSubscriberMQTT.setSubscriberMqttEnable(subscriberMqttEnable);
			ConfigSubscriberMQTT.setSubscriberMqttSSL(subscriberMqttSSL);
			ConfigSubscriberMQTT.setSubscriberMqttAddress(subscriberMqttAddress);
			ConfigSubscriberMQTT.setSubscriberMqttPort(subscriberMqttPort);
			ConfigSubscriberMQTT.setSubscriberMqttClientID(subscriberMqttClientID);
			ConfigSubscriberMQTT.setSubscriberMqttUsername(subscriberMqttUsername);
			ConfigSubscriberMQTT.setSubscriberMqttPassword(subscriberMqttPassword);
			ConfigSubscriberMQTT.setSubscriberMqttTopic(subscriberMqttTopic);
			ConfigSubscriberMQTT.setSubscriberMqttTimeout(subscriberMqttTimeout);
			ConfigSubscriberMQTT.setSubscriberMqttReconnectDelay(subscriberMqttReconnectDelay);
			ConfigSubscriberMQTT.setSubscriberMqttRefresh(subscriberMqttRefresh);		
			ConfigSubscriberMQTT.setSubscriberMqttQos(subscriberMqttQos);		

			ConfigSubscriberMQTT.save();	
			
			if(ConfigSubscriberMQTT.isSubscriberMqttEnable())
			{
				App.subscriberMQTTStart();
			}
			else
			{
				App.subscriberMQTTStop(true);
			}
		}	
		ServerWebSocketAdmin.broadcastServerInfo(ConstantString.SERVICE_ALL);
	}
	
	private void processSMS(String requestBody) {
		Map<String, String> queryPairs = Utility.parseQueryPairs(requestBody);
		if(queryPairs.containsKey("send"))
		{
			String receiver = queryPairs.getOrDefault(JsonKey.RECEIVER, "").trim();			
			String message = queryPairs.getOrDefault(JsonKey.MESSAGE, "").trim();	
			String modemID = queryPairs.getOrDefault(JsonKey.MODEM_ID, "").trim();	
			if(!receiver.isEmpty() && !message.isEmpty())
			{
				try 
				{
					String modemName = GSMUtil.getModemName(modemID);
					message = "Sending a message to "+receiver+" via "+modemName;
					HttpUtil.broardcastWebSocket(message);
					JSONObject response = GSMUtil.sendSMS(receiver, message, modemID);
					if(response.optString("result", "").contains("OK"))
					{
						message = "Waiting for the operator to send the message to the recipient";
					}
					else
					{
						message = "Error occured while sending a message to "+receiver+" via "+modemName;
					}
					HttpUtil.broardcastWebSocket(message);
				} 
				catch (GSMException | InvalidSIMPinException e) 
				{
					HttpUtil.broardcastWebSocket(e.getMessage());
				}
			}
		}		
	}
	
	private void processAccount(String requestBody, CookieServer cookie) {
		Map<String, String> queryPairs = Utility.parseQueryPairs(requestBody);
		String loggedUsername = cookie.getSessionValue(JsonKey.USERNAME, "");
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
		String loggedUsername = cookie.getSessionValue(JsonKey.USERNAME, "");
		if(queryPairs.containsKey(JsonKey.DELETE))
		{
			/**
			 * Delete
			 */
			WebUserAccount.delete(queryPairs, loggedUsername);
			WebUserAccount.save();
		}
		else if(queryPairs.containsKey(JsonKey.DEACTIVATE))
		{
			/**
			 * Deactivate
			 */
			WebUserAccount.deactivate(queryPairs, loggedUsername);
			WebUserAccount.save();
		}
		else if(queryPairs.containsKey(JsonKey.ACTIVATE))
		{
			/**
			 * Activate
			 */
			WebUserAccount.activate(queryPairs);
			WebUserAccount.save();
		}
		else if(queryPairs.containsKey("block"))
		{
			/**
			 * Block
			 */
			WebUserAccount.block(queryPairs, loggedUsername);
			WebUserAccount.save();		
		}
		else if(queryPairs.containsKey("unblock"))
		{
			/**
			 * Unblock
			 */
			WebUserAccount.unblock(queryPairs);
			WebUserAccount.save();
		}
		else if(queryPairs.containsKey("update-data"))
		{
			WebUserAccount.updateData(queryPairs);
			WebUserAccount.save();
		}
		else if(queryPairs.containsKey(JsonKey.UPDATE))
		{
			WebUserAccount.update(queryPairs, loggedUsername);
			WebUserAccount.save();
		}
		else if(queryPairs.containsKey(JsonKey.ADD))
		{
			WebUserAccount.add(queryPairs);	
			WebUserAccount.save();
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
			ConfigAPIUser.delete(queryPairs);
			ConfigAPIUser.save();
		}
		else if(queryPairs.containsKey(JsonKey.DEACTIVATE))
		{
			/**
			 * Deactivate
			 */
			ConfigAPIUser.load(Config.getUserAPISettingPath());
			ConfigAPIUser.deactivate(queryPairs);
			ConfigAPIUser.save();
		}
		else if(queryPairs.containsKey(JsonKey.ACTIVATE))
		{
			/**
			 * Activate
			 */
			ConfigAPIUser.load(Config.getUserAPISettingPath());
			ConfigAPIUser.activate(queryPairs);
			ConfigAPIUser.save();
		}
		else if(queryPairs.containsKey("block"))
		{
			/**
			 * Block
			 */
			ConfigAPIUser.load(Config.getUserAPISettingPath());
			ConfigAPIUser.block(queryPairs);
			ConfigAPIUser.save();
			
		}
		else if(queryPairs.containsKey("unblock"))
		{
			/**
			 * Unblock
			 */
			ConfigAPIUser.load(Config.getUserAPISettingPath());
			ConfigAPIUser.unblock(queryPairs);
			ConfigAPIUser.save();
		}
		else if(queryPairs.containsKey(JsonKey.UPDATE))
		{
			ConfigAPIUser.update(queryPairs);
			ConfigAPIUser.save();	
		}
		else if(queryPairs.containsKey(JsonKey.ADD))
		{
			ConfigAPIUser.load(Config.getUserAPISettingPath());
		    ConfigAPIUser.add(queryPairs);
			ConfigAPIUser.save();
		}
	}

	private void processDDNS(String requestBody) {
		Map<String, String> queryPairs = Utility.parseQueryPairs(requestBody);
		if(queryPairs.containsKey(JsonKey.DELETE))
		{
			/**
			 * Delete
			 */
			ConfigDDNS.delete(queryPairs);
			ConfigDDNS.save();
		}
		else if(queryPairs.containsKey(JsonKey.DEACTIVATE))
		{
			/**
			 * Deactivate
			 */
			ConfigDDNS.deactivate(queryPairs);
			ConfigDDNS.save();
		}
		else if(queryPairs.containsKey(JsonKey.ACTIVATE))
		{
			/**
			 * Activate
			 */
			ConfigDDNS.activate(queryPairs);
			ConfigDDNS.save();
		}
		else if(queryPairs.containsKey(JsonKey.PROXIED))
		{
			/**
			 * Proxied
			 */
			ConfigDDNS.proxied(queryPairs);
			ConfigDDNS.save();
		}
		else if(queryPairs.containsKey(JsonKey.UNPROXIED))
		{
			/**
			 * Unproxied
			 */
			ConfigDDNS.unproxied(queryPairs);
			ConfigDDNS.save();
		}
		else if(queryPairs.containsKey(JsonKey.UPDATE))
		{
			ConfigDDNS.update(queryPairs);
			ConfigDDNS.save();
		}
		else if(queryPairs.containsKey(JsonKey.ADD))
		{
			ConfigDDNS.add(queryPairs);
			ConfigDDNS.save();
		}
	}	
}
