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
     * ??????sku??????????????????
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
     * ?????????????????????
     * @param stockLockTo
     */
    @Override
    public void unLockStock(StockLockTo stockLockTo) {

        StockDetailTo stockDetail = stockLockTo.getStockDetail();
        Long detailId = stockDetail.getId();
        /**
         * ??????
         * 1??????????????????????????????????????????????????????
         *   ?????????????????????????????????
         *      ?????????????????????
         *          1??????????????????????????????????????????
         *          2??????????????????????????????????????????
         *              ???????????????????????????????????????
         *                      ??????????????????????????????
         *   ???????????????????????????????????????????????????????????????????????????
         */
        WareOrderTaskDetailEntity detail = wareOrderTaskDetailService.getById(detailId);
        if(detail!=null){
            //??????????????????????????????????????????????????????
            Long taskId = stockLockTo.getId();
            WareOrderTaskEntity orderTaskEntity = wareOrderTaskService.getById(taskId);
            String orderSn = orderTaskEntity.getOrderSn();
            //?????????????????????????????????
            R data = orderFeignService.getOrderStatus(orderSn);
            if(data.getCode()==0){
                //????????????????????????
                Integer orderStatus = data.getData(new TypeReference<Integer>() {});
                if(orderStatus==null || orderStatus==4){
                    //???????????????????????????????????????????????????????????????????????????
                    if(detail.getLockStatus()==1){
                        //?????????????????????????????????????????????????????????
                        unLockStock(detail.getSkuId(), detail.getWareId(),detail.getSkuNum(), detail.getId());
                    }
                }
            }else {
                //????????????????????????????????????????????????????????????????????????
                //????????????????????????
                throw new RuntimeException("????????????????????????");
            }
        }else {
            //?????????????????????????????????????????????????????????????????????
        }
    }

    /**
     * ???????????????????????????????????????????????????
     * ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     * ???????????????????????????????????????????????????
     * @param orderTo
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void unLockStock(OrderTo orderTo) {
        String orderSn = orderTo.getOrderSn();
        //???????????????????????????????????????????????????????????????
        WareOrderTaskEntity orderTaskEntity = wareOrderTaskService.getOrderTaskByOrderSn(orderSn);

        //??????????????????id???????????? ????????????????????????????????????
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
     * ????????????
     * @param skuId ??????id
     * @param wareId ??????id
     * @param num   ????????????
     * @param taskDetailId ????????????id--????????????????????????
     */
    public void unLockStock(Long skuId,Long wareId,Integer num,Long taskDetailId){
        //????????????
        wareSkuDao.unLockStock(skuId,wareId,num);
        //????????????????????????????????????
        WareOrderTaskDetailEntity detail = wareOrderTaskDetailService.getById(taskDetailId);
        detail.setLockStatus(2);
        wareOrderTaskDetailService.updateById(detail);
    }

    /**
     * TODO ???????????????????????????
     *
     * rabbitmq????????????????????????
     * 1)???????????????????????????????????????????????????????????????????????????????????????????????????????????????
     *
     *
     * 2)????????????????????????????????????????????????????????????????????????????????????????????????
     *      ??????????????????????????????????????????
     * @param vo
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean orderLockStock(WareSkuLockVO vo) {
        /**
         * ???????????????????????????
         * ????????????
         */
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskService.save(wareOrderTaskEntity);


        //1?????????????????????????????????????????????????????????????????????????????????????????????????????????
        //2????????????????????????????????????????????????????????????????????????????????????
        List<OrderItemVO> locks = vo.getLocks();

        List<SkuWareHasStock> collect = locks.stream().map((item) -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());
            //??????????????????????????????????????????
            List<Long> wareIdList = wareSkuDao.listWareIdHasSkuStock(skuId);
            stock.setWareId(wareIdList);
            return stock;
        }).collect(Collectors.toList());

        /**
         * 1???????????????????????????????????????,??????????????????????????????????????????????????????MQ
         * 2???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????id????????????????????????
         * ?????????
         * 1???2???3????????????????????????3??????????????????????????????????????????????????????
         */
        //3.????????????
        for (SkuWareHasStock hasStock:collect){
            Boolean skuStocked=false;
            Long skuId = hasStock.getSkuId();
            List<Long> wareIds = hasStock.getWareId();
            if (wareIds==null || wareIds.size() ==0){
                //??????????????????
                throw new NoStockException(skuId);
            }
            //???????????????????????????????????????????????????
            for (Long wareId:wareIds) {
                //?????????????????????1??????????????????0
                Long count = wareSkuDao.lockSkuStock(skuId,wareId,hasStock.getNum());
                if (count==1){
                    //????????????
                    skuStocked=true;
                    //TODO ???????????????????????????mq
                    /**
                     * ???????????????????????????????????????mq??????
                     */
                    WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity(
                            null, skuId, "", hasStock.getNum(),
                            wareOrderTaskEntity.getId(), wareId, 1);
                    wareOrderTaskDetailService.save(wareOrderTaskDetailEntity);
                    //??????mq??????
                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(wareOrderTaskDetailEntity,stockDetailTo);
                    StockLockTo stockLockTo = new StockLockTo();
                    stockLockTo.setId(wareOrderTaskEntity.getId());
                    stockLockTo.setStockDetail(stockDetailTo);
                    rabbitTemplate.convertAndSend(MyRabbitMQConfig.STOCK_EVENT_EXCHANGE,"stock.locked",stockLockTo);
                    break;
                }else {
                    //??????????????????????????????????????????
                }
            }
            if (!skuStocked){
                //????????????
                throw new NoStockException(skuId);
            }

        }
        //3.??????????????????????????????
        return true;
    }

    /**
     * ??????????????????
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        /**
         *    wareId: 123,//??????id
         *    skuId: 123//??????id
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
     * ??????????????????
     * ????????????????????????????????????????????????????????????
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
     * ??????????????????
     * ????????????????????????????????????????????????????????????
     * @param skuId
     * @param wareId
     * @param skuNum
     */
    @Transactional
    @Override
    public void addStocks(Long skuId, Long wareId, Integer skuNum) {
        //???????????????????????????????????????????????????????????????
        WareSkuEntity wareSkuEntitie = wareSkuDao.selectOne(new QueryWrapper<WareSkuEntity>()
                .eq("sku_id", skuId).eq("ware_id", wareId));
        if (wareSkuEntitie==null){
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntitie.setSkuId(skuId);
            wareSkuEntitie.setWareId(wareId);
            wareSkuEntitie.setStock(skuNum);
            wareSkuEntitie.setStockLocked(0);
            //TODO ????????????????????????sku???name,??????????????????????????????????????????????????????????????????????????????
            //??????try...catch????????????,??????try...catch?????????????????????????????????????????????????????????????????????
            //TODO ??????????????????????????????????????????????????????
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