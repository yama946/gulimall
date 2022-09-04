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




