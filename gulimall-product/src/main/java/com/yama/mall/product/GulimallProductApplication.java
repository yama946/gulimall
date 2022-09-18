package com.yama.mall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 1、整合mybatis-plus
 *      1）导入依赖
 *          <dependency>
 *             <groupId>com.baomidou</groupId>
 *             <artifactId>mybatis-plus-boot-starter</artifactId>
 *             <version>3.4.3.1</version>
 *         </dependency>
 *      2）配置
 *          1、配置数据源
 *              1）、导入数据库的驱动
 *              2）、在application.yml中配置数据源相关信息
 *          2、配置mybatis-plus：
 *              1）、使用@Mapperscan扫描(配置@Mapper注解可以配置此注解)
 *              2）、配置maaper.xml文件位置
 *              3）、配置主键自增
 *
 * 2、逻辑删除步骤
 *  1）配置全局逻辑删除规则（高版本可以直接指定字段，不需要配置第三步）
 *  2）配置逻辑删除组件bean（版本3.1以上不用配置）
 *  3）配置@TableLogic注解给指定成员变量
 */
/**
 * spring-cloud-oss使用流程
 * 1、导入依赖：spring-cloud-alibaba版本必须是2.1.0.RELEASE
 *  其他版本springboot、springcloud、springcloudalibaba需要匹配其他依赖，本依赖不可用
 *         <dependency>
 *             <groupId>com.alibaba.cloud</groupId>
 *             <artifactId>spring-cloud-starter-alicloud-oss</artifactId>
 *         </dependency>
 * 2、在配置文件中配置secret key、endpoinst等信息
 * 3、在需要上传等操作的地方@Autoweird OssClient对象，直接进行操作
 */
/**
 * 3、JSR303服务器端表单校验
 * 双端验证的原因：
 * 前端验证是防止用户异常输入，服务器后端校验是为了防止用户使用postman等其他请求方式发送请求，跳过前端校验实现非法请求
 * 使用步骤：
 * 1）给前端对接的实体类Bean添加校验注解(属于javax.validation.constraints包下是规范，也可以存在其他实现的注解)
 *
 * 自定义校验规则使用@Pattern(regexp = "/^[a-zA-Z]$/")其中为regexp属性传递一个满足自定义规则的正则表达式
 *
 * 注意：对于注解标注的成员属性，有的注解不能使用在某些数据类型上，使用注解是需要参照注释说明
 *
 * 注意：注解中的提示信息message是在ValidationMessages.properties（英文）、ValidationMessages_zh_CN.properties(中文)文件中动态提取的
 *     message属性：自定义校验错误信息
 *     @NotBlank(message="品牌名不能为空")
 *    private String name;
 * 2)、在controller的方法中，需要校验的地方添加@Valid注解
 *    public R save(@Valid @RequestBody BrandEntity brand)
 *    校验失败后默认会有响应信息返回，但是不满足需求，我们需要自定义校验返回对象
 * 3）在校验的bean后，紧随着添加一个BindingResult,就可以获取校验返回结果，用户我们自定义封装结果
 *    public R save(@Valid @RequestBody BrandEntity brand, BindingResult result)
 * 自定义封装代码：
 *     @RequestMapping("/save")
 *     //@RequiresPermissions("product:brand:save")
 *     public R save(@Valid @RequestBody BrandEntity brand, BindingResult result){
 *         //判断校验结果是否存在错误
 *         if(result.hasErrors()){
 *             HashMap<String,String> map = new HashMap<>();
 *             //1.获取校验错误结果
 *             result.getFieldErrors().forEach(error->{
 *                 //FieldError 获得错误提示
 *                 String message = error.getDefaultMessage();
 *                 //获得错误的成员属性信息
 *                 String field = error.getField();
 *                 map.put(field,message);
 *             });
 *             return R.error(404,"提交数据不合法").put("data",map);
 *         }else {
 *             //校验成功执行以下代码
 *             brandService.save(brand);
 *             return R.ok();
 *         }
 *     }
 */
/**
 * 当校验异常是我们如果在每个方法进行判断然后自定义封装，进行返回结果，会造成代码冗余现象
 * 使用SpringMVC提供的异常同一处理机制，使用注解异常配置：@ControllerAdvice
 */
