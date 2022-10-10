package com.yama.mall.order.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.*;

import com.yama.mall.order.entity.OrderEntity;
import com.yama.mall.order.service.OrderService;
import com.yama.mall.common.utils.PageUtils;
import com.yama.mall.common.utils.R;



/**
 * 订单
 *
 * @author yanmu
 * @email yanmu123@gmail.com
 * @date 2022-08-30 18:08:51
 */
@RestController
@RequestMapping("order/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 远程查询订单的状态
     * @param orderSn
     * @return
     */
    @GetMapping("/status/{ordersn}")
    public R getOrderStatus(@PathVariable("ordersn") String orderSn){
        OrderEntity orderEntity = orderService.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        if (orderEntity!=null){
            Integer status = orderEntity.getStatus();
            return R.ok().setData(status);
        }else {
            return R.ok();
        }


    }



    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("order:order:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = orderService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("order:order:info")
    public R info(@PathVariable("id") Long id){
		OrderEntity order = orderService.getById(id);

        return R.ok().put("order", order);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("order:order:save")
    public R save(@RequestBody OrderEntity order){
		orderService.save(order);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("order:order:update")
    public R update(@RequestBody OrderEntity order){
		orderService.updateById(order);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("order:order:delete")
    public R delete(@RequestBody Long[] ids){
		orderService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
