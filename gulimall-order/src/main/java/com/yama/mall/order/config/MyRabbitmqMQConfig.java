package com.yama.mall.order.config;


import com.yama.mall.order.canstant.RabbitConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.HashMap;
import java.util.Map;

/**
 * 使用@Bean的方式创建交换机、队列、绑定关系
 * 容器中的Queue、Binding、Exchange都会自动创建，rabbitmq中没有的情况下
 * @description: rabbitmq延时队列的配置
 * @date: 2022年10月09日 周日 20:31
 * @author: yama946
 */
@Configuration
public class MyRabbitmqMQConfig {

    /**
     * 延时队列
     * @return
     */
    @Bean
    public Queue orderDelayQueue(){

        Map<String, Object> arguments = new HashMap<>();
        /**
         * x-dead-letter-exchange: order-event-exchange
         * x-dead-letter-routing-key: order.release.order
         * x-message-ttl: 60000
         */
        //死信消息的交换机
        arguments.put("x-dead-letter-exchange",RabbitConstant.ORDER_EVENT_EXCHANGE);
        //死信消息的routing-key
        arguments.put("x-dead-letter-routing-key","order.release.order");
        //队列中消息存活时间
        arguments.put("x-message-ttl",RabbitConstant.X_MESSAGE_TTL);

        //public Queue(String name, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments)
        Queue queue = new Queue(RabbitConstant.ORDER_DELAY_QUEUE, true, false, false, arguments);

        return queue;
    }

    /**
     * 死信队列
     * @return
     */
    @Bean
    public Queue orderReleaseOrderQueue(){
        //public Queue(String name, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments)
        Queue queue = new Queue(RabbitConstant.ORDER_RELEASE_ORDER_QUEEU, true, false, false, null);
        return queue;
    }

    /**
     * 模块命名的统一交换机，绑定多个队列
     * @return
     */
    @Bean
    public Exchange orderEventExchange(){
        TopicExchange topicExchange = new TopicExchange(RabbitConstant.ORDER_EVENT_EXCHANGE,true,false);
        return topicExchange;
    }

    /**
     * public Binding(String destination, DestinationType destinationType,
     * String exchange, String routingKey,Map<String, Object> arguments)
     * 绑定延时队列
     * @return
     */
    @Bean
    public Binding orderCreateOrderBinding(){
        return new Binding(RabbitConstant.ORDER_DELAY_QUEUE,
                Binding.DestinationType.QUEUE,RabbitConstant.ORDER_EVENT_EXCHANGE,
                RabbitConstant.CREATE_ROUTING_KEY,null);
    }

    /**
     * 绑定死信队列
     * @return
     */
    @Bean
    public Binding orderReleaseOrderBinding(){
        return new Binding(RabbitConstant.ORDER_RELEASE_ORDER_QUEEU,
                Binding.DestinationType.QUEUE,RabbitConstant.ORDER_EVENT_EXCHANGE,
                RabbitConstant.RELEASE_ROUTING_KEY,null);
    }


    /**
     * 绑定死信队列
     * @return
     */
    @Bean
    public Binding stockReleaseStockBinding(){
        return new Binding("stock.release.stock.queue",
                Binding.DestinationType.QUEUE,RabbitConstant.ORDER_EVENT_EXCHANGE,
                "order.release.other.#",null);
    }


}
