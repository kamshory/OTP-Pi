package com.planetbiru.config;

public class Config {
	private static String smsLogPath = "";
	private static String generalSettingPath = "";
	private static String otpSalt = "IJUjhrfytR^&r7jkjojO";
	private static String secretKey = "planetsecret";
	
	private static String defaultFile = "/index.html";
	
	private static boolean proxyEnable = false;
	private static String proxyHost = "127.0.0.1";
	private static int proxyPort = 8080;
	private static String baseDirConfig = "";
	private static String sessionName = "SMSSESSID";
	private static long sessionLifetime = 1440000;
	private static String emailSettingPath = "";
	private static String subscriberAMQPSettingPath = "";
	private static String subscriberWSSettingPath = "";
	private static String wlanSettingPath = "";
	private static String ddnsSettingPath = "";
	private static String cloudflareSettingPath = "";
	private static String apiSettingPath = "";
	private static String smsSettingPath = "";
	private static String dhcpSettingPath = "";
	private static String ethernetSettingPath = "";
	private static String modemSettingPath = "";
	private static long subscriberWSRecoonectDelay;
	private static String noIPDevice = "OTP Broker/version 1.0";
	private static String noIPSettingPath = "";
	private static String deviceName = "OTP Broker";
	private static String deviceVersion = "version 1.0";
	private static String keystoreSettingPath = "";
	private static String keystoreDataSettingPath = "";
	private static String blockingSettingPath = "";

	private static String userAPISettingPath = "";
	private static String dhcpSettingPathDefault = "";
	private static String wlanSettingPathDefault = "";
	private static String ethernetSettingPathDefault = "";
	
	private static String osWLANConfigPath = "";
	private static String osSSIDKey = "";
	private static String osEthernetConfigPath = "";
	private static String osDHCPConfigPath = "";
	private static String documentRoot = "/static/www";
	private static String userSettingPath = "";
	private static String afraidSettingPath = "";
	private static String dynuSettingPath = "";
	private static long waithModemReady = 10;
	private static long maxWaitModemReady = 10000;
	private static String logDir = "";
	private static long reconnectDelay = 10000;
	private static String sessionFilePath = "";
	
	private static boolean sshEnable = false;
	private static String sshUsername = "";
	private static String sshPassword = "";
	private static String sshHost = "";
	private static int sshPort = 0;
	private static long sshSleep = 10;
	private static String rebootCommand = "";
	private static String mimeSettingPath = "";
	private static String cpuSecret = "83246832yr982fi2hfoi2h3f23yf9823yr98y";
	private static boolean validDevice = true;
	private static String restartCommand = "";
	private static String cleanupCommand = "";
	private static int portManager = 8888;
	private static String firewallSettingPath = "";
	private static boolean debugModem = true;
	private static String storageDir = "";
	private static boolean printMailConsole = true;
	private static String smtpSettingPath = "";
	private static boolean ddnsUpdate = false;
	private static boolean timeUpdate = false;
	private static boolean cronDeviceEnable = false;
	private static boolean cronAMQPEnable = false;
	private static String timeResolution;
	private static boolean showTraffic = false;
	private static int serverPort = 8888;
	private static String wvdialSettingPath = "";
	private static String wvdialCommandConnect = "";
	private static String wvdialCommandDisconnect = "";
	private static String serverStatusSettingPath = "";
	private static String imageName = "otp.jar";
	private static boolean logConfigNotFound = true;
	private static int ddnsTimeout = 10000;
	private static String resetConfigPath = "/otp-pi/reset-config.ini";
	
	private Config()
	{
		
	}

	public static String getOtpSalt() {
		return otpSalt;
	}

	public static void setOtpSalt(String otpSalt) {
		Config.otpSalt = otpSalt;
	}

	public static String getSecretKey() {
		return secretKey;
	}

	public static void setSecretKey(String secretKey) {
		Config.secretKey = secretKey;
	}

