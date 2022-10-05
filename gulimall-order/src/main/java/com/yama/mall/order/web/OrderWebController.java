package com.yama.mall.order.web;

import com.yama.mall.order.service.OrderService;
import com.yama.mall.order.vo.OrderConfirmVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.concurrent.ExecutionException;

/**
 * @description:
 * @date: 2022年10月04日 周二 18:12
 * @author: yama946
 */
@Controller
public class OrderWebController {
    @Autowired
    private OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        //获取订单信息
        OrderConfirmVO orderConfirmVO= orderService.confirmOrder();

        //保存到请求域中，进行页面展示
        model.addAttribute("confirmOrderData",orderConfirmVO);
        //用户订单信息展示
        return "confirm";
    }
}
