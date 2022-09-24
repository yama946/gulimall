package com.yama.mall.thirdparty.constroller;

import com.yama.mall.common.utils.R;
import com.yama.mall.thirdparty.component.ShortMessageCodeComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/sms")
public class ShortMessageCodeController {

    @Autowired
    private ShortMessageCodeComponent shortMessageCodeComponent;

    /**
     * 提供给别的服务进行远程调用
     * @param phone
     * @param verityCode
     * @param minute
     * @return
     */
    @GetMapping("/sendcode")
    public R sendShortMessageCode(@RequestParam("phone") String phone, @RequestParam("verityCode")String verityCode, @RequestParam("minute")String minute){
        shortMessageCodeComponent.sendShortMessageCode(phone,verityCode,minute);
        return R.ok();
    }
}