	public static String getDefaultFile() {
		return defaultFile;
	}

	public static void setDefaultFile(String defaultFile) {
		Config.defaultFile = defaultFile;
	}

	public static boolean isProxyEnable() {
		return proxyEnable;
	}

	public static void setProxyEnable(boolean proxyEnable) {
		Config.proxyEnable = proxyEnable;
	}

	public static String getProxyHost() {
		return proxyHost;
	}

	public static void setProxyHost(String proxyHost) {
		Config.proxyHost = proxyHost;
	}

	public static int getProxyPort() {
		return proxyPort;
	}

	public static void setProxyPort(int proxyPort) {
		Config.proxyPort = proxyPort;
	}

	public static String getBaseDirConfig() {
		return baseDirConfig;
	}

	public static void setBaseDirConfig(String baseDirConfig) {
		Config.baseDirConfig = baseDirConfig;
	}

	public static String getSessionName() {
		return sessionName;
	}

	public static void setSessionName(String sessionName) {
		Config.sessionName = sessionName;
	}

	public static long getSessionLifetime() {
		return sessionLifetime;
	}

	public static void setSessionLifetime(long sessionLifetime) {
		Config.sessionLifetime = sessionLifetime;
	}

	public static String getEmailSettingPath() {
		return emailSettingPath;
	}

	public static void setEmailSettingPath(String emailSettingPath) {
		Config.emailSettingPath = emailSettingPath;
	}

	public static String getSubscriberAMQPSettingPath() {
		return subscriberAMQPSettingPath;
	}

	public static void setSubscriberAMQPSettingPath(String subscriberAMQPSettingPath) {
		Config.subscriberAMQPSettingPath = subscriberAMQPSettingPath;
	}

	public static String getSubscriberWSSettingPath() {
		return subscriberWSSettingPath;
	}

	public static void setSubscriberWSSettingPath(String subscriberWSSettingPath) {
		Config.subscriberWSSettingPath = subscriberWSSettingPath;
	}

	public static String getWlanSettingPath() {
		return wlanSettingPath;
	}

	public static void setWlanSettingPath(String wlanSettingPath) {
		Config.wlanSettingPath = wlanSettingPath;
	}

	public static String getDdnsSettingPath() {
		return ddnsSettingPath;
	}

	public static void setDdnsSettingPath(String ddnsSettingPath) {
		Config.ddnsSettingPath = ddnsSettingPath;
	}

	public static String getCloudflareSettingPath() {
		return cloudflareSettingPath;
	}

	public static void setCloudflareSettingPath(String cloudflareSettingPath) {
		Config.cloudflareSettingPath = cloudflareSettingPath;
	}

	public static String getApiSettingPath() {
		return apiSettingPath;
	}

	public static void setApiSettingPath(String apiSettingPath) {
		Config.apiSettingPath = apiSettingPath;
	}

	public static String getSmsSettingPath() {
		return smsSettingPath;
	}

	public static void setSmsSettingPath(String smsSettingPath) {
		Config.smsSettingPath = smsSettingPath;
	}

	public static String getDhcpSettingPath() {
		return dhcpSettingPath;
	}

	public static void setDhcpSettingPath(String dhcpSettingPath) {
		Config.dhcpSettingPath = dhcpSettingPath;
	}

	public static String getEthernetSettingPath() {
		return ethernetSettingPath;
	}

	public static void setEthernetSettingPath(String ethernetSettingPath) {
		Config.ethernetSettingPath = ethernetSettingPath;
	}

	public static String getModemSettingPath() {
		return modemSettingPath;
	}

	public static void setModemSettingPath(String modemSettingPath) {
		Config.modemSettingPath = modemSettingPath;
	}

	public static long getSubscriberWSRecoonectDelay() {
		return subscriberWSRecoonectDelay;
	}

	public static void setSubscriberWSRecoonectDelay(long subscriberWSRecoonectDelay) {
		Config.subscriberWSRecoonectDelay = subscriberWSRecoonectDelay;
	}

