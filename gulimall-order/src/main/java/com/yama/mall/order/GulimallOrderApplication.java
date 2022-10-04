package com.yama.mall.order;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * TODO springboot整合rabbitmq的步骤：
 * 1.导入启动器----->自动配置类RabbitAutoConfiguration
 *         <dependency>
 *             <groupId>org.springframework.boot</groupId>
 *             <artifactId>spring-boot-starter-amqp</artifactId>
 *         </dependency>
 * 自动配置类导入组件：CachingConnectionFactory、RabbitTemplate、AmqpAdmin、RabbitMessagingTemplate
 * 2.添加注解:@EnableRabbit,开启rabbit消息中间间功能
 *
 * 3.在配置文件中配置rabbitmq的相关信息----->@ConfigurationProperties(prefix = "spring.rabbitmq")
 *
 * 4.创建组件使用AmqpAdmin,发送消息使用RabbitTemplate
 *
 * 5.接收消息：@RabbitListener：使用时必须开启@EnableRabbit注解
 *
 * rabbitmq监听方法注解：
 *          @RabbitListene：作用在，类+方法上---------->指定监听那些队列的消息
 *          @RabbitHandler；作用在方法上，用于接收同一队列中，不同的消息对象----------->重载区分不同的消息
 *
 * @description:
 * @author: yama946
 */
@EnableRabbit
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class GulimallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class, args);
    }

}
