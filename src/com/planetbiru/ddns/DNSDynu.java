package com.planetbiru.ddns;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigVendorDynu;
import com.planetbiru.constant.ConstantString;
import com.planetbiru.util.ResponseEntityCustom;
import com.planetbiru.util.Utility;
import com.sun.net.httpserver.Headers;

public class DNSDynu extends DNS {
	private String email = "";
	private String username = "";
	private String endpoint = "https://api.dynu.com/nic/update";
	private String password = "";
	private String company = "";
	
	@Override
	public JSONObject update(DDNSRecord ddnsRecord)  
	{
		JSONObject res = new JSONObject();
		String ipAddress = this.getIP();
		if(ConfigVendorDynu.getApiVersion().equals("nic"))
		{
			res = this.updateNIC(ddnsRecord, ipAddress);
		}
		else if(ConfigVendorDynu.getApiVersion().equals("v2"))
		{
			res = this.updateV2(ddnsRecord, ipAddress);
		}
		return res;
	}
	
	private JSONObject updateNIC(DDNSRecord ddnsRecord, String ipAddress)
	{
		JSONObject res = new JSONObject();
		String method = "GET";
		Map<String, List<String>> params = new HashMap<>();		
		params.put("hostname", Utility.asList(ddnsRecord.getRecordName()));
		params.put("username", Utility.asList(this.username));
		params.put("password", Utility.asList(Utility.md5(this.password)));
		params.put("myip", Utility.asList(ipAddress));
		String base = this.getBase();
		String queryString = Utility.buildQuery(params);
		String url = String.format("%s?%s", base, queryString);
		String body = null;
		int timeout = 10000;
		Headers requestHeaders = new Headers();
		requestHeaders.add(DDNSKey.HEADER_USER_AGENT, this.createUserAgent());
		requestHeaders.add(DDNSKey.HEADER_CONNECTION, "close");
		this.httpExchange(method, url, requestHeaders, body, timeout);
		return res;
	}
	
	private JSONObject updateV2(DDNSRecord ddnsRecord, String ipAddress) {
		JSONObject result = new JSONObject();
		String token = this.getToken();
		
		String subdomain = ddnsRecord.getRecordName();
		String domain = ddnsRecord.getZone();
		String type = ddnsRecord.getType();
		
		String domainID = null;
		
		JSONObject domainsList = this.listDomain(token);
		if(domainsList.has("domains"))
		{
			JSONArray domains = domainsList.optJSONArray("domains");
			domainID = this.getDomainID(domain, domains);
		}
		
		JSONObject dnsRecordList = this.getDNSRecord(token, ddnsRecord);
		
		if(dnsRecordList.has("dnsRecords"))
		{
			JSONArray dnsRecords = dnsRecordList.optJSONArray("dnsRecords");
			String dnsRecordID = this.getRecordID(domain, subdomain, type, dnsRecords);
			if(dnsRecordID != null && !dnsRecordID.isEmpty())
			{
				// Record is exists
				// Update an existing DNS record for DNS service.
				this.updateDNSRecord(token, ddnsRecord, domainID, dnsRecordID, ipAddress);
			}
			else
			{
				// Record is not exists
				// Add a new DNS record for DNS service.
				this.addDNSRecord(token, ddnsRecord, domainID, ipAddress);
			}
		}
		else
		{
			// No record
			if(domainID != null)
			{
				// Add a new DNS record for DNS service.
				this.addDNSRecord(token, ddnsRecord, domainID, ipAddress);
			}
		}
		return result;
	}
	
	/**
	 * Get a list of domains for DNS service.
	 * @param token
	 * @return
	 */
	private JSONObject listDomain(String token)
	{
		String base = this.getBase();
		String url = String.format("%s/dns", base); 
		String method = "GET";		
		Headers headers = new Headers();
		headers.add(ConstantString.ACCEPT, ConstantString.APPLICATION_JSON);
		headers.add(ConstantString.AUTHORIZATION, ConstantString.BEARER+token);
		int timeout = 10000;
		ResponseEntityCustom response = this.httpExchange(method, url, headers, null, timeout);
	
		JSONObject responseJSON = new JSONObject();
		try
		{
			responseJSON = new JSONObject(response.getBody());
		}
		catch(JSONException e)
		{
			/**
			 * Do nothing
			 */
		}		
		return responseJSON;
	}
	