	public static String getNoIPDevice() {
		return noIPDevice;
	}

	public static void setNoIPDevice(String noIPDevice) {
		Config.noIPDevice = noIPDevice;
	}

	public static String getNoIPSettingPath() {
		return noIPSettingPath;
	}

	public static void setNoIPSettingPath(String noIPSettingPath) {
		Config.noIPSettingPath = noIPSettingPath;
	}

	public static String getDeviceName() {
		return deviceName;
	}

	public static void setDeviceName(String deviceName) {
		Config.deviceName = deviceName;
	}

	public static String getDeviceVersion() {
		return deviceVersion;
	}

	public static void setDeviceVersion(String deviceVersion) {
		Config.deviceVersion = deviceVersion;
	}

	public static String getKeystoreSettingPath() {
		return keystoreSettingPath;
	}

	public static void setKeystoreSettingPath(String keystoreSettingPath) {
		Config.keystoreSettingPath = keystoreSettingPath;
	}

	public static String getKeystoreDataSettingPath() {
		return keystoreDataSettingPath;
	}

	public static void setKeystoreDataSettingPath(String keystoreDataSettingPath) {
		Config.keystoreDataSettingPath = keystoreDataSettingPath;
	}

	public static String getBlockingSettingPath() {
		return blockingSettingPath;
	}

	public static void setBlockingSettingPath(String blockingSettingPath) {
		Config.blockingSettingPath = blockingSettingPath;
	}

	public static String getUserAPISettingPath() {
		return userAPISettingPath;
	}

	public static void setUserAPISettingPath(String userAPISettingPath) {
		Config.userAPISettingPath = userAPISettingPath;
	}

	public static String getDhcpSettingPathDefault() {
		return dhcpSettingPathDefault;
	}

	public static void setDhcpSettingPathDefault(String dhcpSettingPathDefault) {
		Config.dhcpSettingPathDefault = dhcpSettingPathDefault;
	}

	public static String getWlanSettingPathDefault() {
		return wlanSettingPathDefault;
	}

	public static void setWlanSettingPathDefault(String wlanSettingPathDefault) {
		Config.wlanSettingPathDefault = wlanSettingPathDefault;
	}

	public static String getEthernetSettingPathDefault() {
		return ethernetSettingPathDefault;
	}

	public static void setEthernetSettingPathDefault(String ethernetSettingPathDefault) {
		Config.ethernetSettingPathDefault = ethernetSettingPathDefault;
	}

	public static String getOsWLANConfigPath() {
		return osWLANConfigPath;
	}

	public static void setOsWLANConfigPath(String osWLANConfigPath) {
		Config.osWLANConfigPath = osWLANConfigPath;
	}

	public static String getOsSSIDKey() {
		return osSSIDKey;
	}

	public static void setOsSSIDKey(String osSSIDKey) {
		Config.osSSIDKey = osSSIDKey;
	}

	public static String getOsEthernetConfigPath() {
		return osEthernetConfigPath;
	}

	public static void setOsEthernetConfigPath(String osEthernetConfigPath) {
		Config.osEthernetConfigPath = osEthernetConfigPath;
	}

	public static String getOsDHCPConfigPath() {
		return osDHCPConfigPath;
	}

	public static void setOsDHCPConfigPath(String osDHCPConfigPath) {
		Config.osDHCPConfigPath = osDHCPConfigPath;
	}

	public static String getDocumentRoot() {
		return documentRoot;
	}

	public static void setDocumentRoot(String documentRoot) {
		Config.documentRoot = documentRoot;
	}

	public static String getUserSettingPath() {
		return userSettingPath;
	}

	public static void setUserSettingPath(String userSettingPath) {
		Config.userSettingPath = userSettingPath;
	}

	public static String getAfraidSettingPath() {
		return afraidSettingPath;
	}

	public static void setAfraidSettingPath(String afraidSettingPath) {
		Config.afraidSettingPath = afraidSettingPath;
	}

