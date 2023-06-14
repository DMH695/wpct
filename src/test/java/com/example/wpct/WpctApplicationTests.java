package com.example.wpct;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
@SpringBootTest
class WpctApplicationTests {




    @Test
    void contextLoads() {
        String str = "\"Hello, world!\"";
        str = str.replace("\"", "");
        System.out.println(str);
    }

}
