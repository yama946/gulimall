package com.yama.mall.order.web;

import com.yama.mall.order.entity.OrderEntity;
import com.yama.mall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.UUID;

/**
 * @description: 测试rabbitmq消息发送
 * @date: 2022年10月04日 周二 12:28
 * @author: yama946
 */
@Slf4j
@RestController
public class RabbitControllerTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    String exchange = "gulimall.exchange.direct";

    String queueStr = "gulimall.queue";

    @GetMapping("test/sendMQ")
    public String testSendMessage(){
        //1.发送字符串消息
        String msg = "hello rabbitmq";
        for (int i = 0; i < 10; i++) {
            if (i%2==0){
                OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
                reasonEntity.setId(1L);
                reasonEntity.setCreateTime(new Date());
                reasonEntity.setName("jack---"+i);
                //3.发送对象类型的消息，我们也可以使用json---->替换rabbitTemplate中的messageConverter对象
                //发送消息指定消息id
                rabbitTemplate.convertAndSend(exchange,"rabbitmq",reasonEntity,
                        new CorrelationData(UUID.randomUUID().toString()));
                log.info("reasonEntity消息发送成功:{}",i);
            }else {
                OrderEntity orderEntity = new OrderEntity();
                orderEntity.setId(Long.valueOf(String.valueOf(i)));
                log.info("orderEntity消息发送成功:{}",i);
            }
        }
        return "OK";
    }


}
