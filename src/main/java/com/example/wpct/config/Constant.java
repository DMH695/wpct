package com.example.wpct.config;

public class Constant {
    /**
     * 公众号AppId
     */
    public static final String APP_ID = "wxaf4fff5b42d65d60";

    /**
     * 公众号AppSecret
     */
    public static final String APP_SECRET = "4a5b6b0f9d9e7042228a3c6a70d743ed";

    /**
     * 微信支付商户号
     */
    public static final String MCH_ID = "1615491577";

    /**
     * 微信支付API秘钥
     */
    public static final String KEY = "fujianshenglongyanwupingchengtou";

    /**
     * 微信交易类型:公众号支付
     */
    public static final String TRADE_TYPE_JSAPI = "JSAPI";

    /**
     * WEB
     */
    public static final String WEB = "WEB";

    /**
     * 返回成功字符串
     */
    public static final String RETURN_SUCCESS = "SUCCESS";

    /**
     * 支付地址(包涵回调地址)
     */
    public static final String PAY_URL = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx2bef02f0ed84edfc&redirect_uri=http%3a%2f%2fwxpay.pes-soft.com%2fwxpay%2fm%2fweChat%2funifiedOrder&response_type=code&scope=snsapi_base#wechat_redirect";

    /**
     * 微信统一下单url
     */
    public static final String UNIFIED_ORDER_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";

    /**
     * 微信申请退款url
     */
    public static final String REFUND_URL = "https://api.mch.weixin.qq.com/v3/refund/domestic/refunds";

    /**
     * 微信支付通知url
     */
    public static final String NOTIFY_URL = "http://wxpay.pes-soft.com/wxpay/";

    /**
     * 证书位置
     */
    public static final String CERT_PATH = "http://fjwpct.com/apiclient_cert.p12";



    public static final String privateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCrEg/5w8kyx7VF\n" +
            "vNeXPUPtJqwsZftF88CSTYfJ3Z6BqlQbxa2V93bxU+K8YOTfmp9Nd+EFeafEU1aU\n" +
            "T7WNjf4JXs66DudTc4lU06gc2MMSgK9nqOSi7o1kebySnhiUh43UpWM03CXGxuXn\n" +
            "kP3nCr1VhTeQcCxC1LxZUFZudlD0BKQnbRB+bnvayls5wvuNy0eXF2n1MMHIt9dW\n" +
            "6Gh5l7qU/+p261kuozsThQL5wKbinHeFhHC/a55c6ecmt8I5LUSmVZkGXHoysxKJ\n" +
            "m/ogYnx45JlNI53qf4Nk9BnNYcipxe+9f180BVYpLPc0F5kFRhajSQ5I7ZFi4NJk\n" +
            "x78ReAydAgMBAAECggEAB8FS3hTFGzs4eYfdEish2C9noL+iy4IIWdmP7VPRqrkp\n" +
            "BPVKMEE+sCQIZFfUKFrfG+jyV4gzVdAdXa3bYiZsvMoCfybLz9muAR1zQpOFDGKv\n" +
            "EPj0HHrKCCMIrHDkVl9qy5gPzkaoo8FCjOPizsWK3cSjRzX+m6z/OOFOecV9WAKG\n" +
            "4NGKz7nG4DNQwPQVmz1ArZKYZxuGRLRzPklr2SZ9NQppI5TSCWsjp1++8PS80aTD\n" +
            "00+wrPY3WYZTKQ9fcoMr+ulI0Oo9cywhT0HwNvhoVGculL3BiFA9fAR/rOaNLR80\n" +
            "doU1hZIzy+9kKIa14Kd/ax1nT88mMN2AB6Ah5m+mYQKBgQDT5md0opExDqRPprIp\n" +
            "d+RK0yuUGAiYVFptw5YwhpbgRe2p2srqGRMTLj8TPBw3/sqNldFGxdpIpI+bD9mB\n" +
            "/moOBwMVcVdaLC6wFDBfeP9dFhO5ebXpj+o7h6vOEjI1hPXSOQ+4t3gFpp3+fYer\n" +
            "/9/mU1oh2yb8NVUsVljbs9LQVQKBgQDOrFlO46elGgbFU2cca04isYtUz/rOW2ai\n" +
            "/CDVaXPwqpzg4TxMbL+KgwJa3wQu6/SZMSRNKSQMllhXsEjLe+/kJ3E2cjQ6AxtO\n" +
            "b0f6DDv6LbGC2+4RlGuX+hA6YJsEewhU3BhySej2toAklDFz7RFlFFJSmorelyA1\n" +
            "Vw6uaSTzKQKBgQCZR87IeRxL/0pub6frBVX3pl+cpfH5tvMe+V94RLN5EjyEJMep\n" +
            "pW+97TMlR9Ml0kJVPu1+o3puF9PlmAZ/2TiAYyQg67cDfHhdWI1RUqZugJzOCzPC\n" +
            "+x6150MpMtJtEnv7kNlFhjnk5IzfXZC5o8MjymGlofDGehHdm6x1aNPzbQKBgBnX\n" +
            "3VcLCpSPm5cjJbqdBGk8MEMLur4BGeV5h5WIGTJX80P/hi28T3s/hJTeXESNxsk9\n" +
            "Jt/rLBHS3vsgFQo01jt+Xx7CyxsR9mEpcyOmUf2npxHI9I0INSgM4eia2eeGhvUt\n" +
            "jHUGHvZyvbVVPpXTaCBumRixMHpDBYkF+NUAWtl5AoGAJ182+K/Rx4Snys1LtW2C\n" +
            "V+2+U0c+uuWd4thFSdXTQ+rbXGmuRHjsvFnP0CE+OLgYoGfDiHJhpZAIsEnUSGu+\n" +
            "5KbkeSMs4TEJ+lOqdrKAsiZxeRwP/RgqKa1musfThD1bAlk+6xlXkaRn7BvSBuq/\n" +
            "XTZfcX0aiiRXMhhF6n+kFO4=";
    public static final String mchSerialNo = "31D7B29414E2C57650D4738CF4548BE3EC5F9566";
    /**
     * 通过code获取授权access_token的URL
     */
    public static String Authtoken_URL(String code) {
        StringBuffer url = new StringBuffer();
        url.append("https://api.weixin.qq.com/sns/oauth2/access_token?appid=");
        url.append(Constant.APP_ID);
        url.append("&secret=");
        url.append(Constant.APP_SECRET);
        url.append("&code=");
        url.append(code);
        url.append("&grant_type=authorization_code");
        return url.toString();
    }
}