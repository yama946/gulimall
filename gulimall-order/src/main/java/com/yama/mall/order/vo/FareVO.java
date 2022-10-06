package com.yama.mall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @description:
 * @date: 2022年10月06日 周四 10:57
 * @author: yama946
 */
@Data
public class FareVO {
    //详细地址信息
    private MemberAddressVO address;

    //运费信息
    private BigDecimal fare;
}
