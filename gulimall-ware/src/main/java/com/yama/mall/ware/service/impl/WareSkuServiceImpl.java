package com.yama.mall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yama.mall.common.to.OrderTo;
import com.yama.mall.common.to.mq.StockDetailTo;
import com.yama.mall.common.to.mq.StockLockTo;
import com.yama.mall.common.utils.PageUtils;
import com.yama.mall.common.utils.Query;
import com.yama.mall.common.utils.R;
import com.yama.mall.ware.config.MyRabbitMQConfig;
import com.yama.mall.ware.dao.WareSkuDao;
import com.yama.mall.ware.entity.WareOrderTaskDetailEntity;
import com.yama.mall.ware.entity.WareOrderTaskEntity;
import com.yama.mall.ware.entity.WareSkuEntity;
import com.yama.mall.ware.exception.NoStockException;
import com.yama.mall.ware.feign.OrderFeignService;
import com.yama.mall.ware.feign.ProductFeignService;
import com.yama.mall.ware.service.WareOrderTaskDetailService;
import com.yama.mall.ware.service.WareOrderTaskService;
import com.yama.mall.ware.service.WareSkuService;
import com.yama.mall.ware.vo.OrderItemVO;
import com.yama.mall.ware.vo.SkuHasStockVO;
import com.yama.mall.ware.vo.WareSkuLockVO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private WareSkuDao wareSkuDao;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private WareOrderTaskService wareOrderTaskService;

    @Autowired
    private WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    private OrderFeignService orderFeignService;

    @Data
    class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareId;
    }

    /**
     * 判断sku是否存在库存
     * @param skuIds
     * @return
     */
    @Override
    public List<SkuHasStockVO> getSkuHasStock(List<Long> skuIds) {
        List<SkuHasStockVO> stocks = skuIds.stream().map(skuId -> {
            SkuHasStockVO stockVO = new SkuHasStockVO();
            Long stock = this.baseMapper.hasStock(skuId);
            stockVO.setSkuId(skuId);
            stockVO.setHasStock(stock==null?false:stock > 0);
            return stockVO;
        }).collect(Collectors.toList());
        return stocks;
    }

    /**
     * 解锁库存总方法
     * @param stockLockTo
     */
    @Override
    public void unLockStock(StockLockTo stockLockTo) {

        StockDetailTo stockDetail = stockLockTo.getStockDetail();
        Long detailId = stockDetail.getId();
        /**
         * 解锁
         * 1、查询数据库关于这个订单锁定库存信息
         *   有：证明库存锁定成功了
         *      解锁：订单状况
         *          1、没有这个订单，必须解锁库存
         *          2、有这个订单，不一定解锁库存
         *              订单状态：已取消：解锁库存
         *                      没取消：不能解锁库存
         *   没有：库存锁定失败了，库存回滚了。这种情况无需解锁
         */
        WareOrderTaskDetailEntity detail = wareOrderTaskDetailService.getById(detailId);
        if(detail!=null){
            //存在，进行判断关联订单的状态进行解锁
            Long taskId = stockLockTo.getId();
            WareOrderTaskEntity orderTaskEntity = wareOrderTaskService.getById(taskId);
            String orderSn = orderTaskEntity.getOrderSn();
            //远程查询关联订单的状态
            R data = orderFeignService.getOrderStatus(orderSn);
            if(data.getCode()==0){
                //订单数据查询成功
                Integer orderStatus = data.getData(new TypeReference<Integer>() {});
                if(orderStatus==null || orderStatus==4){
                    //订单已被用户取消或者订单创建失败，都要进行解锁库存
                    if(detail.getLockStatus()==1){
                        //当前工作单已锁定，但是未解锁才进行解锁
                        unLockStock(detail.getSkuId(), detail.getWareId(),detail.getSkuNum(), detail.getId());
                    }
                }
            }else {
                //消息拒绝以后重新放在队列里面，让别人继续消费解锁
                //远程调用服务失败
                throw new RuntimeException("远程调用服务失败");
            }
        }else {
            //不存在，工作单详情无需解锁，库存锁定失败，回滚
        }
    }

    /**
     * 订单取消后，主动发送的解锁消息处理
     * 防止订单服务卡顿，导致订单状态消息一直改不了，库存优先到期，查订单状态新建，什么都不处理
     * 导致卡顿的订单，永远都不能解锁库存
     * @param orderTo
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void unLockStock(OrderTo orderTo) {
        String orderSn = orderTo.getOrderSn();
        //查一下最新的库存解锁状态，防止重复解锁库存
        WareOrderTaskEntity orderTaskEntity = wareOrderTaskService.getOrderTaskByOrderSn(orderSn);

        //按照工作单的id找到所有 没有解锁的库存，进行解锁
        Long id = orderTaskEntity.getId();
        List<WareOrderTaskDetailEntity> list = wareOrderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>()
                .eq("task_id", id).eq("lock_status", 1));

        for (WareOrderTaskDetailEntity taskDetailEntity : list) {
            unLockStock(taskDetailEntity.getSkuId(),
                    taskDetailEntity.getWareId(),
                    taskDetailEntity.getSkuNum(),
                    taskDetailEntity.getId());
        }

    }

    /**
     * 解锁库存
     * @param skuId 商品id
     * @param wareId 仓库id
     * @param num   锁定数量
     * @param taskDetailId 订单详情id--删除订单详情数据
     */
    public void unLockStock(Long skuId,Long wareId,Integer num,Long taskDetailId){
        //库存解锁
        wareSkuDao.unLockStock(skuId,wareId,num);
        //更新库存工作单详情的状态
        WareOrderTaskDetailEntity detail = wareOrderTaskDetailService.getById(taskDetailId);
        detail.setLockStatus(2);
        wareOrderTaskDetailService.updateById(detail);
    }

    /**
     * TODO 为某个订单锁定库存
     *
     * rabbitmq解锁库存的场景：
     * 1)、下订单成功，订单过期没有支付被系统自动取消，被用户手动取消。都要解锁库存
     *
     *
     * 2)、下订单成功，库存锁定成功，接下来的业务调用失败，导致订单回滚。
     *      之前锁定的库存就要自动解锁。
     * @param vo
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean orderLockStock(WareSkuLockVO vo) {
        /**
         * 保存库存工作单详情
         * 追溯订单
         */
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskService.save(wareOrderTaskEntity);


        //1、按照下单的收货地址，找到一个就近仓库，锁定库存【实际中复杂逻辑实现】
        //2、找到每个商品在哪个仓库都有库存【本项目中简单逻辑实现】
        List<OrderItemVO> locks = vo.getLocks();

        List<SkuWareHasStock> collect = locks.stream().map((item) -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());
            //查询这个商品在哪个仓库有库存
            List<Long> wareIdList = wareSkuDao.listWareIdHasSkuStock(skuId);
            stock.setWareId(wareIdList);
            return stock;
        }).collect(Collectors.toList());

        /**
         * 1、如果每一个商品都锁定成功,将当前商品锁定了几件的工作单记录发给MQ
         * 2、锁定失败。前面保存的工作单信息都回滚了。发送出去的消息，即使要解锁库存，由于在数据库查不到指定的id，所有就不用解锁
         * 场景：
         * 1，2，3个商品锁定，其中3号商品锁定失败，本地事务会全部回滚。
         */
        //3.锁定库存
        for (SkuWareHasStock hasStock:collect){
            Boolean skuStocked=false;
            Long skuId = hasStock.getSkuId();
            List<Long> wareIds = hasStock.getWareId();
            if (wareIds==null || wareIds.size() ==0){
                //商品没有库存
                throw new NoStockException(skuId);
            }
            //商品存在库存，每个仓库依次扣减库存
            for (Long wareId:wareIds) {
                //锁定成功就返回1，失败就返回0
                Long count = wareSkuDao.lockSkuStock(skuId,wareId,hasStock.getNum());
                if (count==1){
                    //锁定成功
                    skuStocked=true;
                    //TODO 库存锁定成功，告诉mq
                    /**
                     * 保存库存工作单详情，并发送mq消息
                     */
                    WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity(
                            null, skuId, "", hasStock.getNum(),
                            wareOrderTaskEntity.getId(), wareId, 1);
                    wareOrderTaskDetailService.save(wareOrderTaskDetailEntity);
                    //发送mq消息
                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(wareOrderTaskDetailEntity,stockDetailTo);
                    StockLockTo stockLockTo = new StockLockTo();
                    stockLockTo.setId(wareOrderTaskEntity.getId());
                    stockLockTo.setStockDetail(stockDetailTo);
                    rabbitTemplate.convertAndSend(MyRabbitMQConfig.STOCK_EVENT_EXCHANGE,"stock.locked",stockLockTo);
                    break;
                }else {
                    //当前仓库锁定失败，尝试下一个
                }
            }
            if (!skuStocked){
                //锁定失败
                throw new NoStockException(skuId);
            }

        }
        //3.所有商品锁定仓库成功
        return true;
    }

    /**
     * 检索商品库存
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        /**
         *    wareId: 123,//仓库id
         *    skuId: 123//商品id
         */
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();

        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)){
            wrapper.eq("ware_id",wareId);
        }

        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)){
            wrapper.eq("sku_Id",skuId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    /**
     * 自己实现方法
     * 更新仓库中商品数量，采购单完成后执行更新
     * @param skuId
     * @param wareId
     * @param skuNum
     */
    /*@Override
    public void addStocks(Long skuId, Long wareId, Integer skuNum) {
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        HashMap<String, Long> stringLongHashMap = new HashMap<>();
        stringLongHashMap.put("sku_id",skuId);
        stringLongHashMap.put("ware_id",wareId);
        queryWrapper.allEq(stringLongHashMap);
        WareSkuEntity wareSkuEntity = this.getOne(queryWrapper);
        if (wareSkuEntity==null){
            wareSkuEntity = new WareSkuEntity();
            this.save(wareSkuEntity);
        }
        wareSkuEntity.setStock(wareSkuEntity.getStock()+skuNum);
        this.updateById(wareSkuEntity);
    }*/

    /**
     * 视频实现方法
     * 更新仓库中商品数量，采购单完成后执行更新
     * @param skuId
     * @param wareId
     * @param skuNum
     */
    @Transactional
    @Override
    public void addStocks(Long skuId, Long wareId, Integer skuNum) {
        //判断当前是否存在此库存信息，否则是保存操作
        WareSkuEntity wareSkuEntitie = wareSkuDao.selectOne(new QueryWrapper<WareSkuEntity>()
                .eq("sku_id", skuId).eq("ware_id", wareId));
        if (wareSkuEntitie==null){
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntitie.setSkuId(skuId);
            wareSkuEntitie.setWareId(wareId);
            wareSkuEntitie.setStock(skuNum);
            wareSkuEntitie.setStockLocked(0);
            //TODO 远程调用接口设置sku的name,如果微服务不稳定，导致远程调用失败，整个事务无需回滚
            //自己try...catch进行解决,通过try...catch处理异常后，不会抛出异常，也就不会触发回滚操作
            //TODO 还可以用什么办法让异常出现以后不回滚
            try {
                R skuInfoMap = productFeignService.info(skuId);
                Map<String,Object> info = (Map<String,Object>)skuInfoMap.get("skuInfo");

                if (skuInfoMap.getCode() ==0){
                    wareSkuEntity.setSkuName((String) info.get("skuName"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            wareSkuDao.insert(wareSkuEntity);
        }else {
            wareSkuDao.addStock(skuId,wareId,skuNum);
        }
    }

}