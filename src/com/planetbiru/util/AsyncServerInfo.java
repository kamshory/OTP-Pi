package com.planetbiru.util;

import org.json.JSONObject;

public class AsyncServerInfo extends Thread{

	@Override
	public void run()
	{
		long currentTime = System.currentTimeMillis();
		ServerInfo.setCacheServerInfoExpire(currentTime + ServerInfo.getCacheLifetime());

		JSONObject info = new JSONObject();
		info.put("cpu", ServerInfo.cpuTemperatureInfo());
		info.put("storage", ServerInfo.storageInfo());
		info.put("memory", ServerInfo.memoryInfo());
		ServerInfo.setCacheServerInfo(info.toString(0));
	}
}
