package com.yama.mall.auth.feign;

import com.yama.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 第三方远程调用接口
 */
@FeignClient("gulimall-third-party")
public interface ThirdPartyFeignService {
    /**
     * 发送短信验证码
     * @param phone     目标手机号
     * @param verityCode    发送的验证码
     * @param minute    验证码有效时间
     * @return
     */
    @GetMapping("/sms/sendcode")
    R sendShortMessageCode(
            @RequestParam("phone") String phone,
            @RequestParam("verityCode")String verityCode,
            @RequestParam("minute")String minute);
}
