package com.yama.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yama.mall.common.utils.PageUtils;
import com.yama.mall.ware.entity.WareInfoEntity;
import com.yama.mall.ware.vo.FareVO;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 仓库信息
 *
 * @author yanmu
 * @email yanmu123@gmail.com
 * @date 2022-08-30 18:17:04
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 根据收获地址计算运费信息
     * @param attrId
     * @return
     */
    FareVO getFare(Long attrId);
}

