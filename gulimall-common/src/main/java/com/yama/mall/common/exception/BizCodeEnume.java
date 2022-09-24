package com.yama.mall.common.exception;

import lombok.Getter;

/***
 * 当系统中异常众多，如果随意抛出，会导致定位异常比较困难，
 * 因此我们规范化定义异常状态码，以及异常信息，便于我们排除故障快速定位异常。
 *
 * 错误码和错误信息定义类
 * 1. 错误码定义规则为 5 为数字
 * 2. 前两位表示业务场景，最后三位表示错误码。例如：100001。10:通用 001:系统未知
 异常
 * 3. 维护错误码后需要维护错误描述，将他们定义为枚举形式
 * 错误码列表：
 * 10: 通用
 * 001：参数格式校验
 * 002：验证码发送频率过高
 * 11: 商品
 * 12: 订单
 * 13: 购物车
 * 14: 物流
 */
@Getter
public enum BizCodeEnume {
    UNKNOW_EXCEPTION(10000,"系统未知异常"),

    VAILD_EXCEPTION(10001,"参数格式校验失败"),

    SMS_CODE_EXCEPTION(10002,"验证码发送次数过多，请稍后重试"),

    PRODUCT_UP_EXCEPTION(11000,"商品上架异常");

    private int code;

    private String msg;

    BizCodeEnume(int code,String msg){
        this.code=code;
        this.msg = msg;
    }


}
