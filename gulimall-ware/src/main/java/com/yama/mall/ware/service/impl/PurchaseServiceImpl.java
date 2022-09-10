package com.yama.mall.ware.service.impl;

import com.yama.mall.common.constant.WareConstant;
import com.yama.mall.ware.entity.PurchaseDetailEntity;
import com.yama.mall.ware.service.PurchaseDetailService;
import com.yama.mall.ware.vo.MergeVO;
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

import com.yama.mall.ware.dao.PurchaseDao;
import com.yama.mall.ware.entity.PurchaseEntity;
import com.yama.mall.ware.service.PurchaseService;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    private PurchaseDetailService purchaseDetailService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 查询新建未分配状态的采购单
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPageUnreceiveList(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status",0)
        );

        return new PageUtils(page);
    }

    /**
     * 合并菜单方法
     * @param mergeVO
     */
    @Override
    public void mergePurchase(MergeVO mergeVO) {
        Long purchaseId = mergeVO.getPurchaseId();
        if (purchaseId==null){
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            this.save(purchaseEntity);
            purchaseId=purchaseEntity.getId();
        }
        List<Long> item = mergeVO.getItem();
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> purchaseDetailCollect = item.stream().map(i -> {
            PurchaseDetailEntity purchaseDetail = new PurchaseDetailEntity();
            //因为这是更新而不是新建，所以需要根据id进行查找更新
            purchaseDetail.setId(i);
            purchaseDetail.setPurchaseId(finalPurchaseId);
            purchaseDetail.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
            return purchaseDetail;
        }).collect(Collectors.toList());
        purchaseDetailService.updateBatchById(purchaseDetailCollect);

        //更新采购单修改时间
        PurchaseEntity entity = new PurchaseEntity();
        entity.setId(purchaseId);
        this.updateById(entity);

    }

    /**
     * 员工领取采购单
     * @param ids
     */
    @Override
    public void receivedPurchase(List<Long> ids) {

    }


}