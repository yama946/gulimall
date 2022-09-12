package com.yama.mall.search.config;


import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 如果是7.0之前的es使用spring data操作es的依赖，只需要通过yml进行配置即可，
 * 但是我们使用高阶api需要我们自己进行相关配置
 *
 * elasticsearch整合springboot的步骤：（整合后使用操作es）
 * 1、导入依赖
 * 2、编写配置（参考官方文档initialization模块）,也就是进行初始化操作
 *      也就是给容器中注入一个RestHighLevelClient对象用来操作es
 * 3、操作es
 *
 */
@Configuration
public class GulimallElasticSearchConfig {


    @Value("${es.hostname}")
    private String hostname;

    @Value("${es.port}")
    private int port;

    /**
     * RequestOptions,es请求可选项，用于es安全访问时，携带一些请求信息进行验证，
     * 官方建议做成单实例，下面为官方文档中内容。
     */
    public static final RequestOptions COMMON_OPTIONS;

    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
//        builder.addHeader("Authorization", "Bearer " + TOKEN);
//        builder.setHttpAsyncResponseConsumerFactory(
//                new HttpAsyncResponseConsumerFactory
//                        .HeapBufferedResponseConsumerFactory(30 * 1024 * 1024 * 1024));
        COMMON_OPTIONS = builder.build();
    }



    @Bean
    public RestHighLevelClient esClient(){
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        //创建连接，当前只有一个节点，配置一个
                        new HttpHost(hostname, port, "http")));
                return client;
    }


}
