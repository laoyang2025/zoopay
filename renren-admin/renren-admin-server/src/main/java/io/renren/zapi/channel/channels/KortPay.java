package io.renren.zapi.channel.channels;


import cn.hutool.core.lang.Pair;
import com.alibaba.fastjson2.JSONObject;
import io.renren.commons.tools.exception.RenException;
import io.renren.commons.tools.redis.RedisUtils;
import io.renren.zadmin.entity.ZChannelEntity;
import io.renren.zadmin.entity.ZChargeEntity;
import io.renren.zadmin.entity.ZWithdrawEntity;
import io.renren.zapi.ZooConstant;
import io.renren.zapi.channel.dto.ChannelBalanceResponse;
import io.renren.zapi.channel.dto.ChannelChargeQueryResponse;
import io.renren.zapi.channel.dto.ChannelChargeResponse;
import io.renren.zapi.channel.dto.ChannelWithdrawResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

// mumbai的 制作代付
@Slf4j
public class KortPay extends PostJsonChannel {

    private String token;
    private long tokenTime;

    // {"message":{"code":200,"success":["SUCCESS"]},"data":{"access_token":"GwbripkGanejtHy54jG5BBTn7imJXxivdrX1NVRaNFP5vUlz72bG5ZtArtTgXSHRagdzw96JotKqFEXcX79RsY5BzjjN7iiSo8AKTcAch2eW6hfaKJWhuXRyijqEqXtiMVzcjpPOyw67PoF8H9ehG2zaxWT6zSSg7h4zF8kHFHklSM4cgek4w3rfAXiTDwqjZqvjTM3mYJTxdiQqvEMaupRk4qcVDMwZkRDgMSS83xhOUinucrs8CXfeg9TEoOYbMJNPzxmuACUpgr3ekjbzs8rmpKcWbbcgkn999A7oXqXs5EdCDKUmu8BS5dNaY24KT98eWAhTTMCb4lPUMqkzKz1FYpvMd4","expire_time":600},"type":"success"}
    private void getToken() {
        ZChannelEntity channelEntity = channelEntity();
        TreeMap<String, Object> map = new TreeMap<>() {{
            put("client_id", channelEntity.getPublicKey());
            put("secret_id", channelEntity.getPrivateKey());
        }};
        String resp = this.postJSON(channelEntity().getPlatformKey(), map);
        JSONObject parse = JSONObject.parse(resp);
        try {
            this.token = parse.getJSONObject("data").getString("access_token");
            log.info("get token: {}", this.token);
            this.tokenTime = new Date().getTime();
        } catch (Exception e) {
            this.token = null;
            this.tokenTime = 0;
            throw new RenException("无法获取渠道凭证");
        }
    }

    private HttpHeaders getHeaders(String api, TreeMap<String, Object> map) {
        HttpHeaders headers = new HttpHeaders();
        List<HttpMessageConverter<?>> messageConverters = restTemplate().getMessageConverters();
        for (HttpMessageConverter<?> messageConverter : messageConverters) {
            if (messageConverter instanceof StringHttpMessageConverter) {
                ((StringHttpMessageConverter) messageConverter).setDefaultCharset(Charset.forName("UTF8"));
            }
        }
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer " + this.token);
        return headers;
    }

    /**
     * 每个API要如何请求
     */
    @Override
    public String request(String url, TreeMap<String, Object> map, String api) {
        HttpHeaders headers = getHeaders(api, map);
        try {
            String rtn = this.postJSON(url, map, headers);
            return rtn;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 必须要计算签名
     *
     * @return
     */
    @Override
    public Pair<String, String> getSign(TreeMap<String, Object> map, String api) {
        return null;
    }

    @Override
    public void setChargeMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        throw new RenException("渠道不支持收款交易");
    }

    @Override
    public void setChargeQueryMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        throw new RenException("渠道不支持收款查询");
    }

    @Override
    public void setBalanceMap(TreeMap<String, Object> map) {
        throw new RenException("渠道不支持余额查询");
    }


    /**
     * Header
     * ---------------------------------------------------------
     * Content Type	application/json
     * Authorization	Bearer Put your generated token here
     * ---------------------------------------------------------
     * Parameters
     * ---------------------------------------------------------
     * beneName	Devid marshe
     * beneAccountNo	1236541211
     * beneifsc	valid ifsc code here
     * benePhoneNo	9658XXXXX0
     * beneBankName	XXXXXXXXXXXXXXX
     * clientReferenceNo	12XXXXXXXXXXXX
     * amount	1XXX
     * fundTransferType	IMPS
     * pincode	123XXX
     * custName	abc
     * custMobNo	9xxxxxxxx0
     * latlong	22.8031731,88.7874172,
     * paramA
     * paramB
     * Sample Request Body
     */
    public void setWithdrawMap(ZWithdrawEntity entity, TreeMap<String, Object> map) {
        if (this.token == null || new Date().getTime() - this.tokenTime > 3600 * 1000) {
            log.info("没有token, 先申请token...");
            getToken();
        }

        map.put("beneficiary_name", entity.getAccountUser());
        map.put("beneficiary_account_number", entity.getAccountNo());
        map.put("ifsc_code", entity.getAccountIfsc());
        map.put("amount", entity.getAmount().toString());
        map.put("request_id", entity.getId().toString());
        map.put("payment_mode", "IMPS");

    }


