package com.yama.mall.product.web;

import com.yama.mall.product.entity.CategoryEntity;
import com.yama.mall.product.service.CategoryService;
import com.yama.mall.product.vo.Catelog2VO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {

    @Autowired
    private CategoryService categoryService;


    /**
     * 进行页面的跳转操作
     */
    @GetMapping({"/","/index.html"})
    public String indexPage(ModelMap modelMap){
        //1.查询出所有的以及分类，并保存到model中
        List<CategoryEntity> categoryEntities = categoryService.getLevel1Category();

        modelMap.addAttribute("categorys",categoryEntities);

        return "index";
    }

    /**
     * index/catalog.json
     * @return
     */
    @ResponseBody
    @GetMapping("index/catalog.json")
    public Map<String,List<Catelog2VO>> getCatalogJson(){
        Map<String,List<Catelog2VO>> map = categoryService.getCatalogJson();
        return map;
    }
}
