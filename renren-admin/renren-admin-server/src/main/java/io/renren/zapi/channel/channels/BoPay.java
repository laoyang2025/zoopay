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
import java.util.Date;
import java.util.TreeMap;


// 老杨的机构:  Yang
@Slf4j
public class BoPay extends PostFormChannel {

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
        return this.kvMd5Sign(map, null, "key", false);
    }

    /**
     * {
     * "mchNo": "M1682438621",
     * "appId": "646f3530e4b04e3b9b238851",
     * "mchOrderNo": "22130240124114756",
     * "amount": 10000,
     * "customerName": "test",
     * "customerEmail": "213142131@gmail.com",
     * "customerPhone": "9106090211",
     * "notifyUrl": "http://localhost:9216/api/pay/notify",
     * "sign": "114A16B391576447073E482C0C022144"
     * }
     */
    @Override
    public void setChargeMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        map.put("mchNo", channelEntity.getMerchantId());
        map.put("appId", channelEntity.getPlatformKey());
        map.put("mchOrderNo", entity.getId().toString());
        map.put("amount", entity.getAmount().multiply(new BigDecimal(100)).setScale(0));
        map.put("customerName", "NA");
        map.put("customerPhone", "961231312");
        map.put("customerEmail", "NA");
        map.put("notifyUrl", getCollectNotifyUrl(entity));
    }

    /**
     * {
     * "mchOrderNo": "202205101000000000",
     * "payOrderId": "P202205101000111111",
     * "mchNo": "M1234567890",
     * "appId": "60cc09bce4b0f1c0b83761c9",
     * "sign": "E54C1ADC0691F97B68EBFAD0D0B6AFDA"
     * }
     */
    @Override
    public void setChargeQueryMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        map.put("mchNo", channelEntity.getMerchantId());
        map.put("appId", channelEntity.getPlatformKey());
        map.put("mchOrderNo", entity.getId().toString());
        map.put("payOrderId", entity.getChannelOrder());
        map.put("timestamp", new Date().getTime());
    }

    /**
     * 余额查询组串
     * {
     * "mchNo": "M1621873433953",
     * "appId": "60cc09bce4b0f1c0b83761c9",
     * "sign": "C380BEC2BFD727A4B6845133519F3AD6"
     * }
     */
    @Override
    public void setBalanceMap(TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        map.put("mchNo", channelEntity.getMerchantId());
        map.put("appId", channelEntity.getPlatformKey());
    }

    /**
     * {
     * "entryType": "IMPS",
     * "amount": 10000,
     * "accountNo": "342501000001234",
     * "accountCode": "IOBA0003123",
     * "accountName": "Yash Ashish Singh",
     * "mchOrderNo": "202205101000000000",
     * "accountEmail": "test@gmail.com",
     * "accountPhone": "8446429999",
     * "mchNo": "M1234567890",
     * "appId": "60cc09bce4b0f1c0b83761c9",
     * "sign": "E54C1ADC0691F97B68EBFAD0D0B6AFDA",
     * "notifyUrl": "http://127.0.0.1:8888",
     * "transferDesc": "测试",
     * "bankName": "INDUSIND BANK"
     * }
     */
    public void setWithdrawMap(ZWithdrawEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        map.put("entryType", "IMPS");
        map.put("amount", entity.getAmount().multiply(new BigDecimal("100")).setScale(0));
        map.put("accountNo", entity.getAccountNo());
        map.put("accountCode", entity.getAccountIfsc());
        map.put("accountName", entity.getAccountUser());
        map.put("mchOrderNo", entity.getId().toString());
        map.put("accountEmail", "NA@gmail.com");
        map.put("accountPhone", "981231231231");
        map.put("mchNo", channelEntity.getMerchantId());
        map.put("appId", channelEntity.getPlatformKey());
        map.put("notifyUrl", getWithdrawNotifyUrl(entity));
        map.put("transferDesc", new Date().getTime());
        map.put("bankName", entity.getAccountBank());
    }

    /**
     * {
     * "mchNo": "M1621873433953",
     * "appId": "60cc09bce4b0f1c0b83761c9",
     * "transferId": "T20160427210604000490",
     * "mchOrderNo": "20160427210604000490",
     * "sign": "C380BEC2BFD727A4B6845133519F3AD6"
     * }
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
     * {
     * "code": 0,
     * "data": {
     * "errCode": "ACQ.PAYMENT_AUTH_CODE_INVALID",
     * "errMsg": "Business Failed【支付失败，获取顾客账户信息失败，请顾客刷新付款码后重新收款，如再次收款失败，请联系管理员处理。[SOUNDWAVE_PARSER_FAIL]】",
     * "mchOrderNo": "mho1234567890",
     * "orderState": 3,
     * "payOrderId": "P202205101000111111"
     * },
     * "msg": "SUCCESS",
     * "sign": "1F0A241B0349894B0C8D68BE0CB40EE1"
     * }
     * <p>
     * 0-订单生成 1-支付中 2-支付成功 3-支付失败 4-已撤销 5-已退款 6-订单关闭
     */
    @Override
    public ChannelChargeResponse doCharge(JSONObject jsonObject) {
        if (jsonObject.getIntValue("code") == 0) {
            JSONObject data = jsonObject.getJSONObject("data");
            int orderState = data.getIntValue("orderState");
            if (orderState == 0 || orderState == 1) {
                ChannelChargeResponse response = new ChannelChargeResponse();
                String payUrl = data.getString("payData");
                if (StringUtils.isNotEmpty(payUrl)) {
                    response.setChannelOrder(data.getString("payOrderId"));
                    response.setPayUrl(payUrl);
                    response.setUpi(null);
                    response.setRaw(null);
                    return response;
                }
            }
            throw new RenException(channelEntity().getChannelLabel() + "错误:" + jsonObject.getString("message"));
        } else {
            throw new RenException(channelEntity().getChannelLabel() + "错误:" + jsonObject.getString("message"));
        }
    }

    /**
     * @param jsonObject
     * @return {
     * "code": 0,
     * "data": {
     * "transferId": "T15235212621323757826",
     * "mchOrderNo": "1383381636006560990",
     * "amount": 5000,
     * "mchFeeAmount": 150,
     * "amountTo": 5150,
     * "accountNo": "39690000025",
     * "accountName": "Sandhya",
     * "state": 1,
     * "errCode": "500 INTERNAL_SERVER_ERROR",
     * "errMsg": "not yet implemented"
     * },
     * "msg": "SUCCESS",
     * "sign": "50F6F538C710563501767B8610B5A886"
     * }
     * 0-订单生成 1-转账中 2-转账成功 3-转账失败 4-转账关闭
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
     * {
     * "code": 0,
     * "msg": "签名失败",
     * "data": {
     * "customerName": "ZhangSan",
     * "appId": "60cc09bce4b0f1c0b83761c9",
     * "errCode": "1002",
     * "mchNo": "M1621873433953",
     * "state": 2,
     * "extParam": "exercitation ut eiusmod",
     * "errMsg": "Business Failed 失败",
     * "channelOrderNo": "C202205101000222222",
     * "mchOrderNo": "202205101000000000",
     * "createdAt": 1652148000000,
     * "customerEmail": "zhangsan@gmail.com",
     * "amount": 10000,
     * "customerPhone": "13800000000",
     * "payOrderId": "T202108161731281310004",
     * "successTime": 1652168000000,
     * "currency": "INR"
     * },
     * "sign": "1F0A241B0349894B0C8D68BE0CB40EE1"
     * }
     * 1-支付中 2-支付成功 3-支付失败 6-订单关闭
     */
    @Override
    public ChannelChargeQueryResponse doChargeQuery(JSONObject jsonObject) {
        ChannelChargeQueryResponse response = new ChannelChargeQueryResponse();
        int code = jsonObject.getIntValue("code");
        if (code == 0) {
            JSONObject data = jsonObject.getJSONObject("data");
            int state = data.getIntValue("state");
            if (state == 2) {
                response.setChannelOrder(data.getString("payOrderId"));
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
     * {
     * "code": 0,
     * "msg": "SUCCESS",
     * "sign": "CCD9083A6DAD9A2DA9F668C3D4517A84",
     * "data": { "state": 1}
     * }
     * 0-订单生成 1-转账中 2-转账成功 3-转账失败 4-转账关闭
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
     * 从余额查询结果里返回标准的余额查询应答
     * {
     * "code": 0,
     * "data": {
     * "appId": "62f4b3896298cde6414b9616",
     * "balance": 17513,
     * "mchNo": "M1660203913",
     * "payoutBalance": 8240,
     * "agentBalance": 0
     * },
     * "msg": "SUCCESS",
     * "sign": "0C003867FF1DF8C6572F32E8B133AA65"
     * }
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
     *
     */
    @Override
    public ChannelChargeQueryResponse chargeNotified(String contentType, Object body, Long deptId, Long id, HttpServletRequest request, HttpServletResponse response, ZChargeEntity chargeEntity) throws IOException {
        ChannelChargeQueryResponse resp = new ChannelChargeQueryResponse();

        TreeMap<String, Object> map = checkSignByForm((String) body, API_CHARGE_NOTIFY);
        if ("2".equals(map.get("state"))) {
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
