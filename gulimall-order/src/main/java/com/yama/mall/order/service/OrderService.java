package com.yama.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yama.mall.common.utils.PageUtils;
import com.yama.mall.order.entity.OrderEntity;
import com.yama.mall.order.vo.OrderConfirmVO;
import com.yama.mall.order.vo.OrderSubmitVO;
import com.yama.mall.order.vo.SubmitOrderResponseVO;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author yanmu
 * @email yanmu123@gmail.com
 * @date 2022-08-30 18:08:51
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 获取用户订单信息
     * @return
     */
    OrderConfirmVO confirmOrder() throws ExecutionException, InterruptedException;

    /**
     * 下单操作
     * @param vo
     * @return
     */
    SubmitOrderResponseVO submitOrder(OrderSubmitVO vo);
}

