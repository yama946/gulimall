package com.yama.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yama.common.utils.PageUtils;
import com.yama.mall.order.entity.PaymentInfoEntity;

import java.util.Map;

/**
 * 支付信息表
 *
 * @author yanmu
 * @email yanmu123@gmail.com
 * @date 2022-08-30 18:08:51
 */
public interface PaymentInfoService extends IService<PaymentInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

