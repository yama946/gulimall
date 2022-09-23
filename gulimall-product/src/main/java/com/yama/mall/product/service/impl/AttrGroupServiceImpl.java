package com.yama.mall.product.service.impl;

import com.yama.mall.product.entity.AttrEntity;
import com.yama.mall.product.entity.SpuInfoEntity;
import com.yama.mall.product.service.AttrService;
import com.yama.mall.product.service.SpuInfoService;
import com.yama.mall.product.vo.AttrGroupWithAttrsVO;
import com.yama.mall.product.vo.SpuItemAttrGroupVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yama.mall.common.utils.PageUtils;
import com.yama.mall.common.utils.Query;

import com.yama.mall.product.dao.AttrGroupDao;
import com.yama.mall.product.entity.AttrGroupEntity;
import com.yama.mall.product.service.AttrGroupService;
import org.springframework.util.StringUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private SpuInfoService spuInfoService;

    @Autowired
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCatelogId(Map<String, Object> params, Integer catelogId) {
        String key = (String) params.get("key");//关键词查询
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>();
        if (!StringUtils.isEmpty(key)){
            wrapper.and((obj)->{
                obj.eq("attr_group_id",key).or().like("attr_group_name",key);
            });
        }
        
        if (catelogId==0){
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params),
                    wrapper);
            return new PageUtils(page);
        }else{
            wrapper.eq("catelog_id",catelogId);
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
            return new PageUtils(page);
        }
    }

    /**
     * 根据分类id查出所有分组以及组内的属性，属性要求都是基本属性，也就是规格参数
     * @param catelogId
     * @return
     */
    @Override
    public List<AttrGroupWithAttrsVO> getAttrGroupWithattrsByCatelogId(Long catelogId) {
        //1.查出所有分组
        List<AttrGroupEntity> attrGroupEntitys = this.list(
                new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));

        //2.所有分组下关联的属性信息,并封装返回
        List<AttrGroupWithAttrsVO> collect = attrGroupEntitys.stream().map(group -> {
            AttrGroupWithAttrsVO attrsVO = new AttrGroupWithAttrsVO();
            BeanUtils.copyProperties(group,attrsVO);
            //使用已存在的方法
            List<AttrEntity> releationAttr = attrService.getReleationAttr(attrsVO.getAttrGroupId());
            attrsVO.setAttrs(releationAttr);
            return attrsVO;
        }).collect(Collectors.toList());
        return collect;
    }

    /**
     * 通过spuId获取属性组信息，以及属性组对应的属性信息
     * @param spuId
     * @param catalogId
     * @return
     */
    @Override
    public List<SpuItemAttrGroupVO> getAttrGroupWithattrsBySpuId(Long spuId, Long catalogId) {
        //使用联表查询数据
        List<SpuItemAttrGroupVO> spuItemAttrGroupVOS =
                this.getBaseMapper().getAttrGroupWithattrsBySpuId(spuId,catalogId);
        return spuItemAttrGroupVOS;
    }

}