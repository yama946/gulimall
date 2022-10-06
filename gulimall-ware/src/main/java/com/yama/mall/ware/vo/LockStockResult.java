package com.yama.mall.ware.vo;

import lombok.Data;

/**
 * @description: 库存锁定结果
 * @date: 2022年10月06日 周四 20:28
 * @author: yama946
 */
@Data
public class LockStockResult {
    private Long skuId;//锁定商品的id
    private Integer num;//锁定数量
    private Boolean locked;//是否锁定成功
}