	public static String getDynuSettingPath() {
		return dynuSettingPath;
	}

	public static void setDynuSettingPath(String dynuSettingPath) {
		Config.dynuSettingPath = dynuSettingPath;
	}

	public static long getWaithModemReady() {
		return waithModemReady;
	}

	public static void setWaithModemReady(long waithModemReady) {
		Config.waithModemReady = waithModemReady;
	}

	public static long getMaxWaitModemReady() {
		return maxWaitModemReady;
	}

	public static void setMaxWaitModemReady(long maxWaitModemReady) {
		Config.maxWaitModemReady = maxWaitModemReady;
	}

	public static String getLogDir() {
		return logDir;
	}

	public static void setLogDir(String logDir) {
		Config.logDir = logDir;
	}

	public static long getReconnectDelay() {
		return reconnectDelay;
	}

	public static void setReconnectDelay(long reconnectDelay) {
		Config.reconnectDelay = reconnectDelay;
	}

	public static String getSessionFilePath() {
		return sessionFilePath;
	}

	public static void setSessionFilePath(String sessionFilePath) {
		Config.sessionFilePath = sessionFilePath;
	}

	public static boolean isSshEnable() {
		return sshEnable;
	}

	public static void setSshEnable(boolean sshEnable) {
		Config.sshEnable = sshEnable;
	}

	public static String getSshUsername() {
		return sshUsername;
	}

	public static void setSshUsername(String sshUsername) {
		Config.sshUsername = sshUsername;
	}

	public static String getSshPassword() {
		return sshPassword;
	}

	public static void setSshPassword(String sshPassword) {
		Config.sshPassword = sshPassword;
	}

	public static String getSshHost() {
		return sshHost;
	}

	public static void setSshHost(String sshHost) {
		Config.sshHost = sshHost;
	}

	public static int getSshPort() {
		return sshPort;
	}

	public static void setSshPort(int sshPort) {
		Config.sshPort = sshPort;
	}

	public static long getSshSleep() {
		return sshSleep;
	}

	public static void setSshSleep(long sshSleep) {
		Config.sshSleep = sshSleep;
	}

	public static String getRebootCommand() {
		return rebootCommand;
	}

	public static void setRebootCommand(String rebootCommand) {
		Config.rebootCommand = rebootCommand;
	}

	public static String getMimeSettingPath() {
		return mimeSettingPath;
	}

	public static void setMimeSettingPath(String mimeSettingPath) {
		Config.mimeSettingPath = mimeSettingPath;
	}

	public static boolean isValidDevice() {
		return validDevice;
	}

	public static void setValidDevice(boolean validDevice) {
		Config.validDevice = validDevice;
	}

	public static String getCpuSecret() {
		return cpuSecret;
	}

	public static void setCpuSecret(String cpuSecret) {
		Config.cpuSecret = cpuSecret;
	}

	public static String getGeneralSettingPath() {
		return generalSettingPath;
	}

	public static void setGeneralSettingPath(String generalSettingPath) {
		Config.generalSettingPath = generalSettingPath;
	}

	public static String getRestartCommand() {
		return restartCommand;
	}

	public static void setRestartCommand(String restartCommand) {
		Config.restartCommand = restartCommand;
	}

	public static String getCleanupCommand() {
		return cleanupCommand;
	}

	public static void setCleanupCommand(String cleanupCommand) {
		Config.cleanupCommand = cleanupCommand;
	}

	public static int getPortManager() {
		return portManager;
	}

	public static void setPortManager(int portManager) {
		Config.portManager = portManager;
	}

	public static String getFirewallSettingPath() {
		return firewallSettingPath;
	}

	public static void setFirewallSettingPath(String firewallSettingPath) {
		Config.firewallSettingPath = firewallSettingPath;
	}

	public static boolean isDebugModem() {
		return debugModem;
	}

