package io.renren;

import cn.hutool.core.codec.Base64;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.renren.zapi.utils.CommonUtils;
import jakarta.annotation.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class ZooTestController {
    @Resource
    private ObjectMapper objectMapper;

    @RequestMapping("/hello")
    public String json() throws JsonProcessingException {
        Map<String, Object> map = new HashMap<>();
        map.put("accountNo", "1231231231312313");
        map.put("accountUser", "Raj Halj");
        map.put("accountBank", "IBSBINxx");
        map.put("accountIfsc", "1231231231");
        map.put("accountUpi", "asdfasdf@fff.t");
        map.put("accountInfo", "qrcode://asdfasdfasdasdfsd");
        map.put("amount", 10000);
        map.put("id", "123123123123123123");
        map.put("deadline", new Date().getTime() + 300);
        map.put("domain", "https://www.baidu.com");
        String s = objectMapper.writeValueAsString(map);
        String encode = Base64.encode(s);
        System.out.println(encode);
        return encode;
    }

    @RequestMapping("/log")
    public String testLog() {
        CommonUtils.getLogger("merchant").info("hello");
        CommonUtils.getLogger("channel").info("hello");
        return "hhh";
    }
}
