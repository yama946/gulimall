package com.yama.mall.thirdparty.component;

import com.yama.mall.thirdparty.util.HttpUtils;
import lombok.Data;
import org.apache.http.HttpResponse;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties("message.code")
@Data
@Component
public class ShortMessageCodeComponent {

    //短信api地址
    private String host;

    //短线api地址uri
    private String path;

    //请求方式
    private String method;

    //申请短信接口的验证方式
    private String appcode;

    //短信变量
    private String smsSignId;

    //短信模板
    private String templateId;


    /**
     * 发送短信的工具类
     * @param phone     手机号
     * @param verityCode  生成的验证码
     * @return
     */
    public void sendShortMessageCode(String phone,String verityCode,String minute){

        HttpResponse response=null;
        //1.短信api接口地址
//        String host = "https://gyytz.market.alicloudapi.com";
        //2.短信服务api地址uri
//        String path = "/sms/smsSend";
//        String method = "POST";
//        String appcode = "40394e3b27af4ca3867e3a115b6478e7";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("mobile", phone);
        //暂时生成一个六位数验证码不存放在redis中永久有效
//        int code = (int)(Math.random()*1000000);
        verityCode = "**code**:"+verityCode+",**minute**:"+minute;
        querys.put("param",verityCode);
        querys.put("smsSignId", smsSignId);//2e65b1bb3d054466b82f0c9d125465e2
        querys.put("templateId", templateId);//908e94ccf08b4476ba6c876d13f084ad
        Map<String, String> bodys = new HashMap<String, String>();
        try {
            /**
             * 重要提示如下:
             * HttpUtils请从
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
             * 下载
             *
             * 相应的依赖请参照
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             */
            response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
//            System.out.println(response.toString());
            //获取response的body
//            System.out.println();
            //输出结果为：{"msg":"成功","code":"0"}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
