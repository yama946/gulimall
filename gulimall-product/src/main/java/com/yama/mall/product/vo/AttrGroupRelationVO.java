package com.yama.mall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttrGroupRelationVO {
    //[{"attrId":1,"attrGroupId":2}]
    private Long attrId;

    private Long attrGroupId;
}
