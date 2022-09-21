package com.yama.mall.product.feign;

import com.yama.mall.common.utils.R;
import com.yama.mall.common.vo.SkuHasStockVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimall-ware")
public interface WareFeignService {
    /**
     * 1.R设置的时候使用泛型
     * 2.直接返回我们想要的结果
     * 3.自己封装解析结果
     * @param skuIds
     * @return
     */
    @PostMapping("ware/waresku/hasstock")
    List<SkuHasStockVO> getSkusHasStock(@RequestBody List<Long> skuIds);

}
