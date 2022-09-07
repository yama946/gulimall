package com.yama.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yama.common.utils.PageUtils;
import com.yama.mall.product.entity.AttrEntity;
import com.yama.mall.product.vo.AttrVO;

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
}