	/**
	 * Add a new DNS record for DNS service
	 * This method require the id of the domain for DNS service.
	 * @param ddnsRecord
	 * @param token
	 * @param domainID
	 * @param ipAddress
	 * @return
	 */
	private JSONObject addDNSRecord(String token, DDNSRecord ddnsRecord, String domainID, String ipAddress)
	{
		String base = this.getBase();
		String url = String.format("%s/dns/%s/record", base, domainID); 
		String method = "POST";		
		Headers headers = new Headers();
		headers.add(ConstantString.ACCEPT, ConstantString.APPLICATION_JSON);
		headers.add(ConstantString.AUTHORIZATION, ConstantString.BEARER+token);
		int timeout = 10000;
		
		String subdomain = ddnsRecord.getRecordName();
		String domain = ddnsRecord.getZone();
		
		JSONObject requestJSON = new JSONObject();
		String nodeName = this.getNodeName(domain, subdomain);
		
		/**
		 * {
			  "nodeName": "mail",
			  "recordType": "A",
			  "ttl": 300,
			  "state": true,
			  "group": "",
			  "ipv4Address": "204.25.79.214"
			}
		 */
		
		String recordType = ddnsRecord.getType();
		int ttl = ddnsRecord.getTtl();
		boolean state = true;
		String group = "";	
		
		requestJSON.put("nodeName", nodeName);
		requestJSON.put("recordType", recordType);
		requestJSON.put("ttl", ttl);
		requestJSON.put("state", state);
		requestJSON.put("group", group);
		requestJSON.put("ipv4Address", ipAddress);
		
		String body = requestJSON.toString(4);
		
		ResponseEntityCustom response = this.httpExchange(method, url, headers, body, timeout);
	
		JSONObject responseJSON = new JSONObject();
		try
		{
			responseJSON = new JSONObject(response.getBody());
		}
		catch(JSONException e)
		{
			/**
			 * Do nothing
			 */
		}		
		return responseJSON;
	}
	
	private String getNodeName(String domain, String subdomain)
	{
		String nodeName = "";
		if(subdomain.endsWith(domain) && subdomain.length() > domain.length())
		{
			nodeName = subdomain.substring(0, subdomain.length() - domain.length());
		}
		else
		{
			nodeName = subdomain;
		}
		if(nodeName.length() > 1)
		{
			if(nodeName.endsWith("."))
			{
				nodeName = nodeName.substring(0, nodeName.length() - 1);
			}
			if(nodeName.startsWith("."))
			{
				nodeName = nodeName.substring(1);
			}
		}
		return nodeName;
	}

	private JSONObject updateDNSRecord(String token, DDNSRecord ddnsRecord, String domainID, String dnsRecordID,
			String ipAddress) {
		String base = this.getBase();
		String url = String.format("%s/dns/%s/record/%s", base, domainID, dnsRecordID); 
		String method = "GET";		
		Headers headers = new Headers();
		headers.add(ConstantString.ACCEPT, ConstantString.APPLICATION_JSON);
		headers.add(ConstantString.AUTHORIZATION, ConstantString.BEARER+token);
		int timeout = 10000;
		/**
		 * {
			  "nodeName": "mail",
			  "recordType": "A",
			  "ttl": 300,
			  "state": true,
			  "group": "",
			  "ipv4Address": "204.25.79.214"
			}
		 */
		JSONObject requestJSON = new JSONObject();
		String domain = ddnsRecord.getZone();
		String subdomain = ddnsRecord.getRecordName();
		
		String nodeName = this.getNodeName(domain, subdomain);
		String recordType = ddnsRecord.getType();
		int ttl = ddnsRecord.getTtl();
		boolean state = true;
		String group = "";
		
		requestJSON.put("nodeName", nodeName);
		requestJSON.put("recordType", recordType);
		requestJSON.put("ttl", ttl);
		requestJSON.put("state", state);
		requestJSON.put("state", state);
		requestJSON.put("group", group);
		requestJSON.put("ipv4Address", ipAddress);
		
		String requestBody = requestJSON.toString(4);
		
		ResponseEntityCustom response = this.httpExchange(method, url, headers, requestBody, timeout);
	
		JSONObject responseJSON = new JSONObject();
		try
		{
			responseJSON = new JSONObject(response.getBody());
		}
		catch(JSONException e)
		{
			/**
			 * Do nothing
			 */
		}		
		return responseJSON;
		
	}



