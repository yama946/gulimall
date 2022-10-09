package com.yama.mall.common.exception;

/**
 * @description:
 * @date: 2022年10月06日 周四 21:38
 * @author: yama946
 */
public class NoStockException extends RuntimeException {
    private Long skuId;

    public NoStockException(Long skuId){
        super("商品id为"+skuId+"没有库存异常");
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }
}
