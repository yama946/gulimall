package com.yama.mall.product.feign;

import com.yama.mall.common.to.SkuReductionTO;
import com.yama.mall.common.to.SpuBoundTO;
import com.yama.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 远程调用接口
 */
@FeignClient("gulimall-coupon")
public interface CouponFeignService {
    /**
     * 提供者方法：
     *     @RequestMapping("/save")
     *     public R save(@RequestBody SpuBoundsEntity spuBounds)
     *
     * 思考：提供者的方法名与远程接口方法名不同？提供者方法参数类型和参数名与远程调用接口不同？是否可以远程调用成功
     *
     * 答案：可以
     *
     * 远程调用过程解析：
     *      消费者调用：couponFeignService.saveSpuBounds(spuBoundTO);
     *      1、远程接口中的@RequestBody将这个对象转换成json，键值的形式；
     *      2、找到gulimall-coupon服务，给/coupon/spubounds/save发送post请求
     *         将上一步转的json放到请求体位置，发送请求
     *      3、提供者收到请求。请求体中的json数据。键值对应转换数据
     *      (@RequestBody SpuBoundsEntity spuBounds):将请求体中的json转换为SpuBoundsEntity对象。
     *      因为请求体中的数据是json字符串，就像是通过form表单提交被转换一样。
     *
     * 只要json数据模型是兼容的，双方服务无需使用同一个对象TO，也就是提供者无需也传递TO。
     * 这也就要求TO属性名与提供者的函数参数属性名相同，我们有时候就可以直接复制提供者的方法到远程接口中，无需修改。
     * 这也是为什么要新创建TO进行专门传输数据。
     *
     * 思考：
     * 一般不使用getMapping
     * 如果接口使用@GetMapping，消费者发送请求就会拼接字符串，就像是真的请求一样，通过feign进行连接调用服务。
     * 只不过浏览器是通过http协议进行连接，发送请求的。
     *
     *
     * @param spuBoundTO
     * @return
     */
    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTO spuBoundTO);

    @PostMapping("/coupon/skufullreduction/saveInfo")
    R saveSkuReduction(@RequestBody SkuReductionTO skuReductionTO);
}
