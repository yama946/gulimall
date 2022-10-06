package com.yama.mall.order.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @date: 2022年10月06日 周四 9:51
 * @author: yama946
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemHasStockVO {
    private Long skuId;

    private Boolean hasStock;
}
