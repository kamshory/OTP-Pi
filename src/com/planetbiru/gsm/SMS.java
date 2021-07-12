package com.planetbiru.gsm;

import org.json.JSONObject;

public class SMS {
    private int id;
    private String storage;
    private String status;
    private String phoneNumber;
    private String phoneName;
    private String date;
    private String time;
    private String content;
    
    public JSONObject toJSONObject()
    {
    	JSONObject json = new JSONObject();
       	json.put("id", this.id);
       	json.put("storage", this.storage);
       	json.put("status", this.status);
       	json.put("phoneNumber", this.phoneNumber);
       	json.put("phoneName", this.phoneName);
       	json.put("date", this.date);
       	json.put("time", this.time);
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return getId() + " " + getStorage() + " " + getStatus() + " " + getPhoneNumber() + " " + getDate() + " " + getTime() + " " + getContent();
    }
}
