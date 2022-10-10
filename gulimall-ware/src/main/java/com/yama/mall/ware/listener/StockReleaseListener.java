package com.yama.mall.ware.listener;

import com.alibaba.fastjson.TypeReference;
import com.rabbitmq.client.Channel;
import com.yama.mall.common.to.OrderTo;
import com.yama.mall.common.to.mq.StockDetailTo;
import com.yama.mall.common.to.mq.StockLockTo;
import com.yama.mall.common.utils.R;
import com.yama.mall.ware.config.MyRabbitMQConfig;
import com.yama.mall.ware.entity.WareOrderTaskDetailEntity;
import com.yama.mall.ware.entity.WareOrderTaskEntity;
import com.yama.mall.ware.service.WareSkuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * @description: 库存解锁的监听器
 * @date: 2022年10月10日 周一 14:41
 * @author: yama946
 */
@Slf4j
@RabbitListener(queues = MyRabbitMQConfig.STOCK_RELEASE_QUEUE)
public class StockReleaseListener {

    @Autowired
    private WareSkuService wareSkuService;


    /**
     *场景1：库存自动解锁
     *      下订单成功，库存锁定成功，接下来的业务调用失败，导致订单回滚，要解锁
     *场景2：订单失败
     *      锁库存失败
     *
     *
     *TODO 解锁失败，rabbitmq自动消费消费消息ack了
     * 开启手动ack
     * @param stockLockTo
     * @param msg
     */
    @RabbitHandler
    public void handlerStockLockedRelease(StockLockTo stockLockTo, Message msg, Channel channel) throws IOException {
        log.info("接收到解锁库存的消息");
        try{
            wareSkuService.unLockStock(stockLockTo);
            //TODO 手动ack
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            //订单状态获取失败，拒绝消息重新放到队列中重新消费处理
            //TODO 拒绝ack
            channel.basicReject(msg.getMessageProperties().getDeliveryTag(),true);
        }
    }

    /**
     * 订单取消后，主动发送的解锁消息
     * @param orderTo
     * @param msg
     * @param channel
     */
    @RabbitHandler
    public void handlerOrderCloseRelease(OrderTo orderTo,Message msg,Channel channel) throws IOException {
        log.info("订单关闭，准备解锁库存");
        try{
            wareSkuService.unLockStock(orderTo);
            //TODO 手动ack
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            //订单状态获取失败，拒绝消息重新放到队列中重新消费处理
            //TODO 拒绝ack
            channel.basicReject(msg.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
