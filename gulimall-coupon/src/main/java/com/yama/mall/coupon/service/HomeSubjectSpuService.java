package com.yama.mall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yama.mall.common.utils.PageUtils;
import com.yama.mall.coupon.entity.HomeSubjectSpuEntity;

import java.util.Map;

/**
 * δΈι’εε
 *
 * @author yanmu
 * @email yanmu123@gmail.com
 * @date 2022-08-30 17:53:29
 */
public interface HomeSubjectSpuService extends IService<HomeSubjectSpuEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

