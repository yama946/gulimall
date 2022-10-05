package com.yama.mall.order.interceptor;

import com.yama.mall.common.constant.AuthServerConstant;
import com.yama.mall.common.vo.MemberEntityVO;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @description:
 * @date: 2022年10月04日 周二 18:16
 * @author: yama946
 */
@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    //用户数据共享，ThreadLocal使用
    public static ThreadLocal<MemberEntityVO> threadLocal = new ThreadLocal<>();

    /**
     * contrller执行前执行
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1.判断当前请求是否登陆，获取session
        MemberEntityVO attribute = (MemberEntityVO) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
        if (attribute!=null){
            //用户登陆--->ThreadLocal中存放用户并放行
            threadLocal.set(attribute);
            return true;
        }else {
            //用户未登陆---->重定向到登陆页面
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return false;
        }
    }
}
