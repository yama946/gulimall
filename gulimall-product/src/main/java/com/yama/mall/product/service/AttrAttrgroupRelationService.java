package com.yama.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yama.mall.common.utils.PageUtils;
import com.yama.mall.product.entity.AttrAttrgroupRelationEntity;
import com.yama.mall.product.vo.AttrGroupRelationVO;

import java.util.Map;

/**
 * 属性&属性分组关联
 *
 * @author yanmu
 * @email yanmu123@gmail.com
 * @date 2022-08-30 17:50:51
 */
public interface AttrAttrgroupRelationService extends IService<AttrAttrgroupRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void attrGroupRelationDelete(AttrGroupRelationVO[] attrGroupRelationVO);

    void attrGroupRelationSave(AttrGroupRelationVO[] attrGroupRelationVO);
}

