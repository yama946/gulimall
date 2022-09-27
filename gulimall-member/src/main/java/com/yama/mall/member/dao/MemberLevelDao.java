package com.yama.mall.member.dao;

import com.yama.mall.member.entity.MemberLevelEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 会员等级
 * 
 * @author yanmu
 * @email yanmu123@gmail.com
 * @date 2022-08-30 18:01:52
 */
@Repository
@Mapper
public interface MemberLevelDao extends BaseMapper<MemberLevelEntity> {

    MemberLevelEntity getDefaultLevel();

}
