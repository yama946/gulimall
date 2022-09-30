package com.yama.mall.cart.service.impl;

import com.yama.mall.cart.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
}
