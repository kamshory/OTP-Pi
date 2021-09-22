package com.planetbiru.config;

public class ConfigSubscriberRedis {

	private static boolean subscriberMqttEnable = true;
	private static long subscriberWsReconnectDelay = 10000;
	private static boolean connected = false;
	public static String topic = "sms";
	public static String address = "127.0.0.1";
	public static int port = 6379;
	public static boolean ssl = false;
	public static String password = "1234";
	public static String username = "";

	private ConfigSubscriberRedis()
	{
		
	}

	public static long getSubscriberWsReconnectDelay() {
		return subscriberWsReconnectDelay;
	}

	public static void setSubscriberWsReconnectDelay(long subscriberWsReconnectDelay) {
		ConfigSubscriberRedis.subscriberWsReconnectDelay = subscriberWsReconnectDelay;
	}

	public static boolean isSubscriberMqttEnable() {
		return subscriberMqttEnable;
	}

	public static void setSubscriberMqttEnable(boolean subscriberMqttEnable) {
		ConfigSubscriberRedis.subscriberMqttEnable = subscriberMqttEnable;
	}

	public static boolean isConnected() {
		return connected;
	}

	public static void setConnected(boolean connected) {
		ConfigSubscriberRedis.connected = connected;
	}
}
