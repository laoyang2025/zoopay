package io.renren.zapi.channel.channels.crowq;


import cn.hutool.core.lang.Pair;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.renren.commons.tools.exception.RenException;
import io.renren.zadmin.entity.ZChannelEntity;
import io.renren.zadmin.entity.ZChargeEntity;
import io.renren.zadmin.entity.ZWithdrawEntity;
import io.renren.zapi.ZooConstant;
import io.renren.zapi.channel.channels.PostJsonChannel;
import io.renren.zapi.channel.dto.ChannelBalanceResponse;
import io.renren.zapi.channel.dto.ChannelChargeQueryResponse;
import io.renren.zapi.channel.dto.ChannelChargeResponse;
import io.renren.zapi.channel.dto.ChannelWithdrawResponse;
import io.renren.zapi.utils.CommonUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.TreeMap;

// 这个渠道参数特点:
// 商户私钥:  Hmac密钥
// 平台公钥:  商户私钥
// 商户公钥:  商户公钥

// 马克支付 渠道
@Slf4j
public class Crowq extends PostJsonChannel {

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
        String signstr = md5SignString(map, false);
        String sign = HmacSHA256Util.sha256_HMACStr(signstr, channelEntity().getPrivateKey());
        if (api.equals(API_WITHDRAW)) {
            try {
                sign = RSAPrivateKeyEncryption.encrypt(sign, channelEntity().getPlatformKey());
            } catch (Exception e) {
                throw new RenException("can not encrypt for crowq");
            }
        }
        return Pair.of(signstr, sign);
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

    /**
     * 应答里解析出标准应答
     */
    @Override
    public ChannelChargeResponse doCharge(JSONObject jsonObject) {
        if (jsonObject.getIntValue("code") == 200) {
            JSONObject data = jsonObject.getJSONObject("data");
            ChannelChargeResponse response = new ChannelChargeResponse();
            String payUrl = data.getString("orderData");
            if (StringUtils.isNotEmpty(payUrl)) {
                response.setChannelOrder(data.getString("orderNo"));
                response.setPayUrl(payUrl);
                response.setUpi(null);
                response.setRaw(null);
                return response;
            }
            throw new RenException(channelEntity().getChannelLabel() + "错误:" + data.getString("subMsg"));
        } else {
            throw new RenException(channelEntity().getChannelLabel() + "错误:" + jsonObject.getString("msg"));
        }
    }

    /**
     * 余额查询组串
     */
    @Override
    public void setBalanceMap(TreeMap<String, Object> map) {
        map.put("clientId", channelEntity().getMerchantId());
        map.put("secretKey", channelEntity().getPrivateKey());
    }

    public void setWithdrawMap(ZWithdrawEntity entity, TreeMap<String, Object> map) {
        map.put("bankCode", "IMPS");
        map.put("province", entity.getAccountIfsc());
        map.put("accName", entity.getAccountUser());
        map.put("accNo", entity.getAccountNo());
        map.put("busiCode", "203001");
        map.put("currency", "INR");
        map.put("email", "dummy@gmail.com");
        map.put("merNo", channelEntity().getMerchantId());
        map.put("merOrderNo", entity.getId().toString());
        map.put("notifyUrl", getWithdrawNotifyUrl(entity));
        map.put("orderAmount", entity.getAmount().toString());
        map.put("phone", "9812315145");
        map.put("timestamp", new Date().getTime());
    }

    /**
     * 组代付查询请求报文
     */
    @Override
    public void setWithdrawQueryMap(ZWithdrawEntity entity, TreeMap<String, Object> map) {
        map.put("merNo", channelEntity().getMerchantId());
        map.put("requestNo", new Date().getTime());
        map.put("merOrderNo", entity.getId().toString());
        map.put("orderNo", entity.getChannelOrder());
        map.put("timestamp", new Date().getTime());
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
            JSONObject data = jsonObject.getJSONObject("data");
            int status = data.getIntValue("status");
            // 发起代付成功
            if (status == 0 || status == 9) {
                response.setChannelOrder(data.getString("orderNo"));
                response.setError(null);
            } else {
                response.setError(data.getString("subMsg"));
            }
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
                return response;
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
            int status = data.getIntValue("status");
            if (status == 7) {
                response.setChannelOrder(data.getString("orderNo"));
                response.setStatus(ZooConstant.WITHDRAW_STATUS_SUCCESS);
            } else if (status == 2 || status == 6 || status == 8) {
                response.setError("渠道明确失败");
                response.setStatus(ZooConstant.WITHDRAW_STATUS_FAIL);
            } else {
                response.setError("处理中");
                response.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
            }
        } else {
            response.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
            response.setError("渠道异常:" + jsonObject.getString("msg"));
        }
        return response;
    }


    /**
     * 从余额查询结果里返回标准的余额查询应答
     */
    @Override
    public ChannelBalanceResponse doBalance(JSONObject jsonObject) {
        ChannelBalanceResponse response = new ChannelBalanceResponse();
        Long balance = (long) (jsonObject.getJSONObject("body").getDoubleValue("balance") * 100);
        BigDecimal bal = new BigDecimal(balance);
        response.setBalance(bal);
        response.setBalanceMemo(bal.toString());
        return response;
    }

    /**
     *
     */
    @Override
    public ChannelChargeQueryResponse chargeNotified(String contentType, Object body, Long deptId, Long id, HttpServletRequest request, HttpServletResponse response, ZChargeEntity chargeEntity) throws IOException {
        ChannelChargeQueryResponse resp = new ChannelChargeQueryResponse();
        TreeMap<String, Object> map = checkSign((String) body, API_CHARGE_NOTIFY);
        if ((int) map.get("status") == 5) {
            resp.setStatus(ZooConstant.CHARGE_STATUS_SUCCESS);
            return resp;
        }
        resp.setStatus(ZooConstant.CHARGE_STATUS_PROCESSING);
        return resp;
    }


    /**
     * 返回标准的代付状态
     */
    @Override
    public ChannelWithdrawResponse drawNotified(String contentType, Object body, Long deptId, Long id, HttpServletRequest request, HttpServletResponse response, ZWithdrawEntity withdrawEntity) throws IOException {
        TreeMap<String, Object> map = checkSign((String) body, API_WITHDRAW_NOTIFY);
        int status = (int) map.get("status");

        ChannelWithdrawResponse resp = new ChannelWithdrawResponse();

        if (status == 7) {
            resp.setStatus(ZooConstant.WITHDRAW_STATUS_SUCCESS);
        } else if (status == 2 || status == 8) {
            resp.setStatus(ZooConstant.WITHDRAW_STATUS_FAIL);
        } else {
            resp.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
        }
        return resp;
    }

    /**
     * 检查对方签名
     *
     * @param body
     * @param api
     * @return
     * @throws JsonProcessingException
     */
    public TreeMap<String, Object> checkSign(String body, String api) throws JsonProcessingException {
        TreeMap<String, Object> map = getTreeMap(body);
        // 验证签名
        String sign = (String) map.get(signField());
        map.remove(signField());
        Pair<String, String> pair = getSign(map, api);
        if (!pair.getValue().equals(sign)) {
            log.error("验证签名错误: 对方签名[{}], 我方签名[{}], 我方签名串:[{}]", sign, pair.getValue(), pair.getKey());
            throw new RenException("invalid signature");
        }
        return map;
    }


    public String responseChargeOk() {
        return "SUCCESS";
    }

    public String responseWithdrawOk() {
        return "SUCCESS";
    }

}
