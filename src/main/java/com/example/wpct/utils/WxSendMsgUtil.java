package com.example.wpct.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.wpct.config.WxPayConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static com.example.wpct.entity.RedisConstants.ACCESS_TOKEN;


/**
 * @ClassName WxSendMsgUtil
 * @Description TODO 未完成
 * @Author ZXX
 * @DATE 2022/10/12 16:46
 */

@Slf4j
@Component
@CrossOrigin
public class WxSendMsgUtil {

    @Resource
    private WxPayConfig wxPayConfig;

    @Resource
    private RestTemplate restTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private final ReentrantLock lock = new ReentrantLock();


    public String getAccessToken() throws InterruptedException {
        /*先从缓存中取openid，缓存中取不到 说明已经过期，则重新申请*/
        String accessToken = stringRedisTemplate.opsForValue().get(ACCESS_TOKEN);
        Long expire = stringRedisTemplate.getExpire(ACCESS_TOKEN, TimeUnit.MINUTES);

        if (accessToken != null && expire != null && expire > 5L) {
            return accessToken;
        }

        if (lock.tryLock()) {
            Map<String, String> params = new HashMap<>();
            params.put("APPID", wxPayConfig.getAppid());
            params.put("APPSECRET", wxPayConfig.getAppSecret());
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(
                    "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={APPID}&secret={APPSECRET}", String.class, params);
            String body = responseEntity.getBody();
            JSONObject object = JSON.parseObject(body);
            System.out.println("***************" + object);
            assert object != null;
            String Access_Token = object.getString("access_token");
            /*access_token有效时长*/
            int expires_in = object.getInteger("expires_in");
            /*过期时间减去10毫秒：10毫秒是网络连接的程序运行所占用的时间*/
            stringRedisTemplate.opsForValue().set(ACCESS_TOKEN, Access_Token, expires_in - 10, TimeUnit.SECONDS);
            return Access_Token;
        }
        Thread.sleep(1000);
        return getAccessToken();
    }


}
