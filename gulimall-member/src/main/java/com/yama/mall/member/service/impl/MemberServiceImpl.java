package com.yama.mall.member.service.impl;

import com.yama.mall.member.dao.MemberLevelDao;
import com.yama.mall.member.entity.MemberLevelEntity;
import com.yama.mall.member.exception.PhoneExistException;
import com.yama.mall.member.exception.UserNameException;
import com.yama.mall.member.vo.MemberLoginVO;
import com.yama.mall.member.vo.MemberRegisterVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yama.mall.common.utils.PageUtils;
import com.yama.mall.common.utils.Query;

import com.yama.mall.member.dao.MemberDao;
import com.yama.mall.member.entity.MemberEntity;
import com.yama.mall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {
    @Autowired
    private MemberLevelDao memberLevelDao;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 会员注册功能
     * @param vo
     */
    @Override
    public void regist(MemberRegisterVO vo) {
        MemberDao memberDao = this.getBaseMapper();
        MemberEntity memberEntity = new MemberEntity();

        //设置默认等级
        MemberLevelEntity defalutLevel = memberLevelDao.getDefaultLevel();
        memberEntity.setLevelId(defalutLevel.getId());

        //保存手机号,用户名进行校验唯一性保存
        checkPhoneUnique(vo.getPhone());
        checkUserNameUnique(vo.getUserName());

        memberEntity.setMobile(vo.getPhone());
        memberEntity.setUsername(vo.getUserName());
        //密码进行盐值加密保存
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encode = bCryptPasswordEncoder.encode(vo.getPassword());
        memberEntity.setPassword(encode);

        //TODO 设置用户其他默认信息


        //保存
        memberDao.insert(memberEntity);
    }

    /**
     * 检查电话号是否唯一
     * @param phone
     * @throws PhoneExistException
     */
    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException {
        MemberDao memberDao = this.baseMapper;
        Integer count = memberDao.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (count > 0){
            throw new PhoneExistException();
        }
    }

    /**
     * 检查用户名是否唯一
     * @param userName
     * @throws UserNameException
     */
    @Override
    public void checkUserNameUnique(String userName) throws UserNameException {
        MemberDao memberDao = this.baseMapper;
        Integer count = memberDao.selectCount(new QueryWrapper<MemberEntity>().eq("username", userName));
        if (count > 0){
            throw new UserNameException();
        }
    }

    /**
     * 用户登陆校验
     * @param vo
     * @return
     */
    @Override
    public MemberEntity login(MemberLoginVO vo) {
        String loginacct = vo.getLoginacct();
        String password = vo.getPassword();
        MemberDao memberDao = this.getBaseMapper();
        MemberEntity entity = memberDao.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginacct).or().eq("mobile", loginacct));
        if (entity==null){
            //用户不存在
            return null;
        }else{
            //用户存在进行密码校验
            String passwordDb = entity.getPassword();
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            boolean matches = bCryptPasswordEncoder.matches(password, passwordDb);
            if (matches){
                //密码校验成功
                return entity;
            }else {
                //密码校验失败
                return null;
            }
        }
    }
}