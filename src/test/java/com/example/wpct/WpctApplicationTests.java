package com.example.wpct;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;

@SpringBootTest
class WpctApplicationTests {
    @Resource
    private final StringRedisTemplate stringRedisTemplate;


    WpctApplicationTests(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Test
    void contextLoads() {
        System.out.println(stringRedisTemplate.opsForValue().get("1673871410898"));
    }

}
