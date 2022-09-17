package com.yama.mall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yama.mall.product.entity.BrandEntity;
import com.yama.mall.product.service.BrandService;
import com.yama.mall.product.service.CategoryService;
import com.yama.mall.product.vo.BrandVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.yama.mall.product.entity.CategoryBrandRelationEntity;
import com.yama.mall.product.service.CategoryBrandRelationService;
import com.yama.mall.common.utils.PageUtils;
import com.yama.mall.common.utils.R;



/**
 * 品牌分类关联
 *
 * @author yanmu
 * @email yanmu123@gmail.com
 * @date 2022-08-30 17:50:51
 */
@RestController
@RequestMapping("product/categorybrandrelation")
public class CategoryBrandRelationController {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    /**
     * 获取分类关联的品牌信息
     * @param catId
     * @return
     *
     * 1.Controller:处理请求，接受和校验数据
     * 2.Service接受controller传来的数据，进行业务处理
     * 3.Controller接受Service处理完的数据封装页面指定的vo返回
     */
    ///product/categorybrandrelation/brands/list
    @GetMapping("/brands/list")
    public R relationBrandsList(@RequestParam(value = "catId",required = true) Long catId){
        List<BrandEntity> vos = categoryBrandRelationService.getBrandsByCateId(catId);
        List<BrandVO> collect = vos.stream().map(brandEntity -> {
            BrandVO brandVO = new BrandVO();
            brandVO.setBrandId(brandEntity.getBrandId());
            brandVO.setBrandName(brandEntity.getName());
            return brandVO;
        }).collect(Collectors.toList());
        return R.ok().put("data",collect);
    }




    /**
     * 获取当前品牌关联的所有列表分类
     * @param brandId
     * @return
     */
    @RequestMapping("/catelog/list")
    public R catelogList(@RequestParam("brandId") Long brandId){
        List<CategoryBrandRelationEntity> data = categoryBrandRelationService.list(
                new QueryWrapper<CategoryBrandRelationEntity>().eq("brand_id",brandId)
        );

        return R.ok().put("data",data);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:categorybrandrelation:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = categoryBrandRelationService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("product:categorybrandrelation:info")
    public R info(@PathVariable("id") Long id){
		CategoryBrandRelationEntity categoryBrandRelation = categoryBrandRelationService.getById(id);

        return R.ok().put("categoryBrandRelation", categoryBrandRelation);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:categorybrandrelation:save")
    public R save(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
        String brandName = brandService.getById(categoryBrandRelation.getBrandId()).getName();

        String catelogName = categoryService.getById(categoryBrandRelation.getCatelogId()).getName();

        categoryBrandRelation.setBrandName(brandName);

        categoryBrandRelation.setCatelogName(catelogName);
		categoryBrandRelationService.save(categoryBrandRelation);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:categorybrandrelation:update")
    public R update(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
		categoryBrandRelationService.updateById(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:categorybrandrelation:delete")
    public R delete(@RequestBody Long[] ids){
		categoryBrandRelationService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
