package com.yama.mall.ware;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

//单独配置注入不成功，MapperScan只能扫描注入指定的mapper文件，无法扫描注入cong，componte等注解
@MapperScan(basePackages = {"com.yama.mall.ware.dao","com.yama.mall.common.config"})
@ComponentScan(basePackages = "com.yama.mall")
@RefreshScope
@EnableFeignClients
@EnableTransactionManagement//用来开启事务
@EnableDiscoveryClient
@SpringBootApplication
public class GulimallWareApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallWareApplication.class, args);
    }

}
