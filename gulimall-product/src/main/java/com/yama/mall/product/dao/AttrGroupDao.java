package com.yama.mall.product.dao;

import com.yama.mall.product.entity.AttrGroupEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yama.mall.product.vo.SpuItemAttrGroupVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 属性分组
 * 
 * @author yanmu
 * @email yanmu123@gmail.com
 * @date 2022-08-30 17:50:51
 */
@Repository
@Mapper
public interface AttrGroupDao extends BaseMapper<AttrGroupEntity> {

    List<SpuItemAttrGroupVO> getAttrGroupWithattrsBySpuId(@Param("spuId") Long spuId, @Param("catalogId") Long catalogId);
}
