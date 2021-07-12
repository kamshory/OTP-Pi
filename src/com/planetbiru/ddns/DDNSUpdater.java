package com.planetbiru.ddns;

import org.json.JSONObject;

import com.planetbiru.config.ConfigVendorAfraid;
import com.planetbiru.config.ConfigVendorCloudflare;
import com.planetbiru.config.ConfigVendorDynu;
import com.planetbiru.config.ConfigVendorNoIP;

public class DDNSUpdater extends Thread{
	
	private DDNSRecord ddnsRecord;
	public DDNSUpdater(DDNSRecord ddnsRecord, String prevFireTimeStr, String currentTimeStr, String nextValidTimeAfterStr) {
		this.ddnsRecord = ddnsRecord;
	}

	@SuppressWarnings("unused")
	@Override
	public void run()
	{
		if(this.ddnsRecord.getProvider().equals("cloudflare"))
		{
			DNSCloudflare ddns = new DNSCloudflare();
			
			String endpoint = ConfigVendorCloudflare.getEndpoint();
			String accountId = ConfigVendorCloudflare.getAccountId();
			String authEmail = ConfigVendorCloudflare.getAuthEmail();
			String authApiKey = ConfigVendorCloudflare.getAuthApiKey();
			String authToken = ConfigVendorCloudflare.getAuthToken();
			ddns.setConfig(endpoint, accountId, authEmail, authApiKey, authToken);
			
			if(this.ddnsRecord.isForceCreateZone())
			{
				JSONObject res1 = ddns.createZoneIfNotExists(ddnsRecord);
			}
			JSONObject res2 = ddns.update(ddnsRecord);		
		}
		else if(this.ddnsRecord.getProvider().equals("noip"))
		{
			DNSNoIP ddns = new DNSNoIP();
			
			String endpoint = ConfigVendorNoIP.getEndpoint();
			String username = ConfigVendorNoIP.getUsername();
			String password = ConfigVendorNoIP.getPassword();
			String company = ConfigVendorNoIP.getCompany();
			String email = ConfigVendorNoIP.getEmail();
			
			ddns.setConfig(endpoint, username, password, email, company);

			JSONObject res2 = ddns.update(ddnsRecord);		
		}
		else if(this.ddnsRecord.getProvider().equals("afraid"))
		{
			DNSAfraid ddns = new DNSAfraid();
			
			String endpoint = ConfigVendorAfraid.getEndpoint();
			String username = ConfigVendorAfraid.getUsername();
			String password = ConfigVendorAfraid.getPassword();
			String company = ConfigVendorAfraid.getCompany();
			String email = ConfigVendorAfraid.getEmail();
			
			ddns.setConfig(endpoint, username, password, email, company);

			JSONObject res2 = ddns.update(ddnsRecord);		
		}
		else if(this.ddnsRecord.getProvider().equals("dynu"))
		{
			DNSDynu ddns = new DNSDynu();
			
			String endpoint = ConfigVendorDynu.getEndpoint();
			String username = ConfigVendorDynu.getUsername();
			String password = ConfigVendorDynu.getPassword();
			String company = ConfigVendorDynu.getCompany();
			String email = ConfigVendorDynu.getEmail();
			
			ddns.setConfig(endpoint, username, password, email, company);

			JSONObject res2 = ddns.update(ddnsRecord);		
		}
	}
}
