package com.yama.mall.cart.vo;

import lombok.Data;

/**
 * 用户的基础数据，传递给controller
 */
@Data
public class UserInfoTo {
    private Long userId;

    private String userKey;

    private boolean tempUser =false;
}
