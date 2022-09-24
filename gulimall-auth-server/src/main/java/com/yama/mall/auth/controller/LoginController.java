package com.yama.mall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.yama.mall.auth.feign.ThirdPartyFeignService;
import com.yama.mall.auth.vo.UserRegistVO;
import com.yama.mall.common.constant.AuthServerConstant;
import com.yama.mall.common.exception.BizCodeEnume;
import com.yama.mall.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class LoginController {
    //首页点击跳转，登陆页面，注册页面,,通过Veiw-Controller实现跳转
    @Value("${verityCode.validTime.minute}")
    private String minute;

    @Autowired
    private ThirdPartyFeignService thirdPartyFeignService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 发送验证码
     * @param phone 目标手机号
     * @return
     */
    @ResponseBody
    @GetMapping("sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone){
        String redisKey = AuthServerConstant.SMS_CODE_CACHE_PREFIX+phone;
        //2).60秒内不得重复调用，验证码获取接口，防止接口重复调用，60后如果未收到三方接口则再次调用
        String redisValue = stringRedisTemplate.opsForValue().get(redisKey);
        if (redisValue!=null){
            long smsSaveTime = Long.parseLong(redisValue.split("_")[1]);
            Boolean timeStamp = System.currentTimeMillis()-smsSaveTime < 60000;
            if (timeStamp){
                R.error(BizCodeEnume.SMS_CODE_EXCEPTION.getCode(),BizCodeEnume.SMS_CODE_EXCEPTION.getMsg());
            }
        }
        //TODO 1)接口防刷

        //1.生成验证码,保存到redis中的value添加系统时间，用来判断下次请求的间隔时间
        String code = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 6)+"_"+System.currentTimeMillis();
        //2.发送验证码
        String verityCode = code.split("_")[0];
        R sendMessageResult = thirdPartyFeignService.sendShortMessageCode(phone, verityCode, minute);
        try{
            //3.判断是否发送成功，并保存到redis中
            if (sendMessageResult.getCode()==0) {
                //验证码注册校验准备，保存到redis中，格式：key--->sms:code:电话
                stringRedisTemplate.opsForValue().set(redisKey,code,Long.parseLong(minute),TimeUnit.SECONDS);
            }
        }catch (Exception e){
            e.printStackTrace();
            log.debug("验证码发送失败");
        }
        return R.ok("验证码发送成功");
    }


    /**
     *
     * TODO: 重定向携带数据：利用session原理，将数据放在session中。
     * TODO:只要跳转到下一个页面取出这个数据以后，session里面的数据就会删掉
     * TODO：分布下session问题
     * RedirectAttributes：重定向也可以保留数据，不会丢失
     * 用户注册
     * @return
     * @param result  数据校验结果
     */
    @PostMapping(value = "/register")
    public String register(@Valid UserRegistVO vos, BindingResult result,
                           RedirectAttributes attributes) {

        //如果有错误回到注册页面
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            attributes.addFlashAttribute("errors",errors);

            //效验出错回到注册页面
            return "redirect:http://auth.gulimall.com/reg.html";
        }

        //1、效验验证码
        String code = vos.getCode();

        //获取存入Redis里的验证码
        String redisCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vos.getPhone());
        if (!StringUtils.isEmpty(redisCode)) {
            //截取字符串
            if (code.equals(redisCode.split("_")[0])) {
                //删除验证码;令牌机制
                stringRedisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX+vos.getPhone());
                //验证码通过，真正注册，调用远程服务进行注册
//                R register = memberFeignService.register(vos);
                R register = null;
                if (register.getCode() == 0) {
                    //成功
                    return "redirect:http://auth.gulimall.com/login.html";
                } else {
                    //失败
                    Map<String, String> errors = new HashMap<>();
                    errors.put("msg", register.getData("msg",new TypeReference<String>(){}));
                    attributes.addFlashAttribute("errors",errors);
                    return "redirect:http://auth.gulimall.com/reg.html";
                }


            } else {
                //效验出错回到注册页面
                Map<String, String> errors = new HashMap<>();
                errors.put("code","验证码错误");
                attributes.addFlashAttribute("errors",errors);
                return "redirect:http://auth.gulimall.com/reg.html";
            }
        } else {
            //效验出错回到注册页面
            Map<String, String> errors = new HashMap<>();
            errors.put("code","验证码错误");
            attributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }
    }


    //注册要提交表单数据所有，我们使用post请求，并定义一个VO来接受表单数据
    @PostMapping("/regist")
    //TODO 添加数据校验参数注解
    public String register(@RequestBody UserRegistVO registVO){


        //注册成功回到登陆页
        return "redirect:/login.html";
    }

}
