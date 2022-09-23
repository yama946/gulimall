package com.yama.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yama.mall.common.utils.PageUtils;
import com.yama.mall.product.entity.SkuSaleAttrValueEntity;
import com.yama.mall.product.vo.SkuItemSaleAttrVO;

import java.util.List;
import java.util.Map;

/**
 * sku销售属性&值
 *
 * @author yanmu
 * @email yanmu123@gmail.com
 * @date 2022-08-30 17:50:51
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<SkuItemSaleAttrVO> getSaleAttrValueBySpuId(Long spuId);
}

