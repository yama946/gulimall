package com.yama.mall.auth.congfig;

import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.serializer.DefaultSerializer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

//TODO 解决：1、spring session默认发的令牌。session=xxxxx。默认作用域为：当前域（需要解决子域session共享问题，扩大cookie中session的domain的范围）
//TODO 解决2、使用JSON的序列化方式来序列化对象，将数据保存到redis中。
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
