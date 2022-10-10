package com.yama.mall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yama.mall.common.exception.NoStockException;
import com.yama.mall.common.to.OrderTo;
import com.yama.mall.common.utils.PageUtils;
import com.yama.mall.common.utils.Query;
import com.yama.mall.common.utils.R;
import com.yama.mall.common.vo.MemberEntityVO;
import com.yama.mall.order.canstant.OrderConstant;
import com.yama.mall.order.canstant.RabbitConstant;
import com.yama.mall.order.dao.OrderDao;
import com.yama.mall.order.entity.OrderEntity;
import com.yama.mall.order.entity.OrderItemEntity;
import com.yama.mall.order.enume.OrderStatusEnum;
import com.yama.mall.order.feign.CartFeignService;
import com.yama.mall.order.feign.MemberFeignService;
import com.yama.mall.order.feign.ProductFeignService;
import com.yama.mall.order.feign.WareFeignService;
import com.yama.mall.order.interceptor.LoginUserInterceptor;
import com.yama.mall.order.service.OrderItemService;
import com.yama.mall.order.service.OrderService;
import com.yama.mall.order.to.OrderCreateTO;
import com.yama.mall.order.to.SpuInfoTo;
import com.yama.mall.order.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    //本地线程变量共享信息
    ThreadLocal<OrderSubmitVO> confirmVOThreadLocal = new ThreadLocal<>();

    //导入用户远程接口调用对象
    @Autowired
    private MemberFeignService memberFeignService;

    //注入购物车远程调用接口对象
    @Autowired
    private CartFeignService cartFeignService;

    //注入连接池
    @Autowired
    private ThreadPoolExecutor executor;

    //注入库存系统远程调用接口
    @Autowired
    private WareFeignService wareFeignService;

    //防虫令牌redis中保存
    @Autowired
    private StringRedisTemplate redisTemplate;

    //注入Product模块远程调用对象
    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private RabbitTemplate rabbitTemplate;



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
        //获取用户收货地址
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
        }, executor).thenRunAsync(()->{
            //TODO 异步查询货物状态
            //获取购买商品的skuId，远程调用查询库存信息
            List<OrderItemVO> items = orderConfirmVO.getItems();
            //获取所有的商品skuId
            List<Long> skuIds = items.stream().map(item -> item.getSkuId()).collect(Collectors.toList());
            //TODO 远程调用库存系统查询
            List<OrderItemHasStockVO> stockStatus = wareFeignService.getOrderItemHasStock(skuIds);
            Map<Long, Boolean> collect = stockStatus.stream()
                    .collect(Collectors.toMap(item -> item.getSkuId(), item -> item.getHasStock()));
            orderConfirmVO.setStocks(collect);
        },executor);

        //3.查询用户积分信息
        Integer integration = loginUserInfo.getIntegration();
        orderConfirmVO.setIntegration(integration);

        //4.其他信息自动查询

        //TODO 5.防重令牌------>接口幂等性处理
        //生成防重令牌，保存到redis中
        String orderToken = UUID.randomUUID().toString().replaceAll("-", "");
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX+loginUserInfo.getId(),
                orderToken,30, TimeUnit.MINUTES);

        //防重令牌设置到订单对象中
        orderConfirmVO.setOrderToken(orderToken);

        //等待所有异步操作完成
        CompletableFuture.allOf(getAddresses,getOrderItems).get();
        return orderConfirmVO;
    }

    /**
     * TODO 分布式事务解决方案
     * 下单操作，保存提交的订单信息
     * 本地事务，在分布式系统中，只能控制住自己的回滚，控制不了其他服务的回滚
     * 分布式事务：最大的原因，网络问题导致远程调用异常，本地事务回滚，远程事务成功，却无法回滚。
     *
     * @param vo
     * @return
     */
    // @Transactional(isolation = Isolation.READ_COMMITTED) 设置事务的隔离级别
    // @Transactional(propagation = Propagation.REQUIRED)   设置事务的传播级别
    @Transactional(rollbackFor = Exception.class)
