package com.yama.mall.ware.service.impl;

import com.yama.mall.common.constant.WareConstant;
import com.yama.mall.ware.entity.PurchaseDetailEntity;
import com.yama.mall.ware.service.PurchaseDetailService;
import com.yama.mall.ware.service.WareSkuService;
import com.yama.mall.ware.vo.MergeVO;
import com.yama.mall.ware.vo.PurchaseDoneVO;
import com.yama.mall.ware.vo.PurchaseItemDoneVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

import javax.validation.constraints.NotNull;


@Service("purchaseService")
@Slf4j
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    private PurchaseDetailService purchaseDetailService;

    @Autowired
    private WareSkuService wareSkuService;

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
        //TODO 确认采购点的状态是0,1才能进行合并
        List<Long> item = mergeVO.getItems();
        log.debug("item的值：{}",item);
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
        //1.领取的采购单必须是新建或者已分配的
        List<PurchaseEntity> collect = ids.stream().map(id -> {
            PurchaseEntity byId = this.getById(id);
            return byId;
        }).filter(item -> {
            if (item.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode()
                    || item.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()) {
                return true;
            }
            return false;
        }).map(t -> {
            //对满足要求的元素设置status状态值
            t.setStatus(WareConstant.PurchaseStatusEnum.RECEIVED.getCode());
            return t;
        }).collect(Collectors.toList());
        //2.改变当前采购单的状态
        this.updateBatchById(collect);//会依照mybatis-plus自动填充功能自动的更新时间
        //3.改变采购单对应采购需求的状态
        collect.forEach(purchase->{
            Long purchaseId = purchase.getId();
            List<PurchaseDetailEntity> purchaseDetailEntities =
                    purchaseDetailService.listPurchaseDetailByPurchaseId(purchaseId);
            if (purchase!=null){
                purchaseDetailEntities.forEach(p->{
                    p.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                    purchaseDetailService.updateById(p);
                });
            }
        });
    }

    /**
     * 完成订单
     * @param purchaseDoneVO
     */
    @Override
    public void donePurchase(PurchaseDoneVO purchaseDoneVO) {
        //1.改变采购项的状态
        //标志采购单是否成功，如果一项采购项失败，则采购单失败
        Boolean flag = true;
        List<PurchaseItemDoneVO> items = purchaseDoneVO.getItems();
        List<PurchaseDetailEntity> detailEntities = new ArrayList<>();
        for (PurchaseItemDoneVO item:items){
            PurchaseDetailEntity entity = new PurchaseDetailEntity();
            if (item.getStatus()==WareConstant.PurchaseDetailStatusEnum.HAVEERROR.getCode()){
                flag=false;
                entity.setStatus(item.getStatus());
            }else{
                //视频实现方式：entity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISHED.getCode());
                entity.setStatus(item.getStatus());
                //3.将采购成功的进行入库操作
                //以下数据从采购需求中获取，id：itemid
                PurchaseDetailEntity purchaseDetail = purchaseDetailService.getById(item.getItemId());
                Long skuId = purchaseDetail.getSkuId();
                Long wareId = purchaseDetail.getWareId();
                Integer skuNum = purchaseDetail.getSkuNum();

                /**
                 * 传递参数：sku_id,仓库id，商品数量
                 */
                wareSkuService.addStocks(skuId,wareId,skuNum);

            }
            detailEntities.add(entity);
        }
        purchaseDetailService.saveBatch(detailEntities);
        //2.改变采购单的状态
        Long id = purchaseDoneVO.getId();
        PurchaseEntity purchase = new PurchaseEntity();
        purchase.setId(id);
        if (flag){
            purchase.setStatus(WareConstant.PurchaseStatusEnum.FINISHED.getCode());
        }else {
            purchase.setStatus(WareConstant.PurchaseStatusEnum.FAILED.getCode());
        }
        this.updateById(purchase);
    }
}