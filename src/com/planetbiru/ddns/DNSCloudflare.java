package com.planetbiru.ddns;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.config.Config;
import com.planetbiru.constant.ConstantString;
import com.planetbiru.util.ResponseEntityCustom;
import com.planetbiru.util.Utility;
import com.sun.net.httpserver.Headers;

public class DNSCloudflare extends DNS{
	
	private String authEmail = "";
	private String accountId = "";
	private String endpoint = "https://api.cloudflare.com/client/v4";
	private String authApiKey = "";
	private String authToken = "";
	
	public void setConfig(String endpoint, String accountId, String authEmail, String authApiKey, String authToken)
	{
		this.endpoint = endpoint;
		this.accountId = accountId;
		this.authEmail = authEmail;
		this.authApiKey = authApiKey;
		this.authToken = authToken;
	}
	/**
	* Issues an HTTPS request and returns the result
	*
	* @param string String method
	* @param string String endpoint
	* @param array  String params
	*
	* @throws Exception
	*
	* @return mixed
	 * @throws IOException 
	*/
	public ResponseEntityCustom request(String method, String endpoint, Map<String, List<String>> params, String contentType) throws IOException
	{
		int timeout = Config.getDdnsTimeout();
		Headers headers = this.createRequestHeader(contentType);
		headers.add(DDNSKey.HEADER_CONTENT_TYPE, contentType);
		String body = "";
		if(contentType.contains("urlencode") && (method.equals("POST") || method.equals("PUT") || method.equals("PATCH")  || method.equals("DELETE")))
		{
			body = this.buildQuery(params);
		}
		else if(contentType.contains("json") && (method.equals("POST") || method.equals("PUT") || method.equals("PATCH")  || method.equals("DELETE")))
		{
			JSONObject obj = new JSONObject(params);
			body = obj.toString();
		}	
		return this.httpExchange(method, endpoint, null, headers, body, timeout);
	}	
	
	public JSONObject createZoneIfNotExists(DDNSRecord ddnsRecord) throws IOException 
	{
		if(this.getZone(ddnsRecord.getZone()) == null)
		{
			return this.createZone(ddnsRecord);
		}
		return null;
	}
	
	public JSONObject createZone(DDNSRecord ddnsRecord) throws IOException
	{
		return this.createZone(ddnsRecord.getZone(), this.accountId);
	}
	
	public JSONObject createZone(String name) throws IOException
	{
		return this.createZone(name, this.accountId);
	}
	
