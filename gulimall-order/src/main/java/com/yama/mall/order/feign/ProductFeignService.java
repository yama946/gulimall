package com.yama.mall.order.feign;

import com.yama.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @description:
 * @date: 2022年10月06日 周四 16:09
 * @author: yama946
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {
    /**
     * 通过skuId获取SPU信息
     * @param skuId
     * @return
     */
    @GetMapping("/product/spuinfo/{skuId}")
    R getSpuInfoBySkuId(@PathVariable("skuId") Long skuId);
}
