package com.yama.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yama.mall.common.utils.PageUtils;
import com.yama.mall.product.entity.SkuInfoEntity;
import com.yama.mall.product.vo.SkuItemVO;

import java.util.List;
import java.util.Map;

/**
 * sku信息
 *
 * @author yanmu
 * @email yanmu123@gmail.com
 * @date 2022-08-30 17:50:51
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageByCondition(Map<String, Object> params);

    List<SkuInfoEntity> getSkuBySpuId(Long spuId);

    SkuItemVO item(Long skuId);
}

