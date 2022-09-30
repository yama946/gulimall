package com.yama.mall.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 1、spring Session整合:要求存入redis中的对象实现序列化接口
 * 1).添加依赖
 *         <dependency>
 *             <groupId>org.springframework.session</groupId>
 *             <artifactId>spring-session-data-redis</artifactId>
 *         </dependency>
 *         <!--redis作为保存验证码,以及springsesison存储session-->
 *         <dependency>
 *             <groupId>org.springframework.boot</groupId>
 *             <artifactId>spring-boot-starter-data-redis</artifactId>
 *         </dependency>
 * 2).添加相关配置
 *      spring.session.store-type=redis
 *      spring.redis.host=127.0.0.1
 * 3).添加相关注解（非必要）:@EnableRedisHttpSession
 */

/**
 * spring session核心原理：
 * 1）、@EnableRedisHttpSession导入RedisHttpSessionConfiguration配置
 *      1、给容器中添加了一个组件
 *          SessionRepository==》》RedisOperationsSessionRepository：Redis操作session，session的增删改查封装类
 *      2、SpringHttpSessionConfiguration.SessionRepositoryFilter--->Filter :session存储器；每个请求过来都要经过filter
 *          1.创建的时候，就自动从容器中获取SessionRepository
 *          2.原始的request、response对象被包装成SessionRepositoryRequestWrapper、SessionRepositoryResponseWrapper
 *          3.以后获取session。request.getSession();
 *          //SessionRepositoryRequestWrapper
 *          4.WrappedRequest.getSession();==>> SessionRepository中获取
 * 总结：关键词
 *      装饰则模式、redis中session自动续期。
 */
@EnableRedisHttpSession
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class GulimallAuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallAuthServerApplication.class, args);
    }

}
