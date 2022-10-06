package com.yama.mall.order.web;

import com.yama.mall.order.service.OrderService;
import com.yama.mall.order.vo.OrderConfirmVO;
import com.yama.mall.order.vo.OrderSubmitVO;
import com.yama.mall.order.vo.SubmitOrderResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.concurrent.ExecutionException;

/**
 * @description:
 * @date: 2022年10月04日 周二 18:12
 * @author: yama946
 */
@Slf4j
@Controller
public class OrderWebController {
    @Autowired
    private OrderService orderService;

    /**
     * 获取订单信息
     * @param model
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        //获取订单信息
        OrderConfirmVO orderConfirmVO= orderService.confirmOrder();

        //保存到请求域中，进行页面展示
        model.addAttribute("orderConfirmData",orderConfirmVO);
        //用户订单信息展示
        return "confirm";
    }

    /**
     * 提交订单请求处理--->下单功能
     * @return
     */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVO vo,Model model){
        log.info("订单提交的数据为:{}",vo);
        /**
         * 一系列的操作：创建订单，防重令牌校验，校验价格，锁定库存
         * 成功：重定向到支付选择页
         * 失败：返回订单确认页，重新确认订单
         */
        SubmitOrderResponseVO submitOrderResponse = orderService.submitOrder(vo);
        if (submitOrderResponse.getCode()==0){
            //成功：重定向到支付选择页
            model.addAttribute("submitOrderResp",submitOrderResponse);
            return "pay";
        }else {
            //失败：返回订单确认页，重新确认订单
            return "redirect:http://order.gulimall.com/toTrade";
        }
    }
}
