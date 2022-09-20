package com.yama.mall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Redisson的配置，使用启动器可以自动配置
 */
@Configuration
public class MyRedissonConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private String port;

    @Value("${spring.redis.password}")
    private String password;


    /**
     * 所有对Redisson的操作都是通过RedissonClient对象
     * @return
     * @throws IOException
     */
    @Bean(destroyMethod="shutdown")
    public RedissonClient redisson() throws IOException {
        /**
         * idleConnectionTimeout: 10000
         *   connectTimeout: 10000
         *   timeout: 3000
         */
        //1.创建出配置
        Config config = new Config();
        //单节点模式：可以用"rediss://"来表示启用SSL连接----redis://这个字符串要求添加，至少单节点要求
        config.useSingleServer().setAddress("redis://"+host+":"+port);
        config.useSingleServer().setPassword(password);
        config.useSingleServer().setIdleConnectionTimeout(10000);
        config.useSingleServer().setConnectTimeout(10000);
        config.useSingleServer().setTimeout(3000);

        //集群模式
        /*config.useClusterServers()
                .addNodeAddress("127.0.0.1:7004", "127.0.0.1:7001");*/
        //2.根据配置创建出RedissonClient实例
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }
}
