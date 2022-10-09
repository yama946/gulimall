package com.yama.mall.order;

import com.yama.mall.order.entity.OrderEntity;
import com.yama.mall.order.entity.OrderReturnReasonEntity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.Scanner;

/**
 * TODO 测试rabbitmq的使用
 * 目标：
 * 1.如何使用api创建：Exchange、Queue、Binding
 *      1).使用AmqpAdmin进行创建
 * 2.如何发送、接收消息
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallOrderApplicationTests {
    /*@Autowired
    AmqpAdmin amqpAdmin;

    @Autowired
    RabbitTemplate rabbitTemplate;

    String exchange = "gulimall.exchange.direct";

    String queueStr = "gulimall.queue";



    *//**
     * 测试发送消息-----使用RabbitTemplate组件
     *//*
    @Test
    public void testSendMessage(){
        //1.发送字符串消息
        String msg = "hello rabbitmq";
//        rabbitTemplate.convertAndSend(exchange,"rabbitmq",msg);
        //2.发送对象消息，如果发送的消息是个对象，会使用序列化机制序列化对象，将对象写出去，对象类需要实现序列化Serializable接口
        *//*for (int i = 0; i < 10; i++) {
            OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
            reasonEntity.setId(1L);
            reasonEntity.setCreateTime(new Date());
            reasonEntity.setName("jack---"+i);
            //3.发送对象类型的消息，我们也可以使用json---->替换rabbitTemplate中的messageConverter对象
            rabbitTemplate.convertAndSend(exchange,"rabbitmq",reasonEntity);
            log.info("消息发送成功:{}",i);
        }*//*
        for (int i = 0; i < 10; i++) {
            if (i%2==0){
                OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
                reasonEntity.setId(1L);
                reasonEntity.setCreateTime(new Date());
                reasonEntity.setName("jack---"+i);
                //3.发送对象类型的消息，我们也可以使用json---->替换rabbitTemplate中的messageConverter对象
                rabbitTemplate.convertAndSend(exchange,"rabbitmq",reasonEntity);
                log.info("reasonEntity消息发送成功:{}",i);
            }else {
                OrderEntity orderEntity = new OrderEntity();
                orderEntity.setId(Long.valueOf(String.valueOf(i)));
                log.info("orderEntity消息发送成功:{}",i);
            }

        }
    }

    *//**
     * AmqpAdmin创建交换机
     *//*
    @Test
    public void createExchange(){
        //创建直连交换机---gulimall
        //DirectExchange(String name, boolean durable, boolean autoDelete)
        DirectExchange directExchange = new DirectExchange(exchange);
        amqpAdmin.declareExchange(directExchange);
        log.info("Exchange[{}]创建成功","gulimall.exchange.direct");
    }
    *//**
     * AmqpAdmin创建消息队列
     *//*
    @Test
    public void createQueue(){
        //Queue类所属于amqp核心包
        //Queue(String name, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments)
        Queue queue = new Queue(queueStr);
        amqpAdmin.declareQueue(queue);
        log.info("Queue[{}]创建成功","gulimall.queue");
    }

    *//**
     * AmqpAdmin创建队列与交换机的绑定关系
     *//*
    @Test
    public void createBinding(){
        //Binding类所属于amqp核心包
        //Binding(String destination, DestinationType destinationType, String exchange, String routingKey,
        //			Map<String, Object> arguments)
        Binding binding = new Binding(
                "gulimall.queue",
                Binding.DestinationType.QUEUE,
                "gulimall.exchange.direct",
                "rabbitmq",null);
        amqpAdmin.declareBinding(binding);
        log.info("Binding[{}]创建成功",binding);
    }*/

}
