package com.example.wpct.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.wpct.config.WxPayConfig;
import com.example.wpct.entity.Bill;
import com.example.wpct.entity.HousingInformationDto;
import com.example.wpct.entity.PropertyOrderDto;
import com.example.wpct.entity.WechatUser;
import com.example.wpct.mapper.*;
import com.example.wpct.service.WechatPayService;
import com.example.wpct.utils.HttpUtils;
import com.example.wpct.utils.ResultBody;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wechat.pay.contrib.apache.httpclient.exception.HttpCodeException;
import com.wechat.pay.contrib.apache.httpclient.exception.NotFoundException;
import com.wechat.pay.contrib.apache.httpclient.notification.Notification;
import com.wechat.pay.contrib.apache.httpclient.notification.NotificationHandler;
import com.wechat.pay.contrib.apache.httpclient.notification.NotificationRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WechatServiceImpl implements WechatPayService {
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

    @Autowired
    WechatUserMapper wechatUserMapper;

    @Autowired
    VillageMapper villageMapper;

    @Autowired
    BuildMapper buildMapper;


    /**
     * 微信用户缴费后，需要修改property_order表中的status和housing表中的due_date 并生成账单
     */
    @Override
    public String jsapiPay(String openid, List<String> orderIds) throws Exception {
        double total = 0;
        //List<Long> orderIds1 = Arrays.stream(orderIds).boxed().collect(Collectors.toList());
        //遍历累加计算总金额
        for(String orderId : orderIds){
            QueryWrapper queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("order_no",Long.parseLong(orderId));
            PropertyOrderDto propertyOrderDto = propertyOrderMapper.selectOne(queryWrapper);
            total = total + propertyOrderDto.getCost();
        }
        HttpPost httpPost = new HttpPost(wxPayConfig.getDomain().concat("/v3/pay/transactions/jsapi"));
        //请求body参数
        //设置gson不转码
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("appid", wxPayConfig.getAppid());
        paramsMap.put("mchid", wxPayConfig.getMchId());
        String no = String.valueOf(System.currentTimeMillis());
        paramsMap.put("description", "武平城投-缴交物业费");
        paramsMap.put("out_trade_no", no);   //test
        paramsMap.put("notify_url", "http://wpct.x597.com/weixin/jsapi/notify");  //test

        Map amountMap = new HashMap();
        //金额转化为分
        String str  = String.valueOf(total * 100);
        Integer cost = Integer.parseInt(replace(str));
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
                System.out.println(jsonObject);
                return ResultBody.ok(jsonObject).toString();
            }

            String nonceStr = RandomUtil.randomString(32);// 随机字符串
            String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);// 时间戳

            //响应结果
            Map<String, String> resultMap = gson.fromJson(bodyAsString, HashMap.class);
            String prepayId = resultMap.get("prepay_id");


            String Sign = wxPayConfig.getSign(wxPayConfig.getAppid(), Long.parseLong(timeStamp), nonceStr, "prepay_id=" + prepayId);

            resultMap.put("timeStamp", timeStamp);
            resultMap.put("nonceStr", nonceStr);
            resultMap.put("appId", wxPayConfig.getAppid());
            resultMap.put("signType", "RSA");
            resultMap.put("paySign", Sign);
            String resultJson = gson.toJson(resultMap);
            for(String orderId : orderIds){
                QueryWrapper queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("order_no",Long.parseLong(orderId));
                PropertyOrderDto propertyOrderDto = propertyOrderMapper.selectOne(queryWrapper);
                total = total + propertyOrderDto.getCost();
                //修改property_order表中的payment_status
                propertyOrderMapper.updateStatus(Long.parseLong(orderId));
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
                bill.setRoomNum(housingInformationDto.getHouseNo());
                bill.setType("物业费");
                bill.setLocation("微信");
                String date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(Calendar.getInstance().getTime());
                bill.setDate(date);
                billMapper.insert(bill);
            }
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
            bill.setRoomNum(housingInformationDto.getHouseNo());
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
            bill.setRoomNum(housingInformationDto.getHouseNo());
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

    @Override
    public List<WechatUser> getByOpenid(String openid) {
        return wechatUserMapper.getByOpenid(openid);
    }

    @Override
    public void bind(WechatUser wechatUser) {
        wechatUserMapper.bind(wechatUser);
    }

    @Override
    public List<JSONObject> getTree() {
        return null;
    }

    @Override
    public String payNotify(HttpServletRequest request, HttpServletResponse response) throws GeneralSecurityException, NotFoundException, IOException, HttpCodeException {
        Gson gson = new Gson();
        Map<String, String> map = new HashMap<>();  //应答对象  Json格式


        try {
            //处理通知参数
            String body = HttpUtils.readData(request);
            String wechatPaySerial = request.getHeader("Wechatpay-Serial");
            String nonce = request.getHeader("Wechatpay-Nonce");
            String timestamp = request.getHeader("Wechatpay-Timestamp");
            String signature = request.getHeader("Wechatpay-Signature");
            HashMap<String, Object> bodyMap = gson.fromJson(body, HashMap.class);

            /**
             * {"id":"c17f5eaf-0a90-5e4c-9ec8-e1f068addfcf",
             * "create_time":"2022-10-04T13:25:40+08:00",
             * "resource_type":"encrypt-resource","event_type":
             * "TRANSACTION.SUCCESS","summary":"支付成功",
             * "resource":{"original_type":"transaction","algorithm":"AEAD_AES_256_GCM","ciphertext":"gTK9I96p3gXvyN6c9tkLrv3ogD/adDzjFJxvLDWpD9cOybuefaxMxdh/6OxW64wdBBR8IWdCq+nqs,
             * "associated_data":"transaction",
             * "nonce":"VBNu9IF6GGnX"}}
             */
            String requestId = (String) bodyMap.get("id");

            //构建request，传入必要参数(wxPaySDK0.4.8带有request方式验签的方法 github)
            NotificationRequest Nrequest = new NotificationRequest.Builder()
                    .withSerialNumber(wechatPaySerial)
                    .withNonce(nonce)
                    .withTimestamp(timestamp)
                    .withSignature(signature)
                    .withBody(body)
                    .build();

            NotificationHandler handler = new NotificationHandler(wxPayConfig.getVerifier(), wxPayConfig.getApiV3Key().getBytes(StandardCharsets.UTF_8));
            //验签和解析请求体(只有这里会报错)
            Notification notification = handler.parse(Nrequest);

            //从notification获取请求报文(对称解密)
            String plainText = notification.getDecryptData();
            //将密文转成map 方便拿取
            HashMap resultMap = gson.fromJson(plainText, HashMap.class);


            //TODO 处理订单
            //////////////////////////////////////////////////
            //processOrder(plainText);
            /**
             * 验签结果 ===> {"mchid":"1558950191","appid":"wx74862e0dfcf69954",
             * "out_trade_no":"ORDER_20221004132527865","transaction_id":"4200001569202210040857712725",
             * "trade_type":"NATIVE","trade_state":"SUCCESS","trade_state_desc":"支付成功",
             * "bank_type":"OTHERS","attach":"","success_time":"2022-10-04T13:25:40+08:00",
             * payer":{"openid":"oHwsHuCj4_t6OMpypikZIQ1r-FXY"},
             * "amount":{"total":1,"payer_total":1,"currency":"CNY","payer_currency":"CNY"}}
             */

            //成功应答
            response.setStatus(200);
            map.put("code", "SUCCESS");
            map.put("message", "成功");
            return gson.toJson(map);

        } catch (Exception e) {
            e.printStackTrace();

            //应答失败
            response.setStatus(500);
            map.put("code", "ERROR");
            map.put("message", "验签失败");
            return gson.toJson(map);
        }

    }
    public static String replace(String s){
        if(null != s && s.indexOf(".") > 0){
            s = s.replaceAll("0+?$", "");//去掉多余的0
            s = s.replaceAll("[.]$", "");//如最后一位是.则去掉
        }
        return s;
    }
    @Override
    public WechatUser checkBind(String openid, int hid) {
        return wechatUserMapper.checkBind(openid, hid);
    }
}
