package com.yama.mall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.yama.mall.product.entity.ProductAttrValueEntity;
import com.yama.mall.product.service.ProductAttrValueService;
import com.yama.mall.product.vo.AttrRespVO;
import com.yama.mall.product.vo.AttrVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.yama.mall.product.service.AttrService;
import com.yama.mall.common.utils.PageUtils;
import com.yama.mall.common.utils.R;



/**
 * 商品属性
 *
 * @author yanmu
 * @email yanmu123@gmail.com
 * @date 2022-08-30 17:50:51
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;

    @Autowired
    private ProductAttrValueService productAttrValueService;


    /**
     * /product/attr/update/{spuId}
     * 回显spu规格后，进行修改操作的请求
     */
    @PostMapping("update/{spuId}")
    public R updateAttrListForSpu(@PathVariable("spuId")Long spuId,
                                  @RequestBody List<ProductAttrValueEntity> entities){
        productAttrValueService.updateAttrList(spuId,entities);
        return R.ok();
    }


    /**
     * /product/attr/base/listforspu/{spuId}
     * 获取spu规格,进行前端页面的回显
     */
    @GetMapping("base/listforspu/{spuId}")
    public R baseAttrListForSpu(@PathVariable("spuId")Long spuId){
        List<ProductAttrValueEntity> entities =  productAttrValueService.baseAttrListForSpu(spuId);
        return R.ok().put("data", null);
    }


    /**
     *获取属性规格参数的详细信息
     * /product/attr/sale/list/{catelogId}
     * /product/attr/base/list/{catelogId}
     * 两个请求合成一个请求方法
     * /product/attr/{attrType}/list/{catelogId}
     */
    @GetMapping("/{attrType}/list/{catelogId}")
    public R baseListAttr(@RequestParam Map<String, Object> params,
                          @PathVariable("catelogId")Integer catelogId,
                          @PathVariable("attrType")String attrType){
//        PageUtils page = attrService.queryBaseAttrPage(params,catelogId);
        PageUtils page = attrService.queryAttrPageByAttrType(params,catelogId,attrType);

        return R.ok().put("page", page);
    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:attr:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    //@RequiresPermissions("product:attr:info")
    public R info(@PathVariable("attrId") Long attrId){
//		AttrEntity attr = attrService.getById(attrId);
		AttrRespVO respVOs = attrService.getAttrInfo(attrId);

        return R.ok().put("attr", respVOs);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attr:save")
    public R save(@RequestBody AttrVO attrVO){
		attrService.saveAttr(attrVO);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attr:update")
    public R update(@RequestBody AttrVO attr){
		attrService.updateAttr(attr);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
