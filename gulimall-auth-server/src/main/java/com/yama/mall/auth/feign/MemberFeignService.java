package com.yama.mall.auth.feign;

import com.yama.mall.auth.vo.UserLoginVO;
import com.yama.mall.auth.vo.UserRegistVO;
import com.yama.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-member")
public interface MemberFeignService {
    @PostMapping("/member/member/register")
    R register(@RequestBody UserRegistVO vos);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVO vo);
}
