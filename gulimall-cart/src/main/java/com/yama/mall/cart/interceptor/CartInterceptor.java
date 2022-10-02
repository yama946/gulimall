package com.yama.mall.cart.interceptor;

import com.yama.mall.cart.to.UserInfoTo;
import com.yama.mall.common.constant.AuthServerConstant;
import com.yama.mall.common.constant.CartConstant;
import com.yama.mall.common.vo.MemberEntityVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Objects;
import java.util.UUID;

/**
 * 在执行目标方法之前，拦截请求判断登陆状态。封装信息传递给controller方法
 */
@Component
public class CartInterceptor implements HandlerInterceptor {
    //TODO 使用ThreadLocal实现线程上的数据共享
    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();
    /**
     * 目标执行前执行的拦截方法
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {
        UserInfoTo userInfoTo = new UserInfoTo();
        HttpSession session = request.getSession();
        //1.判断是否登陆
        MemberEntityVO loginUser = (MemberEntityVO)session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (loginUser!=null){
            //用户已登录
            userInfoTo.setUserId(loginUser.getId());
        }
        //cookie中获取user-key键值
        Cookie[] cookies = request.getCookies();
        if(cookies!=null){
            for (Cookie cookie:cookies){
                String name = cookie.getName();
                if(Objects.equals(name, CartConstant.TEMP_USER_COOKIE_NAME)){
                    userInfoTo.setUserKey(cookie.getValue());
                    //标识是否存在user-key值
                    userInfoTo.setTempUser(true);
                }
            }
        }
        //判断，如果第一次使用，则创建user-key
        if (StringUtils.isEmpty(userInfoTo.getUserKey())){
            String uuid = UUID.randomUUID().toString();
            userInfoTo.setUserKey(uuid);
        }
        //放行前将数据放到线程上
        threadLocal.set(userInfoTo);
        //放行
        return true;
    }

    /**
     * controller执行后拦截方法
     * controller响应后，需要在cookie中设置user-key并保存一个月
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserInfoTo userInfoTo = threadLocal.get();
        if (!userInfoTo.isTempUser()){
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoTo.getUserKey());
            //设置作用域
            cookie.setDomain(CartConstant.TEMP_USER_COOKIE_DOMAIN);
            //设置超时时间
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }
    }
}
