package com.yama.mall.coupon.service.impl;

import com.yama.mall.common.to.MemberPrice;
import com.yama.mall.common.to.SkuReductionTO;
import com.yama.mall.coupon.entity.MemberPriceEntity;
import com.yama.mall.coupon.entity.SkuLadderEntity;
import com.yama.mall.coupon.service.MemberPriceService;
import com.yama.mall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yama.mall.common.utils.PageUtils;
import com.yama.mall.common.utils.Query;

import com.yama.mall.coupon.dao.SkuFullReductionDao;
import com.yama.mall.coupon.entity.SkuFullReductionEntity;
import com.yama.mall.coupon.service.SkuFullReductionService;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    private SkuLadderService skuLadderService;

    @Autowired
    private MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 远程调用接口方法
     * 保存满减打折信息
     * sku的优惠、满减等信息：gulimall_sms->sms_sku_ladder\sms_sku_full_reduction\sms_member_price
     * @param skuReductionTO
     */
    @Override
    public void saveSkuReduction(SkuReductionTO skuReductionTO) {
        //保存商品阶梯价格
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        skuLadderEntity.setSkuId(skuReductionTO.getSkuId());
        skuLadderEntity.setFullCount(skuLadderEntity.getFullCount());
        skuLadderEntity.setDiscount(skuReductionTO.getDiscount());
        skuLadderEntity.setAddOther(skuReductionTO.getCountStatus());
        skuLadderService.save(skuLadderEntity);
        //保存商品的满减信息
        SkuFullReductionEntity skuFullReduction = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuReductionTO,skuFullReduction);
        if (skuFullReduction.getFullPrice().compareTo(new BigDecimal("0"))==1){
            this.save(skuFullReduction);
        }
        //保存商品会员价格信息
        List<MemberPrice> memberPrices = skuReductionTO.getMemberPrice();
        List<MemberPriceEntity> collect = memberPrices.stream().map(item -> {
            MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
            memberPriceEntity.setSkuId(skuReductionTO.getSkuId());
            memberPriceEntity.setMemberLevelId(item.getId());
            memberPriceEntity.setMemberLevelName(item.getName());
            memberPriceEntity.setMemberPrice(item.getPrice());
            memberPriceEntity.setAddOther(1);
            return memberPriceEntity;
        }).filter(t-> t.getMemberPrice().compareTo(new BigDecimal("0"))==1).collect(Collectors.toList());

        memberPriceService.saveBatch(collect);

    }

}