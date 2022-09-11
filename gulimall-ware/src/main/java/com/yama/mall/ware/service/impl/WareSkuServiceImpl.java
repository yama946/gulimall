package com.yama.mall.ware.service.impl;

import com.yama.mall.common.utils.R;
import com.yama.mall.ware.feign.ProductFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yama.mall.common.utils.PageUtils;
import com.yama.mall.common.utils.Query;

import com.yama.mall.ware.dao.WareSkuDao;
import com.yama.mall.ware.entity.WareSkuEntity;
import com.yama.mall.ware.service.WareSkuService;
import org.springframework.util.StringUtils;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private WareSkuDao wareSkuDao;

    @Autowired
    private ProductFeignService productFeignService;

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