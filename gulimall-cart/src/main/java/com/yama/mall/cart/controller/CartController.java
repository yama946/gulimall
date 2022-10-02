package com.yama.mall.cart.controller;

import com.yama.mall.cart.interceptor.CartInterceptor;
import com.yama.mall.cart.service.CartService;
import com.yama.mall.cart.vo.CartItemVo;
import com.yama.mall.cart.vo.UserInfoTo;
import com.yama.mall.common.constant.AuthServerConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.concurrent.ExecutionException;

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

    @Autowired
    private CartService cartService;

    /**
     * 打开购物车列表页
     * @return
     */
    @GetMapping("/cart.html")
    public String cartListPage(HttpSession session){
        //获取ThreadLocal中的数据
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        log.info("threadLocal中的数据为:{}",userInfoTo);


        return "cartList";
    }

    /**
     * 添加商品到购物车
     * TODO RedirectAttributes的使用
     *      1.addFlashAttribute(): 将数据放到session域中可以从页面中取出，而且只能取一次
     *      2.addAttribute()：将数据会放到重定向请求的请求参数中进行携带
     * @param skuId 商品id
     * @param num   商品数量
     * @return
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num,
                            RedirectAttributes redirectAttributes) throws ExecutionException, InterruptedException {
        CartItemVo cartItem = cartService.addToCart(skuId,num);
//        model.addAttribute("cartItem",cartItem);
        //重定向，并携带参数----->GET  http://cart.gulimall.com/addToCartSuccess.html?skuId=xx
        redirectAttributes.addAttribute("skuId",skuId);

        return "redirect:http://cart.gulimall.com/addToCartSuccess.html";
    }

    /**
     * 防止刷新页面，重复添加商品信息
     * 思路：
     *      重定向，携带参数skuId，再从redis中查询到商品信息封装返回
     * @return
     */
    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId,Model model){
        CartItemVo cartItem = cartService.getCartItem(skuId);
        model.addAttribute("cartItem",cartItem);
        return "success";
    }
}
