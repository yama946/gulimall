package com.yama.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yama.mall.common.utils.PageUtils;
import com.yama.mall.product.entity.CategoryEntity;
import com.yama.mall.product.vo.Catelog2VO;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author yanmu
 * @email yanmu123@gmail.com
 * @date 2022-08-30 17:50:51
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();

    void removeMenuByIds(List<Long> asList);

    Long[] getCatelogPath(Long catelogId);

    void updateCascade(CategoryEntity category);

    List<CategoryEntity> getLevel1Category();

    Map<String, List<Catelog2VO>> getCatalogJson();

}

