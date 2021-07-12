package com.planetbiru.config;

import java.util.Date;

import org.json.JSONObject;

import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.Utility;

public class DataKeystore {

	private String id = "";
	private String fileName = "";
	private String filePassword = "";
	private long fileSize = 0;
	private Date fileUpload = new Date();
	private boolean active = false;
	private String fileExtension = "";
	
	public DataKeystore(JSONObject data)
	{
		this.id = data.optString("id", "");
		this.fileName = data.optString("fileName", "");
		this.filePassword = data.optString("filePassword", "");
		this.fileSize = data.optLong("fileSize", 0);
		this.fileUpload = new Date(data.optLong("fileUpload", 0));
		this.fileExtension = data.optString("fileExtension", "");
		this.active = data.optBoolean("active", false);
	}
	public DataKeystore() {
		/**
		 * Do nothing
		 */
	}
	public JSONObject toJSONObject()
	{
		JSONObject data = new JSONObject();
		data.put("id", this.id);
		data.put("fileName", this.fileName);
		data.put("filePassword", this.filePassword);
		data.put("fileSize", this.fileSize);
		data.put("fileUpload", this.fileUpload.getTime());
		data.put("active", this.active);
		data.put("fileExtension", this.fileExtension);
		return data;
	}
	
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getFilePassword() {
		return filePassword;
	}
	public void setFilePassword(String filePassword) {
		this.filePassword = filePassword;
	}
	public long getFileSize() {
		return fileSize;
	}
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
	public Date getFileUpload() {
		return fileUpload;
	}
	public void setFileUpload(Date fileUpload) {
		this.fileUpload = fileUpload;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public String getFileExtension() {
		return fileExtension;
	}
	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}
	public String getFullPath() {
		String base = Utility.getBaseDir();
		String loc = Config.getKeystoreDataSettingPath();
		String file = String.format("%s/%s/%s.%s", base, loc, this.id, this.fileExtension);
		return FileConfigUtil.fixFileName(file);
	}
	

}
