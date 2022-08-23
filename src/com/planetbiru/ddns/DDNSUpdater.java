package com.planetbiru.ddns;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.planetbiru.config.ConfigVendorAfraid;
import com.planetbiru.config.ConfigVendorCloudflare;
import com.planetbiru.config.ConfigVendorDynu;
import com.planetbiru.config.ConfigVendorNoIP;

public class DDNSUpdater extends Thread{
	
	private static Logger logger = Logger.getLogger(DDNSUpdater.class);
	private static long lastUpdate = 0;
	private DDNSRecord ddnsRecord;
	public DDNSUpdater(DDNSRecord ddnsRecord, String prevFireTimeStr, String currentTimeStr, String nextValidTimeAfterStr)  //NOSONAR
	{
		this.ddnsRecord = ddnsRecord;
	}

	@SuppressWarnings("unused")
	@Override
	public void run()
	{
		try
		{
			if(this.ddnsRecord.getProvider().equals("cloudflare") && ConfigVendorCloudflare.isActive() && ConfigVendorCloudflare.getEndpoint().contains("://"))
			{
				DNSCloudflare ddns = new DNSCloudflare();
				
				String endpoint = ConfigVendorCloudflare.getEndpoint();
				String accountId = ConfigVendorCloudflare.getAccountId();
				String authEmail = ConfigVendorCloudflare.getAuthEmail();
				String authApiKey = ConfigVendorCloudflare.getAuthApiKey();
				String authToken = ConfigVendorCloudflare.getAuthToken();
				boolean active = ConfigVendorCloudflare.isActive();
				ddns.setConfig(endpoint, accountId, authEmail, authApiKey, authToken, active);
				
				if(this.ddnsRecord.isForceCreateZone())
				{
					JSONObject res1 = ddns.createZoneIfNotExists(ddnsRecord);
				}
				JSONObject res2 = ddns.update(ddnsRecord);		
			}
			else if(this.ddnsRecord.getProvider().equals("noip") && ConfigVendorNoIP.isActive() && ConfigVendorNoIP.getEndpoint().contains("://"))
			{
				DNSNoIP ddns = new DNSNoIP();
				
				String endpoint = ConfigVendorNoIP.getEndpoint();
				String username = ConfigVendorNoIP.getUsername();
				String password = ConfigVendorNoIP.getPassword();
				String company = ConfigVendorNoIP.getCompany();
				String email = ConfigVendorNoIP.getEmail();
				boolean active = ConfigVendorNoIP.isActive();
				ddns.setConfig(endpoint, username, password, email, company, active);
	
				JSONObject res2 = ddns.update(ddnsRecord);		
			}
			else if(this.ddnsRecord.getProvider().equals("afraid") && ConfigVendorAfraid.isActive() && ConfigVendorAfraid.getEndpoint().contains("://"))
			{
				DNSAfraid ddns = new DNSAfraid();
				
				String endpoint = ConfigVendorAfraid.getEndpoint();
				String username = ConfigVendorAfraid.getUsername();
				String password = ConfigVendorAfraid.getPassword();
				String company = ConfigVendorAfraid.getCompany();
				String email = ConfigVendorAfraid.getEmail();
				boolean active = ConfigVendorAfraid.isActive();
				ddns.setConfig(endpoint, username, password, email, company, active);
	
				JSONObject res2 = ddns.update(ddnsRecord);		
			}
			else if(this.ddnsRecord.getProvider().equals("dynu") && ConfigVendorDynu.isActive() && ConfigVendorDynu.getEndpoint().contains("://"))
			{
				DNSDynu ddns = new DNSDynu();
				
				String endpoint = ConfigVendorDynu.getEndpoint();
				String username = ConfigVendorDynu.getUsername();
				String password = ConfigVendorDynu.getPassword();
				String company = ConfigVendorDynu.getCompany();
				String email = ConfigVendorDynu.getEmail();
				boolean active = ConfigVendorDynu.isActive();
				ddns.setConfig(endpoint, username, password, email, company, active);
	
				JSONObject res2 = ddns.update(ddnsRecord);		
			}
		}
		catch(IOException e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	public static long getLastUpdate() {
		return lastUpdate;
	}

	public static void setLastUpdate(long lastUpdate) {
		DDNSUpdater.lastUpdate = lastUpdate;
	}
}
