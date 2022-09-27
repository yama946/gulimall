package com.yama.mall.product.feign;

import org.springframework.cloud.openfeign.FeignClient;

//@FeignClient("gulimall-coupon")//出现异常，The bean 'gulimall-coupon.FeignClientSpecification', defined in null,
//多个FeignClient的值相同，导致
@FeignClient("gulimall-member")
public interface SeckillFeignService {

}
