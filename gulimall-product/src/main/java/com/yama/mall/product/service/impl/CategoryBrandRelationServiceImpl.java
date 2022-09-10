package com.yama.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.yama.mall.product.entity.BrandEntity;
import com.yama.mall.product.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yama.mall.common.utils.PageUtils;
import com.yama.mall.common.utils.Query;

import com.yama.mall.product.dao.CategoryBrandRelationDao;
import com.yama.mall.product.entity.CategoryBrandRelationEntity;
import com.yama.mall.product.service.CategoryBrandRelationService;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {

    @Autowired
    private BrandService brandService;

    /**
     * 根据catId获取品牌信息
     * @param catId
     * @return
     */
    @Override
    public List<BrandEntity> getBrandsByCateId(Long catId) {
        List<CategoryBrandRelationEntity> cateBrands = this.baseMapper.selectList(
                new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId));
        List<BrandEntity> brandEntities = cateBrands.stream().map(item -> {
            Long brandId = item.getBrandId();
            BrandEntity brand = brandService.getById(brandId);
            return brand;
        }).collect(Collectors.toList());
        return brandEntities;
    }


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 更新关联表数据
     * @param catId
     * @param name
     */
    @Override
    public void updateCatetory(Long catId, String name) {
        this.baseMapper.updateCategory(catId,name);
    }


    /**
     * 更新关联表数据
     * @param brandId
     * @param name
     */
    @Override
    public void updateBrand(Long brandId, String name) {
        CategoryBrandRelationEntity categoryBrandRelationEntity = new CategoryBrandRelationEntity();
        categoryBrandRelationEntity.setBrandId(brandId);
        categoryBrandRelationEntity.setBrandName(name);
        this.update(categoryBrandRelationEntity,new UpdateWrapper<CategoryBrandRelationEntity>().eq("brand_id",brandId));
    }

}