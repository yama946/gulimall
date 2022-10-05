package com.yama.mall.order.config;

import com.yama.mall.order.interceptor.LoginUserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @description:
 * @date: 2022年10月04日 周二 18:17
 * @author: yama946
 */
@Configuration
public class OrderWebConfiguration implements WebMvcConfigurer {

    @Autowired
    private LoginUserInterceptor loginUserInterceptor;

    /**
     * 添加拦截器配置
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //本项目中任何请求都需要经过拦截器
        registry.addInterceptor(loginUserInterceptor).addPathPatterns("/**").excludePathPatterns("/test/**");

    }
}
