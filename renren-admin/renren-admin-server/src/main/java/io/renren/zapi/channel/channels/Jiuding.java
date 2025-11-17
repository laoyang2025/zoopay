package io.renren.zapi.channel.channels;


import cn.hutool.core.lang.Pair;
import com.alibaba.fastjson2.JSONObject;
import io.renren.commons.tools.exception.RenException;
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
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;


// 老杨的机构:  Yang
@Slf4j
public class Jiuding extends PostFormChannel {

    /**
     * 签名值的字段名称
     */
    @Override
    public String signField() {
        return "sign";
    }

    /**
     * 计算签名
     */
    @Override
    public Pair<String, String> getSign(TreeMap<String, Object> map, String api) {
        return this.kvMd5Sign(map, null, "key", false);
    }

    /**
     *
     */
    @Override
    public void setChargeMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        map.put("mchId", channelEntity.getMerchantId());
        map.put("productId", "2001");
        map.put("mchOrderNo", entity.getId().toString());
        map.put("amount", entity.getAmount().multiply(new BigDecimal(100)).longValue());
        map.put("currency", "INR");
        map.put("returnUrl", entity.getCallbackUrl());
        map.put("notifyUrl", this.getCollectNotifyUrl(entity));
        map.put("subject", "service");
        map.put("body", "service");
        map.put("reqTime", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        map.put("version", "1.0");
    }

    /**
     *
     */
    @Override
    public void setChargeQueryMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        map.put("mchId", channelEntity.getMerchantId());
        map.put("mchOrderNo", entity.getId().toString());
        map.put("payOrderId", entity.getChannelOrder());
        map.put("executeNotify", false);
        map.put("reqTime", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        map.put("version", "1.0");
    }

    /**
     *
     */
    @Override
    public void setBalanceMap(TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        map.put("mchId", channelEntity.getMerchantId());
        map.put("reqTime", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        map.put("version", "1.0");
    }

    /**
     *
     */
    public void setWithdrawMap(ZWithdrawEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        map.put("mchId", channelEntity.getMerchantId());
        map.put("mchOrderNo", entity.getId().toString());
        map.put("amount", entity.getAmount().multiply(new BigDecimal("100")).longValue());
        map.put("mobile", "912314555");
        map.put("email", "dummy@gmail.com");
        map.put("accountName", entity.getAccountUser());
        map.put("accountNo", entity.getAccountNo());
        map.put("bankNumber", entity.getAccountIfsc());
        map.put("notifyUrl", this.getWithdrawNotifyUrl(entity));
        map.put("reqTime", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        map.put("version", "1.0");
    }

    /**
     *
     */
    @Override
    public void setWithdrawQueryMap(ZWithdrawEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        map.put("mchId", channelEntity.getMerchantId());
        map.put("merOrderNo", entity.getId().toString());
        map.put("agentpayOrderId", entity.getChannelOrder());
        map.put("timestamp", new Date().getTime());
        map.put("reqTime", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        map.put("version", "1.0");
    }

    /**
     *
     */
    @Override
    public ChannelChargeResponse doCharge(JSONObject jsonObject) {
        String code = jsonObject.getString("retCode");
        if (!code.equals("0")) {
            throw new RenException("渠道失败:" + jsonObject.getString("retMsg"));
        }
        String payUrl = jsonObject.getString("payUrl");
        ChannelChargeResponse response = new ChannelChargeResponse();
        if (StringUtils.isNotEmpty(payUrl)) {
            response.setChannelOrder(jsonObject.getString("payOrderId"));
            response.setPayUrl(payUrl);
            response.setUpi(null);
            response.setRaw(null);
            return response;
        } else {
            throw new RenException("渠道失败");
        }
    }

    /**
     *
     */
    @Override
    public ChannelWithdrawResponse doWithdraw(JSONObject jsonObject) {
        ChannelWithdrawResponse response = new ChannelWithdrawResponse();
        String code = jsonObject.getString("retCode");
        if (code.equals("0")) {
            response.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
            response.setChannelOrder(jsonObject.getString("agentpayOrderId"));
        } else {
            response.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
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
        if (jsonObject.getString("status").equals("2")) {
            response.setChannelOrder(jsonObject.getString("payOrderId"));
            response.setStatus(ZooConstant.CHARGE_STATUS_SUCCESS);
        } else {
            response.setStatus(ZooConstant.CHARGE_STATUS_PROCESSING);
        }
        return response;
    }

    /**
     *
     */
    @Override
    public ChannelWithdrawResponse doWithdrawQuery(JSONObject jsonObject) {
        ChannelWithdrawResponse response = new ChannelWithdrawResponse();
        String code = jsonObject.getString("retCode");
        if (code.equals("0")) {
            int status = jsonObject.getIntValue("status");
            if (status == 2) {
                response.setStatus(ZooConstant.WITHDRAW_STATUS_SUCCESS);
            } else if (status == 3) {
                response.setStatus(ZooConstant.WITHDRAW_STATUS_FAIL);
            } else if (status == 4) {
                response.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
            } else {
                response.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
            }
            return response;
        }
        throw new RenException("渠道异常");
    }

    /**
     * 代付余额	agentpayBalance	是	int	10000	代付余额
     * 可用代付余额	availableAgentpayBalance	是	int	10000	可用代付余额
     */
    @Override
    public ChannelBalanceResponse doBalance(JSONObject jsonObject) {
        String retCode = jsonObject.getString("retCode");
        if (retCode.equals("0")) {
            BigDecimal dfbalance = new BigDecimal(jsonObject.getString("availableAgentpayBalance")).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            ChannelBalanceResponse response = new ChannelBalanceResponse();
            BigDecimal bal = new BigDecimal(jsonObject.getString("agentpayBalance")).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            response.setBalance(bal);
            response.setBalanceMemo(dfbalance.toString());
            return response;
        }
        throw new RenException("渠道异常:" + jsonObject.getString("retMsg"));
    }

    /**
     *
     */
    @Override
    public ChannelChargeQueryResponse chargeNotified(String contentType, Object body, Long deptId, Long id, HttpServletRequest request, HttpServletResponse response, ZChargeEntity chargeEntity) throws IOException {
        ChannelChargeQueryResponse resp = new ChannelChargeQueryResponse();

        Map<String, String[]> parameterMap = request.getParameterMap();
        TreeMap<String, Object> map = new TreeMap<>();
        parameterMap.forEach((k, v) -> {
            map.put(k, v[0]);
        });

        // 这个渠道变态, POST里传 GET参数
        checkSign(map, API_CHARGE_NOTIFY, false);

        if ("2".equals(map.get("status"))) {
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

//        TreeMap<String, Object> map = checkSignByForm((String) body, API_WITHDRAW_NOTIFY);

        Map<String, String[]> parameterMap = request.getParameterMap();
        TreeMap<String, Object> map = new TreeMap<>();
        parameterMap.forEach((k, v) -> {
//            log.info("{} -> {}", k, v[0]);
            map.put(k, v[0]);
        });

        String state = (String) map.get("status");
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
        return "success";
    }

    public String responseWithdrawOk() {
        return "success";
    }
}
