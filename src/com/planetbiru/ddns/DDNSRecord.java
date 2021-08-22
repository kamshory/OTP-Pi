package com.planetbiru.ddns;

import java.util.Date;

import org.json.JSONObject;

public class DDNSRecord {
	
	private String id = "";
	private String zone = "";
	private String recordName = "";
	private int ttl = 1;
	private String provider = "";
	private boolean proxied = false;
	private String type = "A";
	private String cronExpression = "0 * * * * ?";
	private boolean forceCreateZone = false;
	private boolean active = false;
	private Date nextValid = DDNSRecord.longToDate(0);
	private Date lastUpdate = DDNSRecord.longToDate(0);
	
	public static Date longToDate(long time)
	{
		Date date = new Date();
		date.setTime(time);
		return date;
	}
	
	

	public DDNSRecord() {
	}

	public JSONObject toJSONObject() {
		JSONObject jo = new JSONObject();
		jo.put("id", this.getId());
		jo.put("zone", this.getZone());
		jo.put("recordName", this.getRecordName());
		jo.put("type", this.getType());
		jo.put("proxied", this.isProxied());
		jo.put("ttl", this.getTtl());
		jo.put("forceCreateZone", this.isForceCreateZone());
		jo.put("cronExpression", this.getCronExpression());
		jo.put("provider", this.getProvider());
		jo.put("active", this.isActive());
		jo.put("lastUpdate", this.getLastUpdate().getTime());
		jo.put("nextValid", this.getNextValid().getTime());
		return jo;
	}

	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	public String getRecordName() {
		return recordName;
	}

	public void setRecordName(String recordName) {
		this.recordName = recordName;
	}

	public String getZone() {
		return zone;
	}

	public void setZone(String zone) {
		this.zone = zone;
	}

	public boolean isProxied() {
		return proxied;
	}

	public void setProxied(boolean proxied) {
		this.proxied = proxied;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getTtl() {
		return ttl;
	}

	public void setTtl(int ttl) {
		this.ttl = ttl;
	}

	public boolean isForceCreateZone() {
		return forceCreateZone;
	}

	public void setForceCreateZone(boolean forceCreateZone) {
		this.forceCreateZone = forceCreateZone;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public Date getNextValid() {
		return nextValid;
	}

	public void setNextValid(Date nextValid) {
		this.nextValid = nextValid;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	

}
