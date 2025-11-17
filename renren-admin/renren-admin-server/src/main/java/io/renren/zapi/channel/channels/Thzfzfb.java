package io.renren.zapi.channel.channels;

import cn.hutool.core.lang.Pair;
import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.renren.commons.tools.exception.RenException;
import io.renren.zadmin.entity.ZChannelEntity;
import io.renren.zadmin.entity.ZChargeEntity;
import io.renren.zadmin.entity.ZWithdrawEntity;
import io.renren.zapi.ZooConstant;
import io.renren.zapi.channel.channels.crowq.HmacSHA256Util;
import io.renren.zapi.channel.channels.crowq.RSAPrivateKeyEncryption;
import io.renren.zapi.channel.dto.ChannelBalanceResponse;
import io.renren.zapi.channel.dto.ChannelChargeQueryResponse;
import io.renren.zapi.channel.dto.ChannelChargeResponse;
import io.renren.zapi.channel.dto.ChannelWithdrawResponse;
import io.renren.zapi.utils.CommonUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
public class Thzfzfb extends PostJsonChannel {

    /**
     * 签名值的字段名称
     */
    @Override
    public String signField() {
        return "sign";
    }

    /**
     * 计算签名
     *
     * @return
     */
    @Override
    public Pair<String, String> getSign(TreeMap<String, Object> map, String api) {
        ZChannelEntity channelEntity = channelEntity();

        if (api.equals(API_WITHDRAW)) {
            String signstr = channelEntity.getMerchantId() + map.get("orderNo") + map.get("amount") + channelEntity.getPrivateKey();
            String sign = DigestUtil.md5Hex(signstr);
            return Pair.of(signstr, sign);
        }
        if (api.equals(API_WITHDRAW_QUERY)) {
            String signstr = channelEntity.getMerchantId() + map.get("orderNo") + channelEntity.getPrivateKey();
            String sign = DigestUtil.md5Hex(signstr);
            return Pair.of(signstr, sign);
        }
        if (api.equals(API_BALANCE)) {
            String signstr = channelEntity.getMerchantId() + channelEntity.getPrivateKey();
            String sign = DigestUtil.md5Hex(signstr);
            return Pair.of(signstr, sign);
        }
        throw new RenException("unsupported api:" + api);
    }

    /**
     * 应答里解析出标准应答
     */
    @Override
    public ChannelChargeResponse doCharge(JSONObject jsonObject) {
        if (jsonObject.getIntValue("code") == 200) {
            JSONObject data = jsonObject.getJSONObject("data");
            ChannelChargeResponse response = new ChannelChargeResponse();
            response.setChannelOrder(data.getString("orderNo"));
            response.setPayUrl(data.getString("orderData"));
            response.setUpi(null);
            response.setRaw(null);
            return response;
        } else {
            throw new RenException(jsonObject.getString("msg"));
        }
    }

    /**
     * 余额查询组串
     */
    @Override
    public void setBalanceMap(TreeMap<String, Object> map) {
        map.put("merchantNum", channelEntity().getMerchantId());
    }

