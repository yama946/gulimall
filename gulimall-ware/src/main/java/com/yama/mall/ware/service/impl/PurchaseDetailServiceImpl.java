package com.yama.mall.ware.service.impl;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yama.mall.common.utils.PageUtils;
import com.yama.mall.common.utils.Query;

import com.yama.mall.ware.dao.PurchaseDetailDao;
import com.yama.mall.ware.entity.PurchaseDetailEntity;
import com.yama.mall.ware.service.PurchaseDetailService;
import org.springframework.util.StringUtils;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        /**
         *    key: '华为',//检索关键字
         *    status: 0,//状态
         *    wareId: 1,//仓库id
         */

        QueryWrapper<PurchaseDetailEntity> queryWrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)){
            queryWrapper.and(t->t.eq("purchase_id",key).or().eq("sku_id",key));
        }

        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)){
            queryWrapper.eq("ware_id",wareId);
        }

        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)){
            queryWrapper.eq("status",status);
        }


        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    /**
     * 员工领取采购单辅助方法，
     * 通过采购单id，获取所有采购项
     * @param purchaseId
     * @return
     */
    @Override
    public List<PurchaseDetailEntity> listPurchaseDetailByPurchaseId(Long purchaseId) {
        QueryWrapper<PurchaseDetailEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("purchase_id",purchaseId);
        List<PurchaseDetailEntity> purchaseDetailEntities = this.baseMapper.selectList(queryWrapper);
        return purchaseDetailEntities;
    }

}