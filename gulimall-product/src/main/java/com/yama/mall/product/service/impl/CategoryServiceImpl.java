package com.yama.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yama.common.utils.PageUtils;
import com.yama.common.utils.Query;
import com.yama.mall.product.dao.CategoryDao;
import com.yama.mall.product.entity.CategoryEntity;
import com.yama.mall.product.service.CategoryBrandRelationService;
import com.yama.mall.product.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;


@Service("categoryService")
@Slf4j
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }
    /**
     *  查出所有分类以及子分类，以树形结构组装起来
     */
    @Override
    public List<CategoryEntity> listWithTree() {
        //1.查出所有分类数据
        List<CategoryEntity> entities = baseMapper.selectList(null);
        //2.组装父子的树形接口，在实体类中添加集合

        //2.1.找到所有的一级分类
        List<CategoryEntity> level1Menu = entities
                .stream()
                .filter(categoryEntities -> categoryEntities.getParentCid() == 0)
                .map((menu) -> {
                    menu.setChildren(getChildrens(menu, entities));
                    return menu;
                })
                .sorted((menu1, menu2) -> (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort()))
                .collect(Collectors.toList());
        return level1Menu;
    }
    public List<CategoryEntity> getChildrens(CategoryEntity root,List<CategoryEntity> all){
        List<CategoryEntity> childrens = all
                .stream()
//                .filter(categoryEntity -> categoryEntity.getParentCid() == root.getCatId())
                .filter(categoryEntity -> Objects.equals(categoryEntity.getParentCid(),root.getCatId()))
                .map(categoryEntity -> {
                        categoryEntity.setChildren(getChildrens(categoryEntity,all));
                    return categoryEntity;
                })
                .sorted((menu1,menu2)-> (menu1.getSort()==null?0:menu1.getSort())-(menu2.getSort()==null?0:menu2.getSort()))
                .collect(Collectors.toList());
        return childrens;
    }

    /**
     * 删除菜单
     * @param asList
     */
    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO 判断删除菜单是否被引用
        baseMapper.deleteBatchIds(asList);
    }

    /**
     * 通用写法
     * @param catelogId
     * @return
     */
    /*@Override
    public Long[] getCatelogPath(Long catelogId) {
        List<Long> list = new ArrayList<>();

        list.add(catelogId);

        CategoryEntity byId = baseMapper.selectById(catelogId);

        while(byId.getParentCid()!=0){
            long parentCid = byId.getParentCid();
            list.add(0,parentCid);
            byId=baseMapper.selectById(parentCid);
        }
        return  list.toArray(new Long[list.size()]);
    }*/

    /**
     * 更新关联表数据
     * @param category
     */
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        if (!StringUtils.isEmpty(category.getName())){
            categoryBrandRelationService.updateCatetory(category.getCatId(),category.getName());
        }
    }

    /**
     * 递归写法
     * @param catelogId
     * @return
     */
    @Override
    public Long[] getCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();

        List<Long> parentPath = findParentPath(catelogId, paths);

        //集合反转
        Collections.reverse(parentPath);

        return  parentPath.toArray(new Long[parentPath.size()]);
    }

    private List<Long> findParentPath(Long catelogId,List<Long> paths){
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid()!=0){
            findParentPath(byId.getParentCid(),paths);
        }
        return paths;
    }
}