	private String getDomainID(String domain, JSONArray domains) {
		for(int i = 0; i<domains.length(); i++)
		{
			JSONObject jo = domains.getJSONObject(i);
			if(jo != null && jo.optString("name", "").equals(domain))
			{
				return jo.optString("id", "");
			}
		}
		return null;
	}

	private String getRecordID(String domain, String subdomain, String type, JSONArray records) {
		for(int i = 0; i<records.length(); i++)
		{
			JSONObject jo = records.getJSONObject(i);
			if(jo != null 
					&& (
					jo.optString("hostname", "").equals(subdomain)
					|| jo.optString("nodeName", "").equals(subdomain)
					)
					&& jo.optString("domainName", "").equals(domain)
					&& jo.optString("recordType", "").equals(type)
					)
			{
				return jo.optString("id", "");
			}
		}
		return null;
	}

	private String getToken() {
		String base = this.getBase();
		String url = String.format("%s/oauth2/token", base); 
		String basicAuth = Utility.base64Encode(this.username+":"+this.password);
		String method = "GET";		
		Headers headers = new Headers();
		headers.add(ConstantString.ACCEPT, ConstantString.APPLICATION_JSON);
		headers.add(ConstantString.AUTHORIZATION, "Basic "+basicAuth);
		headers.add("Connection", "close");
		int timeout = 10000;
		ResponseEntityCustom response = this.httpExchange(method, url, headers, null, timeout);
		JSONObject responseJSON = new JSONObject();
		try
		{
			responseJSON = new JSONObject(response.getBody());
		}
		catch(JSONException e)
		{
			/**
			 * Do nothing
			 */
		}	
		return responseJSON.optString("access_token", "");
	}
	private String getBase()
	{
		String base = this.endpoint;
		if(base.endsWith("/"))
		{
			base = base.substring(0, base.length() - 1);
		}
		return base;
	}
	
	/**
	 * Get DNS records based on a hostname and resource record type.
	 * @param token
	 * @param ddnsRecord
	 * @return
	 */
	private JSONObject getDNSRecord(String token, DDNSRecord ddnsRecord) {
		String hostName = ddnsRecord.getRecordName();
		String recordType = ddnsRecord.getType();	
		String base = this.getBase();
		String url = String.format("%s/dns/record/%s?recordType=%s", base, hostName, recordType); 
		String method = "GET";		
		Headers headers = new Headers();
		headers.add(ConstantString.ACCEPT, ConstantString.APPLICATION_JSON);
		headers.add(ConstantString.AUTHORIZATION, ConstantString.BEARER+token);
		int timeout = 10000;
		ResponseEntityCustom response = this.httpExchange(method, url, headers, null, timeout);
	
		JSONObject responseJSON = new JSONObject();
		try
		{
			responseJSON = new JSONObject(response.getBody());
		}
		catch(JSONException e)
		{
			/**
			 * Do nothing
			 */
		}		
		return responseJSON;
	}
	
	private String createUserAgent()
	{
		return String.format("%s %s %s", this.company, Config.getNoIPDevice(), this.email);
	}

	public void setConfig(String endpoint, String username, String password, String email, String company) {
		this.endpoint = endpoint;
		this.username = username;
		this.password = password;
		this.email = email;		
		this.company = company;
	}

	

}
