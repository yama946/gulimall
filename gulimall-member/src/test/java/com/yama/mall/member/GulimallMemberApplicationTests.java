package com.yama.mall.member;


import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.Md5Crypt;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

//@SpringBootTest
public class GulimallMemberApplicationTests {

    @Test
    public void contextLoads() {
        //1.不带盐值，直接md5算法加密方式
        //e10adc3949ba59abbe56e057f20f883e
        //抗修改行： 彩虹表--->计算md值与串之间的对应关系进行暴力破解md5密码
        String s = DigestUtils.md5Hex("123456");
        System.out.println(s);

        //MD5不能直接作为密码进行保存，因为每个串的md5值是固定的
        //因此我们可以使用带盐值的加密方式，就是给元数据添加一个随机值后加密，
        //我们需要验证码，所有要在数据库中设置冗余字段，保存盐值
        String source = "123456"+System.currentTimeMillis();

        //普通盐值加密方式，工具类的使用
        //盐值加密：随机值，加盐：$apr1$
        String s1 = Md5Crypt.apr1Crypt(source);
        System.out.println(s1);//$apr1$4yoR2967$t7aowt0JmqJAyK52RmnUV0
    }

}
