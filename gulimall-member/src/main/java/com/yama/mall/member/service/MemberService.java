package com.yama.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yama.mall.common.utils.PageUtils;
import com.yama.mall.member.entity.MemberEntity;
import com.yama.mall.member.exception.PhoneExistException;
import com.yama.mall.member.exception.UserNameException;
import com.yama.mall.member.vo.MemberLoginVO;
import com.yama.mall.member.vo.MemberRegisterVO;

import java.util.Map;

/**
 * 会员
 *
 * @author yanmu
 * @email yanmu123@gmail.com
 * @date 2022-08-30 18:01:52
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void regist(MemberRegisterVO vo);

    void checkPhoneUnique(String phone) throws PhoneExistException;

    void checkUserNameUnique(String userName) throws UserNameException;

    MemberEntity login(MemberLoginVO vo);
}

