package com.yama.mall.cart.service;

import com.yama.mall.cart.vo.CartItemVo;

import java.util.concurrent.ExecutionException;

public interface CartService {
    CartItemVo addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;
}
