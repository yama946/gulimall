package com.yama.mall.search.controller;

import com.yama.mall.common.exception.BizCodeEnume;
import com.yama.mall.common.to.SkuEsModel;
import com.yama.mall.common.utils.R;
import com.yama.mall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * 与es保存相关的请求
 */

@Slf4j
@RequestMapping("/search/save")
@RestController
public class ElasticSaveController {
    @Autowired
    private ProductSaveService productSaveService;

    /**
     * 商品上架功能，保存商品信息到es中
     * @param skuEsModelList
     * @return
     */
    @PostMapping("/product")
    public R productUp(@RequestBody List<SkuEsModel> skuEsModelList){
        try {
            productSaveService.prodectStatusUp(skuEsModelList);
        } catch (IOException e) {
            log.error("ElasticSaveController商品上架异常：{}",e);
            return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(),BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg());
        }
        return R.ok();
    }

}
