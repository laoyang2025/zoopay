package io.renren.zapi.channel.channels;


import cn.hutool.core.lang.Pair;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import io.renren.commons.tools.exception.RenException;
import io.renren.entity.SysUserEntity;
import io.renren.zadmin.entity.ZChannelEntity;
import io.renren.zadmin.entity.ZChargeEntity;
import io.renren.zadmin.entity.ZWithdrawEntity;
import io.renren.zapi.ZooConstant;
import io.renren.zapi.channel.PayChannel;
import io.renren.zapi.channel.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.RequestNotExecutedException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;


// 老杨的机构:  Yang
@Slf4j
public class UCash implements PayChannel {
    static CloseableHttpClient httpClient = HttpClients.createDefault();
    static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    ChannelContext channelContext;

    @Override
    public ChannelContext getContext() {
        return channelContext;
    }

    @Override
    public void setContext(ChannelContext context) {
        this.channelContext = context;
    }

    @Override
    public ChannelChargeResponse charge(ZChargeEntity chargeEntity) {
        LocalDateTime datetime = LocalDateTime.now(ZoneId.of("America/New_York"));
        String reqTime = dateTimeFormatter.format(datetime);
        //按字典升序
        TreeMap<String, String> body = new TreeMap<>();
        body.put("orderId", chargeEntity.getId().toString());
        body.put("orderTime", reqTime);
        body.put("amount", chargeEntity.getAmount().toString());
        body.put("currencyType", "USD");
        body.put("goods", "goods");
        body.put("notifyUrl", getCollectNotifyUrl(chargeEntity));
        body.put("callBackUrl", getCollectNotifyUrl(chargeEntity));
        body.put("phone", "8197220658");
        body.put("name", "name");
        body.put("email", "zhaoq1753@gmail.com");

        String privateKey = channelContext.getChannelEntity().getPayCode();


        StringBuilder platSignOrigStr = new StringBuilder();
        Set<String> keys = body.keySet();
        Iterator var7 = keys.iterator();
        while (var7.hasNext()) {
            String key = (String) var7.next();
            String value = body.get(key);
            platSignOrigStr.append(key).append("=").append(value).append("&");

        }
        platSignOrigStr.append("key=").append(privateKey);
        System.out.println(platSignOrigStr);
        String sign = DigestUtils.md5Hex(platSignOrigStr.toString());
        System.out.println(sign);
        JSONObject head = new JSONObject();
        head.put("version", "20");
        head.put("mchtId", channelContext.getChannelEntity().getMerchantId());
        head.put("biz", "ca001");

        JSONObject data = new JSONObject();
        data.put("head", head);
        data.put("body", body);
        data.put("sign", sign);

        try {
            String dataStr = URLEncoder.encode(data.toJSONString(), "utf-8");
            System.out.println(dataStr);

            HttpPost httpPost = new HttpPost(channelContext.getChannelEntity().getChargeUrl());
            httpPost.setEntity(new StringEntity(dataStr));
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    System.out.println("空消息");
                }
                String responseStr = EntityUtils.toString(entity);
                log.info("应答: {}", responseStr);
                JSONObject jsonObject = JSON.parseObject(responseStr);
                String payUrl = jsonObject.getJSONObject("body").getString("payUrl");
                ChannelChargeResponse channelChargeResponse = new ChannelChargeResponse();
                channelChargeResponse.setPayUrl(payUrl);
                return channelChargeResponse;
            } catch (Exception ex) {
                throw new RenException("渠道异常");
            }
        } catch (UnsupportedEncodingException e) {
            throw new RenException("渠道异常");
        }
    }

    @Override
    public ChannelChargeQueryResponse chargeQuery(ZChargeEntity chargeEntity) {
        LocalDateTime datetime = LocalDateTime.now(ZoneId.of("America/New_York"));
        String reqTime = dateTimeFormatter.format(datetime);
        //按字典升序
        TreeMap<String, String> body = new TreeMap<>();
        body.put("orderTime", reqTime);
        body.put("currencyType", "USD");
        body.put("orderId", chargeEntity.getId().toString());
        body.put("tradeId", chargeEntity.getChannelOrder());

        String privateKey = channelContext.getChannelEntity().getPayCode();

        StringBuilder platSignOrigStr = new StringBuilder();
        Set<String> keys = body.keySet();
        Iterator var7 = keys.iterator();
        while (var7.hasNext()) {
            String key = (String) var7.next();
            String value = body.get(key);
            platSignOrigStr.append(key).append("=").append(value).append("&");
        }
        platSignOrigStr.append("key=").append(privateKey);
        log.info("待签名串: {}", platSignOrigStr);
        String sign = DigestUtils.md5Hex(platSignOrigStr.toString());
        System.out.println(sign);
        JSONObject head = new JSONObject();
        head.put("version", "20");
        head.put("mchtId", channelContext.getChannelEntity().getMerchantId());
        head.put("biz", "ca001");

        JSONObject data = new JSONObject();
        data.put("head", head);
        data.put("body", body);
        data.put("sign", sign);

        log.info("encode前发送数据: {}", data);

        try {
            String dataStr = URLEncoder.encode(data.toJSONString(), "utf-8");
            log.info("请求数据: {}", dataStr);

            HttpPost httpPost = new HttpPost(channelContext.getChannelEntity().getChargeQueryUrl());
            httpPost.setEntity(new StringEntity(dataStr));
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    System.out.println("空消息");
                }
                String responseStr = EntityUtils.toString(entity);
                log.info("收到应答:{}", responseStr);
            } catch (Exception ex) {
                throw new RenException("渠道异常");
            }
        } catch (UnsupportedEncodingException e) {
            throw new RenException("渠道异常");
        }
        throw new RenException("测试");
    }

    @Override
    public ChannelWithdrawResponse withdraw(ZWithdrawEntity withdrawEntity, SysUserEntity merchant) {
        return null;
    }

    @Override
    public ChannelWithdrawResponse withdrawQuery(ZWithdrawEntity faWithdrawEntity) {
        LocalDateTime datetime = LocalDateTime.now(ZoneId.of("America/New_York"));
        String reqTime = dateTimeFormatter.format(datetime);

        //按字典升序
        TreeMap<String, String> body = new TreeMap<>();
        body.put("currencyType", "USD");
        body.put("orderTime", reqTime);
        body.put("batchOrderNo", null);
        body.put("tradeId", faWithdrawEntity.getChannelOrder());
        body.put("mchtId", channelContext.getChannelEntity().getMerchantId());

        String privateKey = channelContext.getChannelEntity().getPayCode();

        StringBuilder platSignOrigStr = new StringBuilder();
        Set<String> keys = body.keySet();
        Iterator var7 = keys.iterator();
        while (var7.hasNext()) {
            String key = (String) var7.next();
            String value = body.get(key);
            platSignOrigStr.append(key).append("=").append(value).append("&");

        }
        platSignOrigStr.append("key=").append(privateKey);
        System.out.println(platSignOrigStr);
        String sign = DigestUtils.md5Hex(platSignOrigStr.toString());
        System.out.println(sign);
        JSONObject head = new JSONObject();
        head.put("version", "20");
        head.put("mchtId", channelContext.getChannelEntity().getMerchantId());
        head.put("biz", "ca001");

        JSONObject data = new JSONObject();
        data.put("head", head);
        data.put("body", body);
        data.put("sign", sign);

        try {
            String dataStr = URLEncoder.encode(data.toJSONString(), "utf-8");
            System.out.println(dataStr);

            HttpPost httpPost = new HttpPost(channelContext.getChannelEntity().getChargeUrl());
            httpPost.setEntity(new StringEntity(dataStr));
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    System.out.println("空消息");
                }
                String responseStr = EntityUtils.toString(entity);
                System.out.println(responseStr);
            } catch (Exception ex) {
                throw new RenException("渠道异常");
            }
        } catch (UnsupportedEncodingException e) {
            throw new RenException("渠道异常");
        }
        throw new RenException("测试");
    }

    @Override
    public ChannelBalanceResponse balance() {
        //按字典升序
        TreeMap<String, String> body = new TreeMap<>();
        body.put("currencyType", "USD");
        body.put("mchtId", channelContext.getChannelEntity().getMerchantId());

        String privateKey = channelContext.getChannelEntity().getPayCode();

        StringBuilder platSignOrigStr = new StringBuilder();
        Set<String> keys = body.keySet();
        Iterator var7 = keys.iterator();
        while (var7.hasNext()) {
            String key = (String) var7.next();
            String value = body.get(key);
            platSignOrigStr.append(key).append("=").append(value).append("&");
        }
        platSignOrigStr.append("key=").append(privateKey);
        System.out.println(platSignOrigStr);
        String sign = DigestUtils.md5Hex(platSignOrigStr.toString());
        System.out.println(sign);

        JSONObject head = new JSONObject();
        head.put("version", "20");
        head.put("mchtId", channelContext.getChannelEntity().getMerchantId());
        head.put("biz", "ca001");

        JSONObject data = new JSONObject();
        data.put("head", head);
        data.put("body", body);
        data.put("sign", sign);

        try {
            String dataStr = URLEncoder.encode(data.toJSONString(), "utf-8");
            System.out.println(dataStr);

            HttpPost httpPost = new HttpPost(channelContext.getChannelEntity().getBalanceUrl());
            httpPost.setEntity(new StringEntity(dataStr));
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    System.out.println("空消息");
                }
                String responseStr = EntityUtils.toString(entity);
                System.out.println("余额查询返回:" + responseStr);
            } catch (Exception ex) {
                throw new RenException("渠道异常");
            }
        } catch (UnsupportedEncodingException e) {
            throw new RenException("渠道异常");
        }
        throw new RenException("测试");
    }

    @Override
    public ChannelChargeQueryResponse chargeNotified(String contentType, Object body, Long deptId, Long
            id, HttpServletRequest request, HttpServletResponse response, ZChargeEntity chargeEntity) throws IOException {
        String bodystr = (String)body;
        JSONObject jsonObject = JSON.parseObject(bodystr);
        JSONObject headJSON = jsonObject.getJSONObject("head");
        JSONObject bodyJSON = jsonObject.getJSONObject("body");
        String amount = bodyJSON.getString("amount");
        String status = bodyJSON.getString("status");

        ChannelChargeQueryResponse queryResponse = new ChannelChargeQueryResponse();
        if (status.equals("SUCCESS")) {
            response.getWriter().print("SUCCESS");
            queryResponse.setStatus(ZooConstant.CHARGE_STATUS_SUCCESS);
            return queryResponse;
        } else {
            throw new RenException("失败");
        }
    }

    @Override
    public ChannelWithdrawResponse drawNotified(String contentType, Object body, Long deptId, Long
            id, HttpServletRequest request, HttpServletResponse response, ZWithdrawEntity withdrawEntity) throws IOException {
        return null;
    }
}