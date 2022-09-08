package com.yama.mall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrandVO {
    /**
     * "brandId": 0,
     * "brandName": "string",
     */

    private Long brandId;

    private String brandName;
}
