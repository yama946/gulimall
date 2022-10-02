package com.yama.mall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.yama.mall.cart.feign.ProductFeignService;
import com.yama.mall.cart.interceptor.CartInterceptor;
import com.yama.mall.cart.service.CartService;
import com.yama.mall.cart.vo.CartItemVo;
import com.yama.mall.cart.vo.SkuInfoVO;
import com.yama.mall.cart.vo.UserInfoTo;
import com.yama.mall.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 购物车信息保存在redis中
 */
@Slf4j
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    //定义用户购物车的键名前缀
    private final String  CART_PREFIX="gulimall:cart:";

    /**
     * 添加商品到购物车
     * @param skuId
     * @param num
     * @return
     * TODO 使用异步编排优化远程查询过程
     */
    @Override
    public CartItemVo addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        /**
         * 细节思路：
         *      添加商品到购物车前，判断是否存在改商品，如果存在则修改商品数量，不存在才执行新添加商品操作
         */
        //1.获取绑定的操作对象
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        //*.从redis中获取要添加商品是否存在
        String product = (String) cartOps.get(skuId.toString());
        if (product==null){
            CartItemVo cartItemVo = new CartItemVo();
            //线程串行化执行任务1
            CompletableFuture<Void> getskuInfoTask = CompletableFuture.runAsync(() -> {
                //2.远程查询出要添加的购物项商品信息
                R info = productFeignService.getSkuInfo(skuId);
                SkuInfoVO skuInfo = info.getData("skuInfo", new TypeReference<SkuInfoVO>() {
                });
                cartItemVo.setCheck(true);
                cartItemVo.setCount(num);
                cartItemVo.setSkuId(skuId);
                cartItemVo.setImage(skuInfo.getSkuDefaultImg());
                cartItemVo.setPrice(skuInfo.getPrice());
                cartItemVo.setTitle(skuInfo.getSkuTitle());
            }, threadPoolExecutor);
            CompletableFuture<Void> getSaleAttrTask = CompletableFuture.runAsync(() -> {
                //3.远程查询sku的销售属性组合信息
                List<String> skuSaleAttrValues = productFeignService.getSkuSaleAttrValues(skuId);
                cartItemVo.setSkuAttrValues(skuSaleAttrValues);
            }, threadPoolExecutor);
            //数据转换前确保线程执行结束
            CompletableFuture.allOf(getskuInfoTask,getSaleAttrTask).get();
            //保存数据前将对象转为json
            String result = JSON.toJSONString(cartItemVo);
            //将将信息存放到redis中
            cartOps.put(skuId.toString(),result);

            return cartItemVo;
        }else {
            //转换成购物项，并修改数量参数
            CartItemVo cartItem = JSON.parseObject(product, CartItemVo.class);
            //重置商品数量
            cartItem.setCount(cartItem.getCount()+num);
            //保存数据前将对象转为json
            String result = JSON.toJSONString(cartItem);
            //重新保存商品到redis中
            cartOps.put(skuId.toString(),result);
            return cartItem;
        }
    }

    /**
     * 绑定获取操作hash的redis对象
     * @return
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        //1.获取用户的基础数据，判断是否登陆
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        //2.用户购物车键名
        String cartKey = "";
        if (userInfoTo.getUserId()==null){
            //临时用户键名：前缀+user-key
            cartKey =  CART_PREFIX+userInfoTo.getUserKey();
        }else {
            //非临时用户：前缀+用户id
            cartKey = CART_PREFIX+userInfoTo.getUserId();
        }
        //3.当定操作指定键名的redis操作对象
        BoundHashOperations<String, Object, Object> cartHashOps = stringRedisTemplate.boundHashOps(cartKey);
        return cartHashOps;
    }
}
