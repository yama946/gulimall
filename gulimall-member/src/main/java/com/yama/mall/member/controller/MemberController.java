package com.yama.mall.member.controller;

import java.util.Arrays;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.yama.mall.common.exception.BizCodeEnume;
import com.yama.mall.member.exception.PhoneExistException;
import com.yama.mall.member.exception.UserNameException;
import com.yama.mall.member.vo.MemberLoginVO;
import com.yama.mall.member.vo.MemberRegisterVO;
import com.yama.mall.member.vo.SoicalUserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.yama.mall.member.entity.MemberEntity;
import com.yama.mall.member.service.MemberService;
import com.yama.mall.common.utils.PageUtils;
import com.yama.mall.common.utils.R;



/**
 * 会员
 *
 * @author yanmu
 * @email yanmu123@gmail.com
 * @date 2022-08-30 18:01:52
 */
@RestController
@RequestMapping("member/member")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @PostMapping("/auth2/login")
    public R oauth2Login(@RequestBody SoicalUserVO userVO){
        MemberEntity entity = memberService.oauth2Login(userVO);
        if (entity!=null){
            return R.ok().setData(entity);
        }else {
            return R.error(BizCodeEnume.LOGINACCT_PASSWORD_INVALID_EXCEPITON.getCode(),BizCodeEnume.LOGINACCT_PASSWORD_INVALID_EXCEPITON.getMsg());
        }
    }


    /**
     * 远程调用接口，注册保存会员信息
     * @param vo
     * @return
     */
    @PostMapping("/register")
    public R register(@RequestBody MemberRegisterVO vo){
        try{
            memberService.regist(vo);
        }catch (PhoneExistException e){
            R.error(BizCodeEnume.PHONE_EXIST_EXCEPTION.getCode(),BizCodeEnume.PHONE_EXIST_EXCEPTION.getMsg());
        }catch (UserNameException e){
            R.error(BizCodeEnume.USERNAME_EXIST_EXCEPTION.getCode(),BizCodeEnume.USERNAME_EXIST_EXCEPTION.getMsg());
        }
        return R.ok();
    }

    /**
     * 远程用户登陆接口
     * @param vo
     * @return
     */
    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVO vo){
        MemberEntity entity = memberService.login(vo);
        if (entity!=null){
            return R.ok().setData(entity);
        }else {
            return R.error(BizCodeEnume.LOGINACCT_PASSWORD_INVALID_EXCEPITON.getCode(),BizCodeEnume.LOGINACCT_PASSWORD_INVALID_EXCEPITON.getMsg());
        }
    }



    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
