package com.yama.mall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.yama.mall.product.entity.AttrEntity;
import com.yama.mall.product.service.AttrAttrgroupRelationService;
import com.yama.mall.product.service.AttrService;
import com.yama.mall.product.service.CategoryService;
import com.yama.mall.product.vo.AttrGroupRelationVO;
import jdk.nashorn.internal.objects.annotations.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.yama.mall.product.entity.AttrGroupEntity;
import com.yama.mall.product.service.AttrGroupService;
import com.yama.common.utils.PageUtils;
import com.yama.common.utils.R;



/**
 * 属性分组
 *
 * @author yanmu
 * @email yanmu123@gmail.com
 * @date 2022-08-30 17:50:51
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrAttrgroupRelationService attrgroupRelationService;


    /**
     * /product/attrgroup/attr/relation
     */
    @PostMapping("/attr/relation")
    //@RequiresPermissions("product:attrgroup:delete")
    public R attrRelationSave(@RequestBody AttrGroupRelationVO[] attrGroupRelationVO){
        attrgroupRelationService.attrGroupRelationSave(attrGroupRelationVO);
        return R.ok();
    }

    /**
     * 删除属性分组与属性之间的关系
     * /attr/relation/delete
     */
    @PostMapping("/attr/relation/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R attrRelationDelete(@RequestBody AttrGroupRelationVO[] attrGroupRelationVO){
        attrgroupRelationService.attrGroupRelationDelete(attrGroupRelationVO);
        return R.ok();
    }

    /**
     * 分组关联的属性
     * @param attrgroupId
     * @return
     */
    ///product/attrgroup/{attrgroupId}/attr/relation
    @GetMapping("/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupId")Long attrgroupId){
        List<AttrEntity> attrList =  attrService.getReleationAttr(attrgroupId);
        return R.ok().put("data",attrList);
    }
    /**
     * 分组未关联的属性
     * @param attrgroupId
     * @return
     */
    ///product/attrgroup/{attrgroupId}/noattr/relation
    @GetMapping("/{attrgroupId}/noattr/relation")
    public R attrNoRelation(@PathVariable("attrgroupId")Long attrgroupId,
                            @RequestParam Map<String, Object> params){
        PageUtils page =  attrService.getNoReleationAttr(attrgroupId,params);
        return R.ok().put("page",page);
    }


    /**
     * 列表
     */
    @RequestMapping("/list/{catelogId}")
    //@RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params,@PathVariable("catelogId")Integer catelogId){
//        PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPageByCatelogId(params, catelogId);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    //@RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
		//获取当前catelogid
        Long catelogId = attrGroup.getCatelogId();

        Long[] catelogPath = categoryService.getCatelogPath(catelogId);

        attrGroup.setCatelogPath(catelogPath);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }


}
