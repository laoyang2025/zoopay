package io.renren.zapi.channel.channels;


import cn.hutool.core.lang.Pair;
import cn.hutool.crypto.digest.DigestUtil;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.util.List;
import java.util.TreeMap;

//
@Slf4j
public class WorldPay extends PostFormChannel {
    private String token;

    private void getToken() {
        ZChannelEntity channelEntity = channelEntity();
        TreeMap<String, Object> map = new TreeMap<>() {{
            put("clientKey", channelEntity.getPublicKey());
            put("clientSecret", channelEntity.getPrivateKey());
        }};
        String resp = this.postJSON(channelEntity.getChargeUrl(), map);
        JSONObject parse = JSONObject.parse(resp);
        JSONObject data = parse.getJSONObject("data");
        try {
            String token = data.getString("access_token");
            log.info("渠道凭证: {}", token);
            // 存入redis
            RedisUtils redisUtils = getContext().getRedisUtils();
            redisUtils.set("worldpay_" + channelEntity.getId(), token, RedisUtils.HOUR_SIX_EXPIRE);
        } catch (Exception e) {
            log.error("无法获取渠道凭证");
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
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        ZChannelEntity zChannelEntity = channelEntity();
        String token = (String) this.getContext().getRedisUtils().get("worldpay_" + zChannelEntity.getId());
        headers.add("Authorization", "Bearer " + token);
        return headers;
    }

    /**
     * 每个API要如何请求
     */
    @Override
    public String request(String url, TreeMap<String, Object> map, String api) {
        HttpHeaders headers = getHeaders(api, map);
        try {
            String rtn;
            // 代付查询用GET请求
            if (api.equals(AbstractChannel.API_WITHDRAW_QUERY)) {
                rtn = this.getForm(url + "/" + map.get("id"), map, headers);
            } else {
                rtn = this.postForm(url, map, headers);
            }
            return rtn;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public String signField() {
        return "sign";
    }

    @Override
    public Pair<String, String> getSign(TreeMap<String, Object> map, String api) {
        String signstr = this.md5SignString(map, true) + "&key=" + channelEntity().getPrivateKey();
        String sign = DigestUtil.md5Hex(signstr).toLowerCase();
        map.put("sign", sign);
        return Pair.of(signstr, sign);
    }


    @Override
    public void setChargeMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        throw new RenException("不支持");
    }

    @Override
    public void setChargeQueryMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        throw new RenException("不支持");
    }

    @Override
    public void setBalanceMap(TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        map.put("merchantNumber", channelEntity.getMerchantId());
    }

    /**
     * {
     * "outTradeNo": "MKN2347-012123",
     * "merchantNumber": "10086",
     * "sign": "6512bd43d9caa6e02c990b0a82652dca"
     * }
     */
    @Override
    public void setWithdrawQueryMap(ZWithdrawEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        RedisUtils redisUtils = getContext().getRedisUtils();
        String token = (String) redisUtils.get("worldpay_" + channelEntity.getId());
        if (token == null) {
            getToken();
        }

        map.put("id", entity.getId().toString());
    }

    /**
     * amount * 100
     * reference * 23456789
     * trans_mode * imps
     * account * 100
     * ifsc * 456432456
     * name * adarsh
     * email * pateladarsh@gmail.com
     * mobile * 677567896678
     * address * bhopal
     *
     * @param entity
     * @param map
     */
    @Override
    public void setWithdrawMap(ZWithdrawEntity entity, TreeMap<String, Object> map) {

        ZChannelEntity channelEntity = channelEntity();
        RedisUtils redisUtils = getContext().getRedisUtils();
        String token = (String) redisUtils.get("worldpay_" + channelEntity.getId());
        if (token == null) {
            log.info("没有token, 先申请token...");
            getToken();
        }

        int amount = entity.getAmount().intValue();
        map.put("amount", amount);
        map.put("reference", entity.getId().toString());
        map.put("trans_mode", "imps");
        map.put("account", entity.getAccountNo());
        map.put("ifsc", entity.getAccountIfsc());
        map.put("name", entity.getAccountUser());
        map.put("email", "pateladarsh@gmail.com");
        map.put("mobile", "7507504129");
        map.put("address", "bhopal");
    }

    @Override
    public ChannelChargeResponse doCharge(JSONObject jsonObject) {
        throw new RenException("不支持此操作");
    }

    /**
     * {
     * "uuid": "85647968-aee6-4f9a-a79f-e0b3b9edc469",
     * // 200 成功其余失败
     * "code": "200",
     * "msg": "success",
     * "data": {
     * // 平台单号
     * "orderNo": "Onnafals12313231",
     * // 0 = 提交成功 其余失败
     * "status": "0"
     * }
     * }
     *
     * @param jsonObject
     * @return
     */
    @Override
    public ChannelWithdrawResponse doWithdraw(JSONObject jsonObject) {
        ChannelWithdrawResponse response = new ChannelWithdrawResponse();
        JSONObject data = jsonObject.getJSONObject("data");
        if (data.getString("status").equals("Processed")) {
            response.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
            response.setError(null);
        }
        throw new RenException("渠道异常");
    }


    @Override
    public ChannelChargeQueryResponse doChargeQuery(JSONObject jsonObject) {
        throw new RenException("渠道不支持收款查询");
    }


    /**
     * {"status":"success","data":{"transactionNo":"1922899647545327617","status":"Failed","utr":null}}
     *
     * @param jsonObject
     * @return
     */
    @Override
    public ChannelWithdrawResponse doWithdrawQuery(JSONObject jsonObject) {
        JSONObject data = jsonObject.getJSONObject("data");
        String status = data.getString("status");

        ChannelWithdrawResponse response = new ChannelWithdrawResponse();
        if (status.equals("Failed")) {
            response.setStatus(ZooConstant.WITHDRAW_STATUS_FAIL);
        } else if (status.equals("Success")) {
            // 查询成功
            log.info("查询代付成功");
            response.setStatus(ZooConstant.WITHDRAW_STATUS_SUCCESS);
            response.setUtr(data.getString("utr"));
        } else {
            response.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
        }
        return response;
    }

    /**
     * {
     * "uuid": "85647968-aee6-4f9a-a79f-e0b3b9edc469",
     * "code": "200",
     * "msg": "success",
     * "data": {
     * // 商户总余额
     * "balance": "2000",
     * // 代付中余额 : 商户总余额-代付中余额=可用余额
     * "forceBalance": "100",
     * "availableBalance": "1900"
     * }
     * }
     */
    @Override
    public ChannelBalanceResponse doBalance(JSONObject jsonObject) {
        JSONObject data = jsonObject.getJSONObject("data");
        ChannelBalanceResponse response = new ChannelBalanceResponse();
        BigDecimal balance = new BigDecimal(data.getString("balance"));
        BigDecimal real = new BigDecimal(data.getString("availableBalance"));
        response.setBalance(balance);
        response.setBalanceMemo(real.toString());
        return response;
    }


    @Override
    public ChannelChargeQueryResponse chargeNotified(String contentType, Object body, Long deptId, Long id, HttpServletRequest request, HttpServletResponse response, ZChargeEntity chargeEntity) throws IOException {
        throw new RenException("不支持");
    }


    /**
     * 返回标准的代付状态
     */
    @Override
    public ChannelWithdrawResponse drawNotified(String contentType, Object body, Long deptId, Long id, HttpServletRequest request, HttpServletResponse response, ZWithdrawEntity withdrawEntity) throws IOException {
        return this.withdrawQuery(withdrawEntity);
    }

    public String responseChargeOk() {
        return "success";
    }

    public String responseWithdrawOk() {
        return "success";
    }

    @Override
    public Pair<String, Object> webhook(Long deptId, Long channelId, String contentType, String body, HttpServletRequest request, HttpServletResponse response) {
        JSONObject parse = JSONObject.parse(body);
        long orderId = Long.parseLong(parse.getString("ClientOrderId"));
        ZWithdrawEntity entity = getContext().getWithdrawDao().selectById(orderId);
        BigDecimal amount = new BigDecimal(parse.getString("Amount")).setScale(2, RoundingMode.UP);
        if (amount.compareTo(entity.getAmount()) != 0) {
            throw new RenException("金额不匹配");
        }
        return Pair.of(API_WITHDRAW_NOTIFY, entity);
    }

}
