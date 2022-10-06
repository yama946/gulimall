package com.yama.mall.order.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @description: 测试页面是否可以正常访问，静态资源是否正常加载
 * @date: 2022年10月04日 周二 16:39
 * @author: yama946
 */
@Controller
public class HelloControllerTest {
    @GetMapping("test/{page}.html")
    public String listPage(@PathVariable("page") String page){
        return page;
    }
}
