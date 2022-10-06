package com.yama.mall.order.to;

import com.yama.mall.order.entity.OrderEntity;
import com.yama.mall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @description: 创建订单保存到数据库中
 * @date: 2022年10月06日 周四 15:26
 * @author: yama946
 */
@Data
public class OrderCreateTO {

    //订单信息
    private OrderEntity order;

    //订单项信息---->订单关联的商品
    private List<OrderItemEntity> orderItems;

    //应付金额，需要和运费计算出实际支付金额
    private BigDecimal payPrice;

    //运费价格
    private BigDecimal fare;



}
