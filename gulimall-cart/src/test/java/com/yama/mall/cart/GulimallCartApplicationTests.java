package com.yama.mall.cart;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallCartApplicationTests {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    public void testOpsRedisHash(){
        BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps("user-key");
        redisTemplate.opsForHash().put("user-key",23,63);
    }

}
