package com.yama.mall.ware.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: rabbitmq的所有配置
 * @date: 2022年10月10日 周一 10:33
 * @author: yama946
 */
@Configuration
public class MyRabbitMQConfig {

    public static final String STOCK_EVENT_EXCHANGE = "stock-event-exchange";

    public static final String STOCK_DELAY_QUEUE = "stock.delay.queue";

    public static final String STOCK_RELEASE_QUEUE = "stock.release.stock.queue";

    public static final Integer X_MESSAGE_TTL = 60000;

    /**
     * 配置消息转换器，转换成json形式发送消息
     * @return
     */
    @Bean
    public MessageConverter getMessageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 配置ware模块的交换机
     * @return
     */
    @Bean
    public Exchange stockEventExchange(){
        TopicExchange topicExchange = new TopicExchange(MyRabbitMQConfig.STOCK_EVENT_EXCHANGE, true, false);
        return topicExchange;
    }

    /**
     * 延迟队列
     * @return
     */
    @Bean
    public Queue stockDelayQueue(){
        Map<String, Object> arguments = new HashMap<>();
        /**
         * x-dead-letter-exchange: order-event-exchange
         * x-dead-letter-routing-key: order.release.order
         * x-message-ttl: 60000
         */
        //死信消息的交换机
        arguments.put("x-dead-letter-exchange",MyRabbitMQConfig.STOCK_EVENT_EXCHANGE);
        //死信消息的routing-key
        arguments.put("x-dead-letter-routing-key","stock.release");
        //队列中消息存活时间
        arguments.put("x-message-ttl",MyRabbitMQConfig.X_MESSAGE_TTL);
        Queue queue = new Queue(MyRabbitMQConfig.STOCK_DELAY_QUEUE,
                true, false, false, arguments);
        return queue;
    }

    /**
     * 死信队列
     * @return
     */
    @Bean
    public Queue stockReleaseStockQueue(){
        Queue queue = new Queue(MyRabbitMQConfig.STOCK_RELEASE_QUEUE, true, false, false);
        return queue;
    }

    /**
     * 绑定延时队列
     * @return
     */
    @Bean
    public Binding stockDelayBinding(){
        return new Binding(MyRabbitMQConfig.STOCK_DELAY_QUEUE,
                    Binding.DestinationType.QUEUE,MyRabbitMQConfig.STOCK_EVENT_EXCHANGE,
                "stock.locked",null);
    }

    @Bean
    public Binding stockReleaseStockBinding(){
        return new Binding(MyRabbitMQConfig.STOCK_RELEASE_QUEUE,
                Binding.DestinationType.QUEUE,MyRabbitMQConfig.STOCK_EVENT_EXCHANGE,
                "stock.release.#",null);
    }
}
