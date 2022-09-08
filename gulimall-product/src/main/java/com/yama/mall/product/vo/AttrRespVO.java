package com.yama.mall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttrRespVO extends AttrVO {
    private String catelogName;

    private String groupName;

    /**
     * "catelogPath": [2, 34, 225] //分类完整路径
     */
    private Long[] catelogPath;
}
