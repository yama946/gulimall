package com.yama.mall.product.service.impl;

import com.yama.mall.product.vo.AttrGroupRelationVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yama.mall.common.utils.PageUtils;
import com.yama.mall.common.utils.Query;

import com.yama.mall.product.dao.AttrAttrgroupRelationDao;
import com.yama.mall.product.entity.AttrAttrgroupRelationEntity;
import com.yama.mall.product.service.AttrAttrgroupRelationService;


@Service("attrAttrgroupRelationService")
public class AttrAttrgroupRelationServiceImpl extends ServiceImpl<AttrAttrgroupRelationDao, AttrAttrgroupRelationEntity> implements AttrAttrgroupRelationService {



    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrAttrgroupRelationEntity> page = this.page(
                new Query<AttrAttrgroupRelationEntity>().getPage(params),
                new QueryWrapper<AttrAttrgroupRelationEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 优化前实现方法
     * 删除属性分组与基本属性的关联关系
     * @param attrGroupRelationVO
     */
    /*@Override
    public void attrGroupRelationDelete(AttrGroupRelationVO[] attrGroupRelationVO) {
        Stream.of(attrGroupRelationVO).forEach((attrRelation)->{
            this.remove(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id",attrRelation.getAttrId())
                    .eq("attr_group_id",attrRelation.getAttrGroupId()));
        });
    }*/

    /**
     * 优化后实现
     * 通过自定义sql语句，优化sql执行语句，达到优化性能的目的
     * 删除属性分组与基本属性的关联关系
     * @param attrGroupRelationVO
     */
    @Override
    public void attrGroupRelationDelete(AttrGroupRelationVO[] attrGroupRelationVO) {
        List<AttrAttrgroupRelationEntity> attrAttrgroupRelations = Stream.of(attrGroupRelationVO).map((attrRelation) -> {
            AttrAttrgroupRelationEntity attrgroupRelation = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(attrRelation, attrgroupRelation);
            return attrgroupRelation;
        }).collect(Collectors.toList());
        baseMapper.deleteBatchRelation(attrAttrgroupRelations);
    }

    /**
     * 保存属性与组之间的关联关系
     * @param attrGroupRelationVO
     */
    @Override
    public void attrGroupRelationSave(AttrGroupRelationVO[] attrGroupRelationVO) {
        List<AttrAttrgroupRelationEntity> attrAttrgroupRelations = Stream.of(attrGroupRelationVO).map((attrRelation) -> {
            AttrAttrgroupRelationEntity attrgroupRelation = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(attrRelation, attrgroupRelation);
            return attrgroupRelation;
        }).collect(Collectors.toList());
        this.saveBatch(attrAttrgroupRelations);
    }
}