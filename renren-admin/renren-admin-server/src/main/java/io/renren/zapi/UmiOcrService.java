package io.renren.zapi;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class UmiOcrService {
    @Resource
    private ZConfig config;
    @Resource
    private RestTemplate restTemplate;

    public String[] getText(String filePath) {
        byte[] bytes = FileUtil.readBytes(filePath);
        return this.getText(bytes);
    }

    public String[] getText(byte[] bytes) {
        byte[] encode = Base64.getEncoder().encode(bytes);
        String base64 = new String(encode);
        Map<String, Object> options = new HashMap<>() {{
            put("data.format", "text");
        }};
        Map<String, Object> map = new HashMap<>() {{
            put("base64", base64);
            put("options", options);
        }};
        String s = restTemplate.postForObject(config.getOcrApi(), map, String.class);
        JSONObject jsonObject = JSON.parseObject(s);
        String data = jsonObject.getString("data");
        String[] strings = data.lines().toArray(size -> new String[size]);
        return strings;
    }

}
