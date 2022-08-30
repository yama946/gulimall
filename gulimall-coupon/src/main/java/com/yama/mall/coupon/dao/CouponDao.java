package com.yama.mall.coupon.dao;

import com.yama.mall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author yanmu
 * @email yanmu123@gmail.com
 * @date 2022-08-30 17:53:30
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
