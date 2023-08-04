package com.example.wpct;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.DecimalFormat;

import static com.example.wpct.service.impl.WechatServiceImpl.replace;

@SpringBootTest
class WpctApplicationTests {
    public static void main(String[] args) {
        double total = 0.19999999999999996;
        DecimalFormat df = new DecimalFormat("#.##");
        total = Double.parseDouble(df.format(total));
        String str = String.valueOf(total * 100);
        Integer cost = Integer.parseInt(replace(str));
        System.out.println(cost);

    }


}
