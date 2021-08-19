package com.planetbiru.gsm;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONObject;

import com.planetbiru.util.Utility;

public class SMS {
    private int id = 0;
    private String storage = "";
    private String status = "";
    private String phoneNumber = "";
    private String phoneName = "";
    private Date date = new Date();
    private String content = "";
    
    public SMS(String storage, String id, String status, String phoneNumber, String phoneName, String date)
    {
    	this.id = Utility.atoi(id);
    	this.storage = storage;
    	this.status = status;
    	this.phoneNumber = phoneNumber;
    	this.phoneName = phoneName;
    	this.date = this.parseDate(date);
    }

    private Date parseDate(String date) {
    	String dateTimeStr = date.substring(0, 17);
    	int tz = Utility.atoi(date.substring(17));
		Date dateTime;
		try 
		{
			dateTime = Utility.stringToTime(dateTimeStr, "yy/MM/dd,HH:mm:ss", 0);
		} 
		catch (ParseException e) 
		{
			e.printStackTrace();
			dateTime = new Date();
		}	
		final Calendar cal = Calendar.getInstance();
		cal.setTime(dateTime);
	    cal.add(Calendar.MINUTE, tz * -15);
	    return cal.getTime();	
	}

	public SMS() {
		
	}

	public JSONObject toJSONObject(String modemID, String name, String port)
    {
    	JSONObject json = this.toJSONObject();
    	json.put("modemID", modemID);
    	json.put("modemName", name);
    	json.put("modemPort", port);
    	return json;
    }
    public JSONObject toJSONObject()
    {
    	JSONObject json = new JSONObject();
       	json.put("id", this.id);
       	json.put("storage", this.storage);
       	json.put("status", this.status);
       	json.put("phoneNumber", this.phoneNumber);
       	json.put("phoneName", this.phoneName);
       	json.put("date", Utility.date("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", this.date, "UTC"));
       	json.put("content", this.content);
       	return json;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneName() {
        return phoneName;
    }

    public void setPhoneName(String phoneName) {
        this.phoneName = phoneName;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

	public void setDateStr(String date) {
		this.date = this.parseDate(date);
		
	}

	public void appendContent(String content) {
		if(!this.content.isEmpty())
		{
			this.setContent(this.content+"\r\n"+content);
		}
		else
		{
			this.setContent(content);
		}
	}
	
	@Override
    public String toString() {
        return getId() + " " + getStorage() + " " + getStatus() + " " + getPhoneNumber() + " " + getDate() + " " + getContent();
    }
}
