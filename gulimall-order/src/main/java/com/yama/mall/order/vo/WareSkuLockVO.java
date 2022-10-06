package com.yama.mall.order.vo;

import lombok.Data;

import java.util.List;

/**
 * @description:
 * @date: 2022年10月06日 周四 20:19
 * @author: yama946
 */
@Data
public class WareSkuLockVO {
    private String orderSn;//订单号

    private List<OrderItemVO> locks;//要锁定的商品，数量
}
