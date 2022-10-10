package com.yama.mall.order.web;

import com.alipay.api.AlipayApiException;
import com.yama.mall.order.config.AlipayTemplate;
import com.yama.mall.order.service.OrderService;
import com.yama.mall.order.vo.PayVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @description: 支付请求处理
 * @date: 2022年10月10日 周一 21:19
 * @author: yama946
 */
@Slf4j
@Controller
public class PayWebController {
    @Autowired
    private AlipayTemplate alipayTemplate;

    @Autowired
    private OrderService orderService;

    @ResponseBody
    @GetMapping(value = "/pay/order",produces = "text/html")
    public String payOrder(@RequestParam("ordersn") String orderSn) throws AlipayApiException {
        /**
         *         PayVo payVo = new PayVo();
         *         payVo.setBody();
         *         payVo.setOut_trade_no();
         *         payVo.setSubject();
         *         payVo.setTotal_amount();
         */
        //通过orderServic进行查询构造PayVo对象
        PayVo payVo = orderService.getOrderPayVo(orderSn);
        String payResult = alipayTemplate.pay(payVo);
        log.info("支付宝支付请求返回值：{}",payResult);
        return payResult;
    }
}
