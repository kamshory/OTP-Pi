package com.planetbiru.config;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

public class DataModem {
	private String id = "";
	private String name = "";
	private String port = "";
	private String smsCenter = "";
	private int incommingInterval = 0;
	private int timeRange = 0;
	private int maxPerTimeRange = 0;
	private String provider = "";
	private String imei = "";
	private String msisdn = "";
	private String imsi = "";
	private String recipientPrefix = "";
	private String simCardPIN = "";
	private int baudRate = 9600;
	private String parityBit = "";
	private String startBits = "";
	private String stopBits = "";
	private boolean internetAccess = false;
	private boolean smsAPI = false;
	private boolean defaultModem = false;
	private boolean active = false;
	
	private String apn = "";
	private String apnUsername = "";
	private String apnPassword = "";
	private String dialNumner = "";
	private String initDial1 = "";
	private String initDial2 = "";
	private String initDial3 = "";
	private String initDial4 = "";
	private String initDial5 = "";
	private String dialCommand = "";
	private boolean autoreconnect = false;

	public DataModem() {
		
	}
	
	public DataModem(JSONObject jsonObject) {
		this.id = jsonObject.optString("id", "");
		this.name = jsonObject.optString("name", "");
		this.port = jsonObject.optString("port", "");
		this.smsCenter = jsonObject.optString("smsCenter", "");
		this.incommingInterval = jsonObject.optInt("incommingInterval", 0);
		this.timeRange = jsonObject.optInt("timeRange", 0);
		this.maxPerTimeRange = jsonObject.optInt("maxPerTimeRange", 0);
		this.provider = jsonObject.optString("provider", "");
		this.imei = jsonObject.optString("imei", "");
		this.msisdn = jsonObject.optString("msisdn", "");
		this.imsi = jsonObject.optString("imsi", "");
		this.setRecipientPrefix(jsonObject.optString("recipientPrefix", ""));
		this.simCardPIN = jsonObject.optString("simCardPIN", "");
		this.baudRate = jsonObject.optInt("baudRate", 0);
		this.parityBit = jsonObject.optString("parityBit", "");
		this.startBits = jsonObject.optString("startBits", "");
		this.stopBits = jsonObject.optString("stopBits", "");
		this.internetAccess = jsonObject.optBoolean("internetAccess", false);
		this.smsAPI = jsonObject.optBoolean("smsAPI", false);
		this.defaultModem = jsonObject.optBoolean("defaultModem", false);
		this.active = jsonObject.optBoolean("active", false);
		this.apn = jsonObject.optString("apn", "");
		this.apnUsername = jsonObject.optString("apnUsername", "");
		this.apnPassword = jsonObject.optString("apnPassword", "");
		this.dialNumner = jsonObject.optString("dialNumner", "");
		this.initDial1 = jsonObject.optString("initDial1", "");
		this.initDial2 = jsonObject.optString("initDial2", "");
		this.initDial3 = jsonObject.optString("initDial3", "");
		this.initDial4 = jsonObject.optString("initDial4", "");
		this.initDial5 = jsonObject.optString("initDial5", "");
		this.dialCommand = jsonObject.optString("dialCommand", "");
		this.autoreconnect = jsonObject.optBoolean("autoreconnect", false);
	}
	
	public JSONObject toJSONObject() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("id", this.id);
		jsonObject.put("name", this.name);
		jsonObject.put("port", this.port);
		jsonObject.put("smsCenter", this.smsCenter);
		jsonObject.put("incommingInterval", this.incommingInterval);
		jsonObject.put("timeRange", this.timeRange);
		jsonObject.put("maxPerTimeRange", this.maxPerTimeRange);
		jsonObject.put("provider", this.provider);
		jsonObject.put("imei", this.imei);
		jsonObject.put("msisdn", this.msisdn);
		jsonObject.put("imsi", this.imsi);
		jsonObject.put("recipientPrefix", this.getRecipientPrefix());
		jsonObject.put("simCardPIN", this.simCardPIN);
		jsonObject.put("baudRate", this.baudRate);
		jsonObject.put("parityBit", this.parityBit);
		jsonObject.put("startBits", this.startBits);
		jsonObject.put("stopBits", this.stopBits);
		jsonObject.put("internetAccess", this.internetAccess);
		jsonObject.put("smsAPI", this.smsAPI);
		jsonObject.put("defaultModem", this.defaultModem);
		jsonObject.put("active", this.active);
		
