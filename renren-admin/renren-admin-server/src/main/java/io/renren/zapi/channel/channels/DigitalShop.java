package io.renren.zapi.channel.channels;


import cn.hutool.core.lang.Pair;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.renren.zadmin.entity.ZChannelEntity;
import io.renren.zadmin.entity.ZWithdrawEntity;
import io.renren.zapi.ZooConstant;
import io.renren.zapi.channel.dto.ChannelContext;
import io.renren.zapi.channel.dto.ChannelWithdrawResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.TreeMap;

public class DigitalShop extends PostFormChannel {

    /**
     * 签名值的字段名称
     */
    @Override
    public String signField() {
        return "sign";
    }

    /**
     * 计算签名: signstr, sign
     */
    @Override
    public Pair<String, String> getSign(TreeMap<String, Object> map, String api) {
        return null;
    }

    @Override
    public void setWithdrawMap(ZWithdrawEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        map.put("UserID", channelEntity.getMerchantId());  //
        map.put("Token", channelEntity.getPrivateKey());  //

        map.put("Account", entity.getAccountNo());
        map.put("Ifsc", entity.getAccountNo());
        map.put("Name", entity.getAccountUser());
        map.put("Mobile", "9712312312");
        map.put("Email", "rajhad.zj@gmai.com");
        map.put("Amount", entity.getAmount());
        map.put("Description", "service fee");
        map.put("TransactionID", entity.getId().toString());
        map.put("BankName", "UCO Bank");
    }

    /**
     * 代付应答里 --》
     *
     * @param jsonObject
     * @return
     */
    @Override
    public ChannelWithdrawResponse doWithdraw(JSONObject jsonObject) {
        ChannelWithdrawResponse response = new ChannelWithdrawResponse();
        boolean status = jsonObject.getBooleanValue("error");
        if (status) {
            response.setError(jsonObject.getString("message"));
            response.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
        } else {
            response.setChannelOrder(jsonObject.getString("payout_id"));
            response.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
            response.setChannelOrder(jsonObject.getString("Ack_no"));
        }
        return response;
    }


    /**
     * 代付查询结果 --> 标准代付应答
     */
    @Override
    public ChannelWithdrawResponse doWithdrawQuery(JSONObject jsonObject) {
        ChannelWithdrawResponse response = new ChannelWithdrawResponse();
        boolean status = jsonObject.getBooleanValue("status");
        int statusCode = jsonObject.getIntValue("statusCode");
        if (status && statusCode == 200) {
            JSONObject data = jsonObject.getJSONObject("data");
            String inStatus = data.getString("status");
            if (inStatus.equals("Credited")) {
                response.setStatus(ZooConstant.WITHDRAW_STATUS_SUCCESS);
                response.setUtr(data.getString("UTR"));
            } else if (inStatus.equals("Processing") || inStatus.equals("Pending")) {
                response.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
            } else {
                response.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
            }
        } else {
            response.setStatus(ZooConstant.WITHDRAW_STATUS_FAIL);
            response.setError(jsonObject.getString("message"));
        }
        return response;
    }

    /**
     * 组代付查询请求报文
     */
    @Override
    public void setWithdrawQueryMap(ZWithdrawEntity entity, TreeMap<String, Object> map) {
        map.put("payout_id", entity.getChannelOrder());
    }

    @Override
    public ChannelWithdrawResponse drawNotified(String contentType, Object body, Long deptId, Long id, HttpServletRequest request, HttpServletResponse response, ZWithdrawEntity withdrawEntity) throws IOException {
        ChannelWithdrawResponse res = this.withdrawQuery(withdrawEntity);
        return res;
    }

    /**
     * Object: ZWithdrawEntity   ZChargeEntity
     */
    @Override
    public Pair<String, Object> webhook(Long deptId, Long channelId, String contentType, String body, HttpServletRequest request, HttpServletResponse response) {

        JSONObject jsonObject = JSON.parseObject(body);
        String event = jsonObject.getString("event");
        String status = jsonObject.getString("status");
        JSONObject data = jsonObject.getJSONObject("data");

        // 代付通知
        if (event.equals("TRANSFER_STATUS_UPDATE")) {
            if (status.equals("SUCCESS")) {
                String channelOrder = data.getString("payout_id");
                Long id = Long.parseLong(data.getString("reference"));
                ZWithdrawEntity entity = getContext().getWithdrawDao().selectOne(Wrappers.<ZWithdrawEntity>lambdaQuery()
                        .eq(ZWithdrawEntity::getChannelId, channelEntity().getId())
                        .eq(ZWithdrawEntity::getChannelOrder, channelOrder)
                        .eq(ZWithdrawEntity::getId, id)
                );
                return Pair.of(API_WITHDRAW_NOTIFY, entity);
            }
        }

        return null;
    }

    @Override
    public String request(String url, TreeMap<String, Object> map, String api) {
        return this.postForm(url, map, getHttpHeaders());
    }

    // 请求头设置
    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        List<HttpMessageConverter<?>> messageConverters = restTemplate().getMessageConverters();
        for (HttpMessageConverter<?> messageConverter : messageConverters) {
            if (messageConverter instanceof StringHttpMessageConverter) {
                ((StringHttpMessageConverter) messageConverter).setDefaultCharset(Charset.forName("UTF8"));
            }
        }
        ChannelContext context = getContext();
        ZChannelEntity channelEntity = channelEntity();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("x-client-id", channelEntity.getMerchantId());
        headers.add("x-client-secret", channelEntity.getPrivateKey());
        return headers;
    }

    @Override
    public String responseWithdrawOk() {
        return "{\"status\":\"success\",\"message\":\"data received\"}";
    }

    @Override
    public String responseChargeOk() {
        return "{\"status\":\"success\",\"message\":\"data received\"}";
    }

}
