package com.yama.mall.ware.dao;

import com.yama.mall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yama.mall.ware.vo.SkuHasStockVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 商品库存
 * 
 * @author yanmu
 * @email yanmu123@gmail.com
 * @date 2022-08-30 18:17:03
 */
@Repository
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    void addStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);

    Long hasStock(@Param("skuId") Long skuId);

    List<Long> listWareIdHasSkuStock(@Param("skuId") Long skuId);

    /**
     * 锁定库存
     * @param skuId 商品skuId
     * @param wareId 仓库id
     * @param num 购买数量
     * @return
     */
    Long lockSkuStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("num") Integer num);
}
