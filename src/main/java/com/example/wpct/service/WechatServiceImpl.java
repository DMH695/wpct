package com.example.wpct.service;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.wpct.config.WxPayConfig;
import com.example.wpct.entity.Bill;
import com.example.wpct.entity.HousingInformationDto;
import com.example.wpct.entity.PropertyOrderDto;
import com.example.wpct.mapper.BillMapper;
import com.example.wpct.mapper.HousingInformationMapper;
import com.example.wpct.mapper.PropertyOrderMapper;
import com.example.wpct.utils.ResultBody;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
@Service
public class WechatServiceImpl implements WechatPayService{
    @Resource
    WxPayConfig wxPayConfig;

    @Resource(name = "WxPayClient")
    private CloseableHttpClient httpClient;

    @Autowired
    PropertyOrderMapper propertyOrderMapper;

    @Autowired
    HousingInformationMapper housingInformationMapper;

    @Autowired
    BillMapper billMapper;

    /**
     * 微信用户缴费后，需要修改property_order表中的status和housing表中的due_date 并生成账单
     */
    @Override
    public String jsapiPay(String openid, int orderId) throws Exception {
        QueryWrapper queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_no",orderId);
        PropertyOrderDto propertyOrderDto = propertyOrderMapper.selectOne(queryWrapper);
        HttpPost httpPost = new HttpPost(wxPayConfig.getDomain().concat("/v3/pay/transactions/jsapi"));
        //请求body参数
        //设置gson不转码
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("appid", wxPayConfig.getAppid());
        paramsMap.put("mchid", wxPayConfig.getMchId());
        paramsMap.put("description", propertyOrderDto.getCostDetail());
        paramsMap.put("out_trade_no", propertyOrderDto.getOrderNo());   //test
        paramsMap.put("notify_url", "http://wpct.x597.com/weixin/jsapi/notify");  //test

        Map amountMap = new HashMap();
        //金额转化为分
        String str  = String.valueOf(propertyOrderDto.getCost() * 100);
        Integer cost = Integer.parseInt(str);
        amountMap.put("total",cost);
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


            String Sign = wxPayConfig.getSign(wxPayConfig.getAppid(), Long.parseLong(timeStamp), nonceStr, "prepay_id=" + prepayId);

            resultMap.put("timeStamp", timeStamp);
            resultMap.put("nonceStr", nonceStr);
            resultMap.put("appId", wxPayConfig.getAppid());
            resultMap.put("signType", "RSA");
            resultMap.put("paySign", Sign);
            String resultJson = gson.toJson(resultMap);
            //修改property_order表中的payment_status
            propertyOrderMapper.updateStatus(orderId);
            //修改housing中的due_date
            housingInformationMapper.updateDate((int) propertyOrderDto.getHouseId());
            //生成账单
            Bill bill = new Bill();
            bill.setPay(propertyOrderDto.getCost());
            bill.setDetail(propertyOrderDto.getCostDetail());
            bill.setOpenid(openid);
            QueryWrapper queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("id",propertyOrderDto.getHouseId());
            HousingInformationDto housingInformationDto = housingInformationMapper.selectOne(queryWrapper1);
            bill.setVillageName(housingInformationDto.getVillageName());
            bill.setBuildName(housingInformationDto.getBuildNumber());
            bill.setRooNum(housingInformationDto.getHouseNo());
            bill.setType("物业费");
            bill.setLocation("微信");
            String date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(Calendar.getInstance().getTime());
            bill.setDate(date);
            billMapper.insert(bill);
            return resultJson;
        } finally {
            response.close();
        }
    }

    @Override
    public String investProperty(String openid, int money,int hid) throws Exception {
        HttpPost httpPost = new HttpPost(wxPayConfig.getDomain().concat("/v3/pay/transactions/jsapi"));
        //请求body参数
        //设置gson不转码
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("appid", wxPayConfig.getAppid());
        paramsMap.put("mchid", wxPayConfig.getMchId());
        paramsMap.put("description","物业费余额充值");
        paramsMap.put("out_trade_no", hid);   //test
        paramsMap.put("notify_url", "http://wpct.x597.com/weixin/jsapi/notify");  //test

        Map amountMap = new HashMap();
        amountMap.put("total", money * 100);
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


            String Sign = wxPayConfig.getSign(wxPayConfig.getAppid(), Long.parseLong(timeStamp), nonceStr, "prepay_id=" + prepayId);

            resultMap.put("timeStamp", timeStamp);
            resultMap.put("nonceStr", nonceStr);
            resultMap.put("appId", wxPayConfig.getAppid());
            resultMap.put("signType", "RSA");
            resultMap.put("paySign", Sign);
            String resultJson = gson.toJson(resultMap);
            //修改housing_information中的property_fee
            String str = String.valueOf(money);
            Double property_fee = Double.parseDouble(str);
            housingInformationMapper.investProperty(property_fee,hid);
            //生成账单
            Bill bill = new Bill();
            QueryWrapper queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("id",hid);
            HousingInformationDto housingInformationDto = housingInformationMapper.selectOne(queryWrapper1);
            bill.setVillageName(housingInformationDto.getVillageName());
            bill.setBuildName(housingInformationDto.getBuildNumber());
            bill.setRooNum(housingInformationDto.getHouseNo());
            String date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(Calendar.getInstance().getTime());
            bill.setDate(date);
            bill.setOpenid(openid);
            bill.setLocation("微信");
            bill.setPay(property_fee);
            bill.setDetail("物业费余额充值");
            billMapper.insert(bill);
            return resultJson;
        } finally {
            response.close();
        }
    }

    @Override
    public String investShare(String openid, int money, int hid) throws Exception {
        HttpPost httpPost = new HttpPost(wxPayConfig.getDomain().concat("/v3/pay/transactions/jsapi"));
        //请求body参数
        //设置gson不转码
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("appid", wxPayConfig.getAppid());
        paramsMap.put("mchid", wxPayConfig.getMchId());
        paramsMap.put("description","公摊费余额充值");
        paramsMap.put("out_trade_no", hid);   //test
        paramsMap.put("notify_url", "http://wpct.x597.com/weixin/jsapi/notify");  //test

        Map amountMap = new HashMap();
        amountMap.put("total", money * 100);
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


            String Sign = wxPayConfig.getSign(wxPayConfig.getAppid(), Long.parseLong(timeStamp), nonceStr, "prepay_id=" + prepayId);

            resultMap.put("timeStamp", timeStamp);
            resultMap.put("nonceStr", nonceStr);
            resultMap.put("appId", wxPayConfig.getAppid());
            resultMap.put("signType", "RSA");
            resultMap.put("paySign", Sign);
            String resultJson = gson.toJson(resultMap);
            //修改housing_information中的property_fee
            String str = String.valueOf(money);
            Double poolBanlance = Double.parseDouble(str);
            housingInformationMapper.investShare(poolBanlance,hid);
            //生成账单
            Bill bill = new Bill();
            QueryWrapper queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("id",hid);
            HousingInformationDto housingInformationDto = housingInformationMapper.selectOne(queryWrapper1);
            bill.setVillageName(housingInformationDto.getVillageName());
            bill.setBuildName(housingInformationDto.getBuildNumber());
            bill.setRooNum(housingInformationDto.getHouseNo());
            String date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(Calendar.getInstance().getTime());
            bill.setDate(date);
            bill.setOpenid(openid);
            bill.setLocation("微信");
            bill.setPay(poolBanlance);
            bill.setDetail("公摊费余额充值");
            billMapper.insert(bill);
            return resultJson;
        } finally {
            response.close();
        }
    }
}
