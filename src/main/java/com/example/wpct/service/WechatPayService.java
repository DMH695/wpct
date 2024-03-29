package com.example.wpct.service;

import com.alibaba.fastjson.JSONObject;
import com.example.wpct.entity.WechatUser;
import com.wechat.pay.contrib.apache.httpclient.exception.HttpCodeException;
import com.wechat.pay.contrib.apache.httpclient.exception.NotFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public interface WechatPayService {
    String  jsapiPay(String openid, List<String> propertyOrderNos,List<String> sharedOrderNos,String userType) throws Exception;
    String  investProperty(String openid, int property,int shared,int hid,String userType) throws Exception;
    String  investShare(String openid, int money,int hid) throws Exception;
    List<WechatUser> getByOpenid(String openid);
    void bind(WechatUser wechatUser);
    List<JSONObject> getTree();
    String payNotify(HttpServletRequest request, HttpServletResponse response) throws GeneralSecurityException, NotFoundException, IOException, HttpCodeException;
    WechatUser checkBind(String openid,int hid);
    String queryOrder(Long orderNo,String openid) throws IOException;
    String queryOrder1(Long orderNo) throws IOException;
    String test();
    void refund(String out_trade_no, String reason,Integer refundFee,String type) throws Exception;

}
