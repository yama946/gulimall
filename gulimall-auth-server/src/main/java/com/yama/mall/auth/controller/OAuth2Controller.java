package com.yama.mall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.yama.mall.auth.feign.MemberFeignService;
import com.yama.mall.auth.vo.MemberEntityVO;
import com.yama.mall.auth.vo.SoicalUserVO;
import com.yama.mall.common.utils.HttpUtils;
import com.yama.mall.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 处理社交登陆请求，使用github社交登陆
 */
@Slf4j
@Controller
public class OAuth2Controller {


    @Autowired
    MemberFeignService memberFeignService;

    @Value("${appId}")
    String clientId;

    @Value("${appSecret}")
    String clientSecret;

    @GetMapping("/oauth2/github/success")
    public String githubOAth2(@RequestParam("code") String code) throws Exception {
        //1.使用授权码code，获取access_token
        HashMap<String, String> bodyMap = new HashMap<>();
        bodyMap.put("client_id",clientId);
        bodyMap.put("client_secret",clientSecret);
        bodyMap.put("code",code);
        bodyMap.put("redirect_uri","http://auth.gulimall.com/oauth2/github/success");
        HttpResponse postResult = HttpUtils.doPost(
                "https://github.com",
                "/login/oauth/access_token",
                "post",
                null, null, bodyMap);
        log.debug("获取access_token");
        //1)处理获取access——token的结果，视频中返回是json通过封装对象处理这里我们返回的是字符串
        //返回的结果体：access_token=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&scope=user&token_type=bearer
        if (postResult.getStatusLine().getStatusCode()==200){
            //2）获取access_token
            String result = EntityUtils.toString(postResult.getEntity());
            String[] arr = result.split("&");
            Map<String, String> collect = Stream.of(arr).collect(Collectors.toMap(t -> t.split("=")[0], t -> t.split("=")[1]));
            log.debug(collect.toString());
            String access_token = collect.get("access_token");
            String token_type = collect.get("token_type");
            //3)获取当前是那个社交用户，第一次自动注册并登录，不是则进行直接登陆
            HashMap<String, String> headers = new HashMap<>();
            log.debug(token_type+" "+access_token);
            headers.put("Authorization",token_type+" "+access_token);
            HttpResponse getResponse = HttpUtils.doGet("https://api.github.com", "/user", "GET", headers, null);
            log.debug("获取到的用户数据");
            if (getResponse.getStatusLine().getStatusCode()==200){
                log.debug("第二次调用成功");
                SoicalUserVO soicalUserVO = new SoicalUserVO();
                soicalUserVO.setAccessToken(access_token);
                String string = EntityUtils.toString(getResponse.getEntity());
                JSONObject jsonObject = JSON.parseObject(string);
                String login = jsonObject.getString("login");
                String id = jsonObject.getString("id");
                soicalUserVO.setLogin(login);
                soicalUserVO.setSocialUId(id);
                //调用远程接口，判断当前社交用户是否登陆过
                R r = memberFeignService.oauth2Login(soicalUserVO);
                if (r.getCode()==0){
                    log.debug("远程调用成功");
                    MemberEntityVO entity = r.getData("data", new TypeReference<MemberEntityVO>() {});
                    log.info("用户登陆成功：{}",entity);
                    //2.使用access_token，获取用户信息，进行判断是注册，还是直接登陆
                    //登陆成功返回，到首页
                    return "redirect:http://gulimall.com";
                }else {
                    //获取认证授权令牌失败，重定向到登陆页
                    return "redirect:http://auth.gulimall.com/login.html";
                }
            }else {
                //获取认证授权令牌失败，重定向到登陆页
                return "redirect:http://auth.gulimall.com/login.html";
            }
        }else {
            //获取认证授权令牌失败，重定向到登陆页
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }

}
