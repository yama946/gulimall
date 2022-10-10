package com.yama.mall.common.to.mq;

import lombok.Data;

/**
 * @description: 库存锁定to
 * @date: 2022年10月10日 周一 11:28
 * @author: yama946
 */
@Data
public class StockLockTo {
    private Long id;//库存工作单id，一个库存工作单可能对应多个锁定商品的操作，也就是说一个订单存在多个商品

    private StockDetailTo stockDetail;//库存工作单详情id
}