		jsonObject.put("apn", this.apn);
		jsonObject.put("apnUsername", this.apnUsername);
		jsonObject.put("apnPassword", this.apnPassword);
		jsonObject.put("dialNumner", this.dialNumner);
		jsonObject.put("initDial1", this.initDial1);
		jsonObject.put("initDial2", this.initDial2);
		jsonObject.put("initDial3", this.initDial3);
		jsonObject.put("initDial4", this.initDial4);
		jsonObject.put("initDial5", this.initDial5);
		jsonObject.put("dialCommand", this.dialCommand);
		jsonObject.put("autoreconnect", this.autoreconnect);

		return jsonObject;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getSmsCenter() {
		return smsCenter;
	}

	public void setSmsCenter(String smsCenter) {
		this.smsCenter = smsCenter;
	}

	public int getIncommingInterval() {
		return incommingInterval;
	}

	public void setIncommingInterval(int incommingInterval) {
		this.incommingInterval = incommingInterval;
	}

	public int getTimeRange() {
		return timeRange;
	}

	public void setTimeRange(int timeRange) {
		this.timeRange = timeRange;
	}

	public int getMaxPerTimeRange() {
		return maxPerTimeRange;
	}

	public void setMaxPerTimeRange(int maxPerTimeRange) {
		this.maxPerTimeRange = maxPerTimeRange;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public String getImsi() {
		return imsi;
	}

	public void setImsi(String imsi) {
		this.imsi = imsi;
	}

	public String getSimCardPIN() {
		return simCardPIN;
	}

	public void setSimCardPIN(String simCardPIN) {
		this.simCardPIN = simCardPIN;
	}

	public int getBaudRate() {
		return baudRate;
	}

	public void setBaudRate(int baudRate) {
		this.baudRate = baudRate;
	}

	public String getParityBit() {
		return parityBit;
	}

	public void setParityBit(String parityBit) {
		this.parityBit = parityBit;
	}

	public String getStartBits() {
		return startBits;
	}

	public void setStartBits(String startBits) {
		this.startBits = startBits;
	}

	public String getStopBits() {
		return stopBits;
	}

	public void setStopBits(String stopBits) {
		this.stopBits = stopBits;
	}
	
	public boolean isInternetAccess() {
		return internetAccess;
	}

	public void setInternetAccess(boolean internetAccess) {
		this.internetAccess = internetAccess;
	}

	public boolean isSmsAPI() {
		return smsAPI;
	}

	public void setSmsAPI(boolean smsAPI) {
		this.smsAPI = smsAPI;
	}

	public boolean isDefaultModem() {
		return defaultModem;
	}

	public void setDefaultModem(boolean defaultModem) {
		this.defaultModem = defaultModem;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getRecipientPrefix() {
		return recipientPrefix;
	}

	public void setRecipientPrefix(String recipientPrefix) {
		this.recipientPrefix = recipientPrefix;
	}
	
	

	public String getApn() {
		return apn;
	}

	public void setApn(String apn) {
		this.apn = apn;
	}

	public String getApnUsername() {
		return apnUsername;
	}

	public void setApnUsername(String apnUsername) {
		this.apnUsername = apnUsername;
	}

	public String getApnPassword() {
		return apnPassword;
	}

	public void setApnPassword(String apnPassword) {
		this.apnPassword = apnPassword;
	}

	
	public String getDialNumner() {
		return dialNumner;
	}

	public void setDialNumner(String dialNumner) {
		this.dialNumner = dialNumner;
	}

	public String getInitDial1() {
		return initDial1;
	}

	public void setInitDial1(String initDial1) {
		this.initDial1 = initDial1;
	}

	public String getInitDial2() {
		return initDial2;
	}

	public void setInitDial2(String initDial2) {
		this.initDial2 = initDial2;
	}

	public String getInitDial3() {
		return initDial3;
	}

	public void setInitDial3(String initDial3) {
		this.initDial3 = initDial3;
	}

	public String getInitDial4() {
		return initDial4;
	}

	public void setInitDial4(String initDial4) {
		this.initDial4 = initDial4;
	}

	public String getInitDial5() {
		return initDial5;
	}

	public void setInitDial5(String initDial5) {
		this.initDial5 = initDial5;
	}

	public String getDialCommand() {
		return dialCommand;
	}

	public void setDialCommand(String dialCommand) {
		this.dialCommand = dialCommand;
	}

	public boolean isAutoreconnect() {
		return autoreconnect;
	}

	public void setAutoreconnect(boolean autoreconnect) {
		this.autoreconnect = autoreconnect;
	}

	public List<String> getRecipientPrefixList() {
		List<String> perfixes = new ArrayList<>();
		if(this.recipientPrefix.length() > 0)
		{
			String[] arr = recipientPrefix.split(",");
			for(int i = 0; i<arr.length; i++)
			{
				String str = arr[i].trim();
				if(!str.isEmpty())
				{
					perfixes.add(str);
				}
			}
		}
		return perfixes;
	}
	
	
}


