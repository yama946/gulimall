package com.yama.mall.product.feign;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("gulimall-coupon")
public interface SeckillFeignService {

}
