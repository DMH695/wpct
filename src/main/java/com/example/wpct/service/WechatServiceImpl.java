package com.example.wpct.service;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSONObject;
import com.example.wpct.config.WxPayConfig;
import com.example.wpct.utils.ResultBody;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
@Service
public class WechatServiceImpl implements WechatPayService{
    @Resource
    WxPayConfig wxPayConfig;

    @Override
    public String jsapiPay(String openid, String orderId) throws Exception {
        /*OrderInfo orderInfo = orderInfoMapper.getById(orderId);

        HttpPost httpPost = new HttpPost(wxPayConfig.getDomain().concat("/v3/pay/transactions/jsapi"));

        //请求body参数
        //设置gson不转码
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Map<String, Object> paramsMap = new HashMap<>();

        paramsMap.put("appid", wxPayConfig.getAppid());
        paramsMap.put("mchid", wxPayConfig.getMchId());
        paramsMap.put("description", orderInfo.());
        paramsMap.put("out_trade_no", orderInfo.getOrderNo());   //test
        paramsMap.put("notify_url", "http://wpct.x597.com/weixin/jsapi/notify");  //test

        Map amountMap = new HashMap();
        amountMap.put("total", orderInfo.getTotalFee());
        amountMap.put("currency", "CNY");

        Map payerMap = new HashMap();
        payerMap.put("openid", openid);

        paramsMap.put("amount", amountMap);
        paramsMap.put("payer", payerMap);

        //将参数转换成json字符串
        String jsonParams = gson.toJson(paramsMap);

        StringEntity entity = new StringEntity(jsonParams, "utf-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");

        //完成签名并执行请求
        CloseableHttpResponse response = httpClient.execute(httpPost);

        try {
            String bodyAsString = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) { //处理成功
            } else if (statusCode == 204) { //处理成功，无返回Body
            } else {
                //throw new IOException("request failed");
                //给前端返回信息
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("statusCode",statusCode);
                jsonObject.put("bodyAsString",bodyAsString);
                return ResultBody.ok(jsonObject).toString();
            }

            String nonceStr = RandomUtil.randomString(32);// 随机字符串
            String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);// 时间戳

            //响应结果
            Map<String, String> resultMap = gson.fromJson(bodyAsString,
                    HashMap.class);
            String prepayId = resultMap.get("prepay_id");

            *//*
            //存入 预支付交易会话标识 防止调用下单接口
            orderInfo.setPrepayId(prepayId);
            QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("id",orderId);
            orderInfoMapper.update(orderInfo,queryWrapper);
             *//*


            String Sign = wxPayConfig.getSign(wxPayConfig.getAppid(), Long.parseLong(timeStamp), nonceStr, "prepay_id=" + prepayId);

            resultMap.put("timeStamp", timeStamp);
            resultMap.put("nonceStr", nonceStr);
            resultMap.put("appId", wxPayConfig.getAppid());
            resultMap.put("signType", "RSA");
            resultMap.put("paySign", Sign);
            String resultJson = gson.toJson(resultMap);
            return resultJson;
        } finally {
            response.close();
        }*/
        return null;
    }
}
