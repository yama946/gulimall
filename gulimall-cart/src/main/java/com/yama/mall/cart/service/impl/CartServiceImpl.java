package com.yama.mall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.yama.mall.cart.feign.ProductFeignService;
import com.yama.mall.cart.interceptor.CartInterceptor;
import com.yama.mall.cart.service.CartService;
import com.yama.mall.cart.vo.CartItemVo;
import com.yama.mall.cart.vo.CartVo;
import com.yama.mall.cart.vo.SkuInfoVO;
import com.yama.mall.cart.to.UserInfoTo;
import com.yama.mall.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

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
     * 从redis中获取当前添加到购物车单个商品信息
     * @param skuId
     * @return
     */
    @Override
    public CartItemVo getCartItem(Long skuId) {
        //获取当前用户购物车操作对象
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String cartItemJson = (String) cartOps.get(skuId.toString());
        CartItemVo cartItem = JSON.parseObject(cartItemJson, CartItemVo.class);
        return cartItem;
    }

    /**
     * 获取购物车
     * @return
     */
    @Override
    public CartVo getCart() throws ExecutionException, InterruptedException{
        CartVo cart = new CartVo();
        //1.判断用户是临时用户还是登陆用户
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId()!= null){
            //1、登陆用户
            String loginCartKey = CART_PREFIX + userInfoTo.getUserId();
            String tempCartKey = CART_PREFIX + userInfoTo.getUserKey();
            //2、如果临时购物车数据还没有合并，则进行合并，并临时购物车----->构造方法，获取购物车的所有购物项
            List<CartItemVo> tempCartItems = getCartItems(tempCartKey);
            if (tempCartItems != null && tempCartItems.size()>0){
                //临时购物车中存在购物数据
                for (CartItemVo item : tempCartItems) {
                    addToCart(item.getSkuId(), item.getCount());
                }
                /*tempCartItems.forEach(item->{
                    try {
                        addToCart(item.getSkuId(), item.getCount());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });*/
                //清除临时购物车
                clearCart(tempCartKey);
            }
            //获取登陆后用户的购物车数据
            List<CartItemVo> loginCartItems = getCartItems(loginCartKey);
            cart.setItems(loginCartItems);
        }else {
            //1、临时用户,未登录
            String cartRedisKey = CART_PREFIX + userInfoTo.getUserKey();
            List<CartItemVo> tempCartItems = getCartItems(cartRedisKey);
            cart.setItems(tempCartItems);
        }
        return cart;
    }

    /**
     * 清除购物车
     * @param tempCartKey
     */
    @Override
    public void clearCart(String tempCartKey) {
        stringRedisTemplate.delete(tempCartKey);
    }

    /**
     * 勾选购物项
     * @param skuId
     * @param checked
     */
    @Override
    public void checkCartItem(Long skuId, Integer checked) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        //获取当前操作的购物项
        CartItemVo cartItem = getCartItem(skuId);
        //修改购物项的状态
        cartItem.setCheck(checked==1?true:false);
        //将修改后对象重新保存到redis中
        String cartItemJson = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(),cartItemJson);
    }

    /**
     * 修改商品数量
     * @param skuId
     * @param num
     */
    @Override
    public void changeItemCount(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        //获取当前操作的购物项
        CartItemVo cartItem = getCartItem(skuId);
        //修改购物项的状态
        cartItem.setCount(num);
        //将修改后对象重新保存到redis中
        String cartItemJson = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(),cartItemJson);
    }

    /**
     * 删除商品项，并刷新页面
     * @param skuId
     */
    @Override
    public void deleteCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }

    /**
     * 远程接口方法：获取当前用户选中的的购物项
     * @return
     */
    @Override
    public List<CartItemVo> getUserCartItems() {
        //ThreadLocal中获取用户信息
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId()==null){
            //用户登陆异常
            return null;
        }else {
            //用户登陆成功
            String loginCartKey = CART_PREFIX + userInfoTo.getUserId();
            //获取所有购物项
            List<CartItemVo> cartItems = getCartItems(loginCartKey);
            //获取所有选中的购物项
            List<CartItemVo> checkedCartItems = cartItems.stream().filter(item -> item.getCheck())
                    .map(item->{
                        /**
                         * TODO 如何同步购物车中的商品价格？？？？
                         * 我们是从购物车中获取数据，如果购物车添加后，如果一段时间后，商品价格变化并没有同步到购物车中
                         * 则提交的订单价格需要重新查询，远程获取。
                         */
                        //TODO  debug--> BigDecimal转码异常，改变返回类型为R，获取对象为String
                        BigDecimal itemPrice = productFeignService.getPrice(item.getSkuId());
                        item.setPrice(itemPrice);
                        return item;
                    })
                    .collect(Collectors.toList());
            return checkedCartItems;
        }
    }

    /**
     * 绑定当前状态可以操作的购物车redis对象
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

    /**
     * 获取购物车中所有购物项
     * @param cartRedisKey
     * @return
     */
    public List<CartItemVo> getCartItems(String cartRedisKey){
        BoundHashOperations<String, Object, Object> hashOps = stringRedisTemplate.boundHashOps(cartRedisKey);
        //2、获取hash结构的value值集合
        List<Object> cartItems = hashOps.values();
        if (cartItems!=null && cartItems.size() >0){
            //将购物项的Object进行转型
            List<CartItemVo> collect = cartItems.stream().map(item -> {
                String itemJson = (String)item;
                CartItemVo cartItemVo = JSON.parseObject(itemJson, CartItemVo.class);
                return cartItemVo;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }
}
