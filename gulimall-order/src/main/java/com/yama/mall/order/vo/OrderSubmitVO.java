package com.yama.mall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @description: 封装订单提交的数据
 * @date: 2022年10月06日 周四 13:32
 * @author: yama946
 */
@Data
public class OrderSubmitVO {
    //收获地址id，查询用户收货地址信息
    private Long addrId;

    //TODO 支付方式---未实现
    private Integer payType;//在线支付，货到付款

    /**
     * 商品总额，无需提交，使用时去购物车重新查询一遍即可
     */

    //TODO 优惠，发票等信息提交

    //TODO 验价功能，将应付金额与购物车获取的总金额对比，不符合提示用户价格改变，或者其他提示信息
    //支付金额
    private BigDecimal payPrice;


    /**
     * 用户信息，直接从session中获取，无需进行页面提交操作
     */

    //TODO 订单备注信息，备注订单---未实现
    private String note;

    //获取防重令牌
    private String orderToken;
}
