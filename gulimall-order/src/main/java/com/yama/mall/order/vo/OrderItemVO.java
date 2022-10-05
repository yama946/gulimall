package com.yama.mall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @description: 用户选中进行购买的购物项信息
 * @date: 2022年10月04日 周二 19:00
 * @author: yama946
 */
@Data
public class OrderItemVO {
    //商品ID
    private Long skuId;

    //商品标题
    private String title;

    //商品图片
    private String image;

    /**
     * 商品销售属性
     */
    private List<String> skuAttrValues;

    //商品单价
    private BigDecimal price;

    //商品数量
    private Integer count;

    //商品小计
    private BigDecimal totalPrice;
}
