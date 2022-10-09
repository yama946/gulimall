# gulimall

### 知识点1
springboot项目需要配置数据源，引入mysql5和mysql8的时候，springboot2.1异常版本框架默认8版本的驱动。调用mysql5驱动是会报驱动错误 CLIENT_PLUGIN_AUTH is required。

数据库驱动之间也是存在对应的版本问题的，我们需要根据我们当前数据服务器对应版本来合理选择数据库驱动。

导入数据库驱动,官网查看驱动8.0：https://dev.mysql.com/doc/connectors/en/connector-j-versions.html，适配5.7，8.0版本服务器

### 知识点2

在使用mybatis-plus的情况下，在service中我们可以不注入我们的mapper，而直接使用basemapper,其传递的实现类就是我们的实现的mapper。

### 知识点3：mybatis-plus相关

使用mybatis-plus时，需要传递wrapper进行查询时，如果直接传递null值，就是查询全部数据。

### 知识点4：跨域相关
复杂请求会先发送一个OPTIONS类型的请求连接。

网关与网关路由的到微服务中，只能有一个存在跨域配置，不能两者同时配置，会导致参数携带重复而异常

### 知识点5；gateway路由器相关
问题1：
gateway路由器中重写路径规则
- RewritePath=/api/?(?<segment>.*), /renren-fast/$\{segment}
访问路径是/api/name，会被重写成/renren-fast/name路径,这种重写是隐式的，
在浏览器上无法体现，知识网关可以找到正确的微服务地址返回数据。

问题2；断言路径范围大小问题。
path=/api/**与path/api/product/**
断言路径范围大的在后，小的在前，否则大范围会覆盖，不再向后匹配小范围的断言.

### 知识点6 @RequestBody注解注意事项

 @RequestBody：获取请求体数据并转换为对象，请求体要求请求方式为post
 Springmvc自动将请求体数据(JSON),转换为响应对象
 
 
### 知识点7：zuul与gateway的区别
使用zuul，进行路由我们还可以直接访问微服务不通过zuul路由
使用gateway，必须使用路由配置才能访问到微服务。

### 知识点8：mybatis-plus逻辑删除
deleteBatchIds(asList)以及官方定义的deleteByid、update，只要配置逻辑删除字段都会实现逻辑删除功能。

当我们定义删除时标记与默认不同，我们可以通过配置文件或者注解@TableLogic中的值进行设置

第一种方式：
logic-delete-field: flag # 全局逻辑删除的实体字段名(since 3.3.0,配置后可以忽略不配置步骤2,也就是配置@TableLogic注解)
logic-delete-value: 0 # 逻辑已删除值(默认为 1)
logic-not-delete-value: 1 # 逻辑未删除值(默认为 0)
第二种方式：
@TableLogic(value = "1",delval = "0")
```java
public @interface TableLogic {
    /**
     * 默认逻辑未删除值（该值可无、会自动获取全局配置）
     */
    String value() default "";
    /**
     * 默认逻辑删除值（该值可无、会自动获取全局配置）
     */
    String delval() default "";
}
```


### 知识点9：categoryEntity.getParentCid() == root.getCatId()比对失败无法获取子类。

categoryEntity.getParentCid()，返回的是一个Long包裹类对象类型
root.getCatId()，返回的也是要给Long类型的包装类型。
两者通过“==”比较，比较的是地址而不是值。

“==”符号，比较是否相等。
基本类型之间的比较，是比较值
对象之间比较，是比较的地址

示例解析：

```
	public void testEquals() {
		int int1 = 12;
		int int2 = 12;
		
		Integer integer1 = new Integer(12);
		Integer integer2 = new Integer(12);
		Integer integer3 = new Integer(127);
		
		Integer a1 = 127;
		Integer a2 = 127;
		
		Integer a = 128;
		Integer b = 128;
		System.out.println("int1 == int2 -> " + (int1 == int2));					
		System.out.println("int1 == integer1 -> " + (int1 == integer1));			
		System.out.println("integer1 == integer2 -> " + (integer1 == integer2));	
		System.out.println("integer3 == a1 -> " + (integer3 == a1));				
		System.out.println("a1 == a2 -> " + (a1 == a2));							
		System.out.println("a == b -> " + (a == b));													
	}   
