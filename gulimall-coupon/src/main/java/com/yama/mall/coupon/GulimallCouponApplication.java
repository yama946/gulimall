package com.yama.mall.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * -----如何使用nacos作为配置中心统一管理配置---------
 * 1、导入依赖
 *         <dependency>
 *             <groupId>com.alibaba.cloud</groupId>
 *             <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
 *         </dependency>
 * 2.创建bootstrap.properties文件并配置
 *      spring.application.name=gulimall-coupon
*       spring.cloud.nacos.config.server-addr=124.223.201.122:8800
 * 3.需要给配置中心添加一个叫(gulimall-coupon.properties)数据集（Data Id）.DataID默认规则：应用名.properties
 * 4.给应用名.properties添加需要动态刷新的配置
 * 5.动态配置所需要的注解：
 * @RefreshScope：动态获取并刷新配置
 * @Value("${配置项}")
 *
 * 注意：如果配置中心和当前应用的配置文件中配置了相同的项，优先使用配置中心中的配置
 *
 * ------相关细节---------
 * 1）命名空间
 * 作用：配置隔离
 * 默认：public(命名空间)；默认新增的所有配置都在public空间
 * 场景1：
 *      基于开发，测试，生产：利用命名空间来做环境隔离
 * ·    注意：在bootstrap.properties中配置，需要使用那个命名空间，配置如下：
 *          #配置命名空间，默认规则：spring.cloud.nacos.config.namespace=命名空间ID
 *          spring.cloud.nacos.config.namespace=8f9e2c23-8bc6-47b0-ae86-246cf4e212d2
 * 场景2：
 *      每个微服务之间互相隔离配置，每个微服务都创建自己的命名空间，而不是公用一个test，或者dev空间，配置上没有变化。
 *      只是需要将命名空间id，配置成为自己创建的命名空间id。
 *
 *2）配置集：所有配置的集合叫做配置集。
 *
 * 3)配置集ID：类似文件名，也就是nacos中配置的配置文件的名字
 * 默认命名规则：应用名.properties
 *
 * 4)配置分组
 *          默认所有的配置集都属于：DEFAULT_GROUP；
 *          应用场景：比如，双11使用名为<<double 11>>的配置组，618使用另一组配置
 *          在创建配置文件的时候可以自己指定所在的配置组。
 *      在bootstrap.properties中配置当前项目要使用的配置组名，默认为：DEFAULT_GROUP
 *      #配置当前配置组
*                     spring.cloud.nacos.config.group=double11
 *
 *
 * 当前项目配置的要求：
 * 每个微服务创建自己的命名空间，使用配置分组区分配置环境：test、dev、prop
 *
 * 3、同时加载多个配置集
 * 1）：微服务任何配置信息，任何配置都可以放置到配置中心中，最终项目中只有一个bootstrap.properties文件
 * 2）：只需要在bootstrap.properties说明加载配置中心中那些配置文件即可
 * 3）：@Value,@ConfigurationProperties这些注解也可以远程获取到配置中心中的配置
 * 4）：以前springboot任何方法从配置中心中获取值，都能使用。
 *  注意：  配置中心有的配置项优先使用配置中心的
 *  #配置多个数据集
 * spring.cloud.nacos.config.extension-configs[0].data-id=datasource.yml
 * #配置集所在的组
 * spring.cloud.nacos.config.extension-configs[0].group=dev
 * #是否动态刷新
 * spring.cloud.nacos.config.extension-configs[0].refresh=true
 */
@RefreshScope
@EnableDiscoveryClient
@SpringBootApplication
public class GulimallCouponApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallCouponApplication.class, args);
    }

}