	/**
	 * Create Zone <br>
	 * URL : https://api.cloudflare.com/#zone-create-zone
	 * @param name The domain name
	 * @param params 
	 * @return
	 * @throws IOException 
	 */
	public JSONObject createZone(String name, String accountId) throws IOException
	{
		JSONObject json = new JSONObject();
		JSONObject account = new JSONObject();
		account.put("id", accountId);
		json.put("account", account);
		json.put("name", name);
		json.put("jump_start", true);
		json.put("type", "full");		
	
		String url = endpoint + "/zones";
		String body = json.toString();
		int timeout = 1000;
		Headers requestHeaders = this.createRequestHeader();
		requestHeaders.add(DDNSKey.HEADER_CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		ResponseEntityCustom response = httpExchange("POST", url, null, requestHeaders, body, timeout);
		JSONObject resp = new JSONObject();
		try
		{
		   resp = new JSONObject(response.getBody());
		}
		catch(JSONException e)
		{
			/**
			 * Do nothing
			 */
		}
		return resp;
	}
	
	/**
	 * List Zones <br>
	 * URL https://api.cloudflare.com/#zone-list-zones
	 * @param params List Zone Parameters <br>
	 * match (string) : enum(any, all) <br>
	 * name (string(253)) : valid domain name <br>
	 * account.name (string(100)) : Account name <br>
	 * order (string) : enum(name, status, account.id, account.name) <br>
	 * page (number) <br>
	 * per_page (number)
	 * @return
	 * @throws IOException 
	 */
	public JSONArray listZones(Map<String, String> params) throws IOException
	{
		String url = this.endpoint + "/zones";
		Headers requestHeaders = this.createRequestHeader();
		int timeout = Config.getDdnsTimeout();
		ResponseEntityCustom response = httpExchange("GET", url, params, requestHeaders, null, timeout);
		if(response.getBody().length() > 20)
		{
			JSONObject resp = new JSONObject(response.getBody());
			return resp.optJSONArray(DDNSKey.RESULT);
		}
		else
		{
			return null;
		}
	}

	public JSONObject getZone(String name) throws IOException
	{
		Map<String, String> params = new HashMap<>();
		params.put(DDNSKey.NAME, name);
		JSONArray zones = this.listZones(params);
		JSONObject zone = null;
		if(zones != null)
		{
			for(int i = 0; i<zones.length(); i++)
			{
				zone = zones.optJSONObject(i);
				if(zone.optString(DDNSKey.NAME, "").equals(name))
				{
					break;
				}
			}
		}
		return zone;
	}

	public JSONObject deleteZoneByName(String name) throws DDNSException, IOException
	{
		JSONObject zone = this.getZone(name);
		if(zone == null || zone.isEmpty())
		{
			throw new DDNSException("Domain "+name+" not found");
		}
		String zoneId = zone.optString("id", "");
		
		String url = this.endpoint + DDNSKey.ZONES+zoneId;
		Headers requestHeaders = this.createRequestHeader();
		int timeout = Config.getDdnsTimeout();
		ResponseEntityCustom response = httpExchange("DELETE", url, null, requestHeaders, null, timeout);
		
		JSONObject resp = new JSONObject();
		
		try
		{
			resp = new JSONObject(response.getBody());
		}
		catch(JSONException e)
		{
			/**
			 * Do noting
			 */
		}
		return resp;
	}

	/**
	 * Delete Zone
	 * @param zoneId Zone ID
	 * @return
	 * @throws IOException 
	 */
	public JSONObject deleteZone(String zoneId) throws IOException
	{
		String url = this.endpoint + DDNSKey.ZONES+zoneId;
		Headers requestHeaders = this.createRequestHeader();
		
		int timeout = Config.getDdnsTimeout();
		ResponseEntityCustom response = httpExchange("DELETE", url, null, requestHeaders, null, timeout);
		
		JSONObject resp = new JSONObject();
		
		try
		{
			resp = new JSONObject(response.getBody());
		}
		catch(JSONException e)
		{
			/**
			 * Do noting
			 */
		}
		return resp;
	}


	public JSONObject getZoneDnsRecords(String zoneId, Map<String, List<String>> params) throws IOException
	{
		ResponseEntityCustom response = this.get(DDNSKey.ZONES + zoneId + "/dns_records", params, ConstantString.URL_ENCODE);
		JSONObject resp = new JSONObject();
				
		try
		{
			resp = new JSONObject(response.getBody());
		}
		catch(JSONException e)
		{
			/**
			 * Do noting
			 */
		}
		return resp;
	}
	
	private JSONObject createDnsRecord(String zoneId, String type, String name, String content, int ttl, boolean proxied) throws IOException
	{		
		JSONObject json = new JSONObject();
	
		json.put("type", type);
		json.put(DDNSKey.NAME, name);
		json.put("content", content);
		json.put("ttl", ttl);
		json.put("proxied", proxied);
	
		String url = endpoint + DDNSKey.ZONES + zoneId + "/dns_records";
		Headers requestHeaders = this.createRequestHeader(ConstantString.APPLICATION_JSON);
		String body = json.toString();
		int timeout = 1000;
		ResponseEntityCustom response = httpExchange("POST", url, null, requestHeaders, body, timeout);
		JSONObject resp = new JSONObject(response.getBody());
		return resp.optJSONObject(DDNSKey.RESULT);
	}
	
	@Override
	public JSONObject update(DDNSRecord ddnsRecord) throws IOException  
	{
		String ip = this.getIP();
		String domain = ddnsRecord.getZone();
		JSONObject zone = this.getZone(domain);
		JSONObject res = new JSONObject();
		if(zone == null)
		{
			zone = this.createZone(domain, this.accountId);
		}
		if(zone.has("id"))
		{
			String zoneId = zone.optString("id", "");
			String recordName = ddnsRecord.getRecordName().trim();
			boolean proxied = ddnsRecord.isProxied();
			if(!recordName.isEmpty())
			{
				Map<String, List<String>> params1 = new HashMap<>();
				params1.put(DDNSKey.NAME, Utility.asList(recordName));
				JSONObject records = this.getZoneDnsRecords(zoneId, params1);
				
				if(this.isRecordExists(records, recordName))
				{
					String recordId = this.getRecordId(records, recordName);
					res = this.updateDnsRecord(zoneId, ddnsRecord.getType(), ddnsRecord.getRecordName(), ip, ddnsRecord.getTtl(), proxied, recordId);
				}
				else
				{
					res = this.createDnsRecord(zoneId, ddnsRecord.getType(), ddnsRecord.getRecordName(), ip, ddnsRecord.getTtl(), proxied);
				}
			}
		}
		return res;
	}
	
	
	public String getRecordId(JSONObject records, String recordName) {
		if(records != null && records.has(DDNSKey.RESULT))
		{
			JSONArray recs = records.optJSONArray(DDNSKey.RESULT);
			if(recs != null && !recs.isEmpty())
			{
				for(int i = 0; i<recs.length(); i++)
				{
					JSONObject rec = recs.getJSONObject(i);
					if(rec != null && !recordName.isEmpty() && rec.optString(DDNSKey.NAME, "").equals(recordName))
					{
						return rec.optString("id");
					}
				}
			}
		}
		return "";
	}


	public boolean isRecordExists(JSONObject records, String recordName) {
		if(records != null && records.has(DDNSKey.RESULT))
		{
			JSONArray recs = records.optJSONArray(DDNSKey.RESULT);
			if(recs != null && !recs.isEmpty())
			{
				for(int i = 0; i<recs.length(); i++)
				{
					JSONObject rec = recs.getJSONObject(i);
					if(rec != null && !recordName.isEmpty() && rec.optString(DDNSKey.NAME, "").equals(recordName))
					{
						return true;
					}
				}
			}
		}
		return false;
	}


	

	public ResponseEntityCustom get(String path, Map<String, List<String>> params, String contentType) throws IOException
	{
		String url = this.endpoint + path;
		return this.request("GET", url, params, contentType);
	}
	public ResponseEntityCustom post(String path, Map<String, List<String>> params, String contentType) throws IOException
	{
		String url = this.endpoint + path;
		return this.request("POST", url, params, contentType);
	}
	public ResponseEntityCustom put(String path, Map<String, List<String>> params, String contentType) throws IOException
	{
		String url = this.endpoint + path;
		return this.request("PUT", url, params, contentType);
	}
	public ResponseEntityCustom patch(String path, Map<String, List<String>> params, String contentType) throws IOException
	{
		String url = this.endpoint + path;
		return this.request("PATCH", url, params, contentType);
	}
	public ResponseEntityCustom delete(String path, Map<String, List<String>> params, String contentType) throws IOException
	{
		String url = this.endpoint + path;
		return this.request("DELETE", url, params, contentType);
	}
	public ResponseEntityCustom delete(String path, String contentType) throws IOException
	{
		String url = this.endpoint + path;
		Map<String, List<String>> params = new HashMap<>();
		return this.request("DELETE", url, params, contentType);
	}
	
	public Headers createRequestHeader(String contentType)
	{
		Headers requestHeaders = this.createRequestHeader();
		requestHeaders.add(DDNSKey.HEADER_CONTENT_TYPE, contentType);
		return requestHeaders;
	}
	public Headers createRequestHeader() {
		Headers requestHeaders = new Headers();
		requestHeaders.add(DDNSKey.HEADER_X_AUTH_EMAIL, this.authEmail);
		requestHeaders.add(DDNSKey.HEADER_X_AUTH_KEY, this.authApiKey);
		requestHeaders.add(DDNSKey.HEADER_USER_AGENT, "OTP Broker");
		requestHeaders.add(DDNSKey.HEADER_CONNECTION, "close");
		return requestHeaders;
	}

	/**
	* Creates a zone if it doesn"t already exist.
	*
	* Returns information about the zone
	 * @throws IOException 
	*/
	public JSONObject registerDnsZone(String name, String accountId) throws IOException
	{
		JSONObject zone = this.getZone(name);
		if(zone != null)
		{
			return zone;
		}
		else
		{
			zone = this.createZone(name, accountId);
		}
		return zone;
	}

	public JSONObject setDnsZoneSsl(String zoneId, String type) throws DDNSException, IOException
	{
		List<String> allowedTypes = new ArrayList<>();
		allowedTypes.add("off");
		allowedTypes.add("flexible");
		allowedTypes.add("full");
		allowedTypes.add("full_strict");
		if(!allowedTypes.contains(type))
		{
			throw new DDNSException("SSL type not allowed. valid types are " + allowedTypes.toString());
		}
		Map<String, List<String>> params = new HashMap<>();
		params.put(DDNSKey.VALUE, Utility.asList(type));
	
		ResponseEntityCustom response = this.patch(DDNSKey.ZONES + zoneId + "/settings/ssl", params, ConstantString.URL_ENCODE);
		JSONObject resp = new JSONObject(response.getBody());
		return resp.optJSONObject(DDNSKey.RESULT);
	}

	public JSONObject setDnsZoneCache(String zoneId, String type) throws DDNSException, IOException
	{
		List<String> allowedTypes = new ArrayList<>();
		allowedTypes.add("aggressive");
		allowedTypes.add("basic");
		allowedTypes.add("simplified");
		if(!allowedTypes.contains(type))
		{
			throw new DDNSException("Cache type not allowed. valid types are " + allowedTypes.toString());
		}
	
		Map<String, List<String>> params = new HashMap<>();
		params.put(DDNSKey.VALUE, Utility.asList(type));
	
		ResponseEntityCustom response = this.patch(DDNSKey.ZONES + zoneId + "/settings/cache_level", params, ConstantString.URL_ENCODE);
		JSONObject resp = new JSONObject(response.getBody());
		return resp.optJSONObject(DDNSKey.RESULT);
	}

	public ResponseEntityCustom clearZoneCache(String zoneId) throws IOException
	{
		Map<String, List<String>> params = new HashMap<>();
		params.put("purge_everything", Utility.asList(Boolean.toString(true)));
		return this.delete(DDNSKey.ZONES + zoneId + "/purge_cache", params, ConstantString.URL_ENCODE);
	}

	public ResponseEntityCustom setDnsZoneMinify(String zoneId, String settings) throws IOException
	{
		Map<String, List<String>> params = new HashMap<>();
		params.put(DDNSKey.VALUE, Utility.asList(settings));
		return this.patch(DDNSKey.ZONES + zoneId + "/settings/minify", params, ConstantString.URL_ENCODE);
	}

	public JSONObject updateDnsRecord(String zoneId, String type, String name, String content, int ttl, boolean proxied, String recordId) throws IOException
	{
		JSONObject json = new JSONObject();
		
		json.put("type", type);
		json.put(DDNSKey.NAME, name);
		json.put("content", content);
		json.put("ttl", ttl);
		json.put("proxied", proxied);
	
		String url = endpoint + DDNSKey.ZONES + zoneId + "/dns_records/" + recordId;
		Headers requestHeaders = this.createRequestHeader(ConstantString.APPLICATION_JSON);
		String body = json.toString();
		int timeout = 1000;
		ResponseEntityCustom response = httpExchange("PUT", url, null, requestHeaders, body, timeout);		
		
		JSONObject resp = new JSONObject(response.getBody());
		return resp.optJSONObject(DDNSKey.RESULT);

	}

	public ResponseEntityCustom deleteDnsRecord(String zoneId, String recordId) throws IOException
	{
		return this.delete(DDNSKey.ZONES + zoneId + "/dns_records/" + recordId, ConstantString.URL_ENCODE);
	}
	public String getAuthEmail() {
		return authEmail;
	}
	public void setAuthEmail(String authEmail) {
		this.authEmail = authEmail;
	}
	public String getAccountId() {
		return accountId;
	}
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}
	public String getEndpoint() {
		return endpoint;
	}
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
	public String getAuthApiKey() {
		return authApiKey;
	}
	public void setAuthApiKey(String authApiKey) {
		this.authApiKey = authApiKey;
	}
	public String getAuthToken() {
		return authToken;
	}
	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}
	


}
