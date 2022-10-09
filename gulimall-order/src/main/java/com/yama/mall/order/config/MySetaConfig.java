package com.yama.mall.order.config;


import org.springframework.context.annotation.Configuration;

/**
 * 代理数据源配置导致异常：Error updating database.Cause: io.seata.common.exception.NotSupportYetException
 * @description:
 * @date: 2022年10月07日 周五 9:59
 * @author: yama946
 */
@Configuration
public class MySetaConfig {
    //视频配置：seata的代理数据源方式，原来使用的是spring默认配置数据源

}
