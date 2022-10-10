package com.yama.mall.order.config;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 *
 * @description: 配置rabbitmq
 * @date: 2022年10月04日 周二 8:34
 * @author: yama946
 */
@Configuration
public class MyRabbitmqConfig {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    /**
     * 配置消息转换器，将消息转换成json发送
     * @return
     */
    @Bean
    public MessageConverter getMessageConverter(){
        return new Jackson2JsonMessageConverter();
    }
    /**
     * 定制RabbitTemplate,为RabbitTemplate重新设置回调
     * 1、服务收到消息就会回调
     *      1、spring.rabbitmq.publisher-confirms: true
     *      2、设置确认回调
     * 2、消息正确抵达队列就会进行回调
     *      1、spring.rabbitmq.publisher-returns: true
     *         spring.rabbitmq.template.mandatory: true
     *      2、设置确认回调ReturnCallback
     *
     * 3、消费端确认(保证每个消息都被正确消费，此时才可以让broker删除这个消息)
     *      1).默认是自动确认的，只要消息接收到，客户端会自动确认，服务端就会移除这个信息。
     *    问题场景：
     *      我们收到很多信息，会自动回复给服务器ack确认消息，但是只有一个处理成功，消费客户端宕机了。
     *      其他消息也被rabbit服务器删除了，发生消息的丢失。
     *    问题解决：
     *      手动确认，在配置文件配置手动确认，不是默认的自动确认恢复，每处理结束一个消息进行手动回复。
     *
     *      手动确认模式下，只要我们没有明确告诉MQ，货物被签收。没有ACK，消息就一直是unacked状态，即使消费者客户端宕机。
     *      消息也不回丢失，消息会变成ready状态。下一次新的消费者客户端连接重新消费信息。
     *
     */
    @PostConstruct  //MyRabbitConfig对象创建完成以后，执行这个方法
    public void initRabbitTemplate() {

        /**
         * 1、只要消息抵达Broker(并未监听处理开始)就ack=true
         * correlationData：当前消息的唯一关联数据(这个是消息的唯一id)
         * 通过此id，发送消息可以将消息放到数据中失败后，重新读取发送，可以发送时指定id
         * ack：消息是否成功收到
         * cause：失败的原因
         */
        //设置确认回调
        rabbitTemplate.setConfirmCallback((correlationData,ack,cause) -> {
            System.out.println("confirm...correlationData["+correlationData+"]==>ack:["+ack+"]==>cause:["+cause+"]");
        });


        /**
         * 只要消息没有投递给指定的队列，就触发这个失败回调
         * message：投递失败的消息详细信息
         * replyCode：回复的状态码
         * replyText：回复的文本内容
         * exchange：当时这个消息发给哪个交换机
         * routingKey：当时这个消息用哪个路邮键
         */
        rabbitTemplate.setReturnCallback((message,replyCode,replyText,exchange,routingKey) -> {
            System.out.println("Fail Message["+message+"]==>replyCode["+replyCode+"]" +
                    "==>replyText["+replyText+"]==>exchange["+exchange+"]==>routingKey["+routingKey+"]");
        });
    }

}
