package com.yama.mall.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;


@Data
@ToString
public class SkuItemSaleAttrVO {

    private Long attrId;

    private String attrName;

    private String attrValues;
//    private List<AttrValueWithSkuIdVO> attrValues;


}
