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
public class SpuSaveVO {

    private String spuName;
    private String spuDescription;
    private Long catalogId;
    private Long brandId;
    //重量使用BigDecimal，而不是用double类型
    private BigDecimal weight;
    private int publishStatus;
    private List<String> decript;
    private List<String> images;
    private Bounds bounds;
    private List<BaseAttrs> baseAttrs;
    private List<Skus> skus;
}