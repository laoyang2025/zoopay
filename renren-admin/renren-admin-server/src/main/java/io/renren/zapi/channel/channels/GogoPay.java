package io.renren.zapi.channel.channels;


import cn.hutool.core.lang.Pair;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.renren.commons.tools.exception.RenException;
import io.renren.zadmin.entity.ZChannelEntity;
import io.renren.zadmin.entity.ZChargeEntity;
import io.renren.zadmin.entity.ZWithdrawEntity;
import io.renren.zapi.ZooConstant;
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


// 老杨的机构:  Yang
@Slf4j
public class GogoPay extends PostFormChannel {

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
        Pair<String, String> pair = this.kvMd5Sign(map, null, null, false);
        return Pair.of(pair.getKey(), pair.getValue().toLowerCase());
    }

    /**
     * merchantid	是	string	商户号
     * out_trade_no	是	string	商户订单号
     * total_fee	是	string	交易金额（保留两位小数）
     * notify_url	是	string	异步通知地址
     * reply_type	否	string	执行方式：FORM或者URL,默认FORM(form直接跳转到三方收银台；URL是返回带url的json数据，数据结构看下面备注)
     * timestamp	是	string	时间戳
     * customer_name	是	string	客户姓名（必须是英文，去除空格）
     * customer_mobile	是	string	客户手机号
     * customer_email	是	string	客户邮箱
     * sign	是	string	签名
     */
    @Override
    public void setChargeMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        map.put("merchantid", channelEntity.getMerchantId());
        map.put("out_trade_no", entity.getId().toString());
        map.put("total_fee", entity.getAmount());
        map.put("notify_url", getCollectNotifyUrl(entity));
        map.put("reply_type", "url");
        map.put("timestamp", new Date().getTime());
        map.put("customer_name", "NA");
        map.put("customer_mobile", "9612313123123");
        map.put("customer_email", "NA");
    }

    /**
     * 组收款查询报文
     * merchantid	是	string	商户号
     * out_trade_no	是	string	商户订单号
     * timestamp	是	string	时间戳
     * sign	是	string	签名
     */
    @Override
    public void setChargeQueryMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        map.put("merchantid", channelEntity().getMerchantId());
        map.put("out_trade_no", entity.getId().toString());
        map.put("timestamp", new Date().getTime());
    }

    /**
     * 余额查询组串
     * merchantid	是	string	商户号
     * timestamp	是	string	时间戳
     * sign	是	string	签名
     */
    @Override
    public void setBalanceMap(TreeMap<String, Object> map) {
        map.put("merchantid", channelEntity().getMerchantId());
        map.put("timestamp", new Date().getTime());
    }

    /**
     * merchantid	是	string	商户号
     * out_trade_no	是	string	商户订单号
     * total_fee	是	string	交易金额（保留两位小数）
     * notify_url	是	string	异步通知地址
     * timestamp	是	string	时间戳
     * payment_mode	是	string	代付模式（IMPS）
     * account_number	是	string	账号
     * ifsc	是	string	IFSC
     * customer_name	是	string	客户姓名（必须是英文，去除空格）
     * customer_mobile	是	string	客户手机号
     * customer_email	是	string	客户邮箱
     * sign	是	string	签名
     */
    public void setWithdrawMap(ZWithdrawEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        map.put("merchantid", channelEntity.getMerchantId());
        map.put("out_trade_no", entity.getId().toString());
        map.put("total_fee", entity.getAmount());
        map.put("notify_url", getWithdrawNotifyUrl(entity));
        map.put("timestamp", new Date().getTime());
        map.put("payment_mode", "IMPS");
        map.put("account_number", entity.getAccountNo());
        map.put("ifsc", entity.getAccountIfsc());
        map.put("customer_name", entity.getAccountUser());
        map.put("customer_mobile", "981231231231");
        map.put("customer_email", "NA");
    }

    /**
     * 组代付查询请求报文
     */
    @Override
    public void setWithdrawQueryMap(ZWithdrawEntity entity, TreeMap<String, Object> map) {
        map.put("merchantid", channelEntity().getMerchantId());
        map.put("out_trade_no", entity.getId().toString());
        map.put("timestamp", new Date().getTime());
    }

    /**
     *
     */
    @Override
    public ChannelChargeResponse doCharge(JSONObject jsonObject) {
        if (jsonObject.getIntValue("code") == 0) {
            JSONObject data = jsonObject.getJSONObject("data");
            ChannelChargeResponse response = new ChannelChargeResponse();
            String payUrl = data.getString("url");
            if (StringUtils.isNotEmpty(payUrl)) {
                response.setChannelOrder(data.getString("order_number"));
                response.setPayUrl(payUrl);
                response.setUpi(null);
                response.setRaw(null);
                return response;
            }
            throw new RenException(channelEntity().getChannelLabel() + "错误:" + jsonObject.getString("message"));
        } else {
            throw new RenException(channelEntity().getChannelLabel() + "错误:" + jsonObject.getString("message"));
        }
    }

    /**
     * @param jsonObject
     * @return
     */
    @Override
    public ChannelWithdrawResponse doWithdraw(JSONObject jsonObject) {
        ChannelWithdrawResponse response = new ChannelWithdrawResponse();
        if (jsonObject.getIntValue("code") == 0) {
            JSONObject data = jsonObject.getJSONObject("data");
            response.setChannelOrder(data.getString("orderNumber"));
            response.setError(null);
            return response;
        } else {
            throw new RenException("渠道错误:" + jsonObject.getString("message"));
        }
    }

    /**
     * status	string	状态（交易中：payin_ing，交易成功：payin_success，交易失败：payin_fail）
     */
    @Override
    public ChannelChargeQueryResponse doChargeQuery(JSONObject jsonObject) {
        ChannelChargeQueryResponse response = new ChannelChargeQueryResponse();
        int code = jsonObject.getIntValue("code");
        if (code == 0) {
            JSONObject data = jsonObject.getJSONObject("data");
            String status = data.getString("status");
            if (status.equals("payin_success")) {
                response.setChannelOrder(data.getString("order_number"));
                response.setStatus(ZooConstant.CHARGE_STATUS_SUCCESS);
            } else {
                response.setStatus(ZooConstant.CHARGE_STATUS_PROCESSING);
            }
            return response;
        } else {
            response.setStatus(ZooConstant.CHARGE_STATUS_PROCESSING);
            response.setError(jsonObject.getString("msg"));
            return response;
        }
    }

    /**
     * 代付查询结果 --> 标准代付应答
     */
    @Override
    public ChannelWithdrawResponse doWithdrawQuery(JSONObject jsonObject) {
        ChannelWithdrawResponse response = new ChannelWithdrawResponse();

        int code = jsonObject.getIntValue("code");
        if (code == 0) {
            JSONObject data = jsonObject.getJSONObject("data");
            String status = data.getString("status");
            if (status.equals("payout_success")) {
                response.setChannelOrder(data.getString("order_number"));
                response.setStatus(ZooConstant.WITHDRAW_STATUS_SUCCESS);
            } else if (status.equals("payout_fail")) {
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
        int code = jsonObject.getIntValue("code");
        if (code == 0) {
            JSONObject data = jsonObject.getJSONObject("data");

            ChannelBalanceResponse response = new ChannelBalanceResponse();
            BigDecimal bal = new BigDecimal(data.getString("balance"));
            response.setBalance(bal);
            response.setBalanceMemo(bal.toString());
            return response;
        }
        throw new RenException("查询余额失败");
    }

    /**
     *
     */
    @Override
    public ChannelChargeQueryResponse chargeNotified(String contentType, Object body, Long deptId, Long id, HttpServletRequest request, HttpServletResponse response, ZChargeEntity chargeEntity) throws IOException {
        ChannelChargeQueryResponse resp = new ChannelChargeQueryResponse();

        TreeMap<String, Object> map = checkSignByForm((String) body, API_CHARGE_NOTIFY);
        if ("payin_success".equals(map.get("status"))) {
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
        String status = (String) map.get("status");
        ChannelWithdrawResponse resp = new ChannelWithdrawResponse();
        if ("payout_success".equals(status)) {
            resp.setStatus(ZooConstant.WITHDRAW_STATUS_SUCCESS);
        } else if ("payout_fail".equals(status)) {
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
