package com.planetbiru.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;

import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigAPI;
import com.planetbiru.config.ConfigAPIUser;
import com.planetbiru.config.ConfigBlocking;
import com.planetbiru.config.ConfigDDNS;
import com.planetbiru.config.ConfigEmail;
import com.planetbiru.config.ConfigSubscriberWS;
import com.planetbiru.config.ConfigGeneral;
import com.planetbiru.config.ConfigModem;
import com.planetbiru.config.ConfigSMS;
import com.planetbiru.config.ConfigSMTP;
import com.planetbiru.config.ConfigVendorAfraid;
import com.planetbiru.config.ConfigVendorCloudflare;
import com.planetbiru.config.ConfigVendorDynu;
import com.planetbiru.config.ConfigVendorNoIP;
import com.planetbiru.config.PropertyLoader;
import com.planetbiru.gsm.GSMUtil;
import com.planetbiru.gsm.SMSLogger;
import com.planetbiru.receiver.amqp.RabbitMQSubscriber;
import com.planetbiru.receiver.ws.WebSocketClientImpl;
import com.planetbiru.user.WebUserAccount;
import com.planetbiru.web.HandlerWebManagerAPI;

public class ConfigLoader {
	private static Properties properties = new Properties();
	
	private ConfigLoader()
	{
		
	}
	