/**
 * 5、模板引擎
 * 1）、thymeleaf-starter导入，并配置缓存关闭，实时看到修改结果，部署时开启
 * 2）、静态资源都放在static文件夹下就可以按照路径直接访问
 * 3）、页面放在templates下，直接访问
 *      springboot，访问项目的时候，默认找index
 *      可以在源码WebMvcAutoConfiguration自动配置类中找到来源
 */
/**
 * 6、整合redisson作为分布式锁等功能的框架
 *      1).引入依赖
 *         <dependency>
 *             <groupId>org.redisson</groupId>
 *             <artifactId>redisson</artifactId>
 *             <version>3.12.0</version>
 *         </dependency>
 *      2).配置redisson---见MyRedissonConfig
 * 7、整合springCache简化缓存开发
 *      1).引入springcache依赖，同时需要引入redis作为缓存管理器
 *          spring-boot-starter-cache，spring-boot-starter-data-redis
 *      2).配置springcache
 *          1.启动器自动配置了那些东西，
 *       ---CacheAutoConfiguration会自动导入RedisCacheConfiguration，并自动配置好缓存管理器RedisCacheManager
 *          2.自己配置了那些，见application.properties
 *      3).测试使用缓存
 *         @Cacheable: Triggers cache population.触发将数据保存到缓存中的操作
 *         @Cacheable({"catalog","brand"}):可以同时放到多个分区中
 *          每一个需要缓存的数据我们都来指定要放到那个名字的缓存。【缓存的名字相当于缓存的分区（按照业务类型划分）】
 *          方法上：代表当前方法的结果需要缓存，如果缓存中有，方法不用调用；如果缓存中没有，会调用方法，最后将方法的结果放入缓存中保存。
 *         @CacheEvict: Triggers cache eviction.触发将数据从缓存中删除的操作
 *         @CachePut: Updates the cache without interfering with the method execution.不影响方法执行更新缓存
 *          注意：该注解作用在方法上需要方法具有返回值，才能够重新存放，一般更新都没有也就不支持双写模式，所以一般会使用失效模式
 *         @Caching: Regroups multiple cache operations to be applied on a method.组合以上多个操作
 *         @CacheConfig: Shares some common cache-related settings at class-level.在类级别共享缓存的相同配置
 *          1：开启缓存功能---@EnableCaching
 *          2：只需要使用注解就能完成缓存操作
 *      4).原理：想要配置缓存中的valu使用json序列化
 *          CacheAutoConfiguration --->RedisCacheConfiguration --->
 *          自动配置了RedisCacheManager ---> 初始化所有的缓存 ---> 每个缓存决定使用什么配置
 *          -->如果RedisCacheConfiguration配置有就用，没有就自动配置使用默认
 *          -->想改缓存的配置，只需要给容器中放一个RedisCacheConfiguration即可
 *          -->就会应用到当前RedisCacheManager管理的所有缓存分区中。
 * 8、SpringCache的不足
 *  1）、读模式
 *      缓存穿透：查询一个null数据。解决方案：缓存空数据
 *      缓存击穿：大量并发进来同时查询一个正好过期的数据。解决方案：加锁 ? 默认是无加锁的;使用sync = true来解决击穿问题(本地锁)
 *      缓存雪崩：大量的key同时过期。解决：加随机时间。加上过期时间
 *  2)、写模式：（缓存与数据库一致）
 *      1）、读写加锁。
 *      2）、引入Canal,感知到MySQL的更新去更新Redis
 *      3）、读多写多，直接去数据库查询就行
 *
 *  总结：
 *      常规数据（读多写少，即时性，一致性要求不高的数据，完全可以使用Spring-Cache）
 *      写模式(只要缓存的数据有过期时间就足够了)
 *      特殊数据：特殊设计
 *
 *  原理：
 *      CacheManager(RedisCacheManager)->Cache(RedisCache)->Cache负责缓存的读写
 */
//@EnableCaching //开启缓存功能，直接放到缓存配置中，就像开启事务直接放到Mybatisplus配置中
@EnableFeignClients(basePackages = "com.yama.mall.product.feign")
@RefreshScope
@EnableDiscoveryClient
@MapperScan(basePackages = {"com.yama.mall.product.dao","com.yama.mall.common.config"})
@SpringBootApplication
@ComponentScan(basePackages = "com.yama.mall")
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }

}
