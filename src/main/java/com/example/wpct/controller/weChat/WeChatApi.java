package com.example.wpct.controller.weChat;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.wpct.config.WxPayConfig;
import com.example.wpct.entity.WechatUser;
import com.example.wpct.service.WechatPayService;
import com.example.wpct.utils.ResultBody;
import com.google.gson.Gson;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Api(tags = "微信相关API")
@RestController
@RequestMapping("/weixin")
public class WeChatApi {
    @Resource
    WxPayConfig wxPayConfig;

    @Autowired
    WechatPayService wechatPayService;

    @ApiOperation("获取openid")
    @RequestMapping(value = "/get",method = RequestMethod.GET)
    public Object get(HttpServletResponse response, HttpServletRequest request) throws IOException {
        String code = request.getParameter("code");
        // String state = request.getParameter("state");

        /**
         * 获取code后，请求以下链接获取access_token：
         * https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
         */
        //2.通过code换取网页token
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?" +
                "appid=" + wxPayConfig.getAppid() +
                "&secret=" + wxPayConfig.getAppSecret() +
                "&code=" + code +
                "&grant_type=authorization_code";
        String s = HttpUtil.get(url);
        JSONObject object = JSON.parseObject(s);


        /**
         {
         "access_token":"ACCESS_TOKEN",
         "expires_in":7200,
         "refresh_token":"REFRESH_TOKEN",
         "openid":"OPENID",
         "scope":"SCOPE"
         }
         */
        String accessToken = object.getString("access_token");
        String openid = object.getString("openid");

        //3.根据openid和token获取用户基本信息
        /**
         * http：GET（请使用https协议）
         * https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN
         */
        String userUrl = "https://api.weixin.qq.com/sns/userinfo?" +
                "access_token=" + accessToken +
                "&openid=" + openid +
                "&lang=zh_CN";

        // String userRes = WeiXinUtil.httpRequest(userUrl, "GET", null);
        String userRes = HttpUtil.get(userUrl);

        //将openid返回给前端  10-19
        Gson gson = new Gson();
        HashMap getMap = gson.fromJson(userRes, HashMap.class);
        String getOpenid = (String) getMap.get("openid");
        Map<String,String> rMap = new HashMap<>();
        rMap.put("openid",getOpenid);
        JSONObject jsonObject = JSONObject.parseObject(userRes);
        /*WechatUser user = wechatUserService.query().eq("openid", openid).one();
        if (user == null) {   //TODO *一个人也可以绑定多个房屋信息 这里逻辑是一个openid（用户）只能绑定一套房屋
            //保存用户信息
            WechatUser wechatUser = new WechatUser();
            wechatUser.setOpenid(jsonObject.getString("openid"));
            wechatUser.setNickname(jsonObject.getString("nickname"));
            wechatUserService.save(wechatUser);
        }*/
//        if (user == null || user.getPid() == null) {
//            //TODO 跳转注册页面
//        }
//        // TODO 绑定身份证-> 跳转首页


        userUrl = "https://60z8193p42.goho.co//zqb/new.html" + "?openid=" + jsonObject.getString("openid");

        //TODO 如果没有授权登录 跳转注册页面  未完成
        //response.sendRedirect(userUrl);
        return ResultBody.ok(rMap);
    }

    @ApiOperation("JSAPI下单[没写好]")
    @PostMapping("/jsapi/pay")
    public Object wechatPay(@RequestParam String openid, @RequestParam(name = "id") String orderId) throws Exception {
        String resultJson = wechatPayService.jsapiPay(openid, orderId);
        return ResultBody.ok(resultJson);
    }

}
