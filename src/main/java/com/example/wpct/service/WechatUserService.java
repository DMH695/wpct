package com.example.wpct.service;

import com.example.wpct.utils.ResultBody;
import io.swagger.models.auth.In;

import javax.xml.transform.Result;

public interface WechatUserService {
    ResultBody getWechatUserInfoAndHouseInfo(int pageNum, int pageSize);
    ResultBody deleteWechatUser(Integer id);
}
