package com.yama.mall.order.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @EnableConfigurationProperties的作用是把springboot配置文件中的值与我们的xxxProperties.java的属性进行绑定，
 * 需要配合@ConfigurationProperties使用
 *
 * 首先我想说的是，不使用@EnableConfigurationProperties能否进行属性绑定呢？答案是肯定的！
 * 我们只需要给xxxProperties.java加上@Component注解，把它放到容器中，即可实现属性绑定
 *
 * 在属性绑定中@EnableConfigurationProperties和@Component的效果一样，那么为啥springboot还要使用这个注解呢？
 *
 * 答案是：当我们引用第三方jar包时，@Component标注的类是无法注入到spring容器中的，
 * 这时我们可以用@EnableConfigurationProperties来代替@Component
 */
@EnableConfigurationProperties(ThreadPoolConfigProperties.class)
@Configuration
public class MyThreadConfig {
    /**
     * 注入线程池实现对象
     * @param pool
     * @return
     */
    @Bean
    public ThreadPoolExecutor threadPoolExecutor(ThreadPoolConfigProperties pool) {
        return new ThreadPoolExecutor(
                pool.getCoreSize(),
                pool.getMaxSize(),
                pool.getKeepAliveTime(),
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(100000),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }

}