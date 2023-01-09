package com.example.wpct.service;

public interface WechatPayService {
    String  jsapiPay(String openid, String orderId) throws Exception;
}
