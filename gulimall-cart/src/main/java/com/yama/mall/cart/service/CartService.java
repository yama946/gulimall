package com.yama.mall.cart.service;

import com.yama.mall.cart.vo.CartItemVo;
import com.yama.mall.cart.vo.CartVo;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface CartService {
    CartItemVo addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    CartItemVo getCartItem(Long skuId);

    CartVo getCart() throws ExecutionException, InterruptedException;

    void clearCart(String tempCartKey);

    void checkCartItem(Long skuId, Integer checked);

    void changeItemCount(Long skuId, Integer num);

    void deleteCartItem(Long skuId);

    /**
     * 获取当前登陆用户
     * @return
     */
    List<CartItemVo> getUserCartItems();

}