    @Override
    public ChannelChargeResponse doCharge(JSONObject jsonObject) {
        throw new RenException("渠道不支持收款");
    }

    @Override
    public ChannelWithdrawResponse doWithdraw(JSONObject jsonObject) {
        log.info("withdraw response: {}", jsonObject);
        ChannelWithdrawResponse response = new ChannelWithdrawResponse();
        boolean isError = jsonObject.getBooleanValue("isError");
        String status = jsonObject.getString("status");
        if (!isError && "PENDING".equals(status)) {
            response.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
            response.setChannelOrder(jsonObject.getString("transaction"));
            response.setError(null);
            return response;
        } else {
            if (jsonObject.getString("message") != null) {
                throw new RenException("渠道错误:" + jsonObject.getString("message"));
            }
            throw new RenException("渠道错误:未知错误");
        }
    }


    @Override
    public ChannelChargeQueryResponse doChargeQuery(JSONObject jsonObject) {
        throw new RenException("渠道不支持收款查询");
    }

    @Override
    public ChannelWithdrawResponse withdrawQuery(ZWithdrawEntity faWithdrawEntity) {
        if (faWithdrawEntity.getChannelOrder() == null) {
            ChannelWithdrawResponse response = new ChannelWithdrawResponse();
            response.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
            return response;
        }
        RestTemplate restTemplate = getContext().getRestTemplate();
        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        for (HttpMessageConverter<?> messageConverter : messageConverters) {
            if (messageConverter instanceof StringHttpMessageConverter) {
                ((StringHttpMessageConverter) messageConverter).setDefaultCharset(Charset.forName("UTF8"));
            }
        }


        TreeMap<String, Object> map = new TreeMap<>();
        map.put("transactionId", faWithdrawEntity.getChannelOrder());
        HttpHeaders headers = getHeaders(AbstractChannel.API_WITHDRAW_QUERY, map);

        StringBuilder queryString = new StringBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            try {
                String encodedKey = URLEncoder.encode(entry.getKey(), "UTF-8");
                String encodedValue = URLEncoder.encode(entry.getValue().toString(), "UTF-8");
                if (queryString.length() > 0) {
                    queryString.append("&");
                }
                queryString.append(encodedKey).append("=").append(encodedValue);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                throw new RenException("can not encode url");
            }
        }

        String url = channelEntity().getWithdrawQueryUrl() + "?" + queryString;
        getContext().info("send: {}", url);

        RequestEntity<Void> requestEntity = new RequestEntity<>(headers, HttpMethod.GET, URI.create(url));

        HttpEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
        String respStr = responseEntity.getBody();
        getContext().info("recv: {}", respStr);

        JSONObject parse = JSONObject.parseObject(respStr);

        ChannelWithdrawResponse response = new ChannelWithdrawResponse();
        if (parse.getString("status").equals("SUCCESS")) {
            response.setStatus(ZooConstant.WITHDRAW_STATUS_SUCCESS);
        } else if (parse.getString("status").equals("PENDING")) {
            response.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
        } else {
            response.setStatus(ZooConstant.WITHDRAW_STATUS_FAIL);
        }
        return response;
    }


    @Override
    public ChannelBalanceResponse doBalance(JSONObject jsonObject) {
        throw new RenException("渠道不支持查询余额");
    }

    /**
     *
     */
    @Override
    public ChannelChargeQueryResponse chargeNotified(String contentType, Object body, Long deptId, Long id, HttpServletRequest request, HttpServletResponse response, ZChargeEntity chargeEntity) throws IOException {
        throw new RenException("渠道不支持收款, 收款回调错误");
    }


    /**
     * 返回标准的代付状态
     */
    @Override
    public ChannelWithdrawResponse drawNotified(String contentType, Object body, Long deptId, Long id, HttpServletRequest request, HttpServletResponse response, ZWithdrawEntity withdrawEntity) throws IOException {
        JSONObject parse = JSONObject.parse((String) body);
        boolean success = "SUCCESS".equals(parse.getString("status"));

        ChannelWithdrawResponse resp = new ChannelWithdrawResponse();
        if (success) {
            resp.setStatus(ZooConstant.WITHDRAW_STATUS_SUCCESS);
        } else if (!success) {
            resp.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
            // todo: 什么代表明确失败
            // resp.setStatus(ZooConstant.WITHDRAW_STATUS_FAIL);
        } else {
            resp.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
        }
        return resp;
    }

    public String responseChargeOk() {
        return "SUCCESS";
    }

    public String responseWithdrawOk() {
        return "SUCCESS";
    }

    @Override
    public Pair<String, Object> webhook(Long deptId, Long channelId, String contentType, String body, HttpServletRequest request, HttpServletResponse response) {
        JSONObject parse = JSONObject.parse(body);
        long orderId = Long.parseLong(parse.getString("orderId"));
        ZWithdrawEntity entity = getContext().getWithdrawDao().selectById(orderId);
        BigDecimal amount = new BigDecimal(parse.getString("amount"));
        if (amount.compareTo(entity.getAmount()) != 0) {
            throw new RenException("金额不匹配");
        }
        return Pair.of(API_WITHDRAW_NOTIFY, entity);
    }

}
