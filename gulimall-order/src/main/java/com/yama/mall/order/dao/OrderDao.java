package com.yama.mall.order.dao;

import com.yama.mall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author yanmu
 * @email yanmu123@gmail.com
 * @date 2022-08-30 18:08:51
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
