package com.yama.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yama.mall.common.utils.PageUtils;
import com.yama.mall.common.utils.Query;
import com.yama.mall.product.dao.SkuInfoDao;
import com.yama.mall.product.entity.SkuImagesEntity;
import com.yama.mall.product.entity.SkuInfoEntity;
import com.yama.mall.product.entity.SpuInfoDescEntity;
import com.yama.mall.product.service.*;
import com.yama.mall.product.vo.SkuItemSaleAttrVO;
import com.yama.mall.product.vo.SkuItemVO;
import com.yama.mall.product.vo.SpuItemAttrGroupVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {
    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    //注入异步执行使用的线程池对象
    @Autowired
    private ThreadPoolExecutor executor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 多条件分页检索sku数据
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();
        /**
         * key: '华为',//检索关键字
         * catelogId: 0,
         * brandId: 0,
         * min: 0,
         * max: 0
         */
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)){
            queryWrapper.and(wrapper->wrapper.eq("sku_id",key).or().like("sku_name",key));
        }

        String catelogId = (String) params.get("catelogId");
        //等于0，就是查询全部数据
        if (!StringUtils.isEmpty(key) && !"0".equalsIgnoreCase(catelogId)){
            queryWrapper.eq("catalog_id",catelogId);
        }

        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(key) && !"0".equalsIgnoreCase(brandId)){
            queryWrapper.eq("brand_id",brandId);
        }

        String min = (String) params.get("min");
        if (!StringUtils.isEmpty(key)){
            queryWrapper.ge("price",min);

        }

        String max = (String) params.get("max");
        if (!StringUtils.isEmpty(key)){
            try {
                BigDecimal bigDecimal = new BigDecimal(max);
                if (bigDecimal.compareTo(new BigDecimal("0")) == 1){
                    queryWrapper.le("price",max);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    /**
     * 通过spuId获取对应的sku信息
     * @param spuId
     * @return
     */
    @Override
    public List<SkuInfoEntity> getSkuBySpuId(Long spuId) {
        List<SkuInfoEntity> skuInfoEntities = this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
        return skuInfoEntities;
    }

    /**
     * //TODO 使用异步编排进行优化获取商品详情信息的过程，因为获取信息多为远程调用
     * 获取sku商品详情信息
     * @param skuId
     * @return
     */
    public SkuItemVO itemOld(Long skuId) {
        SkuItemVO skuItemVO = new SkuItemVO();
        //1.获取sku的基本信息 0.5s pms_sku_info
        SkuInfoEntity skuInfo = getById(skuId);
        skuItemVO.setInfo(skuInfo);

        //获取辅助信息，spuId，catalogId
        Long spuId = skuInfo.getSpuId();
        Long catalogId = skuInfo.getCatalogId();

        //2.获取sku的图片信息 0.5s pms_sku_images
        List<SkuImagesEntity>  imags = skuImagesService.getImagsBySkuId(skuId);
        skuItemVO.setImages(imags);

        //TODO 3.获取sku的促销信息 1s

        //4.获取spu的介绍信息 1s pms_spu_info_desc

        SpuInfoDescEntity spuinfoDesc = spuInfoDescService.getById(spuId);
        skuItemVO.setDesc(spuinfoDesc);

        //5.获取规格参数组及组下的规格参数 1.5s
        List<SpuItemAttrGroupVO> spuItemAttrGroupVOList = attrGroupService.getAttrGroupWithattrsBySpuId(spuId,catalogId);
        skuItemVO.setGroupAttrs(spuItemAttrGroupVOList);

        //6.获取spu的所有销售属性 1s
        List<SkuItemSaleAttrVO> saleAttr = skuSaleAttrValueService.getSaleAttrValueBySpuId(spuId);
        skuItemVO.setSaleAttr(saleAttr);

        return skuItemVO;
    }

    /**
     * //TODO 使用异步编排进行优化后的获取商品详情的方法
     * 获取sku商品详情信息
     * @param skuId
     * @return
     */
    @Override
    public SkuItemVO item(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVO skuItemVO = new SkuItemVO();
        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            //1.获取sku的基本信息 0.5s pms_sku_info
            SkuInfoEntity skuInfo = getById(skuId);
            skuItemVO.setInfo(skuInfo);
            //返回异步结果
            return skuInfo;
        }, executor);

        //执行依赖infoFuture异步执行结果的任务
        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync(result -> {
            //4.获取spu的介绍信息 1s pms_spu_info_desc
            SpuInfoDescEntity spuinfoDesc = spuInfoDescService.getById(result.getSpuId());
            skuItemVO.setDesc(spuinfoDesc);
        }, executor);

        CompletableFuture<Void> attrGroupFuture = infoFuture.thenAcceptAsync(result -> {
            //5.获取规格参数组及组下的规格参数 1.5s
            List<SpuItemAttrGroupVO> spuItemAttrGroupVOList = attrGroupService
                    .getAttrGroupWithattrsBySpuId(result.getSpuId(), result.getCatalogId());
            skuItemVO.setGroupAttrs(spuItemAttrGroupVOList);
        }, executor);

        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync(result -> {
            //6.获取spu的所有销售属性 1s
            List<SkuItemSaleAttrVO> saleAttr = skuSaleAttrValueService.getSaleAttrValueBySpuId(result.getSpuId());
            skuItemVO.setSaleAttr(saleAttr);
        }, executor);

        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            //2.获取sku的图片信息 0.5s pms_sku_images
            List<SkuImagesEntity> imags = skuImagesService.getImagsBySkuId(skuId);
            skuItemVO.setImages(imags);
        }, executor);

        //等待所有异步执行结果完成
        CompletableFuture.allOf(descFuture,attrGroupFuture,saleAttrFuture,imageFuture).get();

        return skuItemVO;
    }


}