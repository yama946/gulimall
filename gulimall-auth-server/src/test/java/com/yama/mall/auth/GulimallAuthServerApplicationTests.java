package com.yama.mall.auth;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallAuthServerApplicationTests {
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Test
    public void Chars() {
        char name='\u4e2d';
    }

    @Test
    public void redisOption(){
        stringRedisTemplate.opsForValue().set("name","yama",3, TimeUnit.MINUTES);
    }

}
