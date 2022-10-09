package com.yama.mall.ware.feign;

import com.yama.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 1.远程查询收获地址
 * @description: 用户远程接口
 * @date: 2022/10/6 10:30
 * @author: yama946
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {

    @RequestMapping("/member/memberreceiveaddress/info/{id}")
    R getInfo(@PathVariable("id") Long id);
}
