package com.yama.mall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.yama.mall.common.to.SkuEsModel;
import com.yama.mall.search.config.GulimallElasticSearchConfig;
import com.yama.mall.search.constant.EsConstant;
import com.yama.mall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductSaveServiceImpl implements ProductSaveService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 保存商品上架信息到es中
     * @param skuEsModelList
     */
    @Override
    public Boolean prodectStatusUp(List<SkuEsModel> skuEsModelList) throws IOException {
        //保存到es中
        //1.在es中建立索引,product

        //2.给es批量保存数据
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel skuEsModel : skuEsModelList){
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            indexRequest.id(skuEsModel.getSkuId().toString());
            String skuEsModelJson = JSON.toJSONString(skuEsModel);
            indexRequest.source(skuEsModelJson,XContentType.JSON);
            bulkRequest.add(indexRequest);
        }
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

        //TODO 分批批量保存结果，处理批量保存信息
        boolean hasFailures = bulk.hasFailures();
        List<String> collect = Arrays.asList(bulk.getItems()).stream().map(item -> item.getId()).collect(Collectors.toList());

        log.info("商品上架完成：{}",collect);

        return hasFailures;

    }
}