```

```
    1、   int1 == int2 -> true
    解析：
    基本类型通过==比较，比较的就是值
    2、   int1 == integer1 -> true
    解析：
    Integer是int的封装类，当Integer与int进行==比较时，Integer就会拆箱成一个int类型，所以还是相当于两个int类型进行比较，
    这里的Integer,不管是直接赋值，还是new创建的对象，只要跟int比较就会拆箱为int类型，所以就是相等的。
    3、   integer1 == integer2 -> false
    解析：
    两个都是对象类型，而且不会进行拆箱比较，比较的是地址，因此不等
    4、   integer3 == a1 -> false
    解析：
    integer3是一个对象类型，而a1是一个常量它们存放内存的位置不一样，所以也不等
    5、   a1 == a2 -> true
    6、   a == b -> false
    解析：
    jvm会将-128-127的基本类型包装类，进行缓存，如果再次使用直接从缓存获取，而不是重新创建，因此127是相同的。
    128不会被缓存，会在内存中重新创建，因此两者对象地址不同，返回值为false。
```

### 知识点10：JSR303分组校验
对于BrandId成员变量，在添加时，我们校验希望是不携带的，但是更新时，是必须要携带的参数，这种情况我们就要进行分组校验

1、@NotBlank(message="品牌名不能为空",groups = {AddGroup.class})
给校验注解标注什么情况下需要进行注解，接口仅仅作为一种标识没有实际意义
2、在Controller中的注解@Valid不能进行开启分组校验我们需要使用另一个支持分组校验的注解
@Validated({AddGroup.class}),其中配置需要校验的标识，标识与需要校验的成员变量相对应
3、默认没有指定分组的校验注解，在分组校验情况下@Validated({AddGroup.class})不剩下，只会在@Validated不指定分组的情况下生效。


### 知识点11：自定义校验
方式1：使用@Pattern()注解

方式2：
首先我们希望这种方式，能够像以前我们使用注解那样实现校验功能
1、编写一个自定义校验注解
2、编写一个自定义校验器
3、关联自定义校验器和自定义校验注解

### 知识点12：JSON注解的使用
1、
	//表示json字符串返回时，如果为空忽略
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	
## 思考1：mybatis-plus使用插件分页，与当前项目分页的方式？？？？？？


### 知识点13：数据库中表的使用

不进行联表查询的原因：

防止联表查询，在大数据情况下，会出现巨量表的出现，比如10000与10000的数据的表，会生成一个亿级的笛卡尔积；

在进行业务查询时，我们尽量不做联表查询，而是多做分开查询进行合并。
建立中间表放置冗余字段来加快查询，而不是做联表查询，但是基于这种状况，当主表做数据更新操作，在业务上为保持同步，
我们需要为建立方便查询的冗余字段也同时进行数据同步更新操作。



### 注意点：关于事务

起因：更新品牌、分类的同时，更新品牌分类关联表，这种操作涉及到事务需要添加事务的支持。需要在service上添加事务注解

问题：mybatisplus中需要添加开启事务的注解@EnableTransactionManagement事务才起效，是基于老版本的mybatis吗？

### 知识点14：mysql数据库执行语句的优化
传统实现方法：
    /**
     * 删除属性分组与基本属性的关联关系
     * @param attrGroupRelationVO
     */
    @Override
    public void attrGroupRelationDelete(AttrGroupRelationVO[] attrGroupRelationVO) {
        Stream.of(attrGroupRelationVO).forEach((attrRelation)->{
            this.remove(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id",attrRelation.getAttrId())
                    .eq("attr_group_id",attrRelation.getAttrGroupId()));
        });
    }
对于批量删除时的语句：遍历数组中所有对象，每个对象执行一次delete语句
删除3个元素执行三次：
DELETE FROM pms_attr_attrgroup_relation WHERE (attr_id = ? AND attr_group_id = ?)
DELETE FROM pms_attr_attrgroup_relation WHERE (attr_id = ? AND attr_group_id = ?)
DELETE FROM pms_attr_attrgroup_relation WHERE (attr_id = ? AND attr_group_id = ?)

优化方法：
自定义删除方法不使用默认删除方法-----------自定义sql语句方法：
DELETE FROM pms_attr_attrgroup_relation WHERE (attr_id = ? AND attr_group_id = ?) or (attr_id = ? AND attr_group_id = ?) or (attr_id = ? AND attr_group_id = ?)
这样仅仅发起一次连接请求，达到优化sql数据库执行性能的目的

void deleteBatchRelation(@Param("entities") List<AttrAttrgroupRelationEntity> entities);

<delete id="deleteBatchRelation">
    DELETE from
    pms_attr_attrgroup_relation
    <where>
        <foreach collection="entities" item="item" separator=" or ">
            (attr_id=#{item.attrId} and attr_group_id=#{item.attrGroupId})
        </foreach>
    </where>
</delete>

### 知识点15：当mapper中的方法中添加@Param注解后，标签中不添加parameterType=""属性

注意此时的参数取值也有所不同，
比如Person{name:"yanmu",age:29}
使用parameterType=""取值方式为：#{name}
使用@Param标注：取值方式为#{person.name}


### 知识点16：在当前service中调用其他方法，应该注入其Service方法，而不是mapper方法，因为service方法更丰富
这只是一个建议，


### 问题2：远程调用传输的实体类是否要求可序列化。


### 知识点17：设置改变隔离级别，在debug过程中方便查看数据库数据的变化，隔离级别只是为了防止多线程问题导致数据搓读
** 隔离级别的改变，并不影响事务，如果出现异常，即使数据库数据设置了，也会回滚取消**
**设置隔离级别的语句：set session transaction isolation level read uncommitted


### 知识点18：BigDecimal的使用
t.getMemberPrice().compareTo(new BigDecimal("0"))==1
compareTo返回3个值：-1(小于)，0(等于)，1(大于)
new BigDecimal时，推荐使用字符串创建，据说能避免数据精度的缺失

### 知识点19：无论任何状况，都可将请求传递过来的键值对传递给Map集合对象，通过feign远程调用获取的对象默认是map


### 知识点20：mybatis-plus条件构建时，使用and()与不使用生成的sql区别
```
        String key = (String) params.get("key");
        if ( key!= null && key.length() > 0){
            wrapper.and(t->t.eq("id",key).or().like("spu_name",key));
        }
        //不使用and()连接sql语句：catalog_id=1 and id=1 or spu_name like xxx（此时catalog_id就不一定等于指定值）
        //使用and()连接的sql语句：catalog_id=1 and (id=1 or spu_name like xxx):此时就能保证catalog_id满足条件，符合业务要求
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId)){
            //视频实现下方3个同：（直接eq不使用and连接）wrapper..eq("catalog_id",catelogId)
            wrapper.and(a->a.eq("catalog_id",catelogId));
        }
