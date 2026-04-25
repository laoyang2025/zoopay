package io.renren.service;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordTest {

//    @Resource
//    private PasswordEncoder passwordEncoder;
//    @Test
//    public void encode() {
//        String password = "123456";
//        password = passwordEncoder.encode(password);
//        System.out.println(password);
//    }

    @Test
    public void decode() {
        String qrcode = "pay?pa=xxx@123&acc";
        int beg = qrcode.indexOf("?") + 1;
        int end = qrcode.indexOf("&");
        String upi = qrcode.substring(beg, end);
        System.out.println("upi = " + upi);
    }

}
