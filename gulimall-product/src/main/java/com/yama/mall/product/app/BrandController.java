package com.yama.mall.product.controller;

import java.util.Arrays;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.yama.mall.common.valid.AddGroup;
import com.yama.mall.common.valid.UpdateStatusGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yama.mall.product.entity.BrandEntity;
import com.yama.mall.product.service.BrandService;
import com.yama.mall.common.utils.PageUtils;
import com.yama.mall.common.utils.R;


/**
 * 品牌
 *
 * @author yanmu
 * @email yanmu123@gmail.com
 * @date 2022-08-30 17:50:51
 */
@RestController
@RequestMapping("/product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:brand:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    //@RequiresPermissions("product:brand:info")
    public R info(@PathVariable("brandId") Long brandId){
		BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     * 不使用BindingResult进行校验异常，限制直接使用同一异常处理，从异常类中获取BindingResult
     * 进行封装返回异常。
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:brand:save")
//    public R save(@Valid @RequestBody BrandEntity brand/*, BindingResult result*/){
    public R save(@Validated({AddGroup.class}) @RequestBody BrandEntity brand/*, BindingResult result*/){
        //判断校验结果是否存在错误
        /*if(result.hasErrors()){
            HashMap<String,String> map = new HashMap<>();
            //1.获取校验错误结果
            result.getFieldErrors().forEach(error->{
                //FieldError 获得错误提示
                String message = error.getDefaultMessage();
                //获得错误的成员属性信息
                String field = error.getField();
                map.put(field,message);
            });
            return R.error(404,"提交数据不合法").put("data",map);
        }else {*/
            //校验成功执行以下代码
            brandService.save(brand);
            return R.ok();
        /*}*/
    }

    /**
     * 修改,同步更新其他关联表中的数据
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:brand:update")
    public R update(@RequestBody BrandEntity brand){
		brandService.updateDetail(brand);

        return R.ok();
    }

    /**
     * 修改状态
     */
    @RequestMapping("/update/status")
    public R updateStatus(@Validated(UpdateStatusGroup.class) @RequestBody BrandEntity brand){
        brandService.updateById(brand);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:brand:delete")
    public R delete(@RequestBody Long[] brandIds){
		brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
