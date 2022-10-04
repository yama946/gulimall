package com.yama.mall.order.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @description:
 * @date: 2022年10月04日 周二 16:39
 * @author: yama946
 */
@Controller
public class HelloController {
    @GetMapping("/{page}.html")
    public String listPage(@PathVariable("page") String page){
        return page;
    }
}
