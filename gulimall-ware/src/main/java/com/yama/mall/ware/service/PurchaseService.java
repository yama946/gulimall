package com.yama.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yama.mall.common.utils.PageUtils;
import com.yama.mall.ware.entity.PurchaseEntity;
import com.yama.mall.ware.vo.MergeVO;
import com.yama.mall.ware.vo.PurchaseDoneVO;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author yanmu
 * @email yanmu123@gmail.com
 * @date 2022-08-30 18:17:04
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnreceiveList(Map<String, Object> params);

    void mergePurchase(MergeVO mergeVO);

    void receivedPurchase(List<Long> ids);

    void donePurchase(PurchaseDoneVO purchaseDoneVO);

}

