package com.yama.mall.order.feign;

import com.yama.mall.order.vo.OrderItemVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @description:
 * @date: 2022年10月05日 周三 10:49
 * @author: yama946
 */
@FeignClient("gulimall-cart")
public interface CartFeignService {
    /**
     * 获取当前用户选中的购物项
     * @return
     */
    @GetMapping("/concurrentUserCartItems")
    List<OrderItemVO> getconcurrentUserCartItems();
}
