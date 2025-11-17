package io.renren.zapi.channel.channels;


import cn.hutool.core.lang.Pair;
import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.fastjson2.JSON;
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
public class BluSwap extends PostJsonChannel {


    private HttpHeaders getHeaders(String api, TreeMap<String, Object> map) {
        HttpHeaders headers = new HttpHeaders();
        List<HttpMessageConverter<?>> messageConverters = restTemplate().getMessageConverters();
        for (HttpMessageConverter<?> messageConverter : messageConverters) {
            if (messageConverter instanceof StringHttpMessageConverter) {
                ((StringHttpMessageConverter) messageConverter).setDefaultCharset(Charset.forName("UTF8"));
            }
        }
        headers.setContentType(MediaType.APPLICATION_JSON);

        ZChannelEntity zChannelEntity = channelEntity();
        headers.add("x-api-key", zChannelEntity.getPrivateKey());
        return headers;
    }

    /**
     *   * "bank_account_number":"12345678910"
     *      * "ifsc":"BLU123547"
     *      * "name": "John Doe"
     *      * "upi id": "johndoe0l@example"
     *      * "contact_number":"8109001819"
     *      * "email_id: "edexamle.com
     *      * "account_type":"Savings"
     *      * "verify": true
     *
     * @param withdrawEntity
     */
    private String createContact(ZWithdrawEntity withdrawEntity) {
        log.info("创建受益人...");
        TreeMap<String, Object> map = new TreeMap<>();
        map.put("bank_account_number",withdrawEntity.getAccountNo());
        map.put("ifsc",withdrawEntity.getAccountIfsc());
        map.put("name", withdrawEntity.getAccountUser());
        map.put("contact_number","8109001819");
        map.put("email_id", "ed@examle.com");
        map.put("account_type","Savings");
        map.put("verify", true);
        ZChannelEntity channelEntity = channelEntity();
        String rtn = this.request(channelEntity.getChargeUrl(), map, AbstractChannel.API_CREATE_CONTACT);
        JSONObject jsonObject = JSON.parseObject(rtn).getJSONObject("data");
        String contactId = jsonObject.getString("contact_id");
        return contactId;
    }

    private String ensureContact(ZWithdrawEntity withdrawEntity) {
        ZChannelEntity channelEntity = channelEntity();
        RedisUtils redisUtils = getContext().getRedisUtils();
        String hashKey = "BluSwap_" + channelEntity.getId();
        String contactId = (String) redisUtils.hGet(hashKey, withdrawEntity.getAccountNo());
        if (contactId == null) {
            log.info("{} {} 没有登记, 先登记", channelEntity.getChannelLabel(), withdrawEntity.getAccountNo());
            contactId = createContact(withdrawEntity);
            redisUtils.hSet(hashKey, withdrawEntity.getAccountNo(), contactId);
        }
        log.info("{} contact id is: {}", withdrawEntity.getAccountNo(), contactId);
        return contactId;
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
                String requestUrl = url + "/"  + map.get("id") + "/check_status_for_cust";
                rtn = this.getJSON(requestUrl, map, headers);
            }
            else if (api.equals(AbstractChannel.API_BALANCE)) {
                rtn = this.getJSON(url, map, headers);
            }
            // 创建受益人
            else if (api.equals(AbstractChannel.API_CREATE_CONTACT)) {
                log.info("创建受益人:postJSON");
                rtn = this.postJSON(url, map, headers);
            }
            else {
                rtn = this.postJSON(url, map, headers);
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
        log.info("withdraw query: {}", entity.getChannelOrder());
        if (entity.getChannelOrder() == null) {
            throw new RenException("channel order is null, cannot query");
        }
        map.put("id", entity.getChannelOrder());
    }

    /**
     *
     * "contact_id":"14e5c290-2814-4465-acac-5a0a757603d3"
     * "amount": "100.00"
     * "payment_mode": "IMPS"
     * "description":"service'
     *
     * @param entity
     * @param map
     */
    @Override
    public void setWithdrawMap(ZWithdrawEntity entity, TreeMap<String, Object> map) {
        String contactId = this.ensureContact(entity);
        map.put("amount", entity.getAmount().toString());
        map.put("payment_mode", "IMPS");
        map.put("contact_id", contactId);
        map.put("description", "service");
    }

    @Override
    public ChannelChargeResponse doCharge(JSONObject jsonObject) {
        throw new RenException("不支持此操作");
    }

    /**
     * {"status":"SUCCESS","status_code":200,"message":"Succefully initiated payout!","detail":null,"data":{
     * "bluswap_transaction_id":"d62eb98d-65e5-4073-b283-beee2e781e7c",
     * "ref_id":"qohJ6GYw07FCvvf",
     * "psp_transaction_id":"trans_rk8xkk5HLKox7zW4XNFo",
     * "amount":"100.00",
     * "order_id":null,
     * "payment_mode":"IMPS",
     * "status":"Pending",
     * "currency":"INR",
     * "total_balance":"0.00",
     * "available_balance":"678.79",
     * "remarks":"service",
     * "to_account_number":"40302111000779"
     * }
     * }
     */
    @Override
    public ChannelWithdrawResponse doWithdraw(JSONObject jsonObject) {
        ChannelWithdrawResponse response = new ChannelWithdrawResponse();
        JSONObject data = jsonObject.getJSONObject("data");
        if (data.getString("status").equals("Pending")) {
            response.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
            String channelOrderId = data.getString("bluswap_transaction_id");
            response.setChannelOrder(channelOrderId);
            response.setError(null);
            return response;
        }
        throw new RenException("渠道异常");
    }


    @Override
    public ChannelChargeQueryResponse doChargeQuery(JSONObject jsonObject) {
        throw new RenException("渠道不支持收款查询");
    }


    /**
     * {"bluswap_transaction_id":"db79553d-2548-4cad-aa1d-2c1e2c68bf4a","ref_id":"xjJ5bTkCd9i01wv","transaction_type":"PAYOUT","utr":"517719055328","psp_transaction_id":"trans_gMPigAxJKApYKUjTNkWE","amount":"500.00","mode":"IMPS","status":"SUCCESS","beneficiary_account_number":"40261111001495","beneficiary_ifsc":"KLGB0040261","beneficiary_name":"Abhijith Ka","account_holder_name":"Abhijith Ka","status_description":"Payment has been Credited","created_at":"2025-06-26T19:28:13.446976","updated_at":"2025-06-26T19:52:36.551574"}
     */
    @Override
    public ChannelWithdrawResponse doWithdrawQuery(JSONObject jsonObject) {
        String status = jsonObject.getString("status");

        ChannelWithdrawResponse response = new ChannelWithdrawResponse();
        if (status.equals("SUCCESS")) {
            // 查询成功
            log.info("查询代付成功");
            response.setStatus(ZooConstant.WITHDRAW_STATUS_SUCCESS);
            response.setUtr(jsonObject.getString("utr"));
        } else {
            response.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
        }
        return response;
    }

    /**
     */
    @Override
    public ChannelBalanceResponse doBalance(JSONObject jsonObject) {
        JSONObject data = jsonObject.getJSONObject("data");
        ChannelBalanceResponse response = new ChannelBalanceResponse();
        BigDecimal balance = data.getBigDecimal("balance");
        BigDecimal real = data.getBigDecimal("mva_balance");
        response.setBalance(balance);
        response.setBalanceMemo(real.toString() + "|" + balance.toString());
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
