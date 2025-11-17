package io.renren.zapi.channel.channels;

import cn.hutool.core.codec.Base64;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.renren.entity.SysUserEntity;
import io.renren.zapi.channel.*;
import io.renren.zadmin.entity.ZChargeEntity;
import io.renren.zadmin.entity.ZWithdrawEntity;
import io.renren.zapi.channel.dto.*;
import io.renren.zapi.merchant.ApiContext;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TestChannel implements PayChannel {
    private ChannelContext context;

    @Override
    public ChannelContext getContext() {
        return this.context;
    }

    @Override
    public void setContext(ChannelContext context) {
        this.context = context;
    }


    /**
     * 职责: 返回一个ChannelChargeResponse
     * @param entity
     * @return
     */
    @Override
    public ChannelChargeResponse charge(ZChargeEntity entity) {
        ChannelChargeResponse response = new ChannelChargeResponse();
        Map<String, Object> map = new HashMap<>();
        map.put("dev", 1);
        map.put("accountNo",   "6221413523412311");
        map.put("accountUser", "Jessie Raj");
        map.put("accountBank", "BOI bank");
        map.put("accountIfsc", "139807398547");
        map.put("accountUpi",  "jessieRaj@upi");
        map.put("accountInfo", "accountInfo");
        map.put("amount", "10000");
        map.put("id", entity.getId());
        map.put("deadline", new Date().getTime() + 300 * 1000);
        String payUrl = null;
        try {
            String json = context.getObjectMapper().writeValueAsString(map);
            String encode = Base64.encode(json);
            payUrl = context.getConfig().getCdnUrl() + "/" + context.getDept().getCurrency() + "?mode=dev&base64=" + encode;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        response.setChannelOrder(Long.valueOf(System.currentTimeMillis()).toString());
        response.setUpi("xxxx@upi.com");
        response.setPayUrl(payUrl);
        return response;
    }

    /**
     * 职责: 返回一个ChannelWithdrawResponse
     * @param entity
     * @return
     */
    @Override
    public ChannelWithdrawResponse withdraw(ZWithdrawEntity entity, SysUserEntity merchant) {
        ChannelWithdrawResponse response = new ChannelWithdrawResponse();
        response.setChannelOrder(Long.valueOf(System.currentTimeMillis()).toString());
        context.info("send: {}", entity);
        return response;
    }

    /**
     * 返回
     * @param entity
     * @return
     */
    @Override
    public ChannelChargeQueryResponse chargeQuery(ZChargeEntity entity) {
        context.info("send: {}", entity);
        ChannelChargeQueryResponse response = new ChannelChargeQueryResponse();
        response.setChannelOrder(entity.getChannelOrder());
        response.setUpi("xxx@upi");
        response.setError("xxx"); // 有错误信息
        return response;
    }

    @Override
    public ChannelWithdrawResponse withdrawQuery(ZWithdrawEntity entity) {
        ChannelWithdrawResponse response = new ChannelWithdrawResponse();
        response.setChannelOrder(entity.getChannelOrder());
        response.setUpi("xxx@upi");
        response.setUtr("xxx@utr");
        return response;
    }

    @Override
    public ChannelBalanceResponse balance() {
        ChannelBalanceResponse channelBalanceResponse = new ChannelBalanceResponse();
        channelBalanceResponse.setBalanceMemo("no");
        channelBalanceResponse.setBalance(new BigDecimal("10000"));
        return channelBalanceResponse;
    }
}
