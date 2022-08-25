package com.planetbiru.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import java.util.Properties;

import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigAPI;
import com.planetbiru.config.ConfigAPIUser;
import com.planetbiru.config.ConfigBell;
import com.planetbiru.config.ConfigBlocking;
import com.planetbiru.config.ConfigDDNS;
import com.planetbiru.config.ConfigEmail;
import com.planetbiru.config.ConfigSubscriberWS;
import com.planetbiru.config.ConfigGeneral;
import com.planetbiru.config.ConfigModem;
import com.planetbiru.config.ConfigSMS;
import com.planetbiru.config.ConfigSMTP;
import com.planetbiru.config.ConfigSubscriberAMQP;
import com.planetbiru.config.ConfigSubscriberActiveMQ;
import com.planetbiru.config.ConfigSubscriberMQTT;
import com.planetbiru.config.ConfigSubscriberRedis;
import com.planetbiru.config.ConfigSubscriberStomp;
import com.planetbiru.config.ConfigVendorAfraid;
import com.planetbiru.config.ConfigVendorCloudflare;
import com.planetbiru.config.ConfigVendorDynu;
import com.planetbiru.config.ConfigVendorNoIP;
import com.planetbiru.config.PropertyLoader;
import com.planetbiru.device.ConfigActivation;
import com.planetbiru.gsm.GSMUtil;
import com.planetbiru.gsm.SMSLogger;
import com.planetbiru.subscriber.activemq.ActiveMQInstance;
import com.planetbiru.subscriber.amqp.RabbitMQSubV0;
import com.planetbiru.subscriber.mqtt.SubscriberMQTT;
import com.planetbiru.subscriber.redis.SubscriberRedis;
import com.planetbiru.subscriber.ws.WebSocketClientImpl;
import com.planetbiru.user.WebUserAccount;
import com.planetbiru.web.HandlerWebManagerAPI;

