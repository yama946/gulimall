package com.yama.mall.search.service;

import com.yama.mall.search.vo.SearchParamVO;
import com.yama.mall.search.vo.SearchResultVO;

public interface MallSearchService {
    /**
     * 根据检索条件返回检索结果从es中
     * @param paramVO
     * @return
     */
    SearchResultVO search(SearchParamVO paramVO);
}
