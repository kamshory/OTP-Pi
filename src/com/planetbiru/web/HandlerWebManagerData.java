package com.planetbiru.web;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigAPI;
import com.planetbiru.config.ConfigAPIUser;
import com.planetbiru.config.ConfigBlocking;
import com.planetbiru.config.ConfigDDNS;
import com.planetbiru.config.ConfigEmail;
import com.planetbiru.config.ConfigFeederAMQP;
import com.planetbiru.config.ConfigFeederWS;
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
import com.planetbiru.constant.ConstantString;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.cookie.CookieServer;
import com.planetbiru.user.NoUserRegisteredException;
import com.planetbiru.user.WebUserAccount;
import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.FileUtil;
import com.planetbiru.util.ServerInfo;
import com.planetbiru.util.ServerStatus;
import com.planetbiru.util.Utility;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class HandlerWebManagerData implements HttpHandler {

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		String path = httpExchange.getRequestURI().getPath();
		if(path.startsWith("/data/general-setting/get"))
		{
			this.handleGeneralSetting(httpExchange);
		}
		else if(path.startsWith("/data/smtp-setting/get"))
		{
			this.handleSMTPSetting(httpExchange);
		}
		else if(path.startsWith("/data/feeder-ws-setting/get"))
		{
			this.handleFeederWSSetting(httpExchange);
		}
		else if(path.startsWith("/data/log/list"))
		{
			this.handleLogFile(httpExchange);
		}
		else if(path.startsWith("/data/log/download/"))
		{
			this.handleDownloadLogFile(httpExchange);
		}
		else if(path.startsWith("/data/storage/content"))
		{
			this.handleLogStorage(httpExchange);
		}
		else if(path.startsWith("/data/report/sms/list"))
		{
			this.handleSMSLog(httpExchange);
		}
		else if(path.startsWith("/data/report/sms/download/"))
		{
			this.handleDownloadReportFile(httpExchange);
		}
		else if(path.startsWith("/data/block-list/list"))
		{
			this.handleBlockList(httpExchange);
		}
		else if(path.startsWith("/data/feeder-amqp-setting/get"))
		{
			this.handleFeederAMQPSetting(httpExchange);
		}
		else if(path.startsWith("/data/sms-setting/get"))
		{
			this.handleSMSSetting(httpExchange);
		}
		else if(path.startsWith("/data/api-setting/get"))
		{
			this.handleAPISetting(httpExchange);
		}
		else if(path.startsWith("/data/email-account/list"))
		{
			this.handleEmailAccount(httpExchange);
		}
		else if(path.startsWith("/data/email-account/detail/"))
		{
			this.handleEmailAccountDetail(httpExchange);
		}
		else if(path.startsWith("/data/network-dhcp-setting/get"))
		{
			this.handleDHCPSetting(httpExchange);
		}
		else if(path.startsWith("/data/network-wlan-setting/get"))
		{
			this.handleWLANSetting(httpExchange);
		}
		else if(path.startsWith("/data/network-ethernet-setting/get"))
		{
			this.handleEthernetSetting(httpExchange);
		}
		else if(path.startsWith("/data/server-info/get"))
		{
			this.handleServerInfo(httpExchange);
		}
		else if(path.startsWith("/data/server-status/get"))
		{
			this.handleServerStatus(httpExchange);
		}
		else if(path.startsWith("/data/cloudflare/get"))
		{
			this.handleCloudflareSetting(httpExchange);
		}
		else if(path.startsWith("/data/noip/get"))
		{
			this.handleNoIPSetting(httpExchange);
		}
		else if(path.startsWith("/data/afraid/get"))
		{
			this.handleAfraidSetting(httpExchange);
		}
		else if(path.startsWith("/data/dynu/get"))
		{
			this.handleDynuSetting(httpExchange);
		}
		else if(path.startsWith("/data/keystore/list"))
		{
			this.handleKeystoreList(httpExchange);
		}
		else if(path.startsWith("/data/keystore/detail/"))
		{
			this.handleKeystoreDetail(httpExchange);
		}	
		else if(path.startsWith("/data/ddns-record/detail//"))
		{
			this.handleDDNSRecordGet(httpExchange);
		}
		else if(path.startsWith("/data/api-user/list"))
		{
			this.handleUserAPIList(httpExchange);
		}
		else if(path.startsWith("/data/api-user/detail/"))
		{
			this.handleUserAPIDetail(httpExchange);
		}
		else if(path.startsWith("/data/ddns-record/list"))
		{
			this.handleDDNSRecordList(httpExchange);
		}
		else if(path.startsWith("/data/firewall/list"))
		{
			this.handleFirewallList(httpExchange);
		}
		else if(path.startsWith("/data/modem/list"))
		{
			this.handleModemList(httpExchange);
		}
		else if(path.startsWith("/data/modem/detail/"))
		{
			this.handleModemDetail(httpExchange);
		}
		else if(path.startsWith("/data/user/self"))
		{
			this.handleSelfAccount(httpExchange);
		}
		else if(path.startsWith("/data/user/list"))
		{
			this.handleUserList(httpExchange);
		}
		else if(path.startsWith("/data/user/detail/"))
		{
			this.handleUserGet(httpExchange);
		}
		else if(path.startsWith("/data/port/open"))
		{
			this.handleOpenPort(httpExchange);
		}
		else
		{
			httpExchange.sendResponseHeaders(404, 0);
			httpExchange.close();
		}
	}
	
	public void handleOpenPort(HttpExchange httpExchange) throws IOException
	{
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		CookieServer cookie = new CookieServer(requestHeaders, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		int statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				responseBody = ServerInfo.getOpenPort().toString(4).getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
			statusCode = HttpStatus.UNAUTHORIZED;
		}		
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();		
	}
	
	//@GetMapping(path="/data/user/self")
	public void handleSelfAccount(HttpExchange httpExchange) throws IOException
	{
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		CookieServer cookie = new CookieServer(requestHeaders, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		int statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				String loggedUsername = (String) cookie.getSessionValue(JsonKey.USERNAME, "");
				String list = WebUserAccount.getUser(loggedUsername).toString();
				responseBody = list.getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
			statusCode = HttpStatus.UNAUTHORIZED;
		}		
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();		
	}
		
	//@GetMapping(path="/user/list")
	public void handleUserList(HttpExchange httpExchange) throws IOException
	{
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		CookieServer cookie = new CookieServer(requestHeaders, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		int statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				String list = WebUserAccount.listAsString();
				responseBody = list.getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();		
	}
	
	//@GetMapping(path="/data/user/detail/{username}")
	public void handleUserGet(HttpExchange httpExchange) throws IOException
	{
		String path = httpExchange.getRequestURI().getPath();
		String id = path.substring("/data/user/detail/".length());
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		CookieServer cookie = new CookieServer(requestHeaders, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		int statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				String data = WebUserAccount.getUser(id).toString();
				responseBody = data.getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();		
	}
	
	//@GetMapping(path="/data/ddns-record/detail/{id}")
	public void handleDDNSRecordGet(HttpExchange httpExchange) throws IOException
	{
		String path = httpExchange.getRequestURI().getPath();
		String id = path.substring("/data/ddns-record/detail/".length());
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		CookieServer cookie = new CookieServer(requestHeaders, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		int statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				String data = ConfigDDNS.getJSONObject(id).toString();
				responseBody = data.getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();		
	}
	
	//@GetMapping(path="/data/api-user/list")
	public void handleUserAPIList(HttpExchange httpExchange) throws IOException
	{
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		CookieServer cookie = new CookieServer(requestHeaders, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		int statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				ConfigAPIUser.load(Config.getUserAPISettingPath());
				String list = ConfigAPIUser.listAsString();
				responseBody = list.getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();
	}

	//@GetMapping(path="/data/api-user/detail/{username}")
	public void handleUserAPIDetail(HttpExchange httpExchange) throws IOException
	{
		String path = httpExchange.getRequestURI().getPath();
		String id = path.substring("/data/api-user/detail/".length());
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		CookieServer cookie = new CookieServer(requestHeaders, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		int statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				ConfigAPIUser.load(Config.getUserAPISettingPath());
				String data = ConfigAPIUser.getUser(id).toString();
				responseBody = data.getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		
		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();		
	}
	
	//@GetMapping(path="/data/ddns-record/list")
	public void handleDDNSRecordList(HttpExchange httpExchange) throws IOException
	{
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		CookieServer cookie = new CookieServer(requestHeaders, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		int statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				ConfigDDNS.load(Config.getDdnsSettingPath());
				String list = ConfigDDNS.toJSONObject().toString();
				responseBody = list.getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();		
	}
	
	//@GetMapping(path="/data/firewall/list")
	public void handleFirewallList(HttpExchange httpExchange) throws IOException
	{
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		CookieServer cookie = new CookieServer(requestHeaders, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		int statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				ConfigFirewall.load(Config.getFirewallSettingPath());
				String list = ConfigFirewall.getRecords().toString();
				responseBody = list.getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();		
	}
	
	//@GetMapping(path="/data/modem/detail/{id}")
	public void handleModemDetail(HttpExchange httpExchange) throws IOException
	{
		String path = httpExchange.getRequestURI().getPath();
		String id = path.substring("/data/modem/detail/".length());
		
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		CookieServer cookie = new CookieServer(requestHeaders, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		int statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				ConfigModem.load(Config.getModemSettingPath());
				String list = ConfigModem.getModemData(id).toJSONObject().toString();
				responseBody = list.getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();		
	}
	
	//@GetMapping(path="/data/modem/list")
	public void handleModemList(HttpExchange httpExchange) throws IOException
	{
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		CookieServer cookie = new CookieServer(requestHeaders, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		int statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				ConfigModem.load(Config.getModemSettingPath());
				JSONObject list = ConfigModem.getStatus();
				responseBody = list.toString().getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();
	}
	
	//@GetMapping(path="/data/general-setting/get")
	public void handleGeneralSetting(HttpExchange httpExchange) throws IOException
	{
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		CookieServer cookie = new CookieServer(requestHeaders, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		int statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				String list = ConfigGeneral.toJSONObject().toString();
				responseBody = list.getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		
		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();	
	}
		
	//@GetMapping(path="/data/smtp-setting/get")
	public void handleSMTPSetting(HttpExchange httpExchange) throws IOException
	{
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		CookieServer cookie = new CookieServer(requestHeaders, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		int statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				String list = ConfigSMTP.toJSONObject().toString();
				responseBody = list.getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		
		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();
	}

	//@GetMapping(path="/data/feeder-ws-setting/get")
	public void handleFeederWSSetting(HttpExchange httpExchange) throws IOException
	{
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		CookieServer cookie = new CookieServer(requestHeaders, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		int statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				ConfigFeederWS.load(Config.getFeederWSSettingPath());
				String list = ConfigFeederWS.toJSONObject().toString();
				responseBody = list.getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();
	}

	//@GetMapping(path="/data/log/list")
	public void handleLogFile(HttpExchange httpExchange) throws IOException
	{
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		CookieServer cookie = new CookieServer(requestHeaders, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		int statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				File directory = new File(Config.getLogDir());
				JSONArray list = FileUtil.listFile(directory);
				responseBody = list.toString().getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();	
	}
	
	//@GetMapping(path="/data/storage/content")
	public void handleLogStorage(HttpExchange httpExchange) throws IOException
	{
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		CookieServer cookie = new CookieServer(requestHeaders, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		int statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				File directory = new File(Config.getStorageDir());
				JSONArray list = FileUtil.listFile(directory);
				responseBody = list.toString().getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();
	}
	
	//@GetMapping(path="/data/log/download/**")
	public void handleDownloadLogFile(HttpExchange httpExchange) throws IOException
	{
		String path = httpExchange.getRequestURI().getPath();
		path = path.substring("/data/log/download".length());
		
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		CookieServer cookie = new CookieServer(requestHeaders, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		int statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				String fullname = Config.getLogDir() + "/" + path;
				fullname = FileConfigUtil.fixFileName(fullname);	
				responseBody = FileUtil.readResource(fullname);
				String contentType = HttpUtil.getMIMEType(path);
				String baseName = HttpUtil.getBaseName(path);
				responseHeaders.add(ConstantString.CONTENT_TYPE, contentType);
				responseHeaders.add("Content-disposition", "attachment; filename=\""+baseName+"\"");
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch (FileNotFoundException e) 
		{
			statusCode = HttpStatus.NOT_FOUND;
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();	
	}
	
	//@GetMapping(path="/data/report/sms/list")
	public void handleSMSLog(HttpExchange httpExchange) throws IOException
	{
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		CookieServer cookie = new CookieServer(requestHeaders, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		int statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				File directory = new File(Config.getSmsLogPath());
				JSONArray list = FileUtil.listFile(directory);
				responseBody = list.toString().getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();	
	}
	
	//@GetMapping(path="/data/report/sms/download/**")
	public void handleDownloadReportFile(HttpExchange httpExchange) throws IOException
	{
		Headers requestHeaders = httpExchange.getRequestHeaders();
		String path = httpExchange.getRequestURI().getPath();
		path = path.substring("/data/report/sms/download".length());
		Headers responseHeaders = httpExchange.getResponseHeaders();
		CookieServer cookie = new CookieServer(requestHeaders, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		int statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				String fullname = Config.getSmsLogPath() + "/" + path;
				fullname = FileConfigUtil.fixFileName(fullname);	
				responseBody = FileUtil.readResource(fullname);
				String contentType = HttpUtil.getMIMEType(path);
				String baseName = HttpUtil.getBaseName(path);
				responseHeaders.add(ConstantString.CONTENT_TYPE, contentType);
				responseHeaders.add("Content-disposition", "attachment; filename=\""+baseName+"\"");
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch (FileNotFoundException e) 
		{
			statusCode = HttpStatus.NOT_FOUND;
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();	
	}
	
	//@GetMapping(path="/data/block-list/list")
	public void handleBlockList(HttpExchange httpExchange) throws IOException
	{
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		CookieServer cookie = new CookieServer(requestHeaders, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		int statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				ConfigBlocking.load(Config.getBlockingSettingPath());
				String list = ConfigBlocking.toJSONObject().toString();
				responseBody = list.getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();	
	}
	
	//@GetMapping(path="/data/feeder-amqp-setting/get")
	public void handleFeederAMQPSetting(HttpExchange httpExchange) throws IOException
	{
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		CookieServer cookie = new CookieServer(requestHeaders, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		int statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				ConfigFeederAMQP.load(Config.getFeederAMQPSettingPath());
				String list = ConfigFeederAMQP.toJSONObject().toString();
				responseBody = list.getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();	
	}
	
	//@GetMapping(path="/data/sms-setting/get")
	public void handleSMSSetting(HttpExchange httpExchange) throws IOException
	{
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		CookieServer cookie = new CookieServer(requestHeaders, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		int statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				ConfigSMS.load(Config.getSmsSettingPath());
				String list = ConfigSMS.toJSONObject().toString();
				responseBody = list.getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();
	}
	
	//@GetMapping(path="/data/api-setting/get")
	public void handleAPISetting(HttpExchange httpExchange) throws IOException
	{
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		CookieServer cookie = new CookieServer(requestHeaders, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		int statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				ConfigAPI.load(Config.getApiSettingPath());
				String list = ConfigAPI.toJSONObject().toString();
				responseBody = list.getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();	
	}
	
	//@GetMapping(path="/data/email-account/list")
	public void handleEmailAccount(HttpExchange httpExchange) throws IOException
	{
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		CookieServer cookie = new CookieServer(requestHeaders, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		int statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				ConfigEmail.load(Config.getEmailSettingPath());				
				responseBody = ConfigEmail.toJSONArray().toString().getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();	
	}
	
	//@GetMapping(path="/data/email-account/detail/{id}")
	public void handleEmailAccountDetail(HttpExchange httpExchange) throws IOException
	{
		String path = httpExchange.getRequestURI().getPath();
		String id = path.substring("/data/email-account/detail/".length());
		
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		CookieServer cookie = new CookieServer(requestHeaders, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		int statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				ConfigEmail.load(Config.getEmailSettingPath());				
				responseBody = ConfigEmail.getAccount(id).toString().getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();
	}
	
	
	
	//@GetMapping(path="/data/network-dhcp-setting/get")
	public void handleDHCPSetting(HttpExchange httpExchange) throws IOException
	{
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		CookieServer cookie = new CookieServer(requestHeaders, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		int statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				ConfigNetDHCP.load(Config.getDhcpSettingPath());		
				responseBody = ConfigNetDHCP.toJSONObject().toString().getBytes();
				
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();	
	}	
	
	//@GetMapping(path="/data/network-wlan-setting/get")
	public void handleWLANSetting(HttpExchange httpExchange) throws IOException
	{
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		CookieServer cookie = new CookieServer(requestHeaders, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		int statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				ConfigNetWLAN.load(Config.getWlanSettingPath());		
				responseBody = ConfigNetWLAN.toJSONObject().toString().getBytes();
				
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();	
	}
	
	//@GetMapping(path="/data/network-ethernet-setting/get")
	public void handleEthernetSetting(HttpExchange httpExchange) throws IOException
	{
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		CookieServer cookie = new CookieServer(requestHeaders, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		int statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				ConfigNetEthernet.load(Config.getEthernetSettingPath());
				responseBody = ConfigNetEthernet.toJSONObject().toString().getBytes();				
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();	
	}
	
	//@GetMapping(path="/data/server-info/get")
	public void handleServerInfo(HttpExchange httpExchange) throws IOException
	{
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		CookieServer cookie = new CookieServer(requestHeaders, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		int statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				responseBody = ServerInfo.getInfo().getBytes();	
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();	
	}
	
	//@GetMapping(path="/data/server-status/get")
	public void handleServerStatus(HttpExchange httpExchange) throws IOException
	{
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		String query = httpExchange.getRequestURI().getQuery();
		Map<String, String> request = Utility.parseQueryPairs(query);
		CookieServer cookie = new CookieServer(requestHeaders, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		int statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				String timeStr = request.getOrDefault("time", "");
				long from = 0;
				long to = System.currentTimeMillis();
				if(timeStr.equals("1h"))
				{
					from = System.currentTimeMillis() - 3600000;
				}
				else if(timeStr.equals("2h"))
				{
					from = System.currentTimeMillis() - (3600000 * 2);
				}
				else if(timeStr.equals("3h"))
				{
					from = System.currentTimeMillis() - (3600000 * 3);
				}
				else if(timeStr.equals("6h"))
				{
					from = System.currentTimeMillis() - (3600000 * 6);
				}
				else if(timeStr.equals("12h"))
				{
					from = System.currentTimeMillis() - (3600000 * 12);
				}
				else if(timeStr.equals("24h"))
				{
					from = System.currentTimeMillis() - (3600000 * 24);
				}
				else if(timeStr.contains(","))
				{
					String[] tm = timeStr.split(",");
					from = Utility.atol(tm[0]);
					to = Utility.atol(tm[1]);
				}
				responseBody = ServerStatus.load(from, to).toString(4).getBytes();	
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();	
	}
	
	//@GetMapping(path="/data/cloudflare/get")
	public void handleCloudflareSetting(HttpExchange httpExchange) throws IOException
	{
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		CookieServer cookie = new CookieServer(requestHeaders, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		int statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				ConfigVendorCloudflare.load(Config.getCloudflareSettingPath());
				
				responseBody = ConfigVendorCloudflare.toJSONObject().toString().getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();	
	}
	
	//@GetMapping(path="/data/noip/get")
	public void handleNoIPSetting(HttpExchange httpExchange) throws IOException
	{
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		CookieServer cookie = new CookieServer(requestHeaders, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		int statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				ConfigVendorNoIP.load(Config.getNoIPSettingPath());				
				responseBody = ConfigVendorNoIP.toJSONObject().toString().getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();
	}
	
	//@GetMapping(path="/data/afraid/get")
	public void handleAfraidSetting(HttpExchange httpExchange) throws IOException
	{
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		CookieServer cookie = new CookieServer(requestHeaders, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		int statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				ConfigVendorAfraid.load(Config.getAfraidSettingPath());				
				responseBody = ConfigVendorAfraid.toJSONObject().toString().getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();
	}
	
	//@GetMapping(path="/data/dynu/get")
	public void handleDynuSetting(HttpExchange httpExchange) throws IOException
	{
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		CookieServer cookie = new CookieServer(requestHeaders, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		int statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				ConfigVendorDynu.load(Config.getDynuSettingPath());				
				responseBody = ConfigVendorDynu.toJSONObject().toString().getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();	
	}
	
	//@GetMapping(path="/data/keystore/list")
	public void handleKeystoreList(HttpExchange httpExchange) throws IOException
	{
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		CookieServer cookie = new CookieServer(requestHeaders, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		int statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				ConfigKeystore.load(Config.getKeystoreSettingPath());				
				responseBody = ConfigKeystore.toJSONObject().toString().getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();	
	}
	
	//@GetMapping(path="/data/keystore/detail/{id}")
	public void handleKeystoreDetail(HttpExchange httpExchange) throws IOException
	{
		String path = httpExchange.getRequestURI().getPath();
		String id = path.substring("/data/keystore/detail/".length());
		
		Headers requestHeaders = httpExchange.getRequestHeaders();
		Headers responseHeaders = httpExchange.getResponseHeaders();
		CookieServer cookie = new CookieServer(requestHeaders, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		int statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(requestHeaders))
			{
				ConfigKeystore.load(Config.getKeystoreSettingPath());				
				responseBody = ConfigKeystore.get(id).toJSONObject().toString().getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);

		httpExchange.sendResponseHeaders(statusCode, responseBody.length);	 
		httpExchange.getResponseBody().write(responseBody);
		httpExchange.close();	
	}

}
