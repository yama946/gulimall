package com.yama.mall.common.constant;

import lombok.Data;
import lombok.Getter;

/**
 * 所有的值，使用枚举进行替换
 * 当数据规则改变时，可以直接更改枚举类中的值即可实现更改
 */
public class ProductConstant {
    @Getter
    public enum AttrEnum{
        ATTR_TYPE_BASE(1,"基本属性"),ATTR_TYPE_SALE(0,"销售属性");
        private  int code;
        private String msg;
        AttrEnum(int code,String msg){
            this.code = code;
            this.msg = msg;
        }
    }
}
