package com.yama.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.yama.common.constant.ProductConstant;
import com.yama.mall.product.dao.AttrAttrgroupRelationDao;
import com.yama.mall.product.dao.AttrGroupDao;
import com.yama.mall.product.dao.CategoryDao;
import com.yama.mall.product.entity.AttrAttrgroupRelationEntity;
import com.yama.mall.product.entity.AttrGroupEntity;
import com.yama.mall.product.entity.CategoryEntity;
import com.yama.mall.product.vo.AttrRespVO;
import com.yama.mall.product.vo.AttrVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yama.common.utils.PageUtils;
import com.yama.common.utils.Query;

import com.yama.mall.product.dao.AttrDao;
import com.yama.mall.product.entity.AttrEntity;
import com.yama.mall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("attrService")
@Slf4j
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    private AttrGroupDao attrGroupDao;

    @Autowired
    private CategoryDao categoryDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 将vo信息进行保存操作，并保存到关联表中
     * @param attrVO
     */
    @Transactional
    @Override
    public void saveAttr(AttrVO attrVO) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attrVO,attrEntity);
        this.save(attrEntity);
        //保存关联表数据
        if (attrVO.getAttrType()== ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() && attrVO.getAttrGroupId()!=null){
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationEntity.setAttrGroupId(attrVO.getAttrGroupId());
            attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
        }
    }

    /**
     * 获取属性的分页数据，并兼顾数据查询
     * @param params
     * @param catelogId
     * @return
     */
    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Integer catelogId) {
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>();
        if (catelogId!=0){
            wrapper.eq("catelog_id",catelogId);
        }
        String key = (String) params.get("key");//关键词查询
        if (!StringUtils.isEmpty(key)){
            wrapper.and((obj)->{
                obj.eq("attr_id",key).or().like("attr_name",key);
            });
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);
        //向分页数据中继续添加数据
        PageUtils pageUtils = new PageUtils(page);
        //分页对象中获取包裹的实体类对象
        List<AttrEntity> records = page.getRecords();
//            List<AttrEntity> list = (List<AttrEntity>) pageUtils.getList();
        List<AttrRespVO> respVOs = records.stream().map((attrEntity) -> {
            //将集合中的对象进行转换对象返回
            AttrRespVO attrRespVO = new AttrRespVO();
            //赋值属性
            BeanUtils.copyProperties(attrEntity, attrRespVO);
            //获取名字，从中间表进行查询获取数据，但是不联表查询
            AttrAttrgroupRelationEntity attrId =
                    attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
            if (attrId != null) {
                AttrGroupEntity attrGroup = attrGroupDao.selectById(attrId.getAttrGroupId());
                log.debug("当前对象：{}",attrGroup);
                attrRespVO.setGroupName(attrGroup.getAttrGroupName());
                log.debug("当前对象：{}",attrRespVO);
            }
            //获取分类的名字
            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
            attrRespVO.setCatelogName(categoryEntity.getName());
            return attrRespVO;
        }).collect(Collectors.toList());
        //将新的集合封装进分页对象中
        pageUtils.setList(respVOs);
        return pageUtils;
    }

    /**
     * 回显数据，修改操作
     * @param attrId
     * @return
     */
    @Transactional
    @Override
    public AttrRespVO getAttrInfo(Long attrId) {
        AttrEntity attrEntity = this.getById(attrId);
        AttrRespVO respVO = new AttrRespVO();
        //属性赋值
        BeanUtils.copyProperties(attrEntity,respVO);
        //获取Groupid进行设置
        if (attrEntity.getAttrType()==ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()){
            AttrAttrgroupRelationEntity attrGroupId = attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            if (attrGroupId!=null){
                respVO.setAttrGroupId(attrGroupId.getAttrGroupId());
                AttrGroupEntity attrGroup = attrGroupDao.selectById(attrGroupId.getAttrGroupId());
                respVO.setGroupName(attrGroup.getAttrGroupName());
            }
        }
        //获取catelog的数组
        List<Long> catelogIds = new ArrayList<>();
        CategoryEntity categoryParent = categoryDao.selectById(attrEntity.getCatelogId());
        respVO.setCatelogName(categoryParent.getName());
        catelogIds.add(attrEntity.getCatelogId());
        while (categoryParent.getParentCid()!=0){
            catelogIds.add(0,categoryParent.getParentCid());
           categoryParent = categoryDao.selectById(categoryParent.getParentCid());
        }
        respVO.setCatelogPath(catelogIds.toArray(new Long[catelogIds.size()]));
        return respVO;
    }

    /**
     * 更新，并更新关联表表数据
     * @param attr
     */
    @Transactional
    @Override
    public void updateAttr(AttrVO attr) {
        //复制属性到attrEntity中
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr,attrEntity);
        this.updateById(attrEntity);
        //当是基本属性时，才进行关联表的更新
        if (attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()){
            //判断是否存在分组，如果存在则更新，否则是保存
            Integer count = attrAttrgroupRelationDao.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>()
                    .eq("attr_id", attr.getAttrId()));
            AttrAttrgroupRelationEntity attrgroupRelation = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(attr,attrgroupRelation);
            if (count>0){
                //执行更新操作
                //在中间表中更新attrgroup_id
                attrAttrgroupRelationDao.update(attrgroupRelation,
                        new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id",attr.getAttrId()));
            }else{
                //执行保存操作
                attrAttrgroupRelationDao.insert(attrgroupRelation);
            }
        }
    }

    /**
     * 根据attrType判断查询的属性类型并分页
     * @param params
     * @param catelogId
     * @param attrType
     * @return
     */
    @Transactional
    @Override
    public PageUtils queryAttrPageByAttrType(Map<String, Object> params, Integer catelogId, String attrType) {
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>();
        wrapper.eq("attr_type",
                "base".equalsIgnoreCase(attrType)?ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode():
                ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());
        if (catelogId!=0){
            wrapper.eq("catelog_id",catelogId);
        }
        String key = (String) params.get("key");//关键词查询
        if (!StringUtils.isEmpty(key)){
            wrapper.and((obj)->{
                obj.eq("attr_id",key).or().like("attr_name",key);
            });
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);
        //向分页数据中继续添加数据
        PageUtils pageUtils = new PageUtils(page);
        //分页对象中获取包裹的实体类对象
        List<AttrEntity> records = page.getRecords();
        List<AttrRespVO> respVOs = records.stream().map((attrEntity) -> {
            //将集合中的对象进行转换对象返回
            AttrRespVO attrRespVO = new AttrRespVO();
            //赋值属性
            BeanUtils.copyProperties(attrEntity, attrRespVO);
            //获取名字，从中间表进行查询获取数据，但是不联表查询
            if ("base".equalsIgnoreCase(attrType)){
                AttrAttrgroupRelationEntity attrId =
                        attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
                if (attrId != null && attrId.getAttrGroupId()!=null) {
                    AttrGroupEntity attrGroup = attrGroupDao.selectById(attrId.getAttrGroupId());
                    log.debug("当前对象：{}",attrGroup);
                    attrRespVO.setGroupName(attrGroup.getAttrGroupName());
                    log.debug("当前对象：{}",attrRespVO);
                }
            }
            //获取分类的名字
            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
            attrRespVO.setCatelogName(categoryEntity.getName());
            return attrRespVO;
        }).collect(Collectors.toList());
        //将新的集合封装进分页对象中
        pageUtils.setList(respVOs);
        return pageUtils;
    }

    /**
     * 根据分组id获取所有的基本属性
     * @param attrgroupId
     * @return
     */
    @Transactional
    @Override
    public List<AttrEntity> getReleationAttr(Long attrgroupId) {
        //1、从中间表中获取所有关联属性的id
        List<AttrAttrgroupRelationEntity> attrgroupRelationEntities = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>()
                .eq("attr_group_id", attrgroupId));
        //2.从获取的集合中进行遍历
        List<AttrEntity> attrList = attrgroupRelationEntities.stream().map((attrgroupRelationEntitie) -> {
            Long attrId = attrgroupRelationEntitie.getAttrId();
            AttrEntity attrEntity = this.getById(attrId);
            return attrEntity;
        }).collect(Collectors.toList());
        return attrList;
    }

    /**
     * 根据分组id获取未关联属性，并分页
     * 要求：
     * 未关联属性是基本属性，并且未分配给其他分组，基本属性属于当前分类
     * @param attrgroupId
     * @param params
     * @return
     */
    @Override
    public PageUtils getNoReleationAttr(Long attrgroupId, Map<String, Object> params) {
        //1.当前分组只能关联自己所属的分类里面的所有属性，一个分组可能关联多个分类
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();
        //2.当前分组只能关联别的分组没有引用的属性
        //2.1)获取当前分类下的其他分组
        List<AttrGroupEntity> group = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<Long> collect = group.stream().map(item -> item.getAttrGroupId())
                .collect(Collectors.toList());
        //2.2）获取其他分组以及当前分组关联的属性
        List<AttrAttrgroupRelationEntity> groupId = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", collect));
        List<Long> attrCollect = groupId.stream().map(item -> item.getAttrId()).collect(Collectors.toList());
        //2.3）从当前分类的所有属性中移除其他分组关联的属性
        //定义查找所有属性不在attrCollect，集合中并且不是销售属性
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().eq("attr_type", ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
        if (attrCollect!=null && attrCollect.size()>0){
            wrapper.notIn("attr_id", attrCollect);
        }
        //获取关键词key
        String key = (String) params.get("key");
        //判单key是否为空，不为空则添加判断条件
        if (key!=null){
//            wrapper.eq("attr_id",key).or().like("attr_name",key);
            wrapper.and(t->{
                t.eq("attr_id",key).or().like("attr_name",key);
            });
        }
        //创建分页对象
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);

        PageUtils pageUtils = new PageUtils(page);

        return pageUtils;

    }
}