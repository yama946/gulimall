package com.yama.mall.order.vo;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @description: 提交订单页面模型VO
 * @date: 2022年10月04日 周二 18:52
 * @author: yama946
 */
public class OrderConfirmVO {

    //用户收货地址------>ums_member_receive_address表
    @Getter @Setter
    List<MemberAddressVO> address;

    //用户购买商品信息
    @Getter @Setter
    List<OrderItemVO> items;

    //发票记录.......

    //优惠券积分信息......
    @Getter @Setter
    Integer integration;

    /**
     *
     * 防止订单重复提交，我们为订单设置一个orderToken值，进行区分订单。
     */
    @Getter @Setter
    String orderToken;

    /**
     * 直接将库存信息保存到confirm中
     */
    @Getter @Setter
    Map<Long,Boolean> stocks;

    //订单总金额
//    BigDecimal totalPrice;

    /**
     * 获取商品数量，get方法对于thymeleaf相当于存在count属性
     * @return
     */
    public Integer getCount(){
        int i =0;
        if (items!=null){
            for (OrderItemVO item : items){
                i+=item.getCount();
            }
        }
        return i;
    }

    /**
     * 获取商品总价
     * @return
     */
    public BigDecimal getTotalPrice() {
        //总价
        BigDecimal totalPrice = new BigDecimal("0");
        //判断商品，根据商品数量计算
        if (items!=null){
            for (OrderItemVO item : items){
                BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                totalPrice = totalPrice.add(multiply);
            }
        }
        return totalPrice;
    }

    //实际支付价格
//    BigDecimal payPrice;

    /**
     * 获取支付价格
     * @return
     */
    public BigDecimal getPayPrice(){
        return this.getTotalPrice();
    }
}
