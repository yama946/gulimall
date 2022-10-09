package com.yama.mall.ware.controller;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.yama.mall.ware.vo.FareVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.yama.mall.ware.entity.WareInfoEntity;
import com.yama.mall.ware.service.WareInfoService;
import com.yama.mall.common.utils.PageUtils;
import com.yama.mall.common.utils.R;



/**
 * 仓库信息
 *
 * @author yanmu
 * @email yanmu123@gmail.com
 * @date 2022-08-30 18:17:04
 */
@RestController
@RequestMapping("ware/wareinfo")
public class WareInfoController {
    @Autowired
    private WareInfoService wareInfoService;

    /**
     * 查询商品的运费信息
     * @param addrId 收获地址的Id
     * @return
     */
    @GetMapping("/fare")
    public R getFare(@RequestParam("addrId") Long addrId){
        FareVO fare = wareInfoService.getFare(addrId);
        return R.ok().setData(fare);
    }

    /**
     * /ware/wareinfo/list
     * 列表
     * 模糊查询，并分页列表显示
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:wareinfo:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareInfoService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:wareinfo:info")
    public R info(@PathVariable("id") Long id){
		WareInfoEntity wareInfo = wareInfoService.getById(id);

        return R.ok().put("wareInfo", wareInfo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:wareinfo:save")
    public R save(@RequestBody WareInfoEntity wareInfo){
		wareInfoService.save(wareInfo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:wareinfo:update")
    public R update(@RequestBody WareInfoEntity wareInfo){
		wareInfoService.updateById(wareInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:wareinfo:delete")
    public R delete(@RequestBody Long[] ids){
		wareInfoService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
