package com.yama.mall.ware.feign;

import com.yama.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @description: 订单系统远程接口
 * @date: 2022年10月10日 周一 13:26
 * @author: yama946
 */
@FeignClient("gulimall-order")
public interface OrderFeignService {
    @GetMapping("/status/{ordersn}")
    R getOrderStatus(@PathVariable("ordersn") String orderSn);
}
