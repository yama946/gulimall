package com.yama.mall.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkuHasStockVO {
    private Long skuId;

    private Boolean hasStock;
}
