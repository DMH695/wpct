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
    private PropertyOrderMapper propertyOrderMapper;

    @Autowired
    private HousingInformationMapper housingInformationMapper;

    @Autowired
    private BillMapper billMapper;

    @Autowired
    private WechatUserMapper wechatUserMapper;

    @Autowired
    private VillageMapper villageMapper;

    @Autowired
    private BuildMapper buildMapper;

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
     * ????????????????????????????????????property_order?????????status???housing?????????due_date ???????????????
     */
    @Override
    public String jsapiPay(String openid, List<String> propertyOrderNos, List<String> sharedOrderNos) throws Exception {
        double total = 0;
        HousingInformationDto housingInformationDto = new HousingInformationDto();
        JSONObject jsonObject1 = new JSONObject();
        //???????????????????????????
        if (propertyOrderNos != null) {
            for (String orderId : propertyOrderNos) {
                QueryWrapper queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("order_no", Long.parseLong(orderId));
                PropertyOrderDto propertyOrderDto = propertyOrderMapper.selectOne(queryWrapper);
                housingInformationDto = housingInformationMapper.selectById(propertyOrderDto.getHouseId());
                total = total + propertyOrderDto.getCost();
            }
            double propertyFee =  housingInformationDto.getPropertyFee();
            total = total - propertyFee;
            System.out.println(total);
            System.out.println(propertyFee);
            jsonObject1.put("propertyFee",propertyFee);
        }
        if (sharedOrderNos != null) {
            for (String orderId : sharedOrderNos) {
                QueryWrapper queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("order_no", Long.parseLong(orderId));
                SharedFeeOrderDto sharedFeeOrderDto = sharedFeeOrderMapper.selectOne(queryWrapper);
                housingInformationDto = housingInformationMapper.selectById(sharedFeeOrderDto.getHouseId());
                total = total + sharedFeeOrderDto.getCost();
            }
            double sharedFee = housingInformationDto.getPoolBalance();
            total = total - sharedFee;

            System.out.println(total);
            System.out.println(sharedFee);
            jsonObject1.put("sharedFee",sharedFee);
        }
        log.warn("??????????????????api");
        HttpPost httpPost = new HttpPost(wxPayConfig.getDomain().concat("/v3/pay/transactions/jsapi"));
        //??????body??????
        //??????gson?????????
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("appid", wxPayConfig.getAppid());
        paramsMap.put("mchid", wxPayConfig.getMchId());
        String no = String.valueOf(System.currentTimeMillis());
        System.out.println(no);
        paramsMap.put("description", "????????????-???????????????");
        paramsMap.put("out_trade_no", no);   //test
        //???no????????????????????????openid??????redis
        if (propertyOrderNos != null){
            propertyOrderNos.add(openid);
            jsonObject1.put("property", propertyOrderNos);
        }
        if(sharedOrderNos != null){
            sharedOrderNos.add(openid);
            jsonObject1.put("shared", sharedOrderNos);
        }
        stringRedisTemplate.opsForValue().set(no, jsonObject1.toString(), 60 * 10,
                TimeUnit.SECONDS);
        paramsMap.put("notify_url", "http://wpct.x597.com/weixin/jsapi/notify");  //test
        Map amountMap = new HashMap();
        //??????????????????
        String str = String.valueOf(total * 100);
        Integer cost = Integer.parseInt(replace(str));
        amountMap.put("total", cost);
        amountMap.put("currency", "CNY");
        Map payerMap = new HashMap();
        payerMap.put("openid", openid);
        paramsMap.put("amount", amountMap);
        paramsMap.put("payer", payerMap);
        //??????????????????json?????????
        String jsonParams = gson.toJson(paramsMap);
        log.info("????????????:{}", jsonParams);
        StringEntity entity = new StringEntity(jsonParams, "utf-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        //???????????????????????????
        CloseableHttpResponse response = httpClient.execute(httpPost);
        try {
            String bodyAsString = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) { //????????????
                log.info("??????, ???????????? = " + bodyAsString);
            } else if (statusCode == 204) { //????????????????????????Body
                log.info("??????");
            } else {
                //throw new IOException("request failed");
                //?????????????????????
                log.info("JSAPI????????????,????????? = " + statusCode + ",???????????? = " +
                        bodyAsString);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("statusCode", statusCode);
                jsonObject.put("bodyAsString", bodyAsString);
                return ResultBody.ok(jsonObject).toString();
            }
            String nonceStr = RandomUtil.randomString(32);// ???????????????
            String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);// ?????????
            //????????????
            Map<String, String> resultMap = gson.fromJson(bodyAsString, HashMap.class);
            String prepayId = resultMap.get("prepay_id");
            String Sign = wxPayConfig.getSign(wxPayConfig.getAppid(), Long.parseLong(timeStamp), nonceStr, "prepay_id=" + prepayId);
            resultMap.put("timeStamp", timeStamp);
            resultMap.put("nonceStr", nonceStr);
            resultMap.put("appId", wxPayConfig.getAppid());
            resultMap.put("signType", "RSA");
            resultMap.put("paySign", Sign);
            //????????????????????????
            resultMap.put("out_trade_no", no);
            String resultJson = gson.toJson(resultMap);
            log.warn("resultJson???=====>{}", resultJson);
            //?????????????????????
            return resultJson;
        } finally {
            response.close();
        }
    }

    @Override
    public String investProperty(String openid, int property, int shared, int hid) throws Exception {
        HttpPost httpPost = new HttpPost(wxPayConfig.getDomain().concat("/v3/pay/transactions/jsapi"));
        //??????body??????
        //??????gson?????????
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("appid", wxPayConfig.getAppid());
        paramsMap.put("mchid", wxPayConfig.getMchId());
        paramsMap.put("description", "??????????????????????????????");
        String no = String.valueOf(System.currentTimeMillis());
        paramsMap.put("out_trade_no", no);
        paramsMap.put("notify_url", "http://wpct.x597.com/weixin/jsapi/notify");  //test

        Map amountMap = new HashMap();
        amountMap.put("total", property + shared);
        amountMap.put("currency", "CNY");

        Map payerMap = new HashMap();
        payerMap.put("openid", openid);

        paramsMap.put("amount", amountMap);
        paramsMap.put("payer", payerMap);

        //??????????????????json?????????
        String jsonParams = gson.toJson(paramsMap);

        StringEntity entity = new StringEntity(jsonParams, "utf-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");

        //???????????????????????????
        CloseableHttpResponse response = httpClient.execute(httpPost);

        try {
            String bodyAsString = EntityUtils.toString(response.getEntity());
            System.out.println(bodyAsString);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) { //????????????
            } else if (statusCode == 204) { //????????????????????????Body
            } else {
                //throw new IOException("request failed");
                //?????????????????????
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("statusCode", statusCode);
                jsonObject.put("bodyAsString", bodyAsString);
                return ResultBody.ok(jsonObject).toString();
            }

            String nonceStr = RandomUtil.randomString(32);// ???????????????
            String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);// ?????????

            //????????????
            Map<String, String> resultMap = gson.fromJson(bodyAsString,
                    HashMap.class);
            String prepayId = resultMap.get("prepay_id");


            String Sign = wxPayConfig.getSign(wxPayConfig.getAppid(), Long.parseLong(timeStamp), nonceStr, "prepay_id=" + prepayId);

            resultMap.put("timeStamp", timeStamp);
            resultMap.put("nonceStr", nonceStr);
            resultMap.put("appId", wxPayConfig.getAppid());
            resultMap.put("signType", "RSA");
            resultMap.put("paySign", Sign);
            resultMap.put("out_trade_no",no);
            String resultJson = gson.toJson(resultMap);
            //???no???hid???propertyFee???poolBanlance??????redis  ????????????
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("propertyFee", property);
            jsonObject1.put("shared", shared);
            jsonObject1.put("openid", openid);
            jsonObject1.put("hid", hid);
            stringRedisTemplate.opsForValue().set(no, jsonObject1.toString(), 60 * 10,
                    TimeUnit.SECONDS);
            return resultJson;
        } finally {
            response.close();
        }
    }

    @Override
    public String investShare(String openid, int money, int hid) throws Exception {
        HttpPost httpPost = new HttpPost(wxPayConfig.getDomain().concat("/v3/pay/transactions/jsapi"));
        //??????body??????
        //??????gson?????????
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("appid", wxPayConfig.getAppid());
        paramsMap.put("mchid", wxPayConfig.getMchId());
        paramsMap.put("description", "?????????????????????");
        String no = String.valueOf(System.currentTimeMillis());
        paramsMap.put("out_trade_no", no);  //test
        paramsMap.put("notify_url", "http://wpct.x597.com/weixin/jsapi/notify");  //test

        Map amountMap = new HashMap();
        amountMap.put("total", money);
        amountMap.put("currency", "CNY");

        Map payerMap = new HashMap();
        payerMap.put("openid", openid);
        paramsMap.put("amount", amountMap);
        paramsMap.put("payer", payerMap);

        //??????????????????json?????????
        String jsonParams = gson.toJson(paramsMap);

        StringEntity entity = new StringEntity(jsonParams, "utf-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");

        //???????????????????????????
        CloseableHttpResponse response = httpClient.execute(httpPost);

        try {
            String bodyAsString = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) { //????????????
            } else if (statusCode == 204) { //????????????????????????Body
            } else {
                //throw new IOException("request failed");
                //?????????????????????
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("statusCode", statusCode);
                jsonObject.put("bodyAsString", bodyAsString);
                return ResultBody.ok(jsonObject).toString();
            }

            String nonceStr = RandomUtil.randomString(32);// ???????????????
            String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);// ?????????

            //????????????
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
            //??????housing_information??????property_fee
            String str = String.valueOf(money);
            Double poolBanlance1 = Double.parseDouble(str);
            Double poolBanlance = poolBanlance1 / 100;
            housingInformationMapper.investShare(poolBanlance, hid);
            //????????????
            Bill bill = new Bill();
            QueryWrapper queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("id", hid);
            HousingInformationDto housingInformationDto = housingInformationMapper.selectOne(queryWrapper1);
            bill.setVillageName(housingInformationDto.getVillageName());
            bill.setBuildName(housingInformationDto.getBuildNumber());
            bill.setRoomNum(housingInformationDto.getHouseNo());
            String date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(Calendar.getInstance().getTime());
            bill.setDate(date);
            bill.setOpenid(openid);
            bill.setLocation("??????");
            bill.setPay(poolBanlance);
            bill.setDetail("?????????????????????");
            bill.setType("????????????");
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
        Map<String, String> map = new HashMap<>();  //????????????  Json??????


        try {
            //??????????????????
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
             * "TRANSACTION.SUCCESS","summary":"????????????",
             * "resource":{"original_type":"transaction","algorithm":"AEAD_AES_256_GCM","ciphertext":"gTK9I96p3gXvyN6c9tkLrv3ogD/adDzjFJxvLDWpD9cOybuefaxMxdh/6OxW64wdBBR8IWdCq+nqs,
             * "associated_data":"transaction",
             * "nonce":"VBNu9IF6GGnX"}}
             */
            String requestId = (String) bodyMap.get("id");

            //??????request?????????????????????(wxPaySDK0.4.8??????request????????????????????? github)
            NotificationRequest Nrequest = new NotificationRequest.Builder()
                    .withSerialNumber(wechatPaySerial)
                    .withNonce(nonce)
                    .withTimestamp(timestamp)
                    .withSignature(signature)
                    .withBody(body)
                    .build();

            NotificationHandler handler = new NotificationHandler(wxPayConfig.getVerifier(), wxPayConfig.getApiV3Key().getBytes(StandardCharsets.UTF_8));
            //????????????????????????(?????????????????????)
            Notification notification = handler.parse(Nrequest);

            //???notification??????????????????(????????????)
            String plainText = notification.getDecryptData();
            processNotify(plainText);
            //???????????????map ????????????
            HashMap resultMap = gson.fromJson(plainText, HashMap.class);


            //TODO ????????????
            //////////////////////////////////////////////////
            //processOrder(plainText);
            /**
             * ???????????? ===> {"mchid":"1558950191","appid":"wx74862e0dfcf69954",
             "out_trade_no":"ORDER_20221004132527865","transaction_id":"4200001569202210040857712725",
             "trade_type":"NATIVE","trade_state":"SUCCESS","trade_state_desc":"????????????",
             "bank_type":"OTHERS","attach":"","success_time":"2022-10-04T13:25:40+08:00",
             payer":{"openid":"oHwsHuCj4_t6OMpypikZIQ1r-FXY"},
             "amount":{"total":1,"payer_total":1,"currency":"CNY","payer_currency":"CNY"}}
             */

            //????????????
            response.setStatus(200);
            map.put("code", "SUCCESS");
            map.put("message", "??????");
            return gson.toJson(map);
        } catch (Exception e) {
            e.printStackTrace();

            //????????????
            response.setStatus(500);
            map.put("code", "ERROR");
            map.put("message", "????????????");
            return gson.toJson(map);
        }

    }

    /**
     * ??????????????????
     *
     * @param orderNo
     */
    @Override
    public String queryOrder(Long orderNo, String openid) throws IOException {

        log.info("??????????????????====>{}", orderNo);

        String url = String.format(WxApiType.ORDER_QUERY_BY_NO.getType(), Long.toString(orderNo));
        url = wxPayConfig.getDomain().concat(url).concat("?mchid=").concat(wxPayConfig.getMchId());


        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Accept", "application/json");

        //???????????????????????????
        CloseableHttpResponse response = httpClient.execute(httpGet);

        try {
            String bodyAsString = EntityUtils.toString(response.getEntity());//?????????
            int statusCode = response.getStatusLine().getStatusCode();//???????????????
            if (statusCode == 200) { //????????????
                log.info("??????, ???????????? = " + bodyAsString);
                processNotify(bodyAsString);
            } else if (statusCode == 204) { //????????????????????????Body
                log.info("??????");
            } else {
                log.info("JSAPI????????????,????????? = " + statusCode + ",???????????? = " +
                        bodyAsString);
                throw new IOException("request failed");
            }
            return bodyAsString;
        } finally {
            response.close();
        }
    }

    @Override
    public String queryOrder1(Long orderNo) throws IOException {

        String url = String.format(WxApiType.ORDER_QUERY_BY_NO.getType(), Long.toString(orderNo));
        url = wxPayConfig.getDomain().concat(url).concat("?mchid=").concat(wxPayConfig.getMchId());


        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Accept", "application/json");

        //???????????????????????????
        CloseableHttpResponse response = httpClient.execute(httpGet);

        try {
            String bodyAsString = EntityUtils.toString(response.getEntity());//?????????
            int statusCode = response.getStatusLine().getStatusCode();//???????????????
            if (statusCode == 200) { //????????????
                log.info("??????, ???????????? = " + bodyAsString);
                processNotify1(bodyAsString);
            } else if (statusCode == 204) { //????????????????????????Body
                log.info("??????");
            } else {
                log.info("JSAPI????????????,????????? = " + statusCode + ",???????????? = " +
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
                "              \"out_trade_no\":\"1677076750946\",\"transaction_id\":\"4200001569202210040857712725\",\n" +
                "              \"trade_type\":\"NATIVE\",\"trade_state\":\"SUCCESS\",\"trade_state_desc\":\"????????????\",\n" +
                "              \"bank_type\":\"OTHERS\",\"attach\":\"\",\"success_time\":\"2022-10-04T13:25:40+08:00\",\n" +
                "              payer\":{\"openid\":\"oXXFD6gTkRajlHDiSIxE1PpMMvek\"},\n" +
                "              \"amount\":{\"total\":1,\"payer_total\":1,\"currency\":\"CNY\",\"payer_currency\":\"CNY\"}}";
        processNotify(str);
        return "666";
    }

    public void processOrder(String plainText, String openid) {
        log.info("????????????");
        Gson gson = new Gson();

        //??????map??????
        Map<String, Object> plainTextMap = gson.fromJson(plainText, HashMap.class);
        String out_trade_no = (String) plainTextMap.get("out_trade_no");
        String orderIds = stringRedisTemplate.opsForValue().get(out_trade_no);
        orderIds = orderIds.replaceAll("\\[", "").replaceAll("\\]", "");
        List<Long> ids = new ArrayList<>();
        for (String s : orderIds.split(",")) {
            QueryWrapper queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("order_no", Long.parseLong(s.trim()));
            PropertyOrderDto propertyOrderDto = propertyOrderMapper.selectOne(queryWrapper);
            //??????property_order?????????payment_status
            propertyOrderMapper.updateStatus(Long.parseLong(s.trim()));
            //??????housing??????due_date
            housingInformationMapper.updateDate(propertyOrderDto.getHouseId());
            //????????????
            Bill bill = new Bill();
            bill.setPay(propertyOrderDto.getCost());
            bill.setDetail(propertyOrderDto.getCostDetail());
            bill.setOpenid(openid);
            QueryWrapper queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("id", propertyOrderDto.getHouseId());
            HousingInformationDto housingInformationDto = housingInformationMapper.selectOne(queryWrapper1);
            bill.setVillageName(housingInformationDto.getVillageName());
            bill.setBuildName(housingInformationDto.getBuildNumber());
            bill.setRoomNum(housingInformationDto.getHouseNo());
            bill.setType("?????????");
            bill.setLocation("??????");
            String date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(Calendar.getInstance().getTime());
            bill.setDate(date);
            billMapper.insert(bill);
        }

    }

    /**
     * ????????????????????????????????????
     */
    @SneakyThrows
    public void processNotify(String plainText) {
        log.info("????????????");
        Gson gson = new Gson();
        //??????map??????
        Map<String, Object> plainTextMap = gson.fromJson(plainText, HashMap.class);
        String out_trade_no = (String) plainTextMap.get("out_trade_no");
        String res = stringRedisTemplate.opsForValue().get(out_trade_no);
        JSONObject jsonObject = JSONObject.parseObject(res);
        String propertyOrders = jsonObject.getString("property");
        String sharedOrders = jsonObject.getString("shared");
        Double propertyFee = jsonObject.getDouble("propertyFee");
        Double sharedFee = jsonObject.getDouble("sharedFee");
        log.info("???redis?????????????????????" + res);
        log.info("?????????:" + propertyOrders);
        log.info("?????????" + sharedOrders);
        log.info("?????????????????????:" + propertyFee);
        log.info("?????????????????????:" + sharedFee);
        //???????????????
        if (propertyOrders != null) {
            HousingInformationDto housingInformationDto1 = new HousingInformationDto();
            propertyOrders = propertyOrders.replaceAll("\\[", "").replaceAll("\\]", "");
            List<Long> ids = new ArrayList<>();
            String[] strings = propertyOrders.split(",");
            String openid = strings[strings.length - 1].trim();
            //log.info("openid???"  + openid);
            List<String> list = new ArrayList<>(Arrays.asList(strings));
            //log.info("???????????????" + list);
            list.remove(strings.length - 1);
            for (String s : list) {
                QueryWrapper queryWrapper = new QueryWrapper<>();
                s = s.replaceAll("\"", "");
                queryWrapper.eq("order_no", Long.parseLong(s.trim()));
                PropertyOrderDto propertyOrderDto = propertyOrderMapper.selectOne(queryWrapper);
                //??????property_order?????????payment_status
                propertyOrderMapper.updateStatus(Long.parseLong(s.trim()));
                //??????housing??????due_date
                HousingInformationDto housingInformationDto = housingInformationMapper.selectById(propertyOrderDto.getHouseId());
                housingInformationDto1 = housingInformationDto;
                String due_date = housingInformationDto.getDueDate();
                if (due_date == null) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.MONTH, 1);
                    String date1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(calendar.getTime());
                    housingInformationMapper.updateDate(propertyOrderDto.getHouseId());
                } else {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    Date date = sdf.parse(due_date);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    calendar.add(Calendar.MONTH, 1);
                    String date1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(calendar.getTime());
                    housingInformationMapper.updateDate(propertyOrderDto.getHouseId());
                }
                housingInformationMapper.updateDate(propertyOrderDto.getHouseId());
                //????????????
                Bill bill = new Bill();
                bill.setPay(propertyOrderDto.getCost());
                bill.setDetail(propertyOrderDto.getCostDetail());
                bill.setOpenid(openid);
                QueryWrapper queryWrapper1 = new QueryWrapper<>();
                queryWrapper1.eq("id", propertyOrderDto.getHouseId());
                bill.setVillageName(housingInformationDto.getVillageName());
                bill.setBuildName(housingInformationDto.getBuildNumber());
                bill.setRoomNum(housingInformationDto.getHouseNo());
                bill.setType("?????????");
                bill.setLocation("??????");
                bill.setBeginDate(propertyOrderDto.getBeginDate());
                bill.setEndDate(propertyOrderDto.getEndDate());
                bill.setOrderNo(propertyOrderDto.getOrderNo().toString());
                String date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(Calendar.getInstance().getTime());
                bill.setDate(date);
                billMapper.insert(bill);
            }
            //??????housing????????????
            if (propertyFee != null){
                housingInformationMapper.updatePropertyFee(housingInformationDto1.getId(),housingInformationDto1.getPropertyFee() - propertyFee);
            }
        }
        //???????????????
        if (sharedOrders != null) {
            HousingInformationDto housingInformationDto1 = new HousingInformationDto();
            sharedOrders = sharedOrders.replaceAll("\\[", "").replaceAll("\\]", "");
            List<Long> ids = new ArrayList<>();
            String[] strings = sharedOrders.split(",");
            String openid = strings[strings.length - 1].trim();
            //log.info("openid???"  + openid);
            List<String> list = new ArrayList<>(Arrays.asList(strings));
            //log.info("???????????????" + list);
            list.remove(strings.length - 1);
            for (String s : list) {
                s = s.replaceAll("\"", "");
                QueryWrapper queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("order_no", Long.parseLong(s.trim()));
                SharedFeeOrderDto sharedFeeOrderDto = sharedFeeOrderMapper.selectOne(queryWrapper);
                //??????shared_fee_order??????payment_status
                String date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(Calendar.getInstance().getTime());
                sharedFeeOrderMapper.updateDate(Long.parseLong(s.trim()), date);
                //????????????
                Bill bill = new Bill();
                bill.setPay(sharedFeeOrderDto.getCost());
                bill.setDetail(sharedFeeOrderDto.getCostDetail());
                bill.setOpenid(openid);
                QueryWrapper queryWrapper1 = new QueryWrapper<>();
                queryWrapper1.eq("id", sharedFeeOrderDto.getHouseId());
                HousingInformationDto housingInformationDto = housingInformationMapper.selectOne(queryWrapper1);
                housingInformationDto1 = housingInformationDto;
                bill.setVillageName(housingInformationDto.getVillageName());
                bill.setBuildName(housingInformationDto.getBuildNumber());
                bill.setRoomNum(housingInformationDto.getHouseNo());
                bill.setType("?????????");
                bill.setLocation("??????");
                bill.setDate(date);
                bill.setBeginDate(java.sql.Date.valueOf(sharedFeeOrderDto.getBeginDate()));
                bill.setEndDate(java.sql.Date.valueOf(sharedFeeOrderDto.getEndDate()));
                bill.setOrderNo(String.valueOf(sharedFeeOrderDto.getOrderNo()));
                billMapper.insert(bill);
            }
            //???????????????????????????
            if (sharedFee != null){
                housingInformationMapper.updateSharedFee(housingInformationDto1.getId(),housingInformationDto1.getPoolBalance() - sharedFee);
            }
        }
    }

    @SneakyThrows
    public void processNotify1(String plainText) {
        log.info("????????????");
        Gson gson = new Gson();
        //??????map??????
        Map<String, Object> plainTextMap = gson.fromJson(plainText, HashMap.class);
        String out_trade_no = (String) plainTextMap.get("out_trade_no");
        String res = stringRedisTemplate.opsForValue().get(out_trade_no);
        JSONObject jsonObject = JSONObject.parseObject(res);
        int property = jsonObject.getInteger("propertyFee");
        int shared = jsonObject.getInteger("shared");
        String openid = jsonObject.getString("openid");
        int hid = jsonObject.getInteger("hid");
        log.info("???redis?????????????????????" + res);
        log.info("?????????:" + property);
        log.info("?????????" + shared);
        //??????housing_information??????property_fee
        String str = String.valueOf(property);
        Double property_fee1 = Double.parseDouble(str);
        Double property_fee = property_fee1 / 100;
        housingInformationMapper.investProperty(property_fee, hid);
        //??????housing_information??????property_fee
        String str1 = String.valueOf(shared);
        Double poolBanlance1 = Double.parseDouble(str1);
        Double poolBanlance = poolBanlance1 / 100;
        housingInformationMapper.investShare(poolBanlance, hid);
        //????????????
        Bill bill = new Bill();
        QueryWrapper queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.eq("id", hid);
        HousingInformationDto housingInformationDto = housingInformationMapper.selectOne(queryWrapper1);
        bill.setVillageName(housingInformationDto.getVillageName());
        bill.setBuildName(housingInformationDto.getBuildNumber());
        bill.setRoomNum(housingInformationDto.getHouseNo());
        String date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(Calendar.getInstance().getTime());
        bill.setDate(date);
        bill.setOpenid(openid);
        bill.setLocation("??????");
        bill.setPay(property_fee + poolBanlance);
        String detail;
        detail = "???????????????:" + property_fee + "(???),"+"???????????????:" + poolBanlance + "(???)";
        bill.setDetail(detail);
        bill.setType("????????????");
        billMapper.insert(bill);
    }
    public static String replace(String s){
        if(null != s && s.indexOf(".") > 0){
            s = s.replaceAll("0+?$", "");//???????????????0
            s = s.replaceAll("[.]$", "");//??????????????????.?????????
        }
        return s;
    }

    public ResultBody sendMsg(int hid,String name,String cost,String openid) throws Exception{
        WxMsgConfig requestData = this.getMsgConfig(hid,name,cost,openid);

        log.info("???????????????????????????{}", JSON.toJSONString(requestData));

        String url = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + wxSendMsgUtil.getAccessToken();
        log.info("???????????????????????????{}", url);
        JSONObject responseData = postData(url, requestData);
        log.info("???????????????????????????{}", JSON.toJSONString(responseData));

        Integer errorCode = responseData.getInteger("errcode");
        String errorMessage = responseData.getString("errmsg");
        if (errorCode == 0) {
            log.info("????????????????????????");
            return    ResultBody.ok(responseData);
        } else {
            log.info("????????????????????????,errcode???{},errorMessage???{}", errorCode, errorMessage);
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
                "????????????");
        /*?????????*/
        wxMsgTemplateHasten.setKeyword1(villageName + "-"
                + buildName + "-"
                + roomNum);
        /*?????????*/
        wxMsgTemplateHasten.setKeyword2(name);
        /*????????????*/
        wxMsgTemplateHasten.setKeyword3("????????????");
        /*????????????*/
        wxMsgTemplateHasten.setKeyword4("?????????");
        /*????????????*/

        //BigDecimal bigDecimal = new BigDecimal(100);
        wxMsgTemplateHasten.setKeyword5(cost);
        wxMsgTemplateHasten.setRemark("?????????????????????~");
        /*??????????????????????????????*/
        WxMsgConfig wxMsgConfig = new WxMsgConfig();
        wxMsgConfig.setTouser(openid);
        wxMsgConfig.setTemplate_id("QgLoZpp1KWNskam2jclpxXmmSu4nhVZkv8bPU9wEqS4");
        wxMsgConfig.setData(wxMsgTemplateHasten);
        return wxMsgConfig;
    }


    /**
     * ????????????
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