	public static void setDebugModem(boolean debugModem) {
		Config.debugModem = debugModem;
	}

	public static String getStorageDir() {
		return storageDir;
	}

	public static void setStorageDir(String storageDir) {
		Config.storageDir = storageDir;
	}

	public static String getSmtpSettingPath() {
		return smtpSettingPath;
	}

	public static void setSmtpSettingPath(String smtpSettingPath) {
		Config.smtpSettingPath = smtpSettingPath;
	}

	public static String getSmsLogPath() {
		return smsLogPath;
	}

	public static void setSmsLogPath(String smsLogPath) {
		Config.smsLogPath = smsLogPath;
	}

	public static boolean isPrintMailConsole() {
		return printMailConsole;
	}

	public static void setPrintMailConsole(boolean printMailConsole) {
		Config.printMailConsole = printMailConsole;
	}

	public static boolean isDdnsUpdate() {
		return ddnsUpdate;
	}

	public static void setDdnsUpdate(boolean ddnsUpdate) {
		Config.ddnsUpdate = ddnsUpdate;
	}

	public static boolean isTimeUpdate() {
		return timeUpdate;
	}

	public static void setTimeUpdate(boolean timeUpdate) {
		Config.timeUpdate = timeUpdate;
	}

	public static boolean isCronDeviceEnable() {
		return cronDeviceEnable;
	}

	public static void setCronDeviceEnable(boolean cronDeviceEnable) {
		Config.cronDeviceEnable = cronDeviceEnable;
	}

	public static boolean isCronAMQPEnable() {
		return cronAMQPEnable;
	}

	public static void setCronAMQPEnable(boolean cronAMQPEnable) {
		Config.cronAMQPEnable = cronAMQPEnable;
	}

	public static String getTimeResolution() {
		return timeResolution;
	}

	public static void setTimeResolution(String timeResolution) {
		Config.timeResolution = timeResolution;
	}

	public static boolean isShowTraffic() {
		return showTraffic;
	}

	public static void setShowTraffic(boolean showTraffic) {
		Config.showTraffic = showTraffic;
	}

	public static int getServerPort() {
		return serverPort;
	}

	public static void setServerPort(int serverPort) {
		Config.serverPort = serverPort;
	}

	public static String getWvdialSettingPath() {
		return wvdialSettingPath;
	}

	public static void setWvdialSettingPath(String wvdialSettingPath) {
		Config.wvdialSettingPath = wvdialSettingPath;
	}

	public static String getWvdialCommandConnect() {
		return wvdialCommandConnect;
	}

	public static void setWvdialCommandConnect(String wvdialCommandConnect) {
		Config.wvdialCommandConnect = wvdialCommandConnect;
	}

	public static String getWvdialCommandDisconnect() {
		return wvdialCommandDisconnect;
	}

	public static void setWvdialCommandDisconnect(String wvdialCommandDisconnect) {
		Config.wvdialCommandDisconnect = wvdialCommandDisconnect;
	}

	public static String getServerStatusSettingPath() {
		return serverStatusSettingPath;
	}

	public static void setServerStatusSettingPath(String serverStatusSettingPath) {
		Config.serverStatusSettingPath = serverStatusSettingPath;
	}

	public static String getImageName() {
		return imageName;
	}

	public static void setImageName(String imageName) {
		Config.imageName = imageName;
	}

	public static boolean isLogConfigNotFound() {
		return logConfigNotFound;
	}

	public static void setLogConfigNotFound(boolean logConfigNotFound) {
		Config.logConfigNotFound = logConfigNotFound;
	}

	public static int getDdnsTimeout() {
		return ddnsTimeout;
	}

	public static void setDdnsTimeout(int ddnsTimeout) {
		Config.ddnsTimeout = ddnsTimeout;
	}

	public static String getResetConfigPath() {
		return resetConfigPath;
	}

	public static void setResetConfigPath(String resetConfigPath) {
		Config.resetConfigPath = resetConfigPath;
	}
	
}
