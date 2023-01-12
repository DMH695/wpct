package com.example.wpct.service;

public interface WechatPayService {
    String  jsapiPay(String openid, int orderId) throws Exception;
    String  investProperty(String openid, int money,int hid) throws Exception;
    String  investShare(String openid, int money,int hid) throws Exception;

}
