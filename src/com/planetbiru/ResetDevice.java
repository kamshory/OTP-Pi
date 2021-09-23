package com.planetbiru;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigAPI;
import com.planetbiru.config.ConfigAPIUser;
import com.planetbiru.config.ConfigBlocking;
import com.planetbiru.config.ConfigDDNS;
import com.planetbiru.config.ConfigEmail;
import com.planetbiru.config.ConfigFirewall;
import com.planetbiru.config.ConfigKeystore;
import com.planetbiru.config.ConfigModem;
import com.planetbiru.config.ConfigNetDHCP;
import com.planetbiru.config.ConfigNetEthernet;
import com.planetbiru.config.ConfigNetWLAN;
import com.planetbiru.config.ConfigSMS;
import com.planetbiru.config.ConfigSMTP;
import com.planetbiru.config.ConfigSubscriberAMQP;
import com.planetbiru.config.ConfigSubscriberMQTT;
import com.planetbiru.config.ConfigSubscriberWS;
import com.planetbiru.config.ConfigVendorAfraid;
import com.planetbiru.config.ConfigVendorCloudflare;
import com.planetbiru.config.ConfigVendorDynu;
import com.planetbiru.config.ConfigVendorNoIP;
import com.planetbiru.user.WebUserAccount;
import com.planetbiru.util.CommandLineExecutor;
import com.planetbiru.util.Utility;

public class ResetDevice {
	
	private static Logger logger = Logger.getLogger(ResetDevice.class);
	
	private ResetDevice()
	{
		
	}
	
	private static void resetBasicConfig(Properties props)
	{
		if(props.getOrDefault("RESET_DHCP", "").toString().equalsIgnoreCase("true"))
		{
			String defaultConfigDHCP = Config.getDhcpSettingPathDefault();
			String configDHCP = Config.getDhcpSettingPath();
			ConfigNetDHCP.load(defaultConfigDHCP);
			ConfigNetDHCP.save(configDHCP);
			ConfigNetDHCP.apply(Config.getOsDHCPConfigPath());
		}
		if(props.getOrDefault("RESET_WLAN", "").toString().equalsIgnoreCase("true"))
		{
			String defaultConfigWLAN = Config.getWlanSettingPathDefault();
			String configWLAN = Config.getWlanSettingPath();
			ConfigNetWLAN.load(defaultConfigWLAN);
			ConfigNetWLAN.save(configWLAN);
			ConfigNetWLAN.apply(Config.getOsWLANConfigPath(), Config.getOsWLANConfigPath());
			
		}
		if(props.getOrDefault("RESET_ETHERNET", "").toString().equalsIgnoreCase("true"))
		{
			String defaultConfigEthernet = Config.getEthernetSettingPathDefault();	
			String configEthernet = Config.getEthernetSettingPath();
			ConfigNetEthernet.load(defaultConfigEthernet);
			ConfigNetEthernet.save(configEthernet);
			ConfigNetEthernet.apply(Config.getOsEthernetConfigPath());
		}
		if(props.getOrDefault("RESET_USER", "").toString().equalsIgnoreCase("true"))
		{
			WebUserAccount.reset();
			WebUserAccount.save();
		}
		
	}
	
	static void resetConfig() 
	{
		logger.info("Reset Config");
		Properties props = loadResetProfile();
		if(props != null)
		{
			resetBasicConfig(props);
			resetAdvancedConfig(props);
			resetVendorConfig(props);		
			CommandLineExecutor.exec(Config.getRestartCommand());
		}	
		else
		{
			logger.info("Reset File Not Exists");
		}
	}

