package com.yama.mall.cart.config;

import com.yama.mall.cart.interceptor.CartInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 配置拦截器的的拦截路径
 */
public class GulimallWebConfig implements WebMvcConfigurer {
    /**
     * 拦截器配置方法
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        /**
         * addPathPatterns配置拦截路径
         */
        registry.addInterceptor(new CartInterceptor()).addPathPatterns("/**");
    }
}