	public static void loadRelative(String configPath)
	{
		InputStream inputStream = ConfigLoader.class.getClassLoader().getResourceAsStream(configPath);
		if(inputStream != null) 
		{
			try 
			{
				ConfigLoader.properties.load(inputStream);		
				for (Entry<Object, Object> entry : ConfigLoader.properties.entrySet()) 
				{
				    String key = (String) entry.getKey();
				    String keyEnv = key.toUpperCase().replace(".", "_");
				    String value = (String) entry.getValue();		    
				    String valueEnv = System.getenv(keyEnv);
				    if(valueEnv != null)
				    {
				    	value = valueEnv;
				    }
				    ConfigLoader.properties.setProperty(key, value);			    
				}
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		} 
	}
	public static void load(String configPath) throws FileNotFoundException
	{
		configPath = FileConfigUtil.fixFileName(configPath);
		try(
				FileInputStream inputStream = new FileInputStream(configPath)
		) 
		{
			ConfigLoader.properties.load(inputStream);			
			for (Entry<Object, Object> entry : ConfigLoader.properties.entrySet()) 
			{
			    String key = (String) entry.getKey();
			    String keyEnv = key.toUpperCase().replace(".", "_");
			    String value = (String) entry.getValue();		    
			    String valueEnv = System.getenv(keyEnv);
			    if(valueEnv != null)
			    {
			    	value = valueEnv;
			    }
			    ConfigLoader.properties.setProperty(key, value);			    
			}
		} 
		catch (IOException e) 
		{
			throw new FileNotFoundException(e.getMessage());
		}
	}
	
	public static void init()
	{	
		String dhcpSettingPath = ConfigLoader.getConfig("otpbroker.path.setting.dhcp");
		String wlanSettingPath = ConfigLoader.getConfig("otpbroker.path.setting.wlan");
		String ethernetSettingPath = ConfigLoader.getConfig("otpbroker.path.setting.ethernet");
		String dhcpSettingPathDefault = ConfigLoader.getConfig("otpbroker.path.setting.dhcp.default");
		String wlanSettingPathDefault = ConfigLoader.getConfig("otpbroker.path.setting.wlan.default");
		String ethernetSettingPathDefault = ConfigLoader.getConfig("otpbroker.path.setting.ethernet.default");
		String osWLANConfigPath = ConfigLoader.getConfig("otpbroker.path.os.wlan");
		String osSSIDKey = ConfigLoader.getConfig("otpbroker.path.os.ssid.key");
		String osEthernetConfigPath = ConfigLoader.getConfig("otpbroker.path.os.ethernet");
		String osDHCPConfigPath = ConfigLoader.getConfig("otpbroker.path.os.dhcp");
		String baseDirConfig = ConfigLoader.getConfig("otpbroker.path.base.setting");
		String sshUsername = ConfigLoader.getConfig("otpbroker.ssh.username");
		String sshPassword = ConfigLoader.getConfig("otpbroker.ssh.password");
		String sshHost = ConfigLoader.getConfig("otpbroker.ssh.host");
		int sshPort = ConfigLoader.getConfigInt("otpbroker.ssh.port");
		long sshSleep = ConfigLoader.getConfigLong("otpbroker.ssh.sleep");
		boolean sshEnable = ConfigLoader.getConfigBoolean("otpbroker.ssh.enable");
		String rebootCommand = ConfigLoader.getConfig("otpbroker.ssh.reboot.command");		
		String apiSettingPath = ConfigLoader.getConfig("otpbroker.path.setting.api.service");
		String userAPISettingPath = ConfigLoader.getConfig("otpbroker.path.setting.api.user");
		String emailSettingPath = ConfigLoader.getConfig("otpbroker.path.setting.email");	
		String blockingSettingPath = ConfigLoader.getConfig("otpbroker.path.setting.blocking");
		String keystoreDataSettingPath = ConfigLoader.getConfig("otpbroker.path.setting.keystore.data");
		String keystoreSettingPath = ConfigLoader.getConfig("otpbroker.path.setting.keystore");
		String smsSettingPath = ConfigLoader.getConfig("otpbroker.path.setting.sms");
		String restartCommand = ConfigLoader.getConfig("otpbroker.ssh.restart.command");
		String modemSettingPath = ConfigLoader.getConfig("otpbroker.path.setting.modem");
		boolean debugModem = ConfigLoader.getConfigBoolean("otpbroker.debug.modem");
		String smsLogPath = ConfigLoader.getConfig("otpbroker.path.log.sms");
		String wvdialSettingPath = ConfigLoader.getConfig("otpbroker.path.wvdial");
		String wvdialCommandConnect = ConfigLoader.getConfig("otpbroker.wvdial.command.connect");
		String wvdialCommandDisconnect = ConfigLoader.getConfig("otpbroker.wvdial.command.disconnect");		
		String deviceName = ConfigLoader.getConfig("otpbroker.device.name");
		String deviceVersion = ConfigLoader.getConfig("otpbroker.device.version");
		String sessionName = ConfigLoader.getConfig("otpbroker.web.session.name");
		long sessionLifetime = ConfigLoader.getConfigLong("otpbroker.web.session.lifetime");
		String sessionFilePath = ConfigLoader.getConfig("otpbroker.web.session.file.path");
		String documentRoot = ConfigLoader.getConfig("otpbroker.web.document.root");
		String subscriberWSSettingPath = ConfigLoader.getConfig("otpbroker.path.setting.subscriber.ws");
		String subscriberAMQPSettingPath = ConfigLoader.getConfig("otpbroker.path.setting.subscriber.amqp");
		String mimeSettingPath = ConfigLoader.getConfig("otpbroker.path.setting.all");
		String userSettingPath = ConfigLoader.getConfig("otpbroker.path.setting.user");
		String ddnsSettingPath = ConfigLoader.getConfig("otpbroker.path.setting.ddns");
		String cloudflareSettingPath = ConfigLoader.getConfig("otpbroker.path.setting.ddns.cloudflare");
		String noIPSettingPath = ConfigLoader.getConfig("otpbroker.path.setting.ddns.noip");
		String afraidSettingPath = ConfigLoader.getConfig("otpbroker.path.setting.ddns.afraid");
		String dynuSettingPath = ConfigLoader.getConfig("otpbroker.path.setting.ddns.dynu");
		String generalSettingPath = ConfigLoader.getConfig("otpbroker.path.setting.general");
		String smtpSettingPath = ConfigLoader.getConfig("otpbroker.path.setting.smtp");
		String firewallSettingPath = ConfigLoader.getConfig("otpbroker.path.setting.firewall");
		String cleanupCommand = ConfigLoader.getConfig("otpbroker.ssh.cleanup.command");
		String logDir = ConfigLoader.getConfig("otpbroker.log.dir");	
		String storageDir = ConfigLoader.getConfig("otpbroker.storage.dir");
		int portManager = ConfigLoader.getConfigInt("otpbroker.server.port");
		boolean showTraffic = ConfigLoader.getConfigBoolean("otpbroker.show.trafic");
		int serverPort = ConfigLoader.getConfigInt("otpbroker.server.port");
		boolean ddnsUpdate = ConfigLoader.getConfigBoolean("otpbroker.cron.enable.ddns");
		boolean timeUpdate = ConfigLoader.getConfigBoolean("otpbroker.cron.enable.ntp");
		boolean cronDeviceEnable = ConfigLoader.getConfigBoolean("otpbroker.cron.enable.device");
		boolean cronAMQPEnable = ConfigLoader.getConfigBoolean("otpbroker.cron.enable.amqp");
		String timeResolution = ConfigLoader.getConfig("otpbroker.cron.time.resolution:minute");
		String serverStatusSettingPath = ConfigLoader.getConfig("otpbroker.path.setting.server.status");	
		
		/**
		boolean subscriberWsEnable = ConfigLoader.getConfigBoolean("otpbroker.ws.enable");
		boolean subscriberWsSSL = ConfigLoader.getConfigBoolean("otpbroker.ws.ssl");
		String subscriberWsAddress = ConfigLoader.getConfig("otpbroker.ws.address");
		int subscriberWsPort = ConfigLoader.getConfigInt("otpbroker.ws.port");
		String subscriberWsPath = ConfigLoader.getConfig("otpbroker.ws.path");
		String subscriberWsUsername = ConfigLoader.getConfig("otpbroker.ws.username");
		String subscriberWsPassword = ConfigLoader.getConfig("otpbroker.ws.password");
		String subscriberWsTopic = ConfigLoader.getConfig("otpbroker.ws.channel");
		long subscriberWsTimeout = ConfigLoader.getConfigLong("otpbroker.ws.timeout");
		long subscriberWsRefresh = ConfigLoader.getConfigLong("otpbroker.ws.refresh.delay");
		long subscriberWsReconnectDelay = ConfigLoader.getConfigLong("otpbroker.ws.reconnect.delay");
		*/
		long waitLoopParent = ConfigLoader.getConfigLong("otpbroker.ws.wait.loop.parent");
		long waitLoopChild = ConfigLoader.getConfigLong("otpbroker.ws.wait.loop.child");
		String imageName = ConfigLoader.getConfig("otpbroker.image.name");
		boolean logConfigNotFound = ConfigLoader.getConfigBoolean("otpbroker.log.config.not.found");
	
		String resetConfigPath = ConfigLoader.getConfig("otpbroker.path.reset.config");		
		String resetDeviceType = ConfigLoader.getConfig("otpbroker.reset.device.type");
		String resetDeviceFile = ConfigLoader.getConfig("otpbroker.reset.device.file");
		
		Config.setResetDeviceType(resetDeviceType);
		Config.setResetDeviceFile(resetDeviceFile);
		Config.setLogConfigNotFound(logConfigNotFound);
		Config.setImageName(imageName);
		Config.setSmtpSettingPath(smtpSettingPath);
		
		Config.setResetConfigPath(resetConfigPath);

		
		Config.setApiSettingPath(apiSettingPath);
		Config.setWvdialSettingPath(wvdialSettingPath);
		Config.setWvdialCommandConnect(wvdialCommandConnect);
		Config.setWvdialCommandDisconnect(wvdialCommandDisconnect);
		Config.setEmailSettingPath(emailSettingPath);
		/**
		 * This configuration must be loaded first
		 */
		Config.setBaseDirConfig(baseDirConfig);
		Config.setDhcpSettingPath(dhcpSettingPath);
		Config.setWlanSettingPath(wlanSettingPath);
		Config.setEthernetSettingPath(ethernetSettingPath);
		Config.setDhcpSettingPathDefault(dhcpSettingPathDefault);
		Config.setWlanSettingPathDefault(wlanSettingPathDefault);
		Config.setEthernetSettingPathDefault(ethernetSettingPathDefault);			
		Config.setOsWLANConfigPath(osWLANConfigPath);
		Config.setOsSSIDKey(osSSIDKey);
		Config.setOsEthernetConfigPath(osEthernetConfigPath);
		Config.setOsDHCPConfigPath(osDHCPConfigPath);		
		Config.setSshUsername(sshUsername);
		Config.setSshPassword(sshPassword);
		Config.setSshHost(sshHost);
		Config.setSshPort(sshPort);
		Config.setSshSleep(sshSleep);
		Config.setSshEnable(sshEnable);
		Config.setRebootCommand(rebootCommand);
			
		Config.setBaseDirConfig(baseDirConfig);		
		Config.setUserSettingPath(userSettingPath);
		Config.setDocumentRoot(documentRoot);
		Config.setDeviceName(deviceName);
		Config.setDeviceVersion(deviceVersion);
		Config.setNoIPDevice(deviceName+"/"+deviceVersion);	
		Config.setBlockingSettingPath(blockingSettingPath);
		Config.setModemSettingPath(modemSettingPath);
		Config.setSubscriberWSSettingPath(subscriberWSSettingPath);
		Config.setSubscriberAMQPSettingPath(subscriberAMQPSettingPath);
		Config.setSessionFilePath(sessionFilePath);
		Config.setSessionName(sessionName);
		Config.setSessionLifetime(sessionLifetime);
		Config.setDdnsSettingPath(ddnsSettingPath);
		Config.setCloudflareSettingPath(cloudflareSettingPath);
		Config.setNoIPSettingPath(noIPSettingPath);
		Config.setDynuSettingPath(dynuSettingPath);
		Config.setAfraidSettingPath(afraidSettingPath);
		Config.setGeneralSettingPath(generalSettingPath);
		Config.setRestartCommand(restartCommand);
		Config.setCleanupCommand(cleanupCommand);
		Config.setMimeSettingPath(mimeSettingPath);
		Config.setLogDir(logDir);
		Config.setStorageDir(storageDir);
		Config.setPortManager(portManager);
		Config.setFirewallSettingPath(firewallSettingPath);
		Config.setShowTraffic(showTraffic);
		Config.setServerPort(serverPort);
		Config.setUserAPISettingPath(userAPISettingPath);
		Config.setKeystoreSettingPath(keystoreSettingPath);
		Config.setDebugModem(debugModem);
		Config.setSmsLogPath(smsLogPath);
		Config.setKeystoreDataSettingPath(keystoreDataSettingPath);
		Config.setSmsSettingPath(smsSettingPath);	
		Config.setAfraidSettingPath(afraidSettingPath);	
		Config.setDdnsUpdate(ddnsUpdate);
		Config.setTimeUpdate(timeUpdate);
		Config.setCronDeviceEnable(cronDeviceEnable);
		Config.setCronAMQPEnable(cronAMQPEnable);
		Config.setTimeResolution(timeResolution);
		Config.setServerStatusSettingPath(serverStatusSettingPath);

		ConfigSMTP.load(Config.getSmtpSettingPath());		
		ConfigAPIUser.load(Config.getUserAPISettingPath());
		
		ConfigDDNS.load(Config.getDdnsSettingPath());
		ConfigVendorCloudflare.load(Config.getCloudflareSettingPath());
		ConfigVendorNoIP.load(Config.getNoIPSettingPath());
		ConfigVendorDynu.load(Config.getDynuSettingPath());
		ConfigVendorAfraid.load(Config.getAfraidSettingPath());		
		ConfigGeneral.load(Config.getGeneralSettingPath());		
		ServerStatus.load(Config.getServerStatusSettingPath());
		
		Config.setWaitLoopParent(waitLoopParent);
		Config.setWaitLoopChild(waitLoopChild);
		/**
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
		*/
		
		
		
		ConfigSubscriberWS.load(Config.getSubscriberWSSettingPath());
		ConfigSMS.load(Config.getSmsSettingPath());
		ConfigBlocking.setCountryCode(ConfigSMS.getCountryCode());
		ConfigBlocking.load(Config.getBlockingSettingPath());
		ConfigModem.load(Config.getModemSettingPath());
		if(ConfigSMS.isLogSMS())
		{
			SMSLogger.setPath(Config.getSmsLogPath());
		}
		GSMUtil.getCallerType().put(Utility.getClassName(RabbitMQSubscriber.class.toString()), "amqp");
		GSMUtil.getCallerType().put(Utility.getClassName(WebSocketClientImpl.class.toString()), "ws");
		GSMUtil.getCallerType().put(Utility.getClassName(HandlerWebManagerAPI.class.toString()), "rest");
		ConfigAPI.load(Config.getApiSettingPath());	
		/**
		 * Override email setting if exists
		 */
		ConfigEmail.load(Config.getEmailSettingPath());
		ConfigGeneral.load(Config.getGeneralSettingPath());
		ConfigAPI.load(Config.getApiSettingPath());
		WebUserAccount.load(Config.getUserSettingPath());			
		PropertyLoader.load(Config.getMimeSettingPath());	
    	
	}

	public static String getConfig(String name) {
		String value = ConfigLoader.properties.getProperty(name);
		if(value == null)
		{
			value = "";
		}
		return value;
	}

	public static long getConfigLong(String name) {
		String value = ConfigLoader.properties.getProperty(name);
		if(value == null || value.isEmpty())
		{
			value = "0";
		}
		value = value.trim();
		return Utility.atol(value);
	}

	public static int getConfigInt(String name) {
		String value = ConfigLoader.properties.getProperty(name);
		if(value == null || value.isEmpty())
		{
			value = "0";
		}
		value = value.trim();
		return Utility.atoi(value);
	}

	public static boolean getConfigBoolean(String name) {
		String value = ConfigLoader.properties.getProperty(name);
		if(value == null || value.isEmpty())
		{
			value = "false";
		}
		value = value.trim().toLowerCase();
		return value.equals("true");
	}

}
