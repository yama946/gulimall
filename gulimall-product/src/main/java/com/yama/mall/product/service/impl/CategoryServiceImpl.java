package com.yama.mall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.yama.mall.common.utils.PageUtils;
import com.yama.mall.common.utils.Query;
import com.yama.mall.product.dao.CategoryDao;
import com.yama.mall.product.entity.CategoryEntity;
import com.yama.mall.product.service.CategoryBrandRelationService;
import com.yama.mall.product.service.CategoryService;
import com.yama.mall.product.vo.Catelog2VO;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("categoryService")
@Slf4j
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    /**
     * 使用Map作为本地缓存使用
     */
    private Map<String,Object> cache = new HashMap<>();

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;


    @Autowired
    private StringRedisTemplate redisTemplate;


    @Autowired
    private RedissonClient redissonClient;


    /**
     * //TODO 使用redis缓存进行压力测试，会出现OutOfDirectMemoryError，堆外内存溢出，普通测试不会
     * 原因：
     *      1.springboot2.0以后默认使用letture作为操作redis的客户端。它使用netty进行网络通信
     *      2.由于letture中的bug，与netty配合使用导致netty堆外内存溢出，netty如果没有只当堆外内存，会默认使用-Xmx的值
     *      3.根本原因是bug，导致内存未释放，无论设置堆外内存多大，问题都不会解决，只会推迟bug出现时间
     *解决方案：
     *      1.升级letture客户端，当然也可以我们自己修改源码。
     *      2.使用Jedis替换letture作为操作redis的客户端。
     *      <dependency>
     *             <groupId>org.springframework.boot</groupId>
     *             <artifactId>spring-boot-starter-data-redis</artifactId>
     *             <exclusions>
     *                 <exclusion>
     *                     <groupId>io.lettuce</groupId>
     *                     <artifactId>lettuce-core</artifactId>
     *                 </exclusion>
     *             </exclusions>
     *         </dependency>
     *         <!--排除letture，使用jedis作为客户端-->
     *         <dependency>
     *             <groupId>redis.clients</groupId>
     *             <artifactId>jedis</artifactId>
     *         </dependency>
     *
     * 解决方案优缺点比较：
     *      Jedis是一个老版本的操作redis的客户端，好久未更新，性能比较低。
     *      Letture是一个比较新的操作redis的客户端，使用netty作为底层网络通信框架，吞吐量是极高的。
     *
     * 业务优化之redis缓存
     * 获取所有分类数据json
     * @return
     */
    @Override
    public Map<String, List<Catelog2VO>> getCatalogJson() {
        //给缓存中存放Json字符串，拿到的是Json字符串，还要逆转为我们能用的对象类型：【序列化与反序列化】
        //这种序列化是我们自己进行的，不使用java的序列化保存。

        /**
         * 1.空结果缓存：解决缓存穿透问题
         * 2.设置过期时间(加随机值)：解决缓存穿透
         * 3.加锁：解决缓存击穿
         */

        //1.加入缓存业务逻辑，缓存中存放的是Json字符串
        //Json字符串跨语言，跨平台兼容
        String catalogJson = redisTemplate.opsForValue().get("catalogJson");
        if (StringUtils.isEmpty(catalogJson)){
            //2.缓存中没有，查询数据库
            Map<String, List<Catelog2VO>> catalogJsonFromDB = getCatalogJsonFromDBWithRedissonLock();
            /*
            这里的代码要在查询数据库后，未释放锁前加入缓存，否则由于上下文线程切换会导致其他线程再次从数据库重查询
            //3.将数据库中查询到的数据保存到redis缓存中,将对象转换为JSON存放到缓存中
            String catalogJsonData = JSON.toJSONString(catalogJsonFromDB);
            redisTemplate.opsForValue().set("catalogJson",catalogJsonData);*/
            return catalogJsonFromDB;
        }else {
            //4.缓存中有数据值及返回
            Map<String, List<Catelog2VO>> resultMap = JSON.parseObject(catalogJson,new TypeReference<Map<String, List<Catelog2VO>>>(){});
            return resultMap;
        }
    }


    /**
     * 基于redisson作为分布式锁功能框架
     * @return
     * （业务逻辑是先查询缓存，缓存没有再查询数据库，数据库更新了怎么办）
     * 缓存里面的数据如何与数据库保持一致，（我想数据库更新是一并更新缓存）
     * 1）双写模式：也即是缓存和数据库数据同时更新
     * 2）失效模式：数据库修改后将缓存数据删掉，等待下次查询缓存主动更新。
     */
    public Map<String, List<Catelog2VO>> getCatalogJsonFromDBWithRedissonLock() {
        //1.获取锁，锁的名字相同表示同一个锁，锁的粒度，越细性能越好
        //一定要避免多个业务获取同一个锁添加，这样锁的粒度就会扩大。
        //锁的粒度，具体缓存的那个数据，11号商品：product-11-lock，12号商品：product-12-lock，避免12号商品高并发影响，11商品业务
        RLock mylcok = redissonClient.getLock("catalogJson-lock");
        //2.加锁
        mylcok.lock(30,TimeUnit.SECONDS);
        try{
            //3.加锁成功，执行业务
            Map<String, List<Catelog2VO>> dataFromDB = getDataFromDB();
            return dataFromDB;
        }finally {
            //4.释放锁
            mylcok.unlock();
        }
    }


    /**
     * 基于redis中set命令实现分布式锁
     * @return
     */
    public Map<String, List<Catelog2VO>> getCatalogJsonFromDBWithRedisLock() {
        //1.分布式锁，去redis使用SETNX命令等同于方法setIfAbsent()占坑
        //给锁设置过期时间，防止删除前断电等异常导致锁未清除，其他线程无法获取（这样设置是一个原子操作，可以保证过期时间设置成功）
        /**
         * 问题：
         * 1、setnx占好了位，业务代码异常或者程序在页面过程 中宕机。没有执行删除锁逻辑，这就造成了死锁
         * 解决：
         * 设置锁的自动过期，即使没有删除，会自动删除
         *
         * 问题：
         * 2、删除锁直接删除？？？
         * 如果由于业务时间很长，锁自己过期了，我们 直接删除，有可能把别人正在持有的锁删除了。
         *
         * 解决：
         * 占锁的时候，值指定为uuid，每个人匹配是自己 的锁才删除。
         */
        String uuid = UUID.randomUUID().toString().replace("-","");
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid,100, TimeUnit.SECONDS);
        /*Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", "110",100, TimeUnit.SECONDS);*/
        if (lock){
            log.info("分布式锁争用成功");
            //加锁成功。。。执行业务
            //2.设置过期时间,如果在这里加锁，在添加过期事件前异常导致设置失败，也会导致死锁，
            //要解决问题：就要让过期时间的设置与值设置是一个原子的操作，同时执行成功或失败。
            /*redisTemplate.expire("lock",100,TimeUnit.SECONDS);*/
            Map<String, List<Catelog2VO>> dataFromDB = getDataFromDB();
            //业务执行成功释放锁,别人可以获得锁
            /**
             * 下面比对是自己的值成功，才进行删除的问题：
             *      redis查询时值没有失效，但是当值返回过程中肯定存在网络延迟，
             *      此时值过期了，并且其他线程争用成功，并设置值，此时还是会将其他的值删除
             * 解决思路：
             *      获取值比对+比对值成功=原子操作
             * 解决方案：
             *      删除锁必须保证原子性。使用redis+Lua脚本完成
             */
            /*String lockValue = redisTemplate.opsForValue().get("lock");
            if (Objects.equals(uuid,lockValue)){
                redisTemplate.delete("lock");
            }*/
            //释放锁的时候使用脚本告诉Redis:只有key存在并且存储的值和我指定的值一样才能告诉我删除成功
            //执行删除的Lua脚本
            String scriptLua = "if redis.call('get', KEYS[1]) == ARGV[1] then return  redis.call('del', KEYS[1]) else return 0 end";

            //执行脚本删除锁,,Long.class表示脚本执行的返回结果类型
            Long scriptResult = redisTemplate.execute(
                    new DefaultRedisScript<>(scriptLua, Long.class), Arrays.asList("lock"), uuid);
            log.debug("脚本执行的结果为1：成功，0：失败，执行结果为：{}",scriptResult);
            return dataFromDB;
        }else {
            //加锁失败......等待一段时间后重试，类似synchronized()自旋的方式
            try {
                //不休眠，高并发压测，调用过快会导致内存问题
                Thread.sleep(100);//可以选择休眠一段时间重试
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatalogJsonFromDBWithRedisLock();//自己看来是递归
        }
    }

    /**
     * 从数据库中获取数据代码提取成方法
     * 流程：
     *      1.选中代码
     *      2.右键
     *      3.Refactor--->Extract--->Method
     * @return
     */
    private Map<String, List<Catelog2VO>> getDataFromDB() {
        String catalogJson = redisTemplate.opsForValue().get("catalogJson");
        if (StringUtils.isEmpty(catalogJson)) {
            Map<String, List<Catelog2VO>> resultMap = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2VO>>>() {
            });
            return resultMap;
        }
        //将原来数据库中的多次查询变成一次，减少到数据库中的频繁读写，查询出所有的数据
        List<CategoryEntity> categoryAllEntities = this.baseMapper.selectList(null);
        //1.查出所有1级分类
        List<CategoryEntity> level1Category = getParent_cid(categoryAllEntities, 0L);
        //2.封装数据
        Map<String, List<Catelog2VO>> parent_cid = level1Category.stream()
                .collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
                    //根据1级分类，查询到2级分类
                    List<CategoryEntity> entities = getParent_cid(categoryAllEntities, v.getCatId());

                    List<Catelog2VO> catelog2VOs = null;

                    if (entities != null) {
                        catelog2VOs = entities.stream().map(l2 -> {
                            Catelog2VO catelog2VO = new Catelog2VO(
                                    v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                            //找当前2级分类的3级分类进行封装
                            List<CategoryEntity> level3Catelog = getParent_cid(categoryAllEntities, l2.getCatId());

                            if (level3Catelog != null) {
                                List<Catelog2VO.Catelog3VO> catelog3Data = level3Catelog.stream().map(l3 -> {
                                    //封装成指定格式
                                    Catelog2VO.Catelog3VO catelog3VO = new Catelog2VO.Catelog3VO(
                                            l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                                    return catelog3VO;
                                }).collect(Collectors.toList());
                                catelog2VO.setCatalog3List(catelog3Data);
                            }
                            return catelog2VO;
                        }).collect(Collectors.toList());
                    }
                    return catelog2VOs;
                }));
        //在锁释放前加入缓存，防止锁释放后缓存未添加完成，其他线程获得锁进行数据库查询
        String catalogJsonData = JSON.toJSONString(parent_cid);
        redisTemplate.opsForValue().set("catalogJson", catalogJsonData);
        return parent_cid;
    }


    /**
     * 业务优化后代码
     * 获取所有分类数据json
     * 优化问题：
     *       数据库的频繁查询导致业务性能下降，吞吐量低下
     * 优化思路：
     *      将数据从数据库查询进行全量保存，再从我们保存的地方进行查询，不直接从数据库中查询数据
     *      相当于自己建立一个微型缓存。
     * @return
     */
    public Map<String, List<Catelog2VO>> getCatalogJsonFromDBWithLocalLock() {
        /**
         * 1.加锁解决：缓存缓存击穿
         * 2.缓存击穿解决方案逻辑：
         *      获取锁才能进行数据库查询，数据库查询前要先查找缓存是否存在数据
         * 3.如何正确添加锁：----使用synchronized同步锁机制
         *      1).只要是同一把锁，就能锁住需要这个锁的所有线程
         *      2).synchronized(this):springboot中所有的组件在容器中都是单例的。所以可以生效，也可以直接在方法上添加sysnchronized关键字
         * 4.我们使用synchrocized添加的锁是本地，如果是单体应用，可以解决缓存击穿问题。
         *  在分布式环境下，集群服务中，每个服务都是一个容器示例，使用this锁，就相当于每个服务都会放进来一个线程。
         *  我们要实现分布式环境下，高并发时只放进来一个线程查询数据库。我们就需要使用分布式锁。
         * 5.分布式锁与本地锁相比性能要差些。对于微服务部署不是很多使用本地锁，同时放进来几个线程不影响业务情况下也是可以的，
         *   此时也可以不使用分布式锁，分布式锁太重量级影响性能。
         */
        // TODO 本地锁：syntronized、JUC(Lock),只能锁住当前进程中的线程，也就是当前服务中的线程
        // TODO 在分布式锁情况下，要想锁住所有线程，需要使用分布式锁。
        synchronized (this){
            /*1.本地锁逻辑：得到锁之后，再去缓存中查询是否存在，不存在才进行数据库查询*/

            /*2.本地式锁：查询缓存，并判断*/
            String catalogJson = redisTemplate.opsForValue().get("catalogJson");
            if (StringUtils.isEmpty(catalogJson)){
                /*3.本地锁逻辑：再次查询缓存，数据存在返回*/
                Map<String, List<Catelog2VO>> resultMap = JSON.parseObject(catalogJson,new TypeReference<Map<String, List<Catelog2VO>>>(){});
                return resultMap;
            }
            /*3.本地锁逻辑：再次查询缓存不存在才进行数据库查询*/
            //将原来数据库中的多次查询变成一次，减少到数据库中的频繁读写，查询出所有的数据
            List<CategoryEntity> categoryAllEntities = this.baseMapper.selectList(null);
            //1.查出所有1级分类
            List<CategoryEntity> level1Category = getParent_cid(categoryAllEntities,0L);
            //2.封装数据
            Map<String, List<Catelog2VO>> parent_cid = level1Category.stream()
                    .collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
                        //根据1级分类，查询到2级分类
                        List<CategoryEntity> entities = getParent_cid(categoryAllEntities,v.getCatId());

                        List<Catelog2VO> catelog2VOs = null;

                        if (entities != null) {
                            catelog2VOs = entities.stream().map(l2 -> {
                                Catelog2VO catelog2VO = new Catelog2VO(
                                        v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                                //找当前2级分类的3级分类进行封装
                                List<CategoryEntity> level3Catelog = getParent_cid(categoryAllEntities,l2.getCatId());

                                if (level3Catelog!=null){
                                    List<Catelog2VO.Catelog3VO> catelog3Data = level3Catelog.stream().map(l3 -> {
                                        //封装成指定格式
                                        Catelog2VO.Catelog3VO catelog3VO = new Catelog2VO.Catelog3VO(
                                                l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                                        return catelog3VO;
                                    }).collect(Collectors.toList());
                                    catelog2VO.setCatalog3List(catelog3Data);
                                }
                                return catelog2VO;
                            }).collect(Collectors.toList());
                        }
                        return catelog2VOs;
                    }));
            //在锁释放前加入缓存，防止锁释放后缓存未添加完成，其他线程获得锁进行数据库查询
            String catalogJsonData = JSON.toJSONString(parent_cid);
            redisTemplate.opsForValue().set("catalogJson",catalogJsonData);
            return parent_cid;
        }
    }



    /**
     * 获取所有分类数据json
     * 1、业务优化之本地缓存
     *      使用本地缓存进行优化，相当于直接从内存中获取数据
     * 2、本地缓存的缺点：
     *      本地缓存如果是在单体应用上，是基本没有问题的，将数据库数据查询保存，数据库更新是更新缓存数据。
     *      但是
     *          如果是分布式架构的微服务，项目集群环境下就会出现问题
     *      问题1).如果存在a,b,c三台机器，第一次请求负载均衡到a，a建立缓存，第二次负载均衡到b，b还需要再次建立自己的缓存
     *      问题2).数据不一致问题，如果请求负载到a，执行更新操作，数据库数据更改，a中的缓存更改，但是b,c的缓存并不会更改，
     *              那么如果下次请求负载到b,c就会出现与a,数据库中的数据不一致的问题。
     *3、分布式缓存解决方案：
     *      集中式缓存，使用缓存中间件，将分布式集群中的缓存都放到一个中间件中进行集中保存。
     * @retun
     */
    public Map<String, List<Catelog2VO>> getCatalogJsonLocalCache() {
        /**
         * 1.首先获取本地缓存中是否有数据，没有再查询数据库，有直接返回
         */
        Map<String, List<Catelog2VO>> catalogJsonLocalCache = (Map<String, List<Catelog2VO>>)cache.get("catalogJsonLocalCache");
        if (catalogJsonLocalCache==null){
            /**
             * 1、将原来数据库中的多次查询变成一次，减少到数据库中的频繁读写
             *    查询出所有的数据
             */
            List<CategoryEntity> categoryAllEntities = this.baseMapper.selectList(null);
            //1.查出所有1级分类
            List<CategoryEntity> level1Category = getParent_cid(categoryAllEntities,0L);
            //2.封装数据
            Map<String, List<Catelog2VO>> parent_cid = level1Category.stream()
                    .collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
                        //根据1级分类，查询到2级分类
                        List<CategoryEntity> entities = getParent_cid(categoryAllEntities,v.getCatId());

                        List<Catelog2VO> catelog2VOs = null;

                        if (entities != null) {
                            catelog2VOs = entities.stream().map(l2 -> {
                                Catelog2VO catelog2VO = new Catelog2VO(
                                        v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                                //找当前2级分类的3级分类进行封装
                                List<CategoryEntity> level3Catelog = getParent_cid(categoryAllEntities,l2.getCatId());

                                if (level3Catelog!=null){
                                    List<Catelog2VO.Catelog3VO> catelog3Data = level3Catelog.stream().map(l3 -> {
                                        //封装成指定格式
                                        Catelog2VO.Catelog3VO catelog3VO = new Catelog2VO.Catelog3VO(
                                                l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                                        return catelog3VO;
                                    }).collect(Collectors.toList());
                                    catelog2VO.setCatalog3List(catelog3Data);
                                }
                                return catelog2VO;
                            }).collect(Collectors.toList());
                        }
                        return catelog2VOs;
                    }));
            //2.将数据库查询到数据，保存到本地缓存中
            cache.put("catalogJsonLocalCache",parent_cid);
            return parent_cid;
        }else{
            return catalogJsonLocalCache;
        }
    }



    /**
     * 业务优化用到的方法
     * 从我们保存的集合中匹配数据，不再从数据库中匹配查找
     * @param
     * @return
     */
    private List<CategoryEntity> getParent_cid(List<CategoryEntity> categoryAllEntities,Long parentCid) {
        List<CategoryEntity> result = categoryAllEntities.stream()
                .filter(item -> item.getParentCid() == parentCid.longValue()).collect(Collectors.toList());
        return result;
        /*return baseMapper
                .selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));*/
    }


    /**
     * 业务优化前代码
     * 获取所有分类数据json
     * @return
     */
    public Map<String, List<Catelog2VO>> getCatalogJsonOld() {
        //1.查出所有1级分类
        List<CategoryEntity> level1Category = getLevel1Category();
        //2.封装数据
        Map<String, List<Catelog2VO>> parent_cid = level1Category.stream()
                .collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //根据1级分类，查询到2级分类
            List<CategoryEntity> entities = baseMapper
                    .selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));

            List<Catelog2VO> catelog2VOs = null;

            if (entities != null) {
                catelog2VOs = entities.stream().map(l2 -> {
                    Catelog2VO catelog2VO = new Catelog2VO(
                            v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    //找当前2级分类的3级分类进行封装
                    List<CategoryEntity> level3Catelog = baseMapper
                            .selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", l2.getCatId()));

                    if (level3Catelog!=null){
                        List<Catelog2VO.Catelog3VO> catelog3Data = level3Catelog.stream().map(l3 -> {
                            //封装成指定格式
                            Catelog2VO.Catelog3VO catelog3VO = new Catelog2VO.Catelog3VO(
                                    l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return catelog3VO;
                        }).collect(Collectors.toList());
                        catelog2VO.setCatalog3List(catelog3Data);
                    }
                    return catelog2VO;
                }).collect(Collectors.toList());
            }
            return catelog2VOs;
        }));

        return parent_cid;
    }

    /**
     * 获取所有1级分类数据
     * @return
     * 1、每一个需要缓存的数据我们都来指定要放到那个名字的缓存。【缓存的值相当于缓存的分区（按照业务类型划分）】
     * 2、@Cacheable({"catalog","brand"}):可以同时放到多个分区中
      *   作用在方法上：
      *          代表当前方法的结果需要缓存，如果缓存中有，方法不用调用；
     *           如果缓存中没有，会调用方法，最后将方法的结果放入缓存中保存。
     * 3、默认行为：
     *      1).如果缓存中有，方法不用调用
     *      2).存放到redis中的key默认自动生成的,缓存的名字为：category::SimpleKey []
     *      3).缓存的value的值，默认使用jdk序列化机制，将序列化后的数据存到redis中
     *      4).默认ttl(过期时间)时间 -1：永不过期
     * 4.自定义行为：
     *      1）指定生成缓存的key名：注解中的key属性指定，接收一个Spel。key值会替换默认值的SimpleKey
     *         spel参考：https://docs.spring.io/spring-framework/docs/5.2.22.RELEASE/spring-framework-reference/integration.html#cache-spel-context
     *      2）指定缓存的数据的存活时间:配置文档中修改存活时间:spring.cache.redis.time-to-live
     *      3）将数据保存为json格式
     */
    @Cacheable(value = {"category","brand"},key = "#root.methodName",sync = true)
    @Override
    public List<CategoryEntity> getLevel1Category() {
        QueryWrapper<CategoryEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_cid",0).or().eq("cat_level",1);
        List<CategoryEntity> entities = this.baseMapper.selectList(queryWrapper);
        return entities;
    }

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
     * @CacheEvict:缓存数据一致性保证：失效模式
     * 1.需要指定删除那个注解，标识缓存分区名，key属性名，定位缓存的键
     * 2.此时的key属性不能复制，需要写名添加缓存时动态获取到的值，固定写入
     * 注意：key属性接受spel表达式，字符串需要添加单引号表示常量才生效:key = "'getLevel1Category'"
     *
     * 场景1：
     *      当更新数据时，需要删除多个缓存键，两种方法
     *    1.使用缓存的组合多种操作：同时删除两个缓存键
     *        @Caching(evict = {
     *             @CacheEvict(value = "category",key = "'getLevel1Category'"),
     *             @CacheEvict(value = "category",key = "'getLevel1Category'")
     *     })
     *    2.删除整个缓存分区，缓存名就是缓存分区名。需要合理化命名分区
     *      allEntries = true,此属性表示删除整个分区。
     *      @CacheEvict(value = "category",allEntries = true)
     *
     *     @CachePut:表示将方法的返回值，重新放到缓存中，但是方法没有返回值无法使用
     * @param category
     */
//    @CacheEvict(value = "category",key = "'getLevel1Category'")
    /*@Caching(evict = {
            @CacheEvict(value = "category",key = "'getLevel1Category'"),
            @CacheEvict(value = "category",key = "'getLevel1Category'")
    })*/
    @CacheEvict(value = "category",allEntries = true)
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