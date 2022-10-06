package com.yama.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yama.mall.common.utils.PageUtils;
import com.yama.mall.ware.entity.WareSkuEntity;
import com.yama.mall.ware.vo.LockStockResult;
import com.yama.mall.ware.vo.SkuHasStockVO;
import com.yama.mall.ware.vo.WareSkuLockVO;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author yanmu
 * @email yanmu123@gmail.com
 * @date 2022-08-30 18:17:03
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStocks(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVO> getSkuHasStock(List<Long> skuIds);

    Boolean orderLockStock(WareSkuLockVO vo);
}

