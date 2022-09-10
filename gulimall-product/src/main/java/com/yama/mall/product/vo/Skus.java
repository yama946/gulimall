/**
  * Copyright 2019 bejson.com 
  */
package com.yama.mall.product.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Auto-generated: 2019-11-26 10:50:34
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Skus {


    private String skuName;
    private String skuTitle;
    private String skuSubtitle;
    private BigDecimal price;
    private int fullCount;
    private BigDecimal discount;
    private int countStatus;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private int priceStatus;
    private List<Images> images;
    private List<String> descar;
    private List<Attr> attr;
    private List<MemberPrice> memberPrice;


}