package com.yama.mall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yama.mall.common.utils.PageUtils;
import com.yama.mall.common.utils.Query;
import com.yama.mall.common.utils.R;
import com.yama.mall.ware.dao.WareInfoDao;
import com.yama.mall.ware.entity.WareInfoEntity;
import com.yama.mall.ware.feign.MemberFeignService;
import com.yama.mall.ware.service.WareInfoService;
import com.yama.mall.ware.vo.FareVO;
import com.yama.mall.ware.vo.MemberAddressInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Map;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    private MemberFeignService memberFeignService;

    /**
     * 添加关键词查询功能，完善方法
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareInfoEntity> queryWrapper = new QueryWrapper<>();

        String key = (String) params.get("key");

        if (!StringUtils.isEmpty(key)){
            queryWrapper.eq("id",key).or().like("name",key)
                        .or().like("address",key).or().like("areacode",key);
        }

        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    /**
     * 根据收获地址查询运费信息
     * @param attrId
     * @return
     */
    @Override
    public FareVO getFare(Long attrId) {
        //TODO 远程调用用户系统查询收货地址
        R addrInfo = memberFeignService.getInfo(attrId);
        FareVO fareInfo = new FareVO();
        MemberAddressInfoVO memberAddressInfo = addrInfo.getData("memberReceiveAddress", new TypeReference<MemberAddressInfoVO>() {});
        String detailAddress = memberAddressInfo.getDetailAddress();
        //TODO 简单固定运费，不进行业务复杂操作
        if (detailAddress!=null){
            BigDecimal fare = new BigDecimal("18");
            fareInfo.setAddress(memberAddressInfo);
            fareInfo.setFare(fare);
            return fareInfo;
        }
        return null;
    }

}