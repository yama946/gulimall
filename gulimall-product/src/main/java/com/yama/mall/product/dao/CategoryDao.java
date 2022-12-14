package com.yama.mall.product.dao;

import com.yama.mall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 商品三级分类
 * 
 * @author yanmu
 * @email yanmu123@gmail.com
 * @date 2022-08-30 17:50:51
 */
@Repository
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
