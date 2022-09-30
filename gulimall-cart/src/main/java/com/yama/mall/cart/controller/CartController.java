package com.yama.mall.cart.controller;

import com.yama.mall.cart.interceptor.CartInterceptor;
import com.yama.mall.cart.vo.UserInfoTo;
import com.yama.mall.common.constant.AuthServerConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;

/**
 * 问题点：没登陆，即使浏览器关闭后依然可以看到没登录时添加的购物车商品
 * 实现思路：
 *      通过cookie中的user-key：c40030e6-d051-479e-a64b-e0bde56e4d1c表示一个用户身份实现
 *      如果第一次使用购物成功能，都会给一个临时的用户身份
 *      浏览器保存cookie，每次访问都会带上这个cookie
 *拦截器实现
 *      登陆：session有，
 *      没登陆：获取cookie中的user-key进行操作
 *      第一次：没有临时用户，帮忙创建一个临时用户
 */
@Slf4j
@Controller
public class CartController {
    /**
     * 打开购物成列表页
     * @return
     */
    @GetMapping("/cart.html")
    public String cartListPage(HttpSession session){
        //获取ThreadLocal中的数据
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        log.info("threadLocal中的数据为:{}",userInfoTo);


        return "cartList";
    }
}
