package com.yama.mall.member.service.impl;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yama.mall.common.utils.PageUtils;
import com.yama.mall.common.utils.Query;

import com.yama.mall.member.dao.MemberReceiveAddressDao;
import com.yama.mall.member.entity.MemberReceiveAddressEntity;
import com.yama.mall.member.service.MemberReceiveAddressService;


@Service("memberReceiveAddressService")
public class MemberReceiveAddressServiceImpl extends ServiceImpl<MemberReceiveAddressDao, MemberReceiveAddressEntity> implements MemberReceiveAddressService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberReceiveAddressEntity> page = this.page(
                new Query<MemberReceiveAddressEntity>().getPage(params),
                new QueryWrapper<MemberReceiveAddressEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 获取用户地址
     * @return
     * @param memeberId
     */
    @Override
    public List<MemberReceiveAddressEntity> getAddress(Long memeberId) {
        List<MemberReceiveAddressEntity> memberAddresses = this.list(new QueryWrapper<MemberReceiveAddressEntity>().eq("member_id", memeberId));
        return memberAddresses;
    }
}