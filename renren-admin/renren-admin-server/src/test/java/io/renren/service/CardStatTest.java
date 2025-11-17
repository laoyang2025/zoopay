package io.renren.service;


import cn.hutool.core.codec.Base64;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.core.JSONOutputFormat;
import io.renren.zapi.card.CardStat;
import io.renren.zapi.utils.CommonUtils;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CardStatTest {

    @Resource
    private ObjectMapper objectMapper;

    @Test
    public void testMap() throws JsonProcessingException {
        Map<String, Object> x = new HashMap<>(){{
            put("a", 1);
            put("b", "2");
        }};

        String s = objectMapper.writeValueAsString(x);
        System.out.println(s);
        TreeMap<String, Object> stringObjectMap = objectMapper.readValue(s, new TypeReference<TreeMap<String, Object>>() {
        });

        stringObjectMap.forEach((k, v) -> {
            System.out.println(k + " -> " + v);
        });

        System.out.println(objectMapper.writeValueAsString(stringObjectMap));


        s = "{\"orderNo\":\"2409095000000013775754398\",\"orderAmount\":\"200.00\",\"merNo\":\"803200000139615\",\"merOrderNo\":\"1832990086284193794\",\"payTime\":\"2024-09-09 11:47:27\",\"resultCode\":null,\"sign\":\"dd388d295d42de705ad8ba7864c8d3f08130df420a6c29fc1e1d69c472547f5a\",\"status\":7,\"resultMsg\":null}";
        stringObjectMap = objectMapper.readValue(s, new TypeReference<TreeMap<String, Object>>() {});

        stringObjectMap.forEach((k, v) -> {
            System.out.println(k + " -> " + v);
        });
    }

    @Test
    public void amount() {
        BigDecimal matched =  new BigDecimal(10.00);
        BigDecimal entity = new BigDecimal("10");
        if (!matched.subtract(entity).setScale(0).equals(BigDecimal.ZERO)) {
            System.out.println("10.00, 10 not zero");
        } else {
            System.out.println("is zero");
        }
    }

    @Test
    public void matchTest() {
        String message = "## The error may exist in io/renren/zadmin/dao/ZRouteDao.java (best guess)\n" +
                "### The error may involve io.renren.zadmin.dao.ZRouteDao.insert-Inline\n" +
                "### The error occurred while setting parameters\n" +
                "### SQL: INSERT INTO z_route  ( id, dept_id, updater, update_date, dept_name, merchant_id, merchant_name,  route_type, process_mode, object_id, object_name, weight, pay_code,   creator, create_date )  VALUES (  ?, ?, ?, ?, ?, ?, ?,  ?, ?, ?, ?, ?, ?,   ?, ?  )\n" +
                "### Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry '1830962804438577154-withdraw-withdraw-1829532346104578050' for key 'uidx_z_route_0'\n" +
                "; Duplicate entry '1830962804438577154-withdraw-withdraw-1829532346104578050' for key 'uidx_z_route_0'";
        String regex = ".*Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry '(\\d+)-([a-zA-Z]+)-([a-zA-Z]+)-(\\d+)' for key .*";
        Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(message);
        if(matcher.matches()) {
            System.out.println(matcher.group(1));
        } else {
            System.out.println("not matched''");
        }

    }

    @Test
    public void testTimeZone() {
        // 指定目标时区


    }


}