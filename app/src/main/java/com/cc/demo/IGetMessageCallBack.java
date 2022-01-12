package com.cc.demo;

import org.json.JSONException;

public interface IGetMessageCallBack {
    public void setMessage(String message);
    public void setTopicMessage(String topic,String message) throws JSONException;
}