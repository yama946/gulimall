package com.yama.mall.auth.feign;

import com.yama.mall.auth.vo.SoicalUserVO;
import com.yama.mall.auth.vo.UserLoginVO;
import com.yama.mall.auth.vo.UserRegistVO;
import com.yama.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-member")
public interface MemberFeignService {
    @PostMapping("/member/member/register")
    R register(@RequestBody UserRegistVO vos);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVO vo);

    @PostMapping("/member/member/auth2/login")
    R oauth2Login(@RequestBody SoicalUserVO userVO);
}
