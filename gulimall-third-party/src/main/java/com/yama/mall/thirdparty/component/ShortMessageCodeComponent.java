package com.yama.mall.thirdparty.component;

import com.yama.mall.thirdparty.util.HttpUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ConfigurationProperties("message.code")
@Component
@Data
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
     *
     * @param phone      手机号
     * @param verityCode 生成的验证码
     * @return
     */
    public void sendShortMessageCode(String phone, String verityCode, String minute) {
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("mobile", phone);
        querys.put("param","**code**:"+verityCode+",**minute**:"+minute);
        querys.put("smsSignId", smsSignId);//2e65b1bb3d054466b82f0c9d125465e2
        querys.put("templateId", templateId);//908e94ccf08b4476ba6c876d13f084ad
        Map<String, String> bodys = new HashMap<String, String>();
        try {
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
