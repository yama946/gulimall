package com.yama.mall.ware.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseItemDoneVO {
    /**
     *    items: [{itemId:1,status:4,reason:""}]//完成/失败的需求详情
     */
    private Long itemId;

    private Integer status;

    @NotEmpty
    private String reason;

}
