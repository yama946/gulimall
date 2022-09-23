package com.yama.mall.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;


@Data
public class SpuItemAttrGroupVO {

    private String groupName;

    private List<Attr> attrs;


}
