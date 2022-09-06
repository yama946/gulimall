package com.yama.mall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.annotation.MapperScans;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.CrossOrigin;

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
@RefreshScope
@EnableDiscoveryClient
@MapperScan(basePackages = "com.yama.mall.product.dao")
@SpringBootApplication
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }

}
