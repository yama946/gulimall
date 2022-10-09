package com.yama.mall.order.test;

import com.rabbitmq.client.Channel;
import com.yama.mall.order.entity.OrderEntity;
import com.yama.mall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 接收消息使用@RabbitListener注解
 * @description:
 * @date: 2022年10月04日 周二 10:04
 * @author: yama946
 */
@Slf4j
//@RabbitListener(queues = {"gulimall.queue"})
//@Component
public class RabbitmqReceiveMessageTest {
    /**
     * 消息类型：
     *    class org.springframework.amqp.core.Message
     * 接收方法参数可以直接使用：
     *    class org.springframework.amqp.core.Message类型
     * @param message
     *//*
    *//*@RabbitListener(queues = {"gulimall.queue"})
    public void testReciveMessage(Object message){
        log.info("接收到消息：{}",message);
        log.info("接收到消息类型：{}",message.getClass());
        //class org.springframework.amqp.core.Message
    }*//*

    *//**
     * @RabbitListener--->queues：表示要监听的队列
     *
     * TODO rabbitmq消息接收消息监听器的使用
     * 监听方法可以传递的参数：
     * 1.Message message：
     *                  获取到原生消息的详细信息。消息属性+消息体
     * 2.OrderReturnReasonEntity reasonEntity：
     *                  spring会自动将消息体转化为当前参数的类对象，封装传递给监听方法，可以直接获取使用
     *                  不再需要通过原生信息进行转换
     * 3.Channel channel:
     *                  当前传递数据的通道，队列与消费者之间
     *
     *TODO Queue可以被多个消费者监听。
     *
     * 1、Queue可以被多个消费者监听。只要收到消息，队列删除消息，而且只能有一个收到消息
     *
     * 场景1：
     *      1).订单服务启动多个；同一个消息只能被一个消费者客户端接收。
     * 场景2：
     *      消费者处理消息的过程中，还会接收第2个消息吗？
     *      2).不会，只有一个消息处理完，方法运行结束才可以接收下一个消息。
     *
     * @param message
     *//*
//    @RabbitListener(queues = {"gulimall.queue"})
//    public void testReciveMessage(Message message, OrderReturnReasonEntity content, Channel channel) throws InterruptedException {
//        System.out.println("接收到消息体:"+content);
//        //获取消息体
//        byte[] body = message.getBody();
//        //获取消息属性参数
////        MessageProperties messageProperties = message.getMessageProperties();
////        log.info("接收到消息参数：{}",messageProperties);
//        //模拟消息处理过程
//        Thread.sleep(3000);
//        System.out.println("消息处理结束："+content.getName());
//    }

    *//**
     * 对队列中消息对象1
     * @param message
     * @param content
     * @param channel
     * @throws InterruptedException
     *//*
    @RabbitHandler
    public void testReciveMessage(Message message, OrderReturnReasonEntity content, Channel channel) throws InterruptedException {
        System.out.println("接收到消息体:"+content);
        System.out.println("消息处理结束："+content.getName());
        *//**
         * 手动完成消息ack确认
         *//*
        //deliveryTag在通道内是自增的参数
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        System.out.println("deliveryTag==>"+deliveryTag);
        *//**
         * 前收货物，ack消息
         * 参数：
         *      1.message.getMessageProperties().getDeliveryTag()获取到的deliveryTag
         *      2.是否批量确认消息，false表示只确认自己当前处理消息
         *//*
        try {
            if (deliveryTag%2==0){
                //签收货物
                channel.basicAck(deliveryTag,false);
            }else {

            }
        } catch (IOException e) {
            //网络中断，签收失败
            e.printStackTrace();
        }
    }


    @RabbitHandler
    public void testReciveMessage( OrderEntity content) throws InterruptedException {
        System.out.println("接收到消息体:"+content);
        System.out.println("消息处理结束："+content.getId());
    }*/
}
