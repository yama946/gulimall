package com.yama.mall.product.config;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 配置缓存，使用自定义注入RedisCacheConfiguration替换默认的配置
 * 1.jdk序列化保存值修改为Json
 *
 */
@EnableConfigurationProperties(CacheProperties.class)
@EnableCaching //开启缓存
@Configuration
public class MyCacheConfig {

    //第一种方式注入
    @Autowired
    CacheProperties cacheProperties;

    /**
     * 使用配置类配置，配置文件中配置没有用上
     * 1.原来和配置文件绑定的配置类如下：
     *      @ConfigurationProperties(prefix = "spring.cache")
     *      public class CacheProperties {
     * 2.让其生效使用注解，相当于注入CacheProperties到容器中
     *   @EnableConfigurationProperties(CacheProperties.class)
     *
     * @return
     */
    //第二种方式：直接作为参数，会到容器中直接查找
    @Bean
    RedisCacheConfiguration redisCacheConfiguration(CacheProperties cacheProperties){
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
        //修改缓存中key、value的序列化方式
        config = config.serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()));

        config = config.serializeValuesWith(SerializationPair.fromSerializer(new GenericFastJsonRedisSerializer()));
        //获取配置文件中的配置进行设置
        CacheProperties.Redis redisProperties = cacheProperties.getRedis();
        //将配置文件中所有的配置都生效
        if (redisProperties.getTimeToLive() != null) {
            config = config.entryTtl(redisProperties.getTimeToLive());
        }
        if (redisProperties.getKeyPrefix() != null) {
            config = config.prefixKeysWith(redisProperties.getKeyPrefix());
        }
        if (!redisProperties.isCacheNullValues()) {
            config = config.disableCachingNullValues();
        }
        if (!redisProperties.isUseKeyPrefix()) {
            config = config.disableKeyPrefix();
        }
        return config;
    }
}



/*
默认注入的RedisCacheConfiguration类
public class RedisCacheConfiguration {

	private final Duration ttl;
	private final boolean cacheNullValues;
	private final CacheKeyPrefix keyPrefix;
	private final boolean usePrefix;

	private final SerializationPair<String> keySerializationPair;
	private final SerializationPair<Object> valueSerializationPair;
 */