	private static void resetAdvancedConfig(Properties props) {
		if(props.getOrDefault("RESET_API", "").toString().equalsIgnoreCase("true"))
		{
			ConfigAPI.reset();
			ConfigAPI.save();
		}
		if(props.getOrDefault("RESET_API_USER", "").toString().equalsIgnoreCase("true"))
		{
			ConfigAPIUser.reset();
			ConfigAPIUser.save();
		}
		if(props.getOrDefault("RESET_BLOCKING", "").toString().equalsIgnoreCase("true"))
		{
			ConfigBlocking.reset();
			ConfigBlocking.save();
		}
		if(props.getOrDefault("RESET_DDNS", "").toString().equalsIgnoreCase("true"))
		{
			ConfigDDNS.reset();
			ConfigDDNS.save();
		}
		if(props.getOrDefault("RESET_EMAIL", "").toString().equalsIgnoreCase("true"))
		{
			ConfigEmail.reset();
			ConfigEmail.save();
		}
		if(props.getOrDefault("RESET_FEEDER_AMQP", "").toString().equalsIgnoreCase("true"))
		{
			ConfigSubscriberAMQP.reset();
			ConfigSubscriberAMQP.save();
		}
		if(props.getOrDefault("RESET_FEEDER_MQTT", "").toString().equalsIgnoreCase("true"))
		{
			ConfigSubscriberMQTT.reset();
			ConfigSubscriberMQTT.save();
		}
		if(props.getOrDefault("RESET_FEEDER_WS", "").toString().equalsIgnoreCase("true"))
		{
			ConfigSubscriberWS.reset();
			ConfigSubscriberWS.save();
		}
		if(props.getOrDefault("RESET_FIREWALL", "").toString().equalsIgnoreCase("true"))
		{
			ConfigFirewall.reset();
			ConfigFirewall.save();
		}
		if(props.getOrDefault("RESET_KEYSTORE", "").toString().equalsIgnoreCase("true"))
		{
			ConfigKeystore.reset();
			ConfigKeystore.save();
		}
		if(props.getOrDefault("RESET_MODEM", "").toString().equalsIgnoreCase("true"))
		{
			ConfigModem.reset();
			ConfigModem.save();
		}
		if(props.getOrDefault("RESET_SMS", "").toString().equalsIgnoreCase("true"))
		{
			ConfigSMS.reset();
			ConfigSMS.save();
		}
		if(props.getOrDefault("RESET_SMTP", "").toString().equalsIgnoreCase("true"))
		{
			ConfigSMTP.reset();
			ConfigSMTP.save();
		}	
	}
	
	private static void resetVendorConfig(Properties props) {
		if(props.getOrDefault("RESET_VENDOR_AFRAID", "").toString().equalsIgnoreCase("true"))
		{
			ConfigVendorAfraid.reset();
			ConfigVendorAfraid.save();
		}
		if(props.getOrDefault("RESET_VENDOR_CLOUDFLARE", "").toString().equalsIgnoreCase("true"))
		{
			ConfigVendorCloudflare.reset();
			ConfigVendorCloudflare.save();
		}
		if(props.getOrDefault("RESET_VENDOR_DYNU", "").toString().equalsIgnoreCase("true"))
		{
			ConfigVendorDynu.reset();
			ConfigVendorDynu.save();
		}
		if(props.getOrDefault("RESET_VENDOR_NOIP", "").toString().equalsIgnoreCase("true"))
		{
			ConfigVendorNoIP.reset();
			ConfigVendorNoIP.save();
		}		
	}

	private static Properties loadResetProfile() 
	{
		List<String> usbDrives = new ArrayList<>();
		usbDrives.add("/media/usb/a");
		usbDrives.add("/media/usb/b");
		usbDrives.add("/media/usb/c");
		usbDrives.add("/media/usb/d");		
		String fileName = Config.getResetConfigPath();
		
		for(int i = 0; i<usbDrives.size(); i++)
		{
			String path = usbDrives.get(i) + "/otppi/" + fileName;
			try 
			{
				Properties props = ResetDevice.getResetProperties(path);
				if(verifyResetFile(props.getOrDefault("VERIFY", "").toString(), Config.getResetDeviceType(), Config.getResetDeviceFile()))
				{
					return props;
				}
			} 
			catch (IOException e) 
			{
				logger.error(e.getMessage(), e);
			}			
		}
		return null;
	}

	private static boolean verifyResetFile(String verifyString, String deviceType, String baseName) {
		try
		{
			String decoded = Utility.base64Decode(verifyString);
			JSONObject json = new JSONObject(decoded);
			return (json.optString("deviceType", "").equals(deviceType) && json.optString("baseName", "").equals(baseName));
		}
		catch(JSONException e)
		{
			return false;
		}
	}
	
	private static Properties getResetProperties(String path) throws IOException {
		Properties props = new Properties();
		try(InputStream inputStream = new FileInputStream(new File(path)))
		{
			props.load(inputStream);
		}
		finally 
		{
			/**
			 * Do nothing
			 */
		}
		return props;
	}
}
