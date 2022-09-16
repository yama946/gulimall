package com.yama.mall.product.dao;

import com.yama.mall.product.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品属性
 * 
 * @author yanmu
 * @email yanmu123@gmail.com
 * @date 2022-08-30 17:50:51
 */
@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {


    List<Long> selectSearchTypeByAttrIds(@Param("attrIds") List<Long> attrIds);
}
