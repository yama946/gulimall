package com.yama.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yama.mall.common.utils.PageUtils;
import com.yama.mall.product.entity.AttrEntity;
import com.yama.mall.product.vo.AttrRespVO;
import com.yama.mall.product.vo.AttrVO;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author yanmu
 * @email yanmu123@gmail.com
 * @date 2022-08-30 17:50:51
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVO attrVO);


    PageUtils queryBaseAttrPage(Map<String, Object> params, Integer catelogId);

    AttrRespVO getAttrInfo(Long attrId);

    void updateAttr(AttrVO attr);

    PageUtils queryAttrPageByAttrType(Map<String, Object> params, Integer catelogId, String attrType);

    List<AttrEntity> getReleationAttr(Long attrgroupId);

    PageUtils getNoReleationAttr(Long attrgroupId, Map<String, Object> params);

    List<Long> selectSearchAttrs(List<Long> attrIds);
}