//    @GlobalTransactional(rollbackFor = Exception.class) //seata的AT事务模式注解
    @Override
    public SubmitOrderResponseVO submitOrder(OrderSubmitVO vo) {
        confirmVOThreadLocal.set(vo);
        //准备返回数据对象
        SubmitOrderResponseVO response = new SubmitOrderResponseVO();
        response.setCode(0);
        //获取用户数据
        MemberEntityVO memberResponseVo = LoginUserInterceptor.threadLocal.get();
        /*一系列操作：防重令牌校验，校验价格，锁定库存，保存订单信息*/
        //TODO 重点：防重令牌校验，获取redis、页面中保存的令牌进行对比校验
        String orderToken = vo.getOrderToken();
        /*String redisToken = redisTemplate.opsForValue().get(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId());*/

        //验证令牌是否合法【令牌的对比和删除必须保证原子性】，防止用户操作过快
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

        //通过lure脚本原子验证令牌和删除令牌
        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId()),
                orderToken);

        if (result == 0L) {
            //令牌验证失败
            response.setCode(1);
            log.debug("令牌验证失败");
            return response;
        } else {
            //令牌验证成功
            //1、创建订单、订单项等信息
            OrderCreateTO order = createOrder();

            //2、验证价格
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();

            //验价允许存在一定的误差
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                //金额对比成功
                //TODO 3、保存订单
                saveOrder(order);
                //4、锁定库存，只要一有异常就回滚
                //传递数据：订单号，所有订单项（skuId,skuName,num）
                WareSkuLockVO wareSkuLock= new WareSkuLockVO();
                wareSkuLock.setOrderSn(order.getOrder().getOrderSn());
                List<OrderItemVO> orderItems = order.getOrderItems().stream().map(item -> {
                    OrderItemVO orderItemVO = new OrderItemVO();
                    orderItemVO.setSkuId(item.getSkuId());
                    orderItemVO.setCount(item.getSkuQuantity());
                    orderItemVO.setTitle(item.getSkuName());
                    return orderItemVO;
                }).collect(Collectors.toList());
                wareSkuLock.setLocks(orderItems);
                //TODO 4、远程锁定库存
                //TODO 高并发场景，分布式事务解决方案(seta的AT模式多处使用锁机制，不适合并发场景)
                //1.为了保证高并发，让库存服务自己回滚。可以发消息给库存服务
                //2.使用消息队列，让库存服务本身进行自己的解锁服务，可以存在一定的延迟，达到最终一致性即可。
                R lockStockResult = wareFeignService.orderLockStock(wareSkuLock);
                if (lockStockResult.getData(new TypeReference<Boolean>(){})){
                    //库存锁定成功
                    response.setOrder(order.getOrder());
                    //5、TODO 远程扣减积分
//                    int i=10/0;
                    //6、订单创建完成发送消息给MQ
                    rabbitTemplate.convertAndSend(RabbitConstant.ORDER_EVENT_EXCHANGE,"order.create.order",order.getOrder());
                    return response;
                }else {
                    //库存锁定失败，抛出异常
                    response.setCode(3);
                    log.debug("库存锁定异常");
                    throw new NoStockException("库存锁定异常");
                    //TODO 抛出库存异常---优化
                }
            } else {
                //金额对比失败
                response.setCode(2);
                log.debug("验价失败");
                return response;
            }
        }
        /*if (Objects.equals(orderToken,redisToken)){
            //令牌校验成功，可以进行其他下单
        }else {
            //校验失败
        }*/
    }

    /**
     * 关闭订单操作
     * @param order
     */
    @Override
    public void closeOrder(OrderEntity order) {
        //关闭订单之前先查询一下数据库，判断此订单状态是否已支付
        OrderEntity orderInfo = this.getOne(new QueryWrapper<OrderEntity>().
                eq("order_sn",order.getOrderSn()));

        if (orderInfo.getStatus().equals(OrderStatusEnum.CREATE_NEW.getCode())) {
            //待付款状态进行关单
            OrderEntity orderUpdate = new OrderEntity();
            orderUpdate.setId(orderInfo.getId());
            orderUpdate.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(orderUpdate);

            // 关单后，立即发送消息给库存服务，进行解锁库存
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderInfo, orderTo);
            try {
                //TODO（柔性事务：消息可靠性） 确保每个消息发送成功，给每个消息做好日志记录，(给数据库保存每一个详细信息)保存每个消息的详细信息
                rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
            } catch (Exception e) {
                //TODO 1、网络等问题，导致消息发送失败 2、定期扫描数据库，重新发送失败的消息
            }
        }
    }

    /**
     * 获取订单支付信息，封装为PayVo
     * @param orderSn
     * @return
     */
    @Override
    public PayVo getOrderPayVo(String orderSn) {
        PayVo payVo = new PayVo();
        OrderEntity order = this.getOrderByOrderSn(orderSn);
        //TODO 将支付总额BigDicmal处理成保留两位小数进行设置，四位小数支付宝提交异常
        //设置支付金额
        BigDecimal scale = order.getPayAmount().setScale(2, BigDecimal.ROUND_UP);
        payVo.setTotal_amount(scale.toString());
        //设置订单号
        payVo.setOut_trade_no(orderSn);
        //查询第一个商品名进行设置为标
        List<OrderItemEntity> orderItem = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn));
        OrderItemEntity item = orderItem.get(0);
        payVo.setSubject(item.getSkuName());
        //设置商品描述
        payVo.setBody(item.getSpuName());
        return payVo;
    }

    /**
     * 根据订单号获取Order对象
     * @param orderSn
     * @return
     */
    private OrderEntity getOrderByOrderSn(String orderSn) {
        OrderDao orderMaper = this.getBaseMapper();
        OrderEntity order = orderMaper.selectOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        return order;
    }


    /**
     * 保存订单所有数据
     * @param orderCreateTo
     */
    private void saveOrder(OrderCreateTO orderCreateTo) {

        //获取订单信息
        OrderEntity order = orderCreateTo.getOrder();
        order.setModifyTime(new Date());
        order.setCreateTime(new Date());
        //保存订单
        this.baseMapper.insert(order);

        //获取订单项信息
        List<OrderItemEntity> orderItems = orderCreateTo.getOrderItems();
        //批量保存订单项数据
        orderItemService.saveBatch(orderItems);
    }

    /**
     * 创建订单，返回数据会保存到数据库
     * @return
     */
    public OrderCreateTO createOrder(){
        OrderCreateTO createTo = new OrderCreateTO();

        //TODO 使用工具类生成订单号
        //1、生成订单号
        String orderSn = IdWorker.getTimeId();
        //2.创建订单
        OrderEntity orderEntity = builderOrder(orderSn);

        //3、获取到所有的订单项
        List<OrderItemEntity> orderItemEntities = builderOrderItems(orderSn);

        //4、验价(计算价格、积分等信息)
        computePrice(orderEntity,orderItemEntities);

        createTo.setOrder(orderEntity);
        createTo.setOrderItems(orderItemEntities);
        return createTo;
    }

    /**
     * 计算价格的方法
     * @param orderEntity
     * @param orderItemEntities
     */
    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {

        //总价
        BigDecimal total = new BigDecimal("0.0");
        //优惠价
        BigDecimal coupon = new BigDecimal("0.0");
        BigDecimal intergration = new BigDecimal("0.0");
        BigDecimal promotion = new BigDecimal("0.0");

        //积分、成长值
        Integer integrationTotal = 0;
        Integer growthTotal = 0;

        //订单总额，叠加每一个订单项的总额信息
        for (OrderItemEntity orderItem : orderItemEntities) {
            //优惠价格信息
            coupon = coupon.add(orderItem.getCouponAmount());
            promotion = promotion.add(orderItem.getPromotionAmount());
            intergration = intergration.add(orderItem.getIntegrationAmount());

            //总价
            total = total.add(orderItem.getRealAmount());

            //积分信息和成长值信息
            integrationTotal += orderItem.getGiftIntegration();
            growthTotal += orderItem.getGiftGrowth();

        }
        //1、订单价格相关的
        orderEntity.setTotalAmount(total);
        //设置应付总额(总额+运费)
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        orderEntity.setCouponAmount(coupon);
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(intergration);

        //设置积分成长值信息
        orderEntity.setIntegration(integrationTotal);
        orderEntity.setGrowth(growthTotal);

        //设置删除状态(0-未删除，1-已删除)
        orderEntity.setDeleteStatus(0);

    }


    /**
     * 构建订单数据
     * @param orderSn
     * @return
     */
    private OrderEntity builderOrder(String orderSn) {

        //获取当前用户登录信息
        MemberEntityVO memberResponseVO = LoginUserInterceptor.threadLocal.get();

        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setMemberId(memberResponseVO.getId());
        orderEntity.setOrderSn(orderSn);
        orderEntity.setMemberUsername(memberResponseVO.getUsername());

        OrderSubmitVO orderSubmitVO = confirmVOThreadLocal.get();

        //远程获取收货地址和运费信息
        R fareAddressVO = wareFeignService.getFare(orderSubmitVO.getAddrId());
        FareVO fareResp = fareAddressVO.getData("data", new TypeReference<FareVO>() {});

        //获取到运费信息
        BigDecimal fare = fareResp.getFare();
        orderEntity.setFreightAmount(fare);

        //获取到收货地址信息
        MemberAddressVO address = fareResp.getAddress();
        //设置收货人信息
        orderEntity.setReceiverName(address.getName());
        orderEntity.setReceiverPhone(address.getPhone());
        orderEntity.setReceiverPostCode(address.getPostCode());
        orderEntity.setReceiverProvince(address.getProvince());
        orderEntity.setReceiverCity(address.getCity());
        orderEntity.setReceiverRegion(address.getRegion());
        orderEntity.setReceiverDetailAddress(address.getDetailAddress());

        //设置订单相关的状态信息
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setAutoConfirmDay(7);
        orderEntity.setConfirmStatus(0);
        return orderEntity;
    }

    /**
     * 构建所有订单项数据
     * @return
     */
    public List<OrderItemEntity> builderOrderItems(String orderSn) {

        List<OrderItemEntity> orderItemEntityList = new ArrayList<>();

        //最后确定每个购物项的价格
        List<OrderItemVO> currentCartItems = cartFeignService.getconcurrentUserCartItems();
        if (currentCartItems != null && currentCartItems.size() > 0) {
            orderItemEntityList = currentCartItems.stream().map((items) -> {
                //构建订单项数据
                OrderItemEntity orderItemEntity = builderOrderItem(items);
                orderItemEntity.setOrderSn(orderSn);
                return orderItemEntity;
            }).collect(Collectors.toList());
        }
        return orderItemEntityList;
    }

    /**
     * 构建某一个订单项的数据
     * @param items
     * @return
     */
    private OrderItemEntity builderOrderItem(OrderItemVO items) {

        OrderItemEntity orderItemEntity = new OrderItemEntity();

        //1、商品的spu信息
        Long skuId = items.getSkuId();
        //获取spu的信息
        R spuInfo = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoTo spuInfoData = spuInfo.getData("data", new TypeReference<SpuInfoTo>() {
        });
        orderItemEntity.setSpuId(spuInfoData.getId());
        orderItemEntity.setSpuName(spuInfoData.getSpuName());
        orderItemEntity.setSpuBrand(spuInfoData.getBrandName());
        orderItemEntity.setCategoryId(spuInfoData.getCatalogId());

        //2、商品的sku信息
        orderItemEntity.setSkuId(skuId);
        orderItemEntity.setSkuName(items.getTitle());
        orderItemEntity.setSkuPic(items.getImage());
        orderItemEntity.setSkuPrice(items.getPrice());
        orderItemEntity.setSkuQuantity(items.getCount());

        //使用StringUtils.collectionToDelimitedString将list集合转换为String
        String skuAttrValues = StringUtils.collectionToDelimitedString(items.getSkuAttrValues(), ";");
        orderItemEntity.setSkuAttrsVals(skuAttrValues);

        //3、商品的优惠信息

        //4、商品的积分信息
        orderItemEntity.setGiftGrowth(items.getPrice().multiply(new BigDecimal(items.getCount())).intValue());
        orderItemEntity.setGiftIntegration(items.getPrice().multiply(new BigDecimal(items.getCount())).intValue());

        //5、订单项的价格信息
        orderItemEntity.setPromotionAmount(BigDecimal.ZERO);
        orderItemEntity.setCouponAmount(BigDecimal.ZERO);
        orderItemEntity.setIntegrationAmount(BigDecimal.ZERO);

        //当前订单项的实际金额.总额 - 各种优惠价格
        //原来的价格
        BigDecimal origin = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity().toString()));
        //原价减去优惠价得到最终的价格
        BigDecimal subtract = origin.subtract(orderItemEntity.getCouponAmount())
                .subtract(orderItemEntity.getPromotionAmount())
                .subtract(orderItemEntity.getIntegrationAmount());
        orderItemEntity.setRealAmount(subtract);

        return orderItemEntity;
    }
}