package com.example.wpct.entity;

import lombok.Data;

@Data
public class WxMsgConfig {
    /*接收者（用户）的 openid*/
    private String touser;

    /*所需下发的订阅模板id*/
    private String template_id;

    /*点击消息后跳转的页面*/
    private String page;

    /*跳转小程序类型：developer为开发版；trial为体验版；formal为正式版；默认为正式版*/
    private String miniprogram_state="developer";

    /*进入小程序查看”的语言类型，支持zh_CN(简体中文)、en_US(英文)、zh_HK(繁体中文)、zh_TW(繁体中文)，默认为zh_CN返回值*/
    private String lang="zh_CN";

    /*模板数据，这里定义为object是希望所有的模板都能使用这个消息配置*/
    private Object data;

}
