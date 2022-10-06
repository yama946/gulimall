package com.yama.mall.order.feign;

import com.yama.mall.common.utils.R;
import com.yama.mall.order.vo.OrderItemHasStockVO;
import com.yama.mall.order.vo.WareSkuLockVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @description: 调用库存服务，查询库存状态
 * @date: 2022年10月06日 周四 9:47
 * @author: yama946
 */
@FeignClient("gulimall-ware")
public interface WareFeignService {

    /**
     * 判断sku中是否有库存
     * @param skuIds
     * @return
     */
    @PostMapping("ware/waresku/hasstock")
    List<OrderItemHasStockVO> getOrderItemHasStock(@RequestBody List<Long> skuIds);

    /**
     * 获取收货地址信息，运费信息
     * @param attrId
     * @return
     */
    @GetMapping("ware/wareinfo/fare")
    R getFare(@RequestParam("attrId") Long attrId);

    /**
     * 为某个订单锁定库存
     * @param vo
     * @return
     */
    @PostMapping("ware/waresku/order/lock/stock")
    R orderLockStock(@RequestBody WareSkuLockVO vo);
}
