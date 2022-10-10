package com.yama.mall.order.vo;

import lombok.Data;

/**
 * @description: 支付提交信息
 * @date: 2022年10月10日 周一 21:07
 * @author: yama946
 */
@Data
public class PayVo {
    private String out_trade_no; // 商户订单号 必填
    private String subject; // 订单名称 必填
    private String total_amount;  // 付款金额 必填
    private String body; // 商品描述 可空
}

