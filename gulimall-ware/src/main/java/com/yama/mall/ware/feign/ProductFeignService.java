package com.yama.mall.ware.feign;

import com.yama.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-product")
//@FeignClient("gulimall-gateway")
public interface ProductFeignService {
    /**
     * 1)、让所有请求过网关：
     *          1、@FeignClient("gulimall-gateway")：给gulimall-gateway所在的机器发送请求
     *          2、网关根据路由找到指定微服务：@RequestMapping("api/product/skuinfo/info/{skuId}")
     * 2)、直接让后台指定服务处理
     *          1、@FeignClient("gulimall-product")
     *          2、@RequestMapping("product/skuinfo/info/{skuId}")
     * @param skuId
     * @return
     */
//    @RequestMapping("api/product/skuinfo/info/{skuId}")
    @RequestMapping("product/skuinfo/info/{skuId}")
    R info(@PathVariable("skuId") Long skuId);
}
