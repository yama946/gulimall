package com.yama.mall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 2级分类vo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Catelog2VO {
    private String catalog1Id;//1级父分类id

    private List<Catelog3VO> catalog3List;//3级分类

    private String id;

    private String name;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Catelog3VO{
        private String catalog2Id;//父分类，2级分类id

        private String id;

        private String name;
    }
}
