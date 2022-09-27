package com.yama.mall.auth.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * 用户提交表单的数据模型
 */
@Data
public class UserRegistVO {

    //TODO 添加JSR303进行数据校验
    //用户名
    @NotEmpty(message = "用户名必须提交")
    @Length(min = 6,max = 18,message = "用户名必须是6-18个字符")
    private String userName;

    //密码
    @NotEmpty(message = "密码必须提交")
    @Length(min = 6,max = 18,message = "密码必须是6-18个字符")
    private String password;

    //电话号码
    //TODO 电话号的正则表达式的书写，以及unicode编码
    @NotEmpty(message = "手机号必须提交")
    @Pattern(regexp = "^[1]([3-9])[0-9]{9}$",message = "手机号格式错误")
    private String phone;

    //验证码
    @NotEmpty(message = "验证码必须提交")
    private String code;

}
