package com.yama.mall.order.feign;

import com.yama.mall.order.vo.MemberAddressVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @description: 调用Member的远程接口
 * @date: 2022/10/5 9:49
 * @author: yama946
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {

    @GetMapping("member/memberreceiveaddress/{memeberId}/addresses")
    List<MemberAddressVO> getAddress(@PathVariable("memberId") Long memeberId);
}
