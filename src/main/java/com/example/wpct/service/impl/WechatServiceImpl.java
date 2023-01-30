package com.example.wpct.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.wpct.config.WxPayConfig;
import com.example.wpct.entity.*;
import com.example.wpct.mapper.*;
import com.example.wpct.service.WechatPayService;
import com.example.wpct.utils.HttpUtils;
import com.example.wpct.utils.ResultBody;
import com.example.wpct.utils.WxSendMsgUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wechat.pay.contrib.apache.httpclient.exception.HttpCodeException;
import com.wechat.pay.contrib.apache.httpclient.exception.NotFoundException;
import com.wechat.pay.contrib.apache.httpclient.notification.Notification;
import com.wechat.pay.contrib.apache.httpclient.notification.NotificationHandler;
import com.wechat.pay.contrib.apache.httpclient.notification.NotificationRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WechatServiceImpl implements WechatPayService {

    @Resource
    private RestTemplate restTemplate;

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

    @Autowired
    SharedFeeOrderMapper sharedFeeOrderMapper;

    @Autowired
    WxSendMsgUtil wxSendMsgUtil;
    @Resource
    private final StringRedisTemplate stringRedisTemplate;

    public WechatServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }


    /**
     * 微信用户缴费后，需要修改property_order表中的status和housing表中的due_date 并生成账单
     */
    @Override
    public String jsapiPay(String openid, List<String> propertyOrderNos,List<String> sharedOrderNos) throws Exception {
        double total = 0;
        //遍历累加计算总金额
        if (propertyOrderNos != null){
            for(String orderId : propertyOrderNos){
                QueryWrapper queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("order_no",Long.parseLong(orderId));
                PropertyOrderDto propertyOrderDto = propertyOrderMapper.selectOne(queryWrapper);
                total = total + propertyOrderDto.getCost();
            }
        }
        if (sharedOrderNos != null){
            for (String orderId : sharedOrderNos){
                QueryWrapper queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("order_no",Long.parseLong(orderId));
                SharedFeeOrderDto sharedFeeOrderDto = sharedFeeOrderMapper.selectOne(queryWrapper);
                total = total + sharedFeeOrderDto.getCost();
            }
        }
        log.warn("调用统一下单api");
        HttpPost httpPost = new HttpPost(wxPayConfig.getDomain().concat("/v3/pay/transactions/jsapi"));
        //请求body参数
        //设置gson不转码
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("appid", wxPayConfig.getAppid());
        paramsMap.put("mchid", wxPayConfig.getMchId());
        String no = String.valueOf(System.currentTimeMillis());
        System.out.println(no);
        paramsMap.put("description", "武平城投-缴交物业费");
        paramsMap.put("out_trade_no", no);   //test
        //将no和对应的订单号、openid存入redis
        JSONObject jsonObject1 = new JSONObject();
        propertyOrderNos.add(openid);
        jsonObject1.put("property",propertyOrderNos);
        sharedOrderNos.add(openid);
        jsonObject1.put("shared",sharedOrderNos);
        stringRedisTemplate.opsForValue().set(no,jsonObject1.toString(), 60 * 10,
                TimeUnit.SECONDS);
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
        log.info("请求参数:{}", jsonParams);
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
                log.info("成功, 返回结果 = " + bodyAsString);
            } else if (statusCode == 204) { //处理成功，无返回Body
                log.info("成功");
            } else {
                //throw new IOException("request failed");
                //给前端返回信息
                log.info("JSAPI下单失败,响应码 = " + statusCode + ",返回结果 = " +
                        bodyAsString);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("statusCode",statusCode);
                jsonObject.put("bodyAsString",bodyAsString);
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
            //加入自定义订单号
            resultMap.put("out_trade_no",no);
            String resultJson = gson.toJson(resultMap);
            log.warn("resultJson是=====>{}", resultJson);
            //后续的业务逻辑
            /*for(String orderId : orderIds){
                QueryWrapper queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("order_no",Long.parseLong(orderId));
                PropertyOrderDto propertyOrderDto = propertyOrderMapper.selectOne(queryWrapper);
                total = total + propertyOrderDto.getCost();
                //修改property_order表中的payment_status
                propertyOrderMapper.updateStatus(Long.parseLong(orderId));
                //修改housing中的due_date
                housingInformationMapper.updateDate(propertyOrderDto.getHouseId());
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
            }*/
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
            processNotify(plainText);
            //将密文转成map 方便拿取
            HashMap resultMap = gson.fromJson(plainText, HashMap.class);


            //TODO 处理订单
            //////////////////////////////////////////////////
            //processOrder(plainText);
            /**
             * 验签结果 ===> {"mchid":"1558950191","appid":"wx74862e0dfcf69954",
              "out_trade_no":"ORDER_20221004132527865","transaction_id":"4200001569202210040857712725",
              "trade_type":"NATIVE","trade_state":"SUCCESS","trade_state_desc":"支付成功",
              "bank_type":"OTHERS","attach":"","success_time":"2022-10-04T13:25:40+08:00",
              payer":{"openid":"oHwsHuCj4_t6OMpypikZIQ1r-FXY"},
              "amount":{"total":1,"payer_total":1,"currency":"CNY","payer_currency":"CNY"}}
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

    /**
     * 查单接口调用
     *
     * @param orderNo
     */
    @Override
    public String queryOrder(Long orderNo,String openid) throws IOException {

        log.info("查单接口调用====>{}", orderNo);

        String url = String.format(WxApiType.ORDER_QUERY_BY_NO.getType(),Long.toString(orderNo));
        url = wxPayConfig.getDomain().concat(url).concat("?mchid=").concat(wxPayConfig.getMchId());


        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Accept", "application/json");

        //完成签名并执行请求
        CloseableHttpResponse response = httpClient.execute(httpGet);

        try {
            String bodyAsString = EntityUtils.toString(response.getEntity());//响应体
            int statusCode = response.getStatusLine().getStatusCode();//响应状态码
            if (statusCode == 200) { //处理成功
                log.info("成功, 返回结果 = " + bodyAsString);
                processNotify(bodyAsString);
            } else if (statusCode == 204) { //处理成功，无返回Body
                log.info("成功");
            } else {
                log.info("JSAPI查单失败,响应码 = " + statusCode + ",返回结果 = " +
                        bodyAsString);
                throw new IOException("request failed");
            }
            return bodyAsString;
        } finally {
            response.close();
        }
    }

    @Override
    public String test() {
        String str = "{\"mchid\":\"1558950191\",\"appid\":\"wx74862e0dfcf69954\",\n" +
                "              \"out_trade_no\":\"1674019989031\",\"transaction_id\":\"4200001569202210040857712725\",\n" +
                "              \"trade_type\":\"NATIVE\",\"trade_state\":\"SUCCESS\",\"trade_state_desc\":\"支付成功\",\n" +
                "              \"bank_type\":\"OTHERS\",\"attach\":\"\",\"success_time\":\"2022-10-04T13:25:40+08:00\",\n" +
                "              payer\":{\"openid\":\"oXXFD6gTkRajlHDiSIxE1PpMMvek\"},\n" +
                "              \"amount\":{\"total\":1,\"payer_total\":1,\"currency\":\"CNY\",\"payer_currency\":\"CNY\"}}";
        processNotify(str);
        return "666";
    }

    public void processOrder(String plainText,String openid) {
        log.info("处理订单");
        Gson gson = new Gson();

        //拿到map格式
        Map<String, Object> plainTextMap = gson.fromJson(plainText, HashMap.class);
        String out_trade_no = (String) plainTextMap.get("out_trade_no");
        String orderIds = stringRedisTemplate.opsForValue().get(out_trade_no);
        orderIds = orderIds.replaceAll("\\[", "").replaceAll("\\]", "");
        List<Long> ids = new ArrayList<>();
        for(String s : orderIds.split(",")){
            QueryWrapper queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("order_no",Long.parseLong(s.trim()));
            PropertyOrderDto propertyOrderDto = propertyOrderMapper.selectOne(queryWrapper);
            //修改property_order表中的payment_status
            propertyOrderMapper.updateStatus(Long.parseLong(s.trim()));
            //修改housing中的due_date
            housingInformationMapper.updateDate(propertyOrderDto.getHouseId());
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

    }

    /**
     * 结果通知后的也出逻辑处理
     */
    @SneakyThrows
    public void processNotify(String plainText) {
        log.info("处理订单");
        Gson gson = new Gson();
        //拿到map格式
        Map<String, Object> plainTextMap = gson.fromJson(plainText, HashMap.class);
        String out_trade_no = (String) plainTextMap.get("out_trade_no");
        String res = stringRedisTemplate.opsForValue().get(out_trade_no);
        JSONObject jsonObject = JSONObject.parseObject(res);
        String propertyOrders = jsonObject.getString("property");
        String sharedOrders = jsonObject.getString("shared");
        log.info("从redis中获取的数据：" + res);
        log.info("物业费:" + propertyOrders);
        log.info("公摊费" + sharedOrders);
        //物业费处理
        if (propertyOrders != null) {
            propertyOrders = propertyOrders.replaceAll("\\[", "").replaceAll("\\]", "");
            List<Long> ids = new ArrayList<>();
            String[] strings = propertyOrders.split(",");
            String openid = strings[strings.length - 1].trim();
            //log.info("openid为"  + openid);
            List<String> list = new ArrayList<>(Arrays.asList(strings));
            //log.info("当前数组为" + list);
            list.remove(strings.length - 1);
            for (String s : list) {
                QueryWrapper queryWrapper = new QueryWrapper<>();
                s = s.replaceAll("\"","");
                queryWrapper.eq("order_no", Long.parseLong(s.trim()));
                PropertyOrderDto propertyOrderDto = propertyOrderMapper.selectOne(queryWrapper);
                //修改property_order表中的payment_status
                propertyOrderMapper.updateStatus(Long.parseLong(s.trim()));
                //修改housing中的due_date
                HousingInformationDto housingInformationDto = housingInformationMapper.selectById(propertyOrderDto.getHouseId());
                String due_date = housingInformationDto.getDueDate();
                if (due_date == null){
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.MONTH,1);
                    String date1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(calendar.getTime());
                    housingInformationMapper.updateDate(propertyOrderDto.getHouseId());
                }else {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    Date date = sdf.parse(due_date);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    calendar.add(Calendar.MONTH,1);
                    String date1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(calendar.getTime());
                    housingInformationMapper.updateDate(propertyOrderDto.getHouseId());
                }
                housingInformationMapper.updateDate(propertyOrderDto.getHouseId());
                //生成账单
                Bill bill = new Bill();
                bill.setPay(propertyOrderDto.getCost());
                bill.setDetail(propertyOrderDto.getCostDetail());
                bill.setOpenid(openid);
                QueryWrapper queryWrapper1 = new QueryWrapper<>();
                queryWrapper1.eq("id", propertyOrderDto.getHouseId());
                bill.setVillageName(housingInformationDto.getVillageName());
                bill.setBuildName(housingInformationDto.getBuildNumber());
                bill.setRoomNum(housingInformationDto.getHouseNo());
                bill.setType("物业费");
                bill.setLocation("微信");
                String date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(Calendar.getInstance().getTime());
                bill.setDate(date);
                billMapper.insert(bill);
            }
        }
        //公摊费处理
            if (sharedOrders != null){
                sharedOrders = sharedOrders.replaceAll("\\[", "").replaceAll("\\]", "");
                List<Long> ids = new ArrayList<>();
                String[] strings = sharedOrders.split(",");
                String openid = strings[strings.length - 1].trim();
                //log.info("openid为"  + openid);
                List<String> list = new ArrayList<>(Arrays.asList(strings));
                //log.info("当前数组为" + list);
                list.remove(strings.length - 1);
                for (String s : list) {
                    s = s.replaceAll("\"","");
                    QueryWrapper queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("order_no", Long.parseLong(s.trim()));
                    SharedFeeOrderDto sharedFeeOrderDto = sharedFeeOrderMapper.selectOne(queryWrapper);
                    //修改shared_fee_order中的payment_status
                    String date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(Calendar.getInstance().getTime());
                    sharedFeeOrderMapper.updateDate(Long.parseLong(s.trim()),date);
                    //生成账单
                    Bill bill = new Bill();
                    bill.setPay(sharedFeeOrderDto.getCost());
                    bill.setDetail(sharedFeeOrderDto.getCostDetail());
                    bill.setOpenid(openid);
                    QueryWrapper queryWrapper1 = new QueryWrapper<>();
                    queryWrapper1.eq("id", sharedFeeOrderDto.getHouseId());
                    HousingInformationDto housingInformationDto = housingInformationMapper.selectOne(queryWrapper1);
                    bill.setVillageName(housingInformationDto.getVillageName());
                    bill.setBuildName(housingInformationDto.getBuildNumber());
                    bill.setRoomNum(housingInformationDto.getHouseNo());
                    bill.setType("公摊费");
                    bill.setLocation("微信");
                    bill.setDate(date);
                    billMapper.insert(bill);
                }
            }
    }

    public static String replace(String s){
        if(null != s && s.indexOf(".") > 0){
            s = s.replaceAll("0+?$", "");//去掉多余的0
            s = s.replaceAll("[.]$", "");//如最后一位是.则去掉
        }
        return s;
    }

    public ResultBody sendMsg(int hid,String name,String cost,String openid) throws Exception{
        WxMsgConfig requestData = this.getMsgConfig(hid,name,cost,openid);

        log.info("推送消息请求参数：{}", JSON.toJSONString(requestData));

        String url = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + wxSendMsgUtil.getAccessToken();
        log.info("推送消息请求地址：{}", url);
        JSONObject responseData = postData(url, requestData);
        log.info("推送消息返回参数：{}", JSON.toJSONString(responseData));

        Integer errorCode = responseData.getInteger("errcode");
        String errorMessage = responseData.getString("errmsg");
        if (errorCode == 0) {
            log.info("推送消息发送成功");
            return    ResultBody.ok(responseData);
        } else {
            log.info("推送消息发送失败,errcode：{},errorMessage：{}", errorCode, errorMessage);
        }
        return ResultBody.ok(errorCode.toString());
    }

    @SneakyThrows
    public WxMsgConfig getMsgConfig(int hid, String name,String cost,String openid) {
        QueryWrapper queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",hid);
        HousingInformationDto housingInformationDto = housingInformationMapper.selectOne(queryWrapper);
        String villageName = housingInformationDto.getVillageName();
        String buildName = housingInformationDto.getBuildNumber();
        String roomNum = housingInformationDto.getHouseNo();
        WxMsgTemplateHasten wxMsgTemplateHasten = new WxMsgTemplateHasten();
        wxMsgTemplateHasten.setFirst(villageName + "-"
                + buildName + "-"
                + roomNum +
                "缴费提醒");
        /*房屋号*/
        wxMsgTemplateHasten.setKeyword1(villageName + "-"
                + buildName + "-"
                + roomNum);
        /*缴费人*/
        wxMsgTemplateHasten.setKeyword2(name);
        /*缴费类型*/
        wxMsgTemplateHasten.setKeyword3("合计金额");
        /*缴费状态*/
        wxMsgTemplateHasten.setKeyword4("未缴费");
        /*合计金额*/

        //BigDecimal bigDecimal = new BigDecimal(100);
        wxMsgTemplateHasten.setKeyword5(cost);
        wxMsgTemplateHasten.setRemark("请及时缴交费用~");
        /*消息推送配置参数拼接*/
        WxMsgConfig wxMsgConfig = new WxMsgConfig();
        wxMsgConfig.setTouser(openid);
        wxMsgConfig.setTemplate_id("QgLoZpp1KWNskam2jclpxXmmSu4nhVZkv8bPU9wEqS4");
        wxMsgConfig.setData(wxMsgTemplateHasten);
        return wxMsgConfig;
    }


    /**
     * 发送请求
     */
    public JSONObject postData(String url, WxMsgConfig param) {
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(type);
        HttpEntity<WxMsgConfig> httpEntity = new HttpEntity<>(param, headers);
        JSONObject jsonResult = restTemplate.postForObject(url, httpEntity, JSONObject.class);
        return jsonResult;
    }

    @Override
    public WechatUser checkBind(String openid, int hid) {
        return wechatUserMapper.checkBind(openid, hid);
    }
}
