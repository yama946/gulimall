package com.yama.mall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
