package com.yama.mall.search.controller;

import com.yama.mall.search.service.MallSearchService;
import com.yama.mall.search.vo.SearchParamVO;
import com.yama.mall.search.vo.SearchResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SearchController {
    @Autowired
    private MallSearchService mallSearchService;

    @GetMapping("/list.html")
    public String listPage(SearchParamVO paramVO, ModelMap modelMap){
        SearchResultVO result = mallSearchService.search(paramVO);
        modelMap.addAttribute("result",result);
        return "list";
    }
}
