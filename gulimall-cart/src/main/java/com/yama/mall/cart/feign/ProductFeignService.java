package com.yama.mall.cart.feign;

import com.yama.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;

/**
 * @description: 商品信息的远程接口
 * @date: 2022年10月01日 周六 16:24
 * @author: yama946
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {
    /**
     * 获取商品的sku信息
     * @param skuId
     * @return
     */
    @RequestMapping("product/skuinfo/info/{skuId}")
    R getSkuInfo(@PathVariable("skuId") Long skuId);

    /**
     * 获取sku销售属性值信息
     * @param skuId
     * @return
     */
    @GetMapping("product/skusaleattrvalue/list/string/{skuId}")
    List<String> getSkuSaleAttrValues(@PathVariable("skuId") Long skuId);

    /**
     * 获取商品价格
     * @param skuId
     * @return
     */
    @GetMapping("product/skuinfo/{skuId}/price")
    BigDecimal getPrice(@PathVariable("skuId") Long skuId);
}
