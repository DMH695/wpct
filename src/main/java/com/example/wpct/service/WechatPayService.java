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
    String  jsapiPay(String openid, List<String> orderIds) throws Exception;
    String  investProperty(String openid, int money,int hid) throws Exception;
    String  investShare(String openid, int money,int hid) throws Exception;
    List<WechatUser> getByOpenid(String openid);
    void bind(WechatUser wechatUser);
    List<JSONObject> getTree();
    String payNotify(HttpServletRequest request, HttpServletResponse response) throws GeneralSecurityException, NotFoundException, IOException, HttpCodeException;
    WechatUser checkBind(String openid,int hid);
}
