package com.yama.mall.cart.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 远程调用返回json数据，进行转换成当前对象
 * @description:
 * @date: 2022年10月01日 周六 16:49
 * @author: yama946
 */
@Data
public class SkuInfoVO {
    private Long skuId;
    /**
     * spuId
     */
    private Long spuId;
    /**
     * sku名称
     */
    private String skuName;
    /**
     * sku介绍描述
     */
    private String skuDesc;
    /**
     * 所属分类id
     */
    private Long catalogId;
    /**
     * 品牌id
     */
    private Long brandId;
    /**
     * 默认图片
     */
    private String skuDefaultImg;
    /**
     * 标题
     */
    private String skuTitle;
    /**
     * 副标题
     */
    private String skuSubtitle;
    /**
     * 价格
     */
    private BigDecimal price;
    /**
     * 销量
     */
    private Long saleCount;
}
