package com.yama.mall.product.feign;

import com.yama.mall.common.to.SkuEsModel;
import com.yama.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


@FeignClient("gulimall-search")
public interface SearchFeignService {
    @PostMapping("search/save/product")
    R productUp(@RequestBody List<SkuEsModel> skuEsModelList);
}
