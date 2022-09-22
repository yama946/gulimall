package com.yama.mall.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.elasticsearch.rest.RestClientAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * exclude = {RestClientAutoConfiguration.class}排除spring data的es自动配置，使用我们自己配置的high level
 */
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication(exclude = {RestClientAutoConfiguration.class})
public class GulimallSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallSearchApplication.class, args);
    }

}
