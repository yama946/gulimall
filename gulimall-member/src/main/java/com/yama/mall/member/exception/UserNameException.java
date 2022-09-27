package com.yama.mall.member.exception;

public class UserNameException extends RuntimeException {
    public UserNameException() {
        super("用户名已存在");
    }
}