```

### 知识点21：spring.jackson.datafomat配置时间对象转换成json的格式

```yaml
# 使用jackson.data-format配置所有时间数据转换json都会转换成配置的格式
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
```

### 知识点22：@EnableTransactionManagement//用来开启事务，有时候还放到mybatis-plus配置类上，需要测试不添加是否默认开启？？？？？？？？？？？？


### 知识点23：@MapperScan注解的使用
```java
//单独配置注入不成功，MapperScan只能扫描注入指定的mapper文件，无法扫描注入cong，componte等注解
          @MapperScan(basePackages = {"com.yama.mall.ware.dao","com.yama.mall.common.config"})
          @ComponentScan(basePackages = "com.yama.mall")
```

### 知识点24： Mybatis-plus自动填充功能
```java
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("start insert fill ....");
        this.strictInsertFill(metaObject, "createTime", Date.class, new Date()); // 起始版本 3.3.0(推荐使用)
        this.strictInsertFill(metaObject, "updateTime", Date.class, new Date()); // 起始版本 3.3.0(推荐使用)
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("start update fill ....");
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now()); // 起始版本 3.3.0(推荐)
    }
}
```


```
	/**
	 * 
	 */
	@TableField(fill = FieldFill.INSERT)
	private Date createTime;
	/**
	 * 
	 */
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private Date updateTime;
```

需要填充的属性：	private Date updateTime;
this.strictInsertFill(metaObject, "updateTime"（要填充的属性名）, Date.class(要和填充属性数据类型相同), new Date()); // 起始版本 3.3.0(推荐使用)


### 知识点25：Stream流中的filter方法的使用
```
将集合中每个元素进行过滤，判断元素是否返回true，返回true则保留进入流水线的下一步。
filter(item->{
            if (item.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode()
                || item.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()){
                return true;
            }
            return false;
```


### 知识点26：重要！ 重要！ 重要！！！！！！！！！！！！！！！！
```
public void addStocks(Long skuId, Long wareId, Integer skuNum) {
        //判断当前是否存在此库存信息，否则是保存操作
        WareSkuEntity wareSkuEntitie = wareSkuDao.selectOne(new QueryWrapper<WareSkuEntity>()
                .eq("sku_id", skuId).eq("ware_id", wareId));
        if (wareSkuEntitie==null){
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntitie.setSkuId(skuId);
            wareSkuEntitie.setWareId(wareId);
            wareSkuEntitie.setStock(skuNum);
            wareSkuEntitie.setStockLocked(0);
            //TODO 远程调用接口设置sku的name,如果微服务不稳定，导致远程调用失败，整个事务无需回滚
            //自己try...catch进行解决,通过try...catch处理异常后，不会抛出异常，也就不会触发回滚操作
            try {
                R skuInfoMap = productFeignService.info(skuId);

                Map<String,Object> info = (Map<String,Object>)skuInfoMap.get("skuInfo");

                if (skuInfoMap.getCode() ==0){
                    wareSkuEntity.setSkuName((String) info.get("skuName"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            wareSkuDao.insert(wareSkuEntity);
        }else {
            wareSkuDao.addStock(skuId,wareId,skuNum);
        }
    }
```
#### 重点1：
 Map<String,Object> info = (Map<String,Object>)skuInfoMap.get("skuInfo");
 上述代码：skuInfoMap.get("skuInfo")，保存的是一个SkuInfoEntity实体类，可以直接将其中的键值转换成Map集合

#### 重点2：涉及到分布式事务概念
自我思考：此时如果远程调用出现问题，当前本地事务方法，异常回滚，但是远程调用操作不一定会回滚，这就涉及到数据库的一致性。

当微服务不稳定，可能导致远程调用失败，但是我们又不想回滚，可以使用try...catch处理异常不抛出，就不会触发异常回滚。

### 缓存中存放的数据，应该是JSON字符串，而不是Java序列化的字符串
```
    /**
     * 业务优化之redis缓存
     * 获取所有分类数据json
     * @return
     */
    @Override
    public Map<String, List<Catelog2VO>> getCatalogJson() {
        //给缓存中存放Json字符串，拿到的是Json字符串，还要逆转为我们能用的对象类型：【序列化与反序列化】
        //这种序列化是我们自己进行的，不使用java的序列化保存。

        //1.加入缓存业务逻辑，缓存中存放的是Json字符串
        //Json字符串跨语言，跨平台兼容
        ValueOperations<String, String> stringOps = redisTemplate.opsForValue();
        String catalogJson = stringOps.get("catalogJson");
        if (StringUtils.isEmpty(catalogJson)){
            //2.缓存中没有，查询数据库
            Map<String, List<Catelog2VO>> catalogJsonFromDB = getCatalogJsonFromDB();
            //3.将数据库中查询到的数据保存到redis缓存中,将对象转换为JSON存放到缓存中
            String catalogJsonData = JSON.toJSONString(catalogJsonFromDB);
            stringOps.set("catalogJson",catalogJsonData);
            return catalogJsonFromDB;
        }else {
            //4.缓存中有数据值及返回
            Map<String, List<Catelog2VO>> resultMap = JSON.parseObject(catalogJson,new TypeReference<Map<String, List<Catelog2VO>>>(){});
            return resultMap;
        }
```
#### 原因：
* JSON是跨语言，跨平台的
* 如果我们使用其他语言，比如php，编写一个物流系统，同时因为需要从缓存中获取数据，如果缓存中保存的是java序列化字符串，
那么php就无法转换，如果是json，就可以很好的转换为对应语言的对象。

#### 注意点：
* 保存到redis缓存中，以Json字符串的形式，获取使用前需要转换为相应的对象。


### 知识点27：@EnableConfigurationProperties注解的使用
/**
 * @EnableConfigurationProperties的作用是把springboot配置文件中的值与我们的xxxProperties.java的属性进行绑定，
 * 需要配合@ConfigurationProperties使用
 *
 * 首先我想说的是，不使用@EnableConfigurationProperties能否进行属性绑定呢？答案是肯定的！
 * 我们只需要给xxxProperties.java加上@Component注解，把它放到容器中，即可实现属性绑定
 *
 * 在属性绑定中@EnableConfigurationProperties和@Component的效果一样，那么为啥springboot还要使用这个注解呢？
 *
 * 答案是：当我们引用第三方jar包时，@Component标注的类是无法注入到spring容器中的，
 * 这时我们可以用@EnableConfigurationProperties来代替@Component
 */
@EnableConfigurationProperties(ThreadPoolConfigProperties.class)


### 知识点28：Spring MVC提供RedirectAttributes，用来进行请求重定向时携带数据。


### 知识点29:向redis中存放hash类型的数据，其hashKey必须是String类型，否则hi出现异常
错误代码示例：
redisTemplate.opsForHash().put("user-key",23,63);
异常展示：
java.lang.ClassCastException: java.lang.Integer cannot be cast to java.lang.String
正确代码示例：
redisTemplate.opsForHash().put("user-key","23"",63);


