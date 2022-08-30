package com.yama.mall.member.dao;

import com.yama.mall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author yanmu
 * @email yanmu123@gmail.com
 * @date 2022-08-30 18:01:52
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
