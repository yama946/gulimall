package com.yama.mall.product.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @description:
 * @date: 2022年10月06日 周四 16:34
 * @author: yama946
 */
@Data
public class SpuInfoTo {
    /**
     * 商品id
     */
    private Long id;
    /**
     * 商品名称
     */
    private String spuName;
    /**
     * 商品描述
     */
    private String spuDescription;
    /**
     * 所属分类id
     */
    private Long catalogId;
    /**
     * 品牌id
     */
    private Long brandId;

    //品牌名
    private String brandName;

    private BigDecimal weight;
    /**
     * 上架状态[0 - 下架，1 - 上架]
     */
    private Integer publishStatus;

    private Date createTime;

    private Date updateTime;
}
