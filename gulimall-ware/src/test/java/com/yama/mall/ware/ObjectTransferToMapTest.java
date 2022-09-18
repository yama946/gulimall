package com.yama.mall.ware;

import com.alibaba.fastjson.JSON;
import com.yama.mall.common.utils.R;
import com.yama.mall.ware.feign.ProductFeignService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

/**
 * 对象转换成Map测试
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = GulimallWareApplication.class)
public class ObjectTransferToMapTest {
    @Autowired
    ProductFeignService productFeignService;

    @Test
    public void transferToMap(){
        Person person = new Person("jack", 23);
        Map<String,Object> map = new HashMap<>();
        String str = JSON.toJSONString(person);
        map.put("person", str);
        //从map中获取对象并转换成map实例
        log.debug("转换前的数据：{}",map.get("person"));
        Map<String,Object> result = (Map<String,Object>)map.get("person");
        String name = (String)result.get("name");
        log.debug("从map中获取的值为:{}",name);
    }

    /**
     * 远程获取的数据为:{msg=success, code=0, skuInfo={skuId=65, spuId=27, skuName=华为 HUAWEI Mate 10 樱粉金 旗舰版 128G 8G 骁龙885plus, skuDesc=null, catalogId=255, brandId=13, skuDefaultImg=https://gulimall905.oss-cn-hangzhou.aliyuncs.com/2022-09-15/0cd75fe0-7cd4-4802-8f1c-c2c89a8fe243_6a1b2703a9ed8737.jpg, skuTitle=华为 HUAWEI Mate 10 樱粉金 旗舰版 128G 8G 骁龙885plus, skuSubtitle=预订用户预计11月30日左右陆续发货！麒麟970芯片！AI智能拍照！ 华为 HUAWEI Mate 10 Pro 10:08 限时限量抢！, price=4999.0, saleCount=0}}
     * map中获取数据转换器前:{skuId=65, spuId=27, skuName=华为 HUAWEI Mate 10 樱粉金 旗舰版 128G 8G 骁龙885plus, skuDesc=null, catalogId=255, brandId=13, skuDefaultImg=https://gulimall905.oss-cn-hangzhou.aliyuncs.com/2022-09-15/0cd75fe0-7cd4-4802-8f1c-c2c89a8fe243_6a1b2703a9ed8737.jpg, skuTitle=华为 HUAWEI Mate 10 樱粉金 旗舰版 128G 8G 骁龙885plus, skuSubtitle=预订用户预计11月30日左右陆续发货！麒麟970芯片！AI智能拍照！ 华为 HUAWEI Mate 10 Pro 10:08 限时限量抢！, price=4999.0, saleCount=0}
     * map中获取数据转换器后:{skuId=65, spuId=27, skuName=华为 HUAWEI Mate 10 樱粉金 旗舰版 128G 8G 骁龙885plus, skuDesc=null, catalogId=255, brandId=13, skuDefaultImg=https://gulimall905.oss-cn-hangzhou.aliyuncs.com/2022-09-15/0cd75fe0-7cd4-4802-8f1c-c2c89a8fe243_6a1b2703a9ed8737.jpg, skuTitle=华为 HUAWEI Mate 10 樱粉金 旗舰版 128G 8G 骁龙885plus, skuSubtitle=预订用户预计11月30日左右陆续发货！麒麟970芯片！AI智能拍照！ 华为 HUAWEI Mate 10 Pro 10:08 限时限量抢！, price=4999.0, saleCount=0}
     * 远程获取的skuName为：华为 HUAWEI Mate 10 樱粉金 旗舰版 128G 8G 骁龙885plus
     */
    @Test
    public void feignTransferToMap(){
        R skuInfoMap = productFeignService.info(65L);
        log.debug("远程获取的数据为:{}",skuInfoMap);
        Map<String,Object> info = (Map<String,Object>)skuInfoMap.get("skuInfo");
        log.debug("map中获取数据转换器前:{}",skuInfoMap.get("skuInfo"));
        log.debug("map中获取数据转换器后:{}",info);
        String skuName = (String) info.get("skuName");
        log.debug("远程获取的skuName为：{}",skuName);

    }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Person{
    String name;

    Integer age;
}
