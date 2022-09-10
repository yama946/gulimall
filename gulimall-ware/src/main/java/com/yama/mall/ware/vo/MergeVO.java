package com.yama.mall.ware.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MergeVO {
    /**
     *   purchaseId: 1, //整单id
     *   items:[1,2,3,4] //合并项集合
     */

    private Long purchaseId;

    private List<Long> item;
}
