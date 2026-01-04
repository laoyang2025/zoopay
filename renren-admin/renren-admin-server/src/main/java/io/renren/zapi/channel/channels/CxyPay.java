package io.renren.zapi.channel.channels;


import cn.hutool.core.lang.Pair;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.util.concurrent.RateLimiter;
import io.renren.commons.tools.exception.RenException;
import io.renren.zadmin.entity.ZChannelEntity;
import io.renren.zadmin.entity.ZChargeEntity;
import io.renren.zadmin.entity.ZWithdrawEntity;
import io.renren.zapi.ZooConstant;
import io.renren.zapi.channel.channels.cxy.util.MD5Util;
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
import java.math.RoundingMode;
import java.util.*;

import io.renren.zapi.channel.channels.cxy.util.Aes;


// 老杨的机构:  Yang
@Slf4j
public class CxyPay extends PostFormChannel {

    /**
     * 签名值的字段名称
     */
    @Override
    public String signField() {
        return "sign";
    }

    /**
     * 计算签名: 不需要这里做签名
     */
    @Override
    public Pair<String, String> getSign(TreeMap<String, Object> map, String api) {
        return null;
    }

    /**
     * 业务参数
     * 参数名	必选	类型	说明
     * acqMerchantNo	是	string	商户编号
     * orderNo	是	string	平台订单号（保持唯一）
     * payType	是	string	交易类型（WX代表微信,ZFB代表支付宝,KJ代表快捷)
     * acqCode	是	string	通道编号
     * cardNo	否	string	卡号（KJ需要传，可以固定123456789）
     * notifyUrl	是	string	回调地址
     * orderAmt	是	string	交易金额（元）
     * sign	是	string	MD5签名
     * <p>
     * 返回示例
     * 返回参数说明
     * 参数名	类型	说明
     * rescode	string	响应码(00下单成功，其他失败)
     * resmsg	string	响应信息
     * payUrl	string	支付二维码链接
     * orderNo	string	系统订单号
     * time	string	时间戳
     * sign	string	MD5签名
     *
     * @param entity
     * @param map    agentNo	是	string	机构号
     *               body	是	string	请求数据（加密后）
     *               requestId	是	string	请求编号，当日唯一
     */
    @Override
    public void setChargeMap(ZChargeEntity entity, TreeMap<String, Object> map) {

        if (!rateLimiterSecond.tryAcquire()) {
            throw new RenException("被限流");
        }
        if (!rateLimiterMinute.tryAcquire()) {
            throw new RenException("被限流");
        }

        ZChannelEntity channelEntity = channelEntity();

        // body
        Map<String, Object> bodyMap = new TreeMap<>();
        bodyMap.put("acqMerchantNo", channelEntity.getMerchantId());
        bodyMap.put("orderNo", entity.getId().toString());
        bodyMap.put("payType", "ZFB");
        bodyMap.put("acqCode", channelEntity.getPublicKey());
        bodyMap.put("notifyUrl", getCollectNotifyUrl(entity));
        bodyMap.put("orderAmt", entity.getAmount().toString());
        bodyMap.put("sign", MD5Util.signData(bodyMap, channelEntity.getPrivateKey().substring(0, 16)));

        String bodyStr = null;
        try {
            String body = objectMapper().writeValueAsString(bodyMap);
            log.info("body: = {}", body);
            bodyStr = Aes.llyEncrypt(body, channelEntity.getPrivateKey().substring(16, 32));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        map.put("agentNo", channelEntity.getPlatformKey());
        map.put("requestId", UUID.randomUUID().toString());
        map.put("body", bodyStr);
    }


    @Override
    public void setChargeQueryMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();

        Map<String, Object> bodyMap = new TreeMap<>();
        bodyMap.put("orderNoDown", entity.getId().toString());
        bodyMap.put("agentNo", channelEntity.getPlatformKey());

        String bodyStr = null;
        try {
            log.info("签名前包含的字段: {}", objectMapper().writeValueAsString(bodyMap));
            bodyMap.put("sign", MD5Util.signData(bodyMap, channelEntity.getPrivateKey().substring(0, 16)));
            bodyStr = Aes.llyEncrypt(objectMapper().writeValueAsString(bodyMap),
                    channelEntity.getPrivateKey().substring(16, 32));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        map.put("agentNo", channelEntity.getPlatformKey());
        map.put("requestId", UUID.randomUUID().toString());
        map.put("body", bodyStr);
    }

    /**
     * 余额查询组串
     */
    @Override
    public void setBalanceMap(TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        map.put("mchNo", channelEntity.getMerchantId());
        map.put("appId", channelEntity.getPlatformKey());
    }


    private static RateLimiter rateLimiterSecond = RateLimiter.create(0.1);


    /**
     *
     */
    public void setWithdrawMap(ZWithdrawEntity entity, TreeMap<String, Object> map) {

        if (!rateLimiterSecond.tryAcquire()) {
            throw new RenException("被限流");
        }

        ZChannelEntity channelEntity = channelEntity();

    }

    /**
     *
     */
    @Override
    public void setWithdrawQueryMap(ZWithdrawEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        map.put("mchNo", channelEntity.getMerchantId());
        map.put("appId", channelEntity.getPlatformKey());
        map.put("transferId", entity.getChannelOrder());
        map.put("mchOrderNo", entity.getId().toString());
    }


    /**
     * recv: {"orderNo":"WXZFB855CF3AA30C04050920B28657F9",
     * "payUrl":"http://8.129.225.17/lvyou/toKqPay.app?id=WXZFB855CF3AA30C04050920B28657F9",
     * "rescode":"00",
     * "resmsg":"下单成功",
     * "sign":"d5adaab35e99aaa35d48f3b48d9d322f","time":"1767452793785"
     * }
     *
     * @param jsonObject
     * @return
     */
    @Override
    public ChannelChargeResponse doCharge(JSONObject jsonObject) {
        log.info("get response: {}", jsonObject);
        if (jsonObject.getString("rescode").equals("00")) {
            ChannelChargeResponse response = new ChannelChargeResponse();
            String payUrl = jsonObject.getString("payUrl");
            if (StringUtils.isNotEmpty(payUrl)) {
                response.setChannelOrder(jsonObject.getString("orderNo"));
                response.setPayUrl(payUrl);
                response.setUpi(null);
                response.setRaw(null);
                return response;
            }
            throw new RenException(channelEntity().getChannelLabel() + "错误:" + jsonObject.getString("msg"));
        } else {
            throw new RenException(channelEntity().getChannelLabel() + "错误:" + jsonObject.getString("msg"));
        }
    }

    /**
     *
     */
    @Override
    public ChannelWithdrawResponse doWithdraw(JSONObject jsonObject) {
        ChannelWithdrawResponse response = new ChannelWithdrawResponse();
        if (jsonObject.getIntValue("code") == 0) {
            JSONObject data = jsonObject.getJSONObject("data");
            int state = data.getIntValue("state");
            if (state == 0 || state == 1) {
                response.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
                response.setChannelOrder(data.getString("transferId"));
                response.setError(null);
                return response;
            }
            response.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
            response.setError(data.getString("msg"));
            return response;
        } else {
            throw new RenException("渠道错误:" + jsonObject.getString("msg"));
        }
    }

    /**
     *
     */
    @Override
    public ChannelChargeQueryResponse doChargeQuery(JSONObject jsonObject) {
        ChannelChargeQueryResponse response = new ChannelChargeQueryResponse();
        String code = jsonObject.getString("rescode");
        if (code.equals("00")) {
            response.setStatus(ZooConstant.CHARGE_STATUS_SUCCESS);
            return response;
        } else {
            response.setStatus(ZooConstant.CHARGE_STATUS_PROCESSING);
            response.setError(jsonObject.getString("resmsg"));
            return response;
        }
    }

    /**
     *
     */
    @Override
    public ChannelWithdrawResponse doWithdrawQuery(JSONObject jsonObject) {
        ChannelWithdrawResponse response = new ChannelWithdrawResponse();

        int code = jsonObject.getIntValue("code");
        if (code == 0) {
            JSONObject data = jsonObject.getJSONObject("data");
            int state = data.getIntValue("state");
            if (state == 2) {
                response.setChannelOrder(data.getString("order_number"));
                response.setStatus(ZooConstant.WITHDRAW_STATUS_SUCCESS);
            } else if (state == 3 || state == 4) {
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
     *
     */
    @Override
    public ChannelBalanceResponse doBalance(JSONObject jsonObject) {
        int code = jsonObject.getIntValue("code");
        if (code == 0) {
            JSONObject data = jsonObject.getJSONObject("data");

            ChannelBalanceResponse response = new ChannelBalanceResponse();
            BigDecimal bal = data.getBigDecimal("balance").divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            response.setBalance(bal);
            response.setBalanceMemo(bal.toString());
            return response;
        }
        throw new RenException("查询余额失败");
    }

    /**
     * 参数
     * 参数名	必选	类型	说明
     * agentNo	是	string	机构号
     * rescode	是	string	响应码（0000:成功，0001失败，处理中不会回调）
     * resmsg	是	string	响应描述
     * settleNo	是	string	下游订单号
     * money	是	string	金额
     * sign	    是	string	签名(和代付的签名方式一样)
     * <p>
     * 返回示例
     * {"resmsg":"成功","rescode":"00","orderNo":"123456","type":"WX","tradeNo":"45679", "tradeAmt":"123","sign":"A146546SSD5"
     * }
     */
    @Override
    public ChannelChargeQueryResponse chargeNotified(String contentType, Object body, Long deptId, Long id, HttpServletRequest request, HttpServletResponse response, ZChargeEntity chargeEntity) throws IOException {
        ChannelChargeQueryResponse resp = new ChannelChargeQueryResponse();

        TreeMap<String, Object> map = this.getTreeMapByForm((String) body);

        if ("0000".equals(map.get("rescode"))) {
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
        TreeMap<String, Object> map = checkSignByForm((String) body, API_WITHDRAW_NOTIFY);
        String state = (String) map.get("state");
        ChannelWithdrawResponse resp = new ChannelWithdrawResponse();
        if ("2".equals(state)) {
            resp.setStatus(ZooConstant.WITHDRAW_STATUS_SUCCESS);
        } else if ("3".equals(state)) {
            resp.setStatus(ZooConstant.WITHDRAW_STATUS_FAIL);
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

}
