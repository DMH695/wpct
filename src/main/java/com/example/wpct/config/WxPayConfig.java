package com.example.wpct.config;

import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.auth.PrivateKeySigner;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Credentials;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Validator;
import com.wechat.pay.contrib.apache.httpclient.cert.CertificatesManager;
import com.wechat.pay.contrib.apache.httpclient.exception.HttpCodeException;
import com.wechat.pay.contrib.apache.httpclient.exception.NotFoundException;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Base64;

/**
 * @Author ZXX
 * @ClassName WxPayConfig
 * @Description
 * @DATE 2022/10/6 13:41
 */

@Slf4j
@Configuration
@PropertySource("classpath:wxpay.properties") //读取配置文件
@ConfigurationProperties(prefix = "wxpay") //读取wxpay节点
@Data //使用set方法将wxpay节点中的值填充到当前类的属性中
public class WxPayConfig {

    // 商户号
    private String mchId;

    // 商户API证书序列号
    private String mchSerialNo;

    // 商户私钥文件
    private String privateKeyPath;

    // APIv3密钥
    private String apiV3Key;

    // APPID
    private String appid;

    //APP密钥
    private String appSecret;

    // 微信服务器地址
    private String domain;

    // 接收结果通知地址
    private String notifyDomain;


    /**
     * 获取商户私钥
     *
     * @param
     * @return 私钥信息
     */
    private PrivateKey getPrivateKey() throws UnsupportedEncodingException {

        try {
             PrivateKey merchantPrivateKey = PemUtil.loadPrivateKey(new ByteArrayInputStream( Constant.privateKey.getBytes("utf-8")));
             //log.info("merchantPrivateKey===load===>{}",merchantPrivateKey);
             return merchantPrivateKey;
             //return PemUtil.loadPrivateKey(new FileInputStream(filename));
        } catch (Exception e) {
            throw new RuntimeException("商户私钥文件不存在", e);
        }
    }

    /**
     * 获取签名验证器
     *
     * @return Verifier
     */
    @Bean
    public Verifier getVerifier() throws NotFoundException, GeneralSecurityException, IOException, HttpCodeException {

        log.info("获取签名验证器");

        //获取商户私钥
        PrivateKey privateKey = getPrivateKey();
        //平台证书的商户信息
        WechatPay2Credentials wechatPay2Credentials = new WechatPay2Credentials(mchId, new PrivateKeySigner(mchSerialNo, privateKey));

        // 获取证书管理器实例
        CertificatesManager certificatesManager = CertificatesManager.getInstance();
        // 向证书管理器增加需要自动更新平台证书的商户信息 // ... 若有多个商户号，可继续调用putMerchant添加商户信息
        certificatesManager.putMerchant(mchId, wechatPay2Credentials, apiV3Key.getBytes(StandardCharsets.UTF_8));

        // 从证书管理器中获取verifier
        Verifier verifier = certificatesManager.getVerifier(mchId);
        return verifier;
    }

    /**
     * 获取HttpClient对象
     *
     * @param verifier 签名验证器
     */
    @Primary
    @Bean(name = "WxPayClient")
    public CloseableHttpClient getWxPayClient(Verifier verifier) throws UnsupportedEncodingException {

        log.info("获取HttpClient");

        //获取商户私钥
        PrivateKey privateKey = getPrivateKey();

        //用于构建HttpClient
        WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
                .withMerchant(mchId, mchSerialNo, privateKey)
                .withValidator(new WechatPay2Validator(verifier));
        // ... 接下来，你仍然可以通过builder设置各种参数，来配置你的HttpClient

        // 通过WechatPayHttpClientBuilder构造的HttpClient，会自动的处理签名和验签，并进行证书自动更新
        CloseableHttpClient httpClient = builder.build();
        return httpClient;
    }


    @Bean(name = "wxPayNoSignClient")
    public CloseableHttpClient getWxPayNoSignClient(Verifier verifier) throws UnsupportedEncodingException {
        log.info("初始化wxPayNoSignClient");
        //获取商户私钥
        PrivateKey privateKey = getPrivateKey();

        WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
                .withMerchant(mchId, mchSerialNo, privateKey)
                //设置响应对象无需签名
                .withValidator((response) -> true);
        CloseableHttpClient wxPayNoSignClient = builder.build();
        log.info("wxPayNoSignClient初始化完成");
        return wxPayNoSignClient;
    }


    //    public  String getSign(String appid ,String timeStamp,String nonceStr ,String prepayId  , String  privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
//
//            String signatureStr = Stream.of(appid, timeStamp, nonceStr, prepayId)
//                    .collect(Collectors.joining("\n", "", "\n"));
//            Signature sign = Signature.getInstance("SHA256withRSA");
//            PrivateKey merchantPrivateKey = this.getPrivateKey(privateKey);
//            sign.initSign(merchantPrivateKey);
//            sign.update(signatureStr.getBytes(StandardCharsets.UTF_8));
//            return Base64.getEncoder().encodeToString(sign.sign());
//
//    }
    private String buildMessage(String appId, long timestamp, String nonceStr, String pack) {
        return appId + "\n"
                + timestamp + "\n"
                + nonceStr + "\n"
                + pack + "\n";
    }
    private String sign(byte[] message) throws Exception{
        PrivateKey merchantPrivateKey = PemUtil
                .loadPrivateKey(new ByteArrayInputStream( Constant.privateKey.getBytes("utf-8")));

        Signature sign = Signature.getInstance("SHA256withRSA");
        //这里需要一个PrivateKey类型的参数，就是商户的私钥。
        sign.initSign(merchantPrivateKey);
        sign.update(message);
        return Base64.getEncoder().encodeToString(sign.sign());
    }
    public String getSign(String appId, long timestamp, String nonceStr, String pack) throws Exception {
        String message = buildMessage(appId, timestamp, nonceStr, pack);
        String paySign = sign(message.getBytes("utf-8"));
        return paySign;
    }


}
