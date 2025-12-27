package com.panyu.mybolg;

import com.panyu.mybolg.util.PasswordUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MyBolgApplicationTests {

    @Test
    void contextLoads() throws Exception {
        System.out.println(PasswordUtil.encryptPassword("YuPan9527"));
    }
}
