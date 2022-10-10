package com.yama.mall.order.canstant;

/**
 * @description: rabbitmq所使用的常量
 * @date: 2022年10月10日 周一 9:27
 * @author: yama946
 */
public class RabbitConstant {
    public static final String ORDER_DELAY_QUEUE="order.delay.queue";

    public static final String ORDER_RELEASE_ORDER_QUEEU = "order.release.order.queue";

    public static final String ORDER_EVENT_EXCHANGE = "order-event-exchange";

    public static final Integer X_MESSAGE_TTL = 60000;

    public static final String CREATE_ROUTING_KEY = "order.create.order";

    public static final String RELEASE_ROUTING_KEY = "order.release.order";

}
