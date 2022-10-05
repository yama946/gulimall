package com.yama.mall.order.service.impl;

import com.yama.mall.common.vo.MemberEntityVO;
import com.yama.mall.order.feign.CartFeignService;
import com.yama.mall.order.feign.MemberFeignService;
import com.yama.mall.order.interceptor.LoginUserInterceptor;
import com.yama.mall.order.vo.MemberAddressVO;
import com.yama.mall.order.vo.OrderConfirmVO;
import com.yama.mall.order.vo.OrderItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yama.mall.common.utils.PageUtils;
import com.yama.mall.common.utils.Query;

import com.yama.mall.order.dao.OrderDao;
import com.yama.mall.order.entity.OrderEntity;
import com.yama.mall.order.service.OrderService;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    //导入用户远程接口调用对象
    @Autowired
    private MemberFeignService memberFeignService;

    //注入购物车远程调用接口对象
    @Autowired
    private CartFeignService cartFeignService;

    //注入连接池
    @Autowired
    private ThreadPoolExecutor executor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 获取用户订单信息
     * TODO 多为异步查询，使用异步编排优化
     * @return
     */
    @Override
    public OrderConfirmVO confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVO orderConfirmVO = new OrderConfirmVO();
        //获取登陆用户信息
        MemberEntityVO loginUserInfo = LoginUserInterceptor.threadLocal.get();
        Long memberId = loginUserInfo.getId();


        //TODO :获取当前线程请求头信息(解决Feign异步调用丢失请求头问题)
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        //TODO 异步模式下，远程过程调用导致请求头丢失
        CompletableFuture<Void> getAddresses = CompletableFuture.runAsync(() -> {
            //每一个线程都来共享之前的请求数据
            RequestContextHolder.setRequestAttributes(requestAttributes);

            //1.ums_member_receive_address表中获取用户收货地址信息
            List<MemberAddressVO> addresses = memberFeignService.getAddress(memberId);
            orderConfirmVO.setAddress(addresses);
        }, executor);

        CompletableFuture<Void> getOrderItems = CompletableFuture.runAsync(() -> {
            //每一个线程都来共享之前的请求数据
            RequestContextHolder.setRequestAttributes(requestAttributes);

            //2.获取用户勾选的商品信息
            //TODO 远程调用cart拦截器使用请求头中sessionId获取session，但是远程调用请求未携带请求头
            //Feign远程调用前进行构造请求，需要调用多个RequestInterceptor拦截器机型增强请求
            List<OrderItemVO> orderItems = cartFeignService.getconcurrentUserCartItems();
            orderConfirmVO.setItems(orderItems);
        }, executor);

        //3.查询用户积分信息
        Integer integration = loginUserInfo.getIntegration();
        orderConfirmVO.setIntegration(integration);

        //4.其他信息自动查询

        //TODO 5.防重令牌------>接口幂等性处理


        CompletableFuture.allOf(getAddresses,getOrderItems).get();
        return orderConfirmVO;
    }


}