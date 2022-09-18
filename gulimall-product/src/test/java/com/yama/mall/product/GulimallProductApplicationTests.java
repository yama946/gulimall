package com.yama.mall.product;

import com.yama.mall.product.dao.AttrAttrgroupRelationDao;
import com.yama.mall.product.entity.AttrAttrgroupRelationEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = GulimallProductApplication.class)
public class GulimallProductApplicationTests {

    @Autowired
    RedissonClient redissonClient;

    @Test
    public void testRedisson(){
        System.out.println(redissonClient);
    }

}
