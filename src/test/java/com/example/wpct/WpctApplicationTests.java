package com.example.wpct;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;

@SpringBootTest
class WpctApplicationTests {



    @Test
    void contextLoads() {
        String bindCount = "";
        Integer count = Integer.parseUnsignedInt(bindCount) + 1;
        System.out.println(count);
    }

}
