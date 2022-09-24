package com.yama.mall.auth.vo;

import lombok.Data;

/**
 * 用户提交表单的数据模型
 */
@Data
public class UserRegistVO {

    //TODO 添加JSR303进行数据校验
    //用户名
    private String userName;

    //密码
    private String password;

    //电话号码
    private String phone;

    //验证码
    private String code;

}
