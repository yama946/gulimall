package com.yama.mall.search.feign;

import com.yama.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient("gulimall-product")
public interface ProductFeignService {
    @PostMapping("product/attr/{attrId}")
    R attrInfo(@PathVariable("attrId") long attrId);
}