public class ConfigLoader {
	private static Properties properties = new Properties();
	private static Logger logger = Logger.getLogger(ConfigLoader.class);
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
				ConfigLoader.overwriteConfig();
			} 
			catch (IOException e) 
			{
				logger.error(e.getMessage(), e);
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
			ConfigLoader.overwriteConfig();
		} 
		catch (IOException e)
		{
			throw new FileNotFoundException(e.getMessage());
		}
	}
	
	public static void init()
	{	
		String dhcpSettingPath = ConfigLoader.getConfig("otppi.path.setting.dhcp");
		String wlanSettingPath = ConfigLoader.getConfig("otppi.path.setting.wlan");
		String ethernetSettingPath = ConfigLoader.getConfig("otppi.path.setting.ethernet");
		String dhcpSettingPathDefault = ConfigLoader.getConfig("otppi.path.setting.dhcp.default");
		String wlanSettingPathDefault = ConfigLoader.getConfig("otppi.path.setting.wlan.default");
		String ethernetSettingPathDefault = ConfigLoader.getConfig("otppi.path.setting.ethernet.default");
		String osWLANConfigPath = ConfigLoader.getConfig("otppi.path.os.wlan");
		String osSSIDKey = ConfigLoader.getConfig("otppi.path.os.ssid.key");
		String osEthernetConfigPath = ConfigLoader.getConfig("otppi.path.os.ethernet");
		String osDHCPConfigPath = ConfigLoader.getConfig("otppi.path.os.dhcp");
		String baseDirConfig = ConfigLoader.getConfig("otppi.path.setting.base");
		String sshUsername = ConfigLoader.getConfig("otppi.ssh.username");
		String sshPassword = ConfigLoader.getConfig("otppi.ssh.password");
		String sshHost = ConfigLoader.getConfig("otppi.ssh.host");
		int sshPort = ConfigLoader.getConfigInt("otppi.ssh.port");
		long sshSleep = ConfigLoader.getConfigLong("otppi.ssh.sleep");
		boolean sshEnable = ConfigLoader.getConfigBoolean("otppi.ssh.enable");
		String rebootCommand = ConfigLoader.getConfig("otppi.ssh.reboot.command");		
		String apiSettingPath = ConfigLoader.getConfig("otppi.path.setting.api.service");
		String userAPISettingPath = ConfigLoader.getConfig("otppi.path.setting.api.user");
		String emailSettingPath = ConfigLoader.getConfig("otppi.path.setting.email");	
		String blockingSettingPath = ConfigLoader.getConfig("otppi.path.setting.blocking");
		String keystoreDataSettingPath = ConfigLoader.getConfig("otppi.path.setting.keystore.data");
		String keystoreSettingPath = ConfigLoader.getConfig("otppi.path.setting.keystore");
		String smsSettingPath = ConfigLoader.getConfig("otppi.path.setting.sms");
		String restartCommand = ConfigLoader.getConfig("otppi.ssh.restart.command");
		String modemSettingPath = ConfigLoader.getConfig("otppi.path.setting.modem");
		boolean debugModem = ConfigLoader.getConfigBoolean("otppi.modem.debug");
		String smsLogPath = ConfigLoader.getConfig("otppi.sms.path.log");
		String wvdialSettingPath = ConfigLoader.getConfig("otppi.wvdial.path.config");
		String wvdialCommandConnect = ConfigLoader.getConfig("otppi.wvdial.command.connect");
		String wvdialCommandDisconnect = ConfigLoader.getConfig("otppi.wvdial.command.disconnect");		
		String deviceName = ConfigLoader.getConfig("otppi.device.name");
		String deviceVersion = ConfigLoader.getConfig("otppi.device.version");
		String sessionName = ConfigLoader.getConfig("otppi.web.session.name");
		long sessionLifetime = ConfigLoader.getConfigLong("otppi.web.session.lifetime");
		String sessionFilePath = ConfigLoader.getConfig("otppi.web.session.file.path");
		String documentRoot = ConfigLoader.getConfig("otppi.web.document.root");
		String subscriberWSSettingPath = ConfigLoader.getConfig("otppi.path.setting.subscriber.ws");
		String subscriberAMQPSettingPath = ConfigLoader.getConfig("otppi.path.setting.subscriber.amqp");
		String subscriberRedisSettingPath = ConfigLoader.getConfig("otppi.path.setting.subscriber.redis");
		String subscriberStompSettingPath = ConfigLoader.getConfig("otppi.path.setting.subscriber.stomp");
		String subscriberMQTTSettingPath = ConfigLoader.getConfig("otppi.path.setting.subscriber.mqtt");
		String subscriberActiveMQSettingPath = ConfigLoader.getConfig("otppi.path.setting.subscriber.activemq");
		String mimeSettingPath = ConfigLoader.getConfig("otppi.path.setting.all");
		String userSettingPath = ConfigLoader.getConfig("otppi.path.setting.user");
		String ddnsSettingPath = ConfigLoader.getConfig("otppi.path.setting.ddns");
		String cloudflareSettingPath = ConfigLoader.getConfig("otppi.path.setting.ddns.cloudflare");
		String noIPSettingPath = ConfigLoader.getConfig("otppi.path.setting.ddns.noip");
		String afraidSettingPath = ConfigLoader.getConfig("otppi.path.setting.ddns.afraid");
		String dynuSettingPath = ConfigLoader.getConfig("otppi.path.setting.ddns.dynu");
		String generalSettingPath = ConfigLoader.getConfig("otppi.path.setting.general");
		String smtpSettingPath = ConfigLoader.getConfig("otppi.path.setting.smtp");
		String firewallSettingPath = ConfigLoader.getConfig("otppi.path.setting.firewall");
		String cleanupCommand = ConfigLoader.getConfig("otppi.ssh.cleanup.command");
		String logDir = ConfigLoader.getConfig("otppi.server.dir.log");	
		String storageDir = ConfigLoader.getConfig("otppi.server.dir.storage");
		int portManager = ConfigLoader.getConfigInt("otppi.server.port");
		boolean showTraffic = ConfigLoader.getConfigBoolean("otppi.sms.show.trafic");
		int serverPort = ConfigLoader.getConfigInt("otppi.server.port");
		boolean ddnsUpdate = ConfigLoader.getConfigBoolean("otppi.cron.enable.ddns");
		boolean timeUpdate = ConfigLoader.getConfigBoolean("otppi.cron.enable.ntp");
		boolean cronDeviceEnable = ConfigLoader.getConfigBoolean("otppi.cron.enable.device");
		boolean cronAMQPEnable = ConfigLoader.getConfigBoolean("otppi.cron.enable.amqp");
		String timeResolution = ConfigLoader.getConfig("otppi.cron.time.resolution");
		String serverStatusSettingPath = ConfigLoader.getConfig("otppi.path.setting.server.status");	
		boolean cacheHTMLFile = ConfigLoader.getConfigBoolean("otppi.web.cache.file.html");
		
		long waitLoopParent = ConfigLoader.getConfigLong("otppi.ws.wait.loop.parent");
		long waitLoopChild = ConfigLoader.getConfigLong("otppi.ws.wait.loop.child");
		String imageName = ConfigLoader.getConfig("otppi.device.image.name");
		boolean logConfigNotFound = ConfigLoader.getConfigBoolean("otppi.server.log.config.not.found");
	
		String resetConfigPath = ConfigLoader.getConfig("otppi.device.reset.config.path");		
		String resetDeviceType = ConfigLoader.getConfig("otppi.device.reset.type");
		String resetDeviceFile = ConfigLoader.getConfig("otppi.device.reset.config.name");
		
		String bellSettingPath = ConfigLoader.getConfig("otppi.path.setting.bell");
		
		String soundTestTone = ConfigLoader.getConfig("otppi.sound.test.tone");
		int soundTestOctave = ConfigLoader.getConfigInt("otppi.sound.test.octave");
		int soundTestTempo = ConfigLoader.getConfigInt("otppi.sound.test.tempo");
		
		String soundAlertTone = ConfigLoader.getConfig("otppi.sound.alert.tone");
		int soundAlertOctave = ConfigLoader.getConfigInt("otppi.sound.alert.octave");
		int soundAlertTempo = ConfigLoader.getConfigInt("otppi.sound.alert.tempo");
		
		String soundDisconnectTone = ConfigLoader.getConfig("otppi.sound.disconnect.tone");
		int soundDisconnectOctave = ConfigLoader.getConfigInt("otppi.sound.disconnect.octave");
		int soundDisconnectTempo = ConfigLoader.getConfigInt("otppi.sound.disconnect.tempo");
		
		String soundErrorTone = ConfigLoader.getConfig("otppi.sound.error.tone");
		int soundErrorOctave = ConfigLoader.getConfigInt("otppi.sound.error.octave");
		int soundErrorTempo = ConfigLoader.getConfigInt("otppi.sound.error.tempo");
		int soundPIN = ConfigLoader.getConfigInt("otppi.sound.pin");
		boolean soundEnable = ConfigLoader.getConfigBoolean("otppi.sound.enable");
		
		String smsInboxStorage = ConfigLoader.getConfig("otppi.sms.inbox.storage");	
		String smsInboxStatus = ConfigLoader.getConfig("otppi.sms.inbox.status");	
		
		String otpCacheFile = ConfigLoader.getConfig("otppi.otp.cache.file");	
		String otpSalt = ConfigLoader.getConfig("otppi.otp.salt");	
		int otpLength = ConfigLoader.getConfigInt("otppi.otp.length");
		long otpLifetime = ConfigLoader.getConfigInt("otppi.otp.lifetime");
		int otpGCInterval = ConfigLoader.getConfigInt("otppi.otp.gc.ingterval");

		long inspectModemInterval = ConfigLoader.getConfigLong("otppi.modem.inspect.interval");
		
		
		int maxServerStatusRecord = ConfigLoader.getConfigInt("otppi.server.status.max.record");
		Config.setMaxServerStatusRecord(maxServerStatusRecord);
		
		String hwClock = ConfigLoader.getConfig("otppi.hwclock");
		Config.setHwClock(hwClock);
		
		Config.setInspectModemInterval(inspectModemInterval);
		
		Config.setOtpCacheFile(otpCacheFile);
		Config.setOtpLength(otpLength);
		Config.setOtpLifetime(otpLifetime);
		Config.setOtpGCInterval(otpGCInterval);
		Config.setOtpSalt(otpSalt);
		
		Config.setSmsInboxStorage(smsInboxStorage);
		Config.setSmsInboxStatus(smsInboxStatus);
		Config.setSoundTestTone(soundTestTone);
		Config.setSoundTestOctave(soundTestOctave);
		Config.setSoundTestTempo(soundTestTempo);
		Config.setSoundAlertTone(soundAlertTone);
		Config.setSoundAlertOctave(soundAlertOctave);
		Config.setSoundAlertTempo(soundAlertTempo);
		Config.setSoundDisconnectTone(soundDisconnectTone);
		Config.setSoundDisconnectOctave(soundDisconnectOctave);
		Config.setSoundDisconnectTempo(soundDisconnectTempo);
		Config.setSoundErrorTone(soundErrorTone);
		Config.setSoundErrorOctave(soundErrorOctave);
		Config.setSoundErrorTempo(soundErrorTempo);	
		
		Config.setBellSettingPath(bellSettingPath);
		Config.setResetDeviceType(resetDeviceType);
		Config.setResetDeviceFile(resetDeviceFile);
		Config.setLogConfigNotFound(logConfigNotFound);
		Config.setImageName(imageName);
		Config.setSmtpSettingPath(smtpSettingPath);
		Config.setSoundEnable(soundEnable);
		Config.setSoundPIN(soundPIN);
		
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
		Config.setSubscriberRedisSettingPath(subscriberRedisSettingPath);
		Config.setSubscriberStompSettingPath(subscriberStompSettingPath);
		Config.setSubscriberMQTTSettingPath(subscriberMQTTSettingPath);
		Config.setSubscriberActiveMQSettingPath(subscriberActiveMQSettingPath);
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
		
		Config.setCacheHTMLFile(cacheHTMLFile);

		ConfigSMTP.load(Config.getSmtpSettingPath());		
		ConfigAPIUser.load(Config.getUserAPISettingPath());
		
		ConfigDDNS.load(Config.getDdnsSettingPath());
		ConfigVendorCloudflare.load(Config.getCloudflareSettingPath());
		ConfigVendorNoIP.load(Config.getNoIPSettingPath());
		ConfigVendorDynu.load(Config.getDynuSettingPath());
		ConfigVendorAfraid.load(Config.getAfraidSettingPath());		
		ConfigGeneral.load(Config.getGeneralSettingPath());		
		ServerStatus.load(Config.getServerStatusSettingPath());

		ConfigBell.load(Config.getBellSettingPath());		

		Config.setWaitLoopParent(waitLoopParent);
		Config.setWaitLoopChild(waitLoopChild);		
		
		ConfigSubscriberWS.load(Config.getSubscriberWSSettingPath());
		ConfigSubscriberAMQP.load(Config.getSubscriberAMQPSettingPath());
		ConfigSubscriberRedis.load(Config.getSubscriberRedisSettingPath());
		ConfigSubscriberStomp.load(Config.getSubscriberStompSettingPath());
		ConfigSubscriberActiveMQ.load(Config.getSubscriberActiveMQSettingPath());
		ConfigSubscriberMQTT.load(Config.getSubscriberMQTTSettingPath());
		ConfigSMS.load(Config.getSmsSettingPath());
		ConfigBlocking.setCountryCode(ConfigSMS.getCountryCode());
		ConfigBlocking.load(Config.getBlockingSettingPath());
		ConfigModem.load(Config.getModemSettingPath());
		if(ConfigSMS.isLogSMS())
		{
			SMSLogger.setPath(Config.getSmsLogPath());
		}
		GSMUtil.getCallerType().put(Utility.getClassName(RabbitMQSubV0.class.toString()), "amqp");
		GSMUtil.getCallerType().put(Utility.getClassName(SubscriberMQTT.class.toString()), "mqtt");
		GSMUtil.getCallerType().put(Utility.getClassName(WebSocketClientImpl.class.toString()), "ws");
		GSMUtil.getCallerType().put(Utility.getClassName(SubscriberRedis.class.toString()), "redis");
		GSMUtil.getCallerType().put(Utility.getClassName(ActiveMQInstance.class.toString()), "activemq");
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
		
		
		Config.setDefaultHttpPort(ConfigLoader.getConfigInt("otppi.default.http.port"));
		Config.setDefaultHttpsPort(ConfigLoader.getConfigInt("otppi.default.https.port"));
		Config.setDefaultHttpEnable(ConfigLoader.getConfigBoolean("otppi.default.http.enable"));
		Config.setDefaultHttpsEnable(ConfigLoader.getConfigBoolean("otppi.default.https.enable"));	
		Config.setDefaultOtpPath(ConfigLoader.getConfig("otppi.default.api.path.otp")); 
		Config.setDefaultMessagePath(ConfigLoader.getConfig("otppi.default.api.path.message")); 
		Config.setDefaultSmsPath(ConfigLoader.getConfig("otppi.default.api.path.sms")); 
		Config.setDefaultEmailPath(ConfigLoader.getConfig("otppi.default.api.path.email")); 
		Config.setDefaultBlockingPath(ConfigLoader.getConfig("otppi.default.api.path.blocking")); 
		Config.setDefaultUnblockingPath(ConfigLoader.getConfig("otppi.default.api.path.unblocking"));
		
		ConfigActivation.setUsername(ConfigLoader.getConfig("otppi.activation.username"));
		ConfigActivation.setPassword(ConfigLoader.getConfig("otppi.activation.password"));
		ConfigActivation.setMethod(ConfigLoader.getConfig("otppi.activation.method"));
		ConfigActivation.setUrl(ConfigLoader.getConfig("otppi.activation.url"));
		ConfigActivation.setContentType(ConfigLoader.getConfig("otppi.activation.content.type"));
		ConfigActivation.setAuthorization(ConfigLoader.getConfig("otppi.activation.authorization"));
		ConfigActivation.setRequestTimeout(ConfigLoader.getConfigInt("otppi.activation.request.timeout"));
		
		
		Config.setCronExpressionDeviceCheck(ConfigLoader.getConfig("otppi.cron.expression.device"));
		Config.setCronExpressionAMQPCheck(ConfigLoader.getConfig("otppi.cron.expression.amqp"));
		Config.setCronExpressionRedisCheck(ConfigLoader.getConfig("otppi.cron.expression.redis"));
		Config.setCronExpressionMQTTCheck(ConfigLoader.getConfig("otppi.cron.expression.mqtt"));
		Config.setCronExpressionActiveMQCheck(ConfigLoader.getConfig("otppi.cron.expression.activemq"));
		Config.setCronExpressionWSCheck(ConfigLoader.getConfig("otppi.cron.expression.ws"));
		Config.setCronUpdateAMQP(ConfigLoader.getConfigBoolean("otppi.cron.enable.amqp"));
		Config.setCronUpdateMQTT(ConfigLoader.getConfigBoolean("otppi.cron.enable.mqtt"));
		Config.setCronUpdateRedis(ConfigLoader.getConfigBoolean("otppi.cron.enable.redis"));
		Config.setCronUpdateActiveMQ(ConfigLoader.getConfigBoolean("otppi.cron.enable.activemq"));
		Config.setCronUpdateWS(ConfigLoader.getConfigBoolean("otppi.cron.enable.ws"));			
		Config.setCronUpdateDDNS(ConfigLoader.getConfigBoolean("otppi.cron.enable.ddns"));
		Config.setCronExpressionDDNSUpdate(ConfigLoader.getConfig("otppi.cron.expression.general"));
		Config.setCronUpdateServerStatus(ConfigLoader.getConfigBoolean("otppi.cron.enable.server.status"));
		Config.setCronExpressionStatusServer(ConfigLoader.getConfig("otppi.cron.expression.server.status"));			
		Config.setCronServiceCheck(ConfigLoader.getConfigBoolean("otppi.cron.enable.device"));
		
		ConfigLoader.printUsed();
    	
	}
	
	private static void printUsed() {
		List<String> listKey = new ArrayList<>();
		for (Entry<Object, Object> entry : ConfigLoader.properties.entrySet()) 
		{
		    String key = (String) entry.getKey();
		    String keyEnv = key.toUpperCase().replace(".", "_");
		    String value = (String) entry.getValue();	
		    
		    listKey.add(key);
		    
		    String valueEnv = System.getenv(keyEnv);
		    if(valueEnv != null)
		    {
		    	value = valueEnv;
		    }
		    ConfigLoader.properties.setProperty(key, value);			    
		}
		
		boolean printConfig = ConfigLoader.properties.getOrDefault("otppi.device.print.config", "").toString().equalsIgnoreCase("true");
		if(printConfig)
		{
			Collections.sort(listKey);
			logger.info("\r\nList of Unused Config:\r\n");
			for(int i = 0; i<listKey.size(); i++)
			{
				if(!usedList.contains(listKey.get(i)))
				{
					logger.info(listKey.get(i));
				}
			}
		}
		
	}

	private static void overwriteConfig() {
		List<String> listKey = new ArrayList<>();
		List<String> listKeyValue = new ArrayList<>();
		for (Entry<Object, Object> entry : ConfigLoader.properties.entrySet()) 
		{
		    String key = (String) entry.getKey();
		    String keyEnv = key.toUpperCase().replace(".", "_");
		    String value = (String) entry.getValue();	
		    
		    listKey.add(key);
		    listKeyValue.add("echo 'export "+keyEnv+"=\""+value.replace("'", "\\'")+"\"' >> $HOME/.bashrc");
		    
		    String valueEnv = System.getenv(keyEnv);
		    if(valueEnv != null)
		    {
		    	value = valueEnv;
		    }
		    ConfigLoader.properties.setProperty(key, value);			    
		}
		
		boolean printConfig = ConfigLoader.properties.getOrDefault("otppi.device.print.config", "").toString().equalsIgnoreCase("true");
		if(printConfig)
		{
			Collections.sort(listKeyValue);
			Collections.sort(listKey);
			for(int i = 0; i<listKeyValue.size(); i++)
			{
				logger.info(listKeyValue.get(i));
			}
		}
		
	}
	
	


	public static String getConfig(String name) {
		usedList.add(name);
		String value = ConfigLoader.properties.getProperty(name);
		if(value == null)
		{
			value = "";
		}
		return value;
	}

	public static long getConfigLong(String name) {
		usedList.add(name);
		String value = ConfigLoader.properties.getProperty(name);
		if(value == null || value.isEmpty())
		{
			value = "0";
		}
		value = value.trim();
		return Utility.atol(value);
	}

	public static int getConfigInt(String name) {
		usedList.add(name);
		String value = ConfigLoader.properties.getProperty(name);
		if(value == null || value.isEmpty())
		{
			value = "0";
		}
		value = value.trim();
		return Utility.atoi(value);
	}

	public static boolean getConfigBoolean(String name) {
		usedList.add(name);
		String value = ConfigLoader.properties.getProperty(name);
		if(value == null || value.isEmpty())
		{
			value = "false";
		}
		value = value.trim().toLowerCase();
		return value.equals("true");
	}

	private static List<String> usedList = new ArrayList<>();
}
