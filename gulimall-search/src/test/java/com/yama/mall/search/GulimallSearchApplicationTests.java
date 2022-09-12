package com.yama.mall.search;

import com.alibaba.fastjson.JSON;
import com.yama.mall.search.config.GulimallElasticSearchConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class GulimallSearchApplicationTests {


    @Resource(name = "esClient")
    private RestHighLevelClient client;

    /**
     * es中复杂检索操作
     * @throws IOException
     */
    @Test
    public void searchData() throws IOException{
        /**
         *         SearchRequest searchRequest = new SearchRequest();
         *         SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
         *         searchSourceBuilder.query(QueryBuilders.matchAllQuery());
         *         searchRequest.source(searchSourceBuilder);
         */
        //1.创建SearchRequest 对象
        SearchRequest searchRequest = new SearchRequest();
        //指定检索的索引
        searchRequest.indices("bank");
        //指定检索的条件，DSL
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //封装查询结果条件
        searchSourceBuilder.query(QueryBuilders.matchQuery("address","mill"));
        //封装聚合检索条件
        //按照年龄的值分布进行聚合
        AggregationBuilder aggregationBuilder = AggregationBuilders.
                terms("ageAgg").field("age").size(10);
        searchSourceBuilder.aggregation(aggregationBuilder);



        searchRequest.source(searchSourceBuilder);
        log.info("封装好的检索条件:{}",searchSourceBuilder);

        //2.指定检索请求
        /**
         * SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
         */
        SearchResponse search = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
        log.info("检索请求结果：{}",search);

        //3.分析检索结果


    }




    /**
     * 存储数据到es中
     */
    @Test
    public void indexData() throws IOException {
        /**
         * IndexRequest request = new IndexRequest("posts");
         * request.id("1");
         * String jsonString = "{" +
         *         "\"user\":\"kimchy\"," +
         *         "\"postDate\":\"2013-01-30\"," +
         *         "\"message\":\"trying out Elasticsearch\"" +
         *         "}";
         * request.source(jsonString, XContentType.JSON);
         */
        IndexRequest request = new IndexRequest("users");
        request.id("1");
        User user = new User("李易峰", 34, "男");
        String userJson = JSON.toJSONString(user);
        request.source(userJson, XContentType.JSON);
        /**
         * 发送请求的方式，同步，异步
         * IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);
         */
        IndexResponse indexResponse = client.index(request, GulimallElasticSearchConfig.COMMON_OPTIONS);
        //直接打印响应结果
        log.info("响应结果:{}",indexResponse);

        //响应结果中获取数据
        String index = indexResponse.getIndex();
        String id = indexResponse.getId();
        if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {
            log.info("第一次创建");
        } else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
            log.info("处理(如果需要)文档重写为已经存在的情况");
        }
        ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();
        if (shardInfo.getTotal() != shardInfo.getSuccessful()) {
            log.info("处理成功碎片数量少于总碎片的情况");
        }
        if (shardInfo.getFailed() > 0) {
            for (ReplicationResponse.ShardInfo.Failure failure :
                    shardInfo.getFailures()) {
                log.info("处理潜在的故障");
                String reason = failure.reason();
            }
        }

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class User{
        private String name;

        private Integer age;

        private String agender;
    }

    @Test
    public void contextLoads() {
        System.out.println(client);
    }

}
