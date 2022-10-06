package com.yama.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yama.mall.common.utils.PageUtils;
import com.yama.mall.product.entity.SpuInfoEntity;
import com.yama.mall.product.to.SpuInfoTo;
import com.yama.mall.product.vo.SpuSaveVO;

import java.util.Map;

/**
 * spu信息
 *
 * @author yanmu
 * @email yanmu123@gmail.com
 * @date 2022-08-30 17:50:51
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfo(SpuSaveVO vo);

    void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity);

    PageUtils queryPageByCondition(Map<String, Object> params);

    void up(Long spuId);

    /**
     * 通过skuId获取spu实体类
     * @param skuId
     * @return
     */
    SpuInfoTo getSpuInfoBySkuId(Long skuId);
}

