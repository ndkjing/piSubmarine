package com.cc.demo;


import okhttp3.*;

import java.util.concurrent.TimeUnit;


/**
 * @author CC
 */
public class OkHttpUtils {

    private final static String BASEURL = "http://172.25.61.4:8002/news/";
    //联网工具类对象，主要是联网进行一些操作
    public static OkHttpUtils instance;
    //1 创建一个OkHttp对象
    private static OkHttpClient client;
    MediaType jsonType = MediaType.parse("application/json; charset=utf-8");

    public OkHttpUtils() {
        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();
    }

    //网络请求的单例模式 一般在有3个线程以上时应用...
    public static OkHttpUtils getInstance() {
        if (instance == null) {
            synchronized (OkHttpUtils.class) {
                if (instance == null) {
                    instance = new OkHttpUtils();
                }
            }
        }
        return instance;
    }


    /**
     * 新增订单
     */
    public void addOrder(String data, Callback callback) {
        RequestBody body = RequestBody.create(jsonType, data);
        doPost(BASEURL + "express", body, callback);

    }

    /**
     * 根据订单号、获取快递详情
     */
    public void getExpressListDetail(String exNum, Callback callback) {
        doGet(BASEURL + "express/list?exNum=" + exNum, callback);

    }

    /**
     * 获取本人的快递列表
     */
    public void getExpressListBYType(String account, String type, Callback callback) {
        doGet(BASEURL + "express/list?exSenderUserPhone=" + account + "&exState=" + type, callback);

    }

    /**
     * 获取本人收到的快递列表(我的快递)
     */
    public void getExpressList(String account, Callback callback) {
        doGet(BASEURL + "express/list?exReceiveUserPhone=" + account + "&exState=待取件", callback);

    }

    /**
     * 获取本人的快递列表
     */
    public void addAddress(String data, Callback callback) {
        RequestBody body = RequestBody.create(jsonType, data);
        doPost(BASEURL + "address", body, callback);

    }

    /**
     * 修改快递状态
     */
    public void modifyExpressStatus(String data, Callback callback) {
        RequestBody body = RequestBody.create(jsonType, data);
        doPut(BASEURL + "express", body, callback);

    }

    /**
     * 获取本人的地址
     */
    public void getAddressList(String account, Callback callback) {
        doGet(BASEURL + "address/list?userPhone=" + account, callback);

    }

    /**
     * 获取快递公司列表
     */
    public void getCompanyList(Callback callback) {
        doGet(BASEURL + "company/list", callback);

    }

    /**
     * 搜索
     */
    public void find(String account, Callback callback) {
        doGet(BASEURL + "express/findInfo?exNum=" + account, callback);
    }

    private void doGet(String url, Callback callback) {
        Request requestGet = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .get()
                .build();
        client.newCall(requestGet).enqueue(callback);
    }

    private void doPost(String url, RequestBody body, Callback callback) {
        Request requestPost = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();
        client.newCall(requestPost).enqueue(callback);
    }

    private void doPut(String url, RequestBody body, Callback callback) {
        Request requestPost = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .put(body)
                .build();
        client.newCall(requestPost).enqueue(callback);
    }

    private void doDelete(String url, Callback callback) {
        Request requestPost = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .delete()
                .build();
        client.newCall(requestPost).enqueue(callback);
    }


    public void reg(String data, Callback callback) {
        RequestBody body = RequestBody.create(jsonType, data);
        doPost(BASEURL + "user", body, callback);
    }

    public void reduce(Callback callback) {


    }

    public void addition(Callback callback) {

    }

    public void closeCamera(Callback callback) {

    }

    public void automaticMode(Callback callback) {

    }

    public void reset(Callback callback) {

    }
}
