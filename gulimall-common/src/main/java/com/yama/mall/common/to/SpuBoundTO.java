package com.yama.mall.common.to;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * couponFeignService.saveSpuBounds(SpuBoundTO)
 *
 *      其中远程调用接口couponFeignService调用要传递数据，
 *      对象数据从Consumer传递到Producter，会先转化为JSON进行传递，之后再转换为对象这个过程。
 *      我们将这种数据传输模型称为“TO”
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpuBoundTO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * TO中的数据一般为远程可以获取，并且是提供者需要保存的-----这是句废话
     *
     * 需要进行远程调用保存的操作一般都还是跨数据库的，也有的是服务划分太细导致的
     *
     * 将想要通过远程服务保存的跨数据库数据，全部需要封装的字段组成一个对象即可
     */

    /**
     *
     */
    private Long spuId;
    /**
     * 成长积分
     */
    private BigDecimal growBounds;
    /**
     * 购物积分
     */
    private BigDecimal buyBounds;

}
