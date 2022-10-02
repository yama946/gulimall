package com.yama.mall.cart.service;

import com.yama.mall.cart.vo.CartItemVo;
import com.yama.mall.cart.vo.CartVo;

import java.util.concurrent.ExecutionException;

public interface CartService {
    CartItemVo addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    CartItemVo getCartItem(Long skuId);

    CartVo getCart() throws ExecutionException, InterruptedException;

    void clearCart(String tempCartKey);
}
