package com.yama.mall.search.service;

import com.yama.mall.common.to.SkuEsModel;

import java.io.IOException;
import java.util.List;

public interface ProductSaveService {
    Boolean prodectStatusUp(List<SkuEsModel> skuEsModelList) throws IOException;
}
