package com.yama.mall.thirdparty;

import com.yama.mall.thirdparty.component.ShortMessageCodeComponent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = GulimallThirdPartyApplication.class)
public class GulimallThirdPartyApplicationTests {

    @Autowired
    ShortMessageCodeComponent codeComponent;

    @Test
    public void sendMessageCodeTest(){
        codeComponent.sendShortMessageCode("15236628719","235232","3");
    }

}
