package com.yama.mall.cart.config;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

//TODO 当前使用的redis库，不是保存session使用的库，能成功取出session吗？
@EnableRedisHttpSession
@Configuration
public class GulimallSessionConfig {
    /**
     * 改变cookie中的session的作用域，使其能够作用在任何子域
     * 使用CookieSerializer接口进行自定义cookie
     * @return
     */
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        //修改session的作用域
        serializer.setDomainName("gulimall.com");
        //自定义session的名字
        serializer.setCookieName("GULISESSION");

        return serializer;
    }

    /**
     * 自定义springsession的redis序列化机制
     * @return
     */
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }
}
