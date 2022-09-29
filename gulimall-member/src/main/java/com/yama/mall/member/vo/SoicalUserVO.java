package com.yama.mall.member.vo;

import lombok.Data;

@Data
public class SoicalUserVO {
    /**
     *     "login": "yama946",
     *     "id": 55392911,
     */
    private String login;

    private String socialUId;

    private String accessToken;
}
