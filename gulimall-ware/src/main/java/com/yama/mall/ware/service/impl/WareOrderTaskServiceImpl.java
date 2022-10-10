package com.yama.mall.ware.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yama.mall.common.utils.PageUtils;
import com.yama.mall.common.utils.Query;

import com.yama.mall.ware.dao.WareOrderTaskDao;
import com.yama.mall.ware.entity.WareOrderTaskEntity;
import com.yama.mall.ware.service.WareOrderTaskService;


@Service("wareOrderTaskService")
public class WareOrderTaskServiceImpl extends ServiceImpl<WareOrderTaskDao, WareOrderTaskEntity> implements WareOrderTaskService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareOrderTaskEntity> page = this.page(
                new Query<WareOrderTaskEntity>().getPage(params),
                new QueryWrapper<WareOrderTaskEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 根据订单号获取库存单信息
     * @param orderSn
     * @return
     */
    @Override
    public WareOrderTaskEntity getOrderTaskByOrderSn(String orderSn) {
        WareOrderTaskDao baseMapper = this.getBaseMapper();
        WareOrderTaskEntity wareOrderTaskEntity = baseMapper.selectOne(new QueryWrapper<WareOrderTaskEntity>().eq("order_sn", orderSn));
        return wareOrderTaskEntity;
    }

}