package com.yama.mall.product.service.impl;

import com.yama.mall.product.vo.SkuItemSaleAttrVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yama.mall.common.utils.PageUtils;
import com.yama.mall.common.utils.Query;

import com.yama.mall.product.dao.SkuSaleAttrValueDao;
import com.yama.mall.product.entity.SkuSaleAttrValueEntity;
import com.yama.mall.product.service.SkuSaleAttrValueService;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 查询商品的销售属性，并使用分组查询的方式
     * @param spuId
     * @return
     */
    @Override
    public List<SkuItemSaleAttrVO> getSaleAttrValueBySpuId(Long spuId) {
        List<SkuItemSaleAttrVO> skuItemSaleAttrVOS = this.getBaseMapper().getSaleAttrValueBySpuId(spuId);
        return skuItemSaleAttrVOS;
    }

}