    /**
     *
     */
    @Override
    public void setChargeMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        map.put("merNo", channelEntity.getMerchantId());
        map.put("merOrderNo", entity.getId().toString());
        map.put("name", "Raj");
        map.put("email", "910231231234@gmail.com");
        map.put("phone", "910123125244");
        map.put("orderAmount", entity.getAmount().toString());
        map.put("currency", "INR");
        map.put("busiCode", channelEntity.getPayCode());
        map.put("pageUrl", entity.getCallbackUrl());
        map.put("notifyUrl", getCollectNotifyUrl(entity));
        map.put("timestamp", System.currentTimeMillis());
    }

    /**
     * 组收款查询报文
     */
    @Override
    public void setChargeQueryMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        map.put("merNo", channelEntity().getMerchantId());
        map.put("requestNo", CommonUtils.randomDigitString(15));
        map.put("merOrderNo", entity.getId().toString());
        map.put("orderNo", entity.getChannelOrder());
        map.put("timestamp", String.valueOf(System.currentTimeMillis()));
    }

    public void setWithdrawMap(ZWithdrawEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        map.put("merchantNum", channelEntity.getMerchantId());
        map.put("amount", entity.getAmount().toString());
        map.put("orderNo", entity.getId().toString());
        map.put("notifyUrl", getWithdrawNotifyUrl(entity));
        map.put("payType", channelEntity.getPayCode());
        map.put("bankName", entity.getAccountBank());
        map.put("account", entity.getAccountUser());
        map.put("cardNumber", entity.getAccountNo());
    }

    /**
     * 组代付查询请求报文
     */
    @Override
    public void setWithdrawQueryMap(ZWithdrawEntity entity, TreeMap<String, Object> map) {
        map.put("merchantNum", channelEntity().getMerchantId());
        map.put("amount", entity.getAmount().toString());
        map.put("orderNo", entity.getId().toString());
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
        if (jsonObject.getIntValue("code") == 200) {
            response.setError("init success");
            return response;
        } else {
            response.setError(jsonObject.getString("msg"));
        }
        return response;
    }

    /**
     *
     */
    @Override
    public ChannelChargeQueryResponse doChargeQuery(JSONObject jsonObject) {
        ChannelChargeQueryResponse response = new ChannelChargeQueryResponse();
        int code = jsonObject.getIntValue("code");
        if (code == 200) {
            JSONObject data = jsonObject.getJSONObject("data");
            int status = data.getIntValue("status");
            if (status == 5) {
                response.setChannelOrder(data.getString("orderNo"));
                response.setStatus(ZooConstant.CHARGE_STATUS_SUCCESS);
            }
            response.setStatus(ZooConstant.CHARGE_STATUS_PROCESSING);
        } else {
            response.setStatus(ZooConstant.CHARGE_STATUS_PROCESSING);
            response.setError(jsonObject.getString("msg"));
        }
        return response;
    }


    /**
     * 代付查询结果 --> 标准代付应答
     */
    @Override
    public ChannelWithdrawResponse doWithdrawQuery(JSONObject jsonObject) {
        ChannelWithdrawResponse response = new ChannelWithdrawResponse();
        int code = jsonObject.getIntValue("code");
        if (code == 200) {
            JSONObject data = jsonObject.getJSONObject("data");
            String state = data.getString("state");
            if (state.equals("3")) {
                response.setStatus(ZooConstant.WITHDRAW_STATUS_SUCCESS);
            } else if (state.equals("4")) {
                response.setError("渠道失败");
                response.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
            } else if (state.equals("5")) {
                response.setError("渠道取消");
                response.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
            } else {
                response.setError("渠道未知状态");
                response.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
            }
        }
        return response;
    }


    /**
     * 从余额查询结果里返回标准的余额查询应答
     */
    @Override
    public ChannelBalanceResponse doBalance(JSONObject jsonObject) {
        ChannelBalanceResponse response = new ChannelBalanceResponse();
        int code = jsonObject.getIntValue("code");
        if (code == 200) {
            double data = jsonObject.getDoubleValue("data");
            long bal = (long) data * 100;
            BigDecimal balance = new BigDecimal(bal).divide(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP);
            response.setBalance(balance);
            response.setBalanceMemo(balance.toString());
            return response;
        }
        throw new RenException("无法获取余额");
    }

    /**
     *
     */
    @Override
    public ChannelChargeQueryResponse chargeNotified(String contentType, Object body, Long deptId, Long id, HttpServletRequest request, HttpServletResponse response, ZChargeEntity chargeEntity) throws IOException {
        ChannelChargeQueryResponse resp = new ChannelChargeQueryResponse();
        resp.setStatus(ZooConstant.CHARGE_STATUS_PROCESSING);
        return resp;
    }


    /**
     * 返回标准的代付状态
     */
    @Override
    public ChannelWithdrawResponse drawNotified(String contentType, Object body, Long deptId, Long id, HttpServletRequest request, HttpServletResponse response, ZWithdrawEntity withdrawEntity) throws IOException {
        TreeMap<String, Object> map = parseForm((String) body);
        String state = (String)map.get("state");
        if (state.equals("1") || state.equals("0")) {
            ChannelWithdrawResponse resp = this.withdrawQuery(withdrawEntity);
            return resp;
        }
        ChannelWithdrawResponse resp = new ChannelWithdrawResponse();
        resp.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
        return resp;
    }

    public String responseChargeOk() {
        return "SUCCESS";
    }

    public String responseWithdrawOk() {
        return "SUCCESS";
    }

}
