package com.yama.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yama.mall.common.constant.ProductConstant;
import com.yama.mall.common.to.SkuEsModel;
import com.yama.mall.common.to.SkuReductionTO;
import com.yama.mall.common.to.SpuBoundTO;
import com.yama.mall.common.utils.PageUtils;
import com.yama.mall.common.utils.Query;
import com.yama.mall.common.utils.R;
import com.yama.mall.common.vo.SkuHasStockVO;
import com.yama.mall.product.dao.SpuInfoDao;
import com.yama.mall.product.entity.*;
import com.yama.mall.product.feign.CouponFeignService;
import com.yama.mall.product.feign.SearchFeignService;
import com.yama.mall.product.feign.WareFeignService;
import com.yama.mall.product.service.*;
import com.yama.mall.product.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {
    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private SpuImagesService spuImagesService;


    @Autowired
    private AttrService attrService;


    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private SkuInfoService skuInfoService;


    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    //注入远程调用接口
    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    //ware远程调用接口
    @Autowired
    private WareFeignService wareFeignService;

    //es远程调用接口
    private SearchFeignService searchFeignService;


    /**
     * 商品上架，保存信息到es中
     * @param spuId
     */
    @Override
    public void up(Long spuId) {
        //1、组装数据es模型
        //1).查出当前spuId对应的sku信息
        List<SkuInfoEntity> skus = skuInfoService.getSkuBySpuId(spuId);

        //TODO 4.查询当前sku对应的所有可以检索的规格属性
        List<ProductAttrValueEntity> productAttrValues = productAttrValueService.baseAttrListForSpu(spuId);
        
        //获取id，进行查询可检索的属性
        List<Long> attrIds = productAttrValues.stream().map(attr -> attr.getAttrId()).collect(Collectors.toList());

        List<Long> searchAttrIds = attrService.selectSearchAttrs(attrIds);

        //查询所有符合检索要求的属性
        //视频使用set中的container方法
//        Set<Long> setIds = new HashSet<>(searchAttrIds);
        List<SkuEsModel.Attrs> skuEsModelAttrs = productAttrValues.stream()
                .filter(productAttrValueEntity -> searchAttrIds.contains(productAttrValueEntity.getAttrId()))
                .map(item -> {
                    SkuEsModel.Attrs attrs = new SkuEsModel.Attrs();
                    BeanUtils.copyProperties(item, attrs);
                    return attrs;
                }).collect(Collectors.toList());

        //通过spu获取skuid信息，到库存中查询所有sku是否有库存

        //在外部调用防止内部循环多次调用
        /*AtomicReference<Boolean> isExistStock= new AtomicReference<>(true);*/

        List<SkuInfoEntity> skuInfos = skuInfoService.getSkuBySpuId(spuId);

        List<Long> skuIds = skuInfos.stream().map(item -> item.getSkuId()).collect(Collectors.toList());

        //异常处理保证异常后续操作可以进行
        Map<Long, Boolean> stockMap = null;
        try {
            R<List<SkuHasStockVO>> skusHasStock = wareFeignService.getSkusHasStock(skuIds);

            stockMap = skusHasStock.getData().stream()
                    .collect(Collectors.toMap(SkuHasStockVO::getSkuId, SkuHasStockVO::getHasStock));
        } catch (Exception e) {
            log.error("库存服务调用异常:{}",e);
        }

        /*if (skusHasStock.getCode()==0){

            List<Map<String,Object>> skuStockInfo = (List<Map<String,Object>>)skusHasStock.get("data");
            skuStockInfo.forEach(isExistStockInfo->{
                if (!(Boolean)isExistStockInfo.get("hasStock")){
                    isExistStock.set(false);
                }
            });//自己的写法
        }*/
        Map<Long, Boolean> finalStockMap = stockMap;

        List<SkuEsModel> skuEsModels = skus.stream().map(sku -> {
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(sku, skuEsModel);
            //不能属性对拷的：skuPrice,skuImg
            skuEsModel.setSkuPrice(sku.getPrice());
            skuEsModel.setSkuImg(sku.getSkuDefaultImg());
            //TODO 1.远程调用查询是否有库存
            if (finalStockMap==null){
                skuEsModel.setHasStock(false);
            }else {
                skuEsModel.setHasStock(finalStockMap.get(sku.getSkuId()));
            }
            //TODO 2.热度，热度是一个后台较为复杂的逻辑，后期完善，当前设置为0
            skuEsModel.setHotScore(0L);
            //TODO 3.查询品牌和分类的名字信息
            /**
             *     private String brandName;
             *
             *     private String brandImg;
             *
             *     private String catalogName;
             *b
             *     private List<Attrs> attrs;
             */
            BrandEntity brandEntity = brandService.getById(sku.getBrandId());

            skuEsModel.setBrandName(brandEntity.getName());

            skuEsModel.setBrandImg(brandEntity.getLogo());

            CategoryEntity categoryEntity = categoryService.getById(sku.getCatalogId());

            skuEsModel.setCatalogName(categoryEntity.getName());

            //设置检索属性
            skuEsModel.setAttrs(skuEsModelAttrs);

            return skuEsModel;
        }).collect(Collectors.toList());

        //TODO 2.将封装好的数据保存到es
        R productUpResult = searchFeignService.productUp(skuEsModels);
        if (productUpResult.getCode()==0){
            //远程调用成功
            //TODO 修改当前spu的状态
            this.baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        }else {
            //远程调用失败
            //TODO 重复调用问题，接口幂等性问题
            /**
             * 问题关联：
             * Feign的调用流程
             * 1.构造请求数据，将对象转化为json
             *
             * 2.发送请求进行执行（执行成功会解码响应数据）
             *
             * 3.执行请求会有重试机制，
             */
        }


    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 保存商品基本信息
     * @param spuInfoEntity
     */
    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

    /**
     * spu管理中检索spu，多条件查询分页检索
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        /**
         *    key: '华为',//检索关键字
         *    catelogId: 6,//三级分类id
         *    brandId: 1,//品牌id
         *    status: 0,//商品状态
         */
        String key = (String) params.get("key");
        if ( key!= null && key.length() > 0){
            wrapper.and(t->t.eq("id",key).or().like("spu_name",key));
        }
        //不使用and()连接sql语句：catalog_id=1 and id=1 or spu_name like xxx（此时catalog_id就不一定等于指定值）
        //使用and()连接的sql语句：catalog_id=1 and (id=1 or spu_name like xxx):此时就能保证catalog_id满足条件，符合业务要求
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)){
            //视频实现下方3个同：（直接eq不使用and连接）wrapper..eq("catalog_id",catelogId)
            wrapper.and(a->a.eq("catalog_id",catelogId));
        }

        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)){
            wrapper.and(a->a.eq("brand_id",brandId));
        }

        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)){
            wrapper.and(a->a.eq("publish_status",status));
        }


        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }

    /**
     * //TODO 高级部分完善，比如远程调用失败是否是否正常回滚---分布式事务；服务不稳定怎么办---服务的熔断和降级
     * 保存商品信息
     * @param vo
     */
    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {
        //1.保存spu基本信息、pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo,spuInfoEntity);
        this.saveBaseSpuInfo(spuInfoEntity);
        //2.保存spu的描述图片、pms_spu_info_desc
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity spuInfoDesc = new SpuInfoDescEntity();
        spuInfoDesc.setSpuId(spuInfoEntity.getId());
        spuInfoDesc.setDecript(String.join(",",decript));
        spuInfoDescService.saveSpuInfoDesc(spuInfoDesc);
        //3.保存spu的图片集、pms_spu_images
        List<String> images = vo.getImages();
        spuImagesService.saveImages(spuInfoEntity.getId(),images);
        //4.保存spu的规格参数：pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(item -> {
            ProductAttrValueEntity productAttrValue = new ProductAttrValueEntity();
            productAttrValue.setSpuId(spuInfoEntity.getId());
            productAttrValue.setAttrId(item.getAttrId());
            AttrEntity attrEntity = attrService.getById(item.getAttrId());
            productAttrValue.setAttrName(attrEntity.getAttrName());
            productAttrValue.setAttrValue(item.getAttrValues());
            productAttrValue.setQuickShow(item.getShowDesc());
            return productAttrValue;
        }).collect(Collectors.toList());
        productAttrValueService.saveBatch(collect);
        //5.保存spu的积分信息：gulimall_sms->sms_spu_bounds
        Bounds bounds = vo.getBounds();
        SpuBoundTO spuBoundTO = new SpuBoundTO();
        BeanUtils.copyProperties(bounds,spuBoundTO);
        spuBoundTO.setSpuId(spuInfoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundTO);
        if (r.getCode()!=0){
            log.error("远程保存spu积分信息失败");
        }

        //6.保存当前spu对应的所有sku信息：
        //1）.sku的基本信息：pms_sku_info
        List<Skus> skus = vo.getSkus();
        if (skus != null && skus.size()>0){
            skus.forEach(item->{
                String defaultImage = "";
                List<Images> imgs = item.getImages();
                for (Images img: imgs){
                    if (img.getDefaultImg()==1){
                        defaultImage = img.getImgUrl();
                    }
                }
                //为SKUInfo设置属性
                SkuInfoEntity skuInfo = new SkuInfoEntity();
                BeanUtils.copyProperties(item,skuInfo);
                skuInfo.setBrandId(spuInfoEntity.getBrandId());
                skuInfo.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfo.setSaleCount(0L);
                skuInfo.setSpuId(spuInfoEntity.getId());
                skuInfo.setSkuDefaultImg(defaultImage);
                skuInfoService.save(skuInfo);
                //2).sku的图片信息：pms_sku_images
                //收集图片信息
                List<SkuImagesEntity> skuImageCollect = item.getImages().stream().map(pic->{
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuInfo.getSkuId());
                    skuImagesEntity.setImgUrl(pic.getImgUrl());
                    skuImagesEntity.setDefaultImg(pic.getDefaultImg());
                    return skuImagesEntity;
                    //TODO 没有图片的路径无需保存，debug过程中会保存空路径数据
                }).filter(entity-> !StringUtils.isEmpty(entity.getImgUrl())).collect(Collectors.toList());
                skuImagesService.saveBatch(skuImageCollect);
                //3).sku的销售属性信息：pms_sku_sale_attr_value
                List<SkuSaleAttrValueEntity> skuSaleAttrValueCollect = item.getAttr().stream().map(attr -> {
                    SkuSaleAttrValueEntity skuSaleAttrValue = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(attr, skuSaleAttrValue);
                    skuSaleAttrValue.setSkuId(skuInfo.getSkuId());
                    return skuSaleAttrValue;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueCollect);
                //4).sku的优惠、满减等信息：gulimall_sms->sms_sku_ladder\sms_sku_full_reduction\sms_member_price
                SkuReductionTO skuReductionTO = new SkuReductionTO();
                BeanUtils.copyProperties(item,skuReductionTO);
                skuReductionTO.setSkuId(skuInfo.getSkuId());
//                if (skuReductionTO.getFullCount()>0 && skuReductionTO.getFullPrice().intValue() >0){
                if (skuReductionTO.getFullCount()>0 || skuReductionTO.getFullPrice().compareTo(new BigDecimal("0"))==1){
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTO);
                    if (r1.getCode()!=0){
                        log.error("远程保存sku优惠信息失败");
                    }
                }
            });
        }





    }
}