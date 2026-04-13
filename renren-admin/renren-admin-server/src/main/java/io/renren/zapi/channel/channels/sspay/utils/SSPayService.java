package io.renren.zapi.channel.channels.sspay.utils;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

/**
 * @author Justin Yi
 * @version 1.0
 * @classname DomeService
 * @description TODO
 * @date 2022/3/8 17:50
 **/
@Service
@Slf4j
public class SSPayService {


    @Autowired
    private PropConfig propConfig;


    public String demo(MerchantBaseVo vo){
        String decrypt="";
        try {
            decrypt = AESUtil.decrypt(vo.getContent(), Constants.AES_KEY);
            log.info(decrypt);
            MerchantContentResultVo merchantContentResultVo = JSON.parseObject(decrypt, MerchantContentResultVo.class);
            if (1==merchantContentResultVo.getStatus()) {
                log.info("返回成功");
                return "success";
            }
            return "fail";
        } catch (Exception e) {
            log.error("错误信息"+e);
            return "fail";
        }
    }



    public void createOrderTest(){
        //放入参数
        MerchantContentVo vo=new MerchantContentVo();
        vo.setMno("A222205088888888");
        vo.setOrderno(UUIDUtil.getUUID());
        vo.setAmount(1000);
        vo.setCode(PayType.ZFB.getCode());
        vo.setAsync_notify_url("http://172.18.56.22:8080/order/demo");
        //签名加密
        vo.setSign(createOrderSign(vo));
        log.info("sign加密为:"+vo.getSign());
        String json = JSON.toJSONString(vo);
        String content ="";
        //内容加密
        try {
            content = AESUtil.encrypt(json,Constants.AES_KEY);
            log.info("content加密为:"+content);

            log.info("MerchantContentVo:"+json);
            //请求参数
            MultiValueMap<String, String> postParameters = new LinkedMultiValueMap<>();
            postParameters.add("mno", "A222205088888888");
            postParameters.add("content", content);
            //构建headers
            HttpHeaders headers = new HttpHeaders();
            headers.add( HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
            HttpEntity<MultiValueMap<String, String>> r = new HttpEntity<>(postParameters, headers);
            URI uri = new URI(propConfig.getCreateorderUrl());
            RestTemplate restTemplate=new RestTemplate();
            //发送请求
            String responseMessage = restTemplate.postForObject(uri, r, String.class);
            log.info(responseMessage);
        } catch (Exception e) {
            log.error("请求异常",e);

        }

    }



    public void queryOrderTest(){
        //放入参数
        MerchantContentQueryVo vo=new MerchantContentQueryVo();
        vo.setMno("A222205088888888");
        vo.setOrderno("17BFD803ABA644C28B068B89D3CBE360");
        vo.setAmount(1000);
        //签名加密
        vo.setSign(queryOrderSign(vo));
        log.info("sign加密为:"+vo.getSign());
        String json = JSON.toJSONString(vo);
        String content ="";
        //内容加密
        try {
            content = AESUtil.encrypt(json,Constants.AES_KEY);
            log.info("content加密为:"+content);

            log.info("MerchantContentVo:"+json);
            //请求参数
            MultiValueMap<String, String> postParameters = new LinkedMultiValueMap<>();
            postParameters.add("mno", "A222205088888888");
            postParameters.add("content", content);
            //构建headers
            HttpHeaders headers = new HttpHeaders();
            headers.add( HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
            HttpEntity<MultiValueMap<String, String>> r = new HttpEntity<>(postParameters, headers);
            URI uri = new URI(propConfig.getQueryOrderUrl());
            RestTemplate restTemplate=new RestTemplate();
            //发送请求
            String responseMessage = restTemplate.postForObject(uri, r, String.class);
            System.out.println(JSON.toJSONString(responseMessage));
        } catch (Exception e) {
            log.error("请求异常",e);
        }

    }



    public String createOrderSign(MerchantContentVo vo){
        String sign= String.format("amount=%s&async_notify_url=%s&mno=%s&orderno=%s&code=%s&key=%s",
                vo.getAmount(),vo.getAsync_notify_url(),vo.getMno(),vo.getOrderno(),vo.getCode(), Constants.MD5_KEY);
        log.info("未加密前："+sign);
        return MD5Util.encrypt(sign);
    }

    public String queryOrderSign(MerchantContentQueryVo vo){
        String sign= String.format("amount=%s&mno=%s&orderno=%s&key=%s",
                vo.getAmount(),vo.getMno(),vo.getOrderno(), Constants.MD5_KEY);
        log.info("未加密前："+sign);
        return MD5Util.encrypt(sign);
    }
}
