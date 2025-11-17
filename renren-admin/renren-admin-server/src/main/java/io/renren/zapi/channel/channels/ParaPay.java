package io.renren.zapi.channel.channels;


import cn.hutool.core.lang.Pair;
import com.alibaba.fastjson2.JSONObject;
import io.renren.commons.tools.exception.RenException;
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

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.TreeMap;

// mumbai的 制作代付
@Slf4j
public class ParaPay extends PostJsonChannel {


    /**
     * 必须要计算签名
     *
     * @return
     */
    @Override
    public Pair<String, String> getSign(TreeMap<String, Object> map, String api) {
        return null;
    }

    @Override
    public void setChargeMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        throw new RenException("渠道不支持收款交易");
    }

    @Override
    public void setChargeQueryMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        throw new RenException("渠道不支持收款查询");
    }

    @Override
    public void setBalanceMap(TreeMap<String, Object> map) {
        map.put("clientId", getContext().getChannelEntity().getPrivateKey());
        map.put("secretKey", getContext().getChannelEntity().getPublicKey());
    }

    @Override
    public void setWithdrawQueryMap(ZWithdrawEntity entity, TreeMap<String, Object> map) {
        map.put("clientId", getContext().getChannelEntity().getPrivateKey());
        map.put("secretKey", getContext().getChannelEntity().getPublicKey());
        map.put("clientOrderId", entity.getId().toString());
    }


    /**
     * "number": "Mobile Number"
     * "amount": "Txn Amount",
     * "transferMode":"IMPS",
     * "accourntNo": "Account Number",
     * "ifscCode":" ifscCode",
     * "beneficlaryName":BENENAME'
     * "vpa":"",
     * clientOrderId ": " Unigue Client Transaction Reference Number"
     *
     * @param entity
     * @param map
     */
    @Override
    public void setWithdrawMap(ZWithdrawEntity entity, TreeMap<String, Object> map) {
        map.put("clientId", getContext().getChannelEntity().getPrivateKey());
        map.put("secretKey", getContext().getChannelEntity().getPublicKey());

        map.put("number", "9821709914");
        map.put("amount", entity.getAmount().toString());
        map.put("transferMode", "IMPS");
        map.put("paymentMode", "5");
        map.put("accountNo", entity.getAccountNo());
        map.put("ifscCode", entity.getAccountIfsc());
        map.put("beneficiaryName", entity.getAccountUser());
        map.put("vpa", "");
        map.put("clientOrderId", entity.getId().toString());
    }


    @Override
    public ChannelChargeResponse doCharge(JSONObject jsonObject) {
        throw new RenException("渠道不支持收款");
    }


    @Override
    public ChannelWithdrawResponse doWithdraw(JSONObject jsonObject) {
        ChannelWithdrawResponse response = new ChannelWithdrawResponse();
        int statusCode = jsonObject.getIntValue("statusCode");
        if (statusCode == 1) {
            response.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
            response.setChannelOrder(jsonObject.getString("orderId"));
            response.setError(null);
            return response;
        } else {
            if (jsonObject.getString("message") != null) {
                throw new RenException("渠道错误:" + jsonObject.getString("message"));
            }
            throw new RenException("渠道错误:未知错误");
        }
    }


    @Override
    public ChannelChargeQueryResponse doChargeQuery(JSONObject jsonObject) {
        throw new RenException("渠道不支持收款查询");
    }

    @Override
    public ChannelWithdrawResponse doWithdrawQuery(JSONObject jsonObject) {
        ChannelWithdrawResponse response = new ChannelWithdrawResponse();
        int status = jsonObject.getIntValue("status");
        if (status == 1) {
            String utr = jsonObject.getString("utr");
            response.setUtr(utr);
            response.setStatus(ZooConstant.WITHDRAW_STATUS_SUCCESS);
        } else {
            response.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
        }
        return response;
    }


    /**
     * balance: {"statusCode":0,"message":"Credentials are invalid.","balance":0}
     * {"statusCode":1,"message":"Successfully","balance":2000.0000}
     *
     * @param jsonObject
     * @return
     */
    @Override
    public ChannelBalanceResponse doBalance(JSONObject jsonObject) {
        ChannelBalanceResponse response = new ChannelBalanceResponse();
        BigDecimal balance = jsonObject.getBigDecimal("balance").setScale(2, RoundingMode.UP);
        response.setBalance(balance);
        response.setBalanceMemo(balance.toString());
        return response;
    }

    /**
     *
     */
    @Override
    public ChannelChargeQueryResponse chargeNotified(String contentType, Object body, Long deptId, Long id, HttpServletRequest request, HttpServletResponse response, ZChargeEntity chargeEntity) throws IOException {
        throw new RenException("渠道不支持收款, 收款回调错误");
    }


    /**
     * 返回标准的代付状态
     */
    @Override
    public ChannelWithdrawResponse drawNotified(String contentType, Object body, Long deptId, Long id, HttpServletRequest request, HttpServletResponse response, ZWithdrawEntity withdrawEntity) throws IOException {

////        ZWithdrawEntity entity = (ZWithdrawEntity) withdrawEntity;
//        JSONObject parse = JSONObject.parse((String) body);
//        int status = parse.getIntValue("status");
//        ChannelWithdrawResponse resp = new ChannelWithdrawResponse();
//        if (status == 1) {
//            resp.setStatus(ZooConstant.WITHDRAW_STATUS_SUCCESS);
//        } else {
//            resp.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
//        }
//        return resp;

        return this.withdrawQuery(withdrawEntity);
    }

    public String responseChargeOk() {
        return "SUCCESS";
    }

    public String responseWithdrawOk() {
        return "SUCCESS";
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
