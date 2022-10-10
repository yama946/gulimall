package com.yama.mall.order.listener;

import com.rabbitmq.client.Channel;
import com.yama.mall.order.canstant.RabbitConstant;
import com.yama.mall.order.entity.OrderEntity;
import com.yama.mall.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @description: 关闭订单的监听器
 * @date: 2022年10月10日 周一 14:42
 * @author: yama946
 */
@Slf4j
@Service
@RabbitListener(queues = RabbitConstant.ORDER_RELEASE_ORDER_QUEEU)
public class OrderCloseListener {
    @Autowired
    private OrderService orderService;

    /**
     * 消费过期消息，进行关闭订单操作
     * @param order
     * @param msg
     * @param channel
     * @throws IOException
     */
    @RabbitHandler
    public void getReleaseMessage(OrderEntity order, Message msg, Channel channel) throws IOException {
        log.info("接收到过期订单，订单详情:{}",order.getOrderSn());
        //订单关闭操作------------------------
        try{
            orderService.closeOrder(order);
            //使用通道进行签收消息
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            e.printStackTrace();
            //关单失败，重新返回队列
            channel.basicReject(msg.getMessageProperties().getDeliveryTag(),true);
        }


    }
}
