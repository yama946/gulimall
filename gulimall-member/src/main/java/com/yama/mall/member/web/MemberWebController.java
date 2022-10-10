package com.yama.mall.member.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @description: 支付宝回调请求处理
 * @date: 2022年10月10日 周一 22:10
 * @author: yama946
 */
@Controller
public class MemberWebController {
    /**
     * 支付宝同步回调请求处理
     * @return
     */
    @GetMapping("memberOrder.html")
    public String memberOrderPage(){
        //查询当前登陆用户所有订单列表数据
        return "orderList";
    }
}
