package com.yama.mall.product.app;

import com.yama.mall.common.utils.PageUtils;
import com.yama.mall.common.utils.R;
import com.yama.mall.product.entity.SpuInfoEntity;
import com.yama.mall.product.service.SpuInfoService;
import com.yama.mall.product.vo.SpuSaveVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;



/**
 * spu信息
 *
 * @author yanmu
 * @email yanmu123@gmail.com
 * @date 2022-08-30 17:50:51
 */
@Slf4j
@RestController
@RequestMapping("product/spuinfo")
public class SpuInfoController {
    @Autowired
    private SpuInfoService spuInfoService;



    /**
     * /product/spuinfo/{spuId}/up
     * 商品上架
     * 将sku信息以及其他冗余信息保存到es数据库中
     * @param spuId
     * @return
     */
    @PostMapping("/{spuId}/up")
    public R upSpu(@PathVariable("spuId") Long spuId){
        spuInfoService.up(spuId);
        return R.ok();
    }


    /**
     * /product/spuinfo/list---spu高级检索---多个条件合并检索
     * 分页查询列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:spuinfo:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = spuInfoService.queryPageByCondition(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("product:spuinfo:info")
    public R info(@PathVariable("id") Long id){
		SpuInfoEntity spuInfo = spuInfoService.getById(id);
        R info = R.ok().put("spuInfo", spuInfo);
        return info;
    }

    /**
     * 保存
     * ///product/spuinfo/save
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:spuinfo:save")
//    public R save(@RequestBody SpuInfoEntity spuInfo){
    public R save(@RequestBody SpuSaveVO vo){
//		spuInfoService.save(skuInfo);
        spuInfoService.saveSpuInfo(vo);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:spuinfo:update")
    public R update(@RequestBody SpuInfoEntity spuInfo){
		spuInfoService.updateById(spuInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:spuinfo:delete")
    public R delete(@RequestBody Long[] ids){
		spuInfoService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
