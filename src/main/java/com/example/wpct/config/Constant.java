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
    public static final String MCH_ID = "1640340722";

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



    public static final String privateKey = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCoGNbAljPM3tSq\n" +
            "7rlIPKajYEHmvfH061BuoMz1H37eo5y4O3BBpzGgPHhnthryTxPk5XfKtqzFs9tu\n" +
            "OPtK0RwdoD0H3Y0P2V8kKWVsQeCpc758YEOx0hk2HyxQhCEqVMESnlk797S/4Ikn\n" +
            "EC0w/9ZAvbi1adizFyYC3vALW1hEFYqv/M1RNGcqR+6OdxcFHlMvyAnHE7s5LLhR\n" +
            "CYLWT4kqALVHu/4p7PeGjOrcMjcgAInKErEptm004UQuFNAX1XF4XwDzi1/JNaXq\n" +
            "BHMFOaik/xpSLcfIko4o50dpATZ7XxPjzioc6kY56Gb0Yk4dO/DOQ973rvoiFPFl\n" +
            "INolhQQfAgMBAAECggEBAIA9ouh+LrqnbTuSsEvJ6Z9OejhcxNCDDXO4vWRsSM/1\n" +
            "pkRvJC8SLKwANiN5LImbhqPj5EYEoz6Y/ljSo/PqIDcB3k232jwzK4BdDIFQqOLd\n" +
            "6H7eugZZMN63XYl3pA21Zy6bG5zCV2TjGf9xOejd5Uiy9M2CGM5YZKlgB2XCpE4b\n" +
            "m+ygd36Canf2J4Tn+203oGFikOxzB9s0y2E5PbPtPHsFqq5ZtIHkc0OGHFxvZJEc\n" +
            "4WKY1OodsMPYRoBFnAWb6Ji59CnVustWt+dlrrhmWkfERPnavS7qQJMztr4CDD6b\n" +
            "dwa93U9qWd60fvqrKrhVkQV5OnbctkEdzDx+ZaQd6aECgYEA3S6jpI5u9SkcFYOL\n" +
            "+gWaYANfoD3YhVZeMx2mU02cvbmA/Q13PkBM/LXpIb1zv6nQ86l0AcLpAuE7UJ5e\n" +
            "3pCNPksupEY8ors0sbS6BVKqacTdt8bW4V03iqLRmF2qfFvuBqnzku/UUNcLkFKT\n" +
            "VtPcwEV2nPrC7gCpMDx276036kkCgYEAwo7vb8XnwYT472b7dIwm3dulqzZ9MBlS\n" +
            "xa87VeN5jxdldvnDIeUYAG1HwXJ9yHiQgmWWgW1yAUM1Ahb3KcM1klEhQ1qBER98\n" +
            "kd5xpg9vQEcgKZtjPAVNcAYRX3JvZ7cC6v3CMpaYLeMpsb16MFFougmK/g15iyyI\n" +
            "D/URviL1uycCgYBpa0VLtSdlagacqmoY7Hq7sF/vfAqq4pNbQZJ4udEvDC9SbQyc\n" +
            "ewE2oW1QS3/TphiVSfqkrEVqvfcLYbBN3A+11ReR7HgSB7AB0uWmi9P1PYN/iCH6\n" +
            "n/dM+HV1bY9IXniAbJhPPEexkA1O6EyZYbkhWvpBr7MgxOfeubLmYWT5IQKBgDB3\n" +
            "dwI7JN66h2DsIJwKBeyiuQSL0GfZBxuj6X4d6rxQscAPvCKHdZE1p/hy0w7LqgQx\n" +
            "1mprn0jdOylIG05WCNRoTYb/1sMzOs8DwQADPE2g6kwzH2dmUsIjYNSMJ2CI4Ls1\n" +
            "d9kuVr6npk1WzEeZICOoGO3s4WhgMYz4GfFpuBllAoGBALDn5PNUN3PFwBmObmXR\n" +
            "n+madMHYE80j2oVPBi6pGYqpdNUCJufBUkKU47MePJdrRenx2Rmr65QLT1fC1QB0\n" +
            "sfsAPy8KVhX4tqatYJjmQTivDL2G99t+VHdcOxxrFBVquVqu9s5CqY+mFjHklTYV\n" +
            "7e0ZtlQZX4JPr8Je4JD7AWRB";
    public static final String mchSerialNo = "6F9D79F6077366591B5121B57EE243E5C629E174";
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