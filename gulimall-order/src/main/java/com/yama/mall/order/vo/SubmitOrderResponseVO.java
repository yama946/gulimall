package com.yama.mall.order.vo;

import com.yama.mall.order.entity.OrderEntity;
import lombok.Data;

/**
 * 下单成功，返回用户地址信息，支付金额信息
 * 支付失败：
 * @description: 提交订单请求处理返回数据
 * @date: 2022年10月06日 周四 14:13
 * @author: yama946
 */
@Data
public class SubmitOrderResponseVO {
    //订单实体类
    private OrderEntity order;

    private Integer code;//请求是否成功，0表示成功，1表示失败
}
