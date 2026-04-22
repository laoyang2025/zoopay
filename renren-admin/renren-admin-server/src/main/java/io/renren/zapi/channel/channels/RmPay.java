package io.renren.zapi.channel.channels;


import cn.hutool.core.lang.Pair;
import cn.hutool.crypto.digest.DigestUtil;
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

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.TreeMap;

// mumbai的 制作代付
@Slf4j
public class RmPay extends PostFormChannel {

    @Override
    public String signField() {
        return "pay_md5sign";
    }

    /**
     * 必须要计算签名
     *
     * @return
     */
    @Override
    public Pair<String, String> getSign(TreeMap<String, Object> map, String api) {
        ZChannelEntity channelEntity = channelEntity();
        // String signStr = this.md5SignString(map, false) + "&key=" + channelEntity.getPrivateKey();
        String signStr = this.md5SignString(map, false) + "&key=" + channelEntity.getPrivateKey();
        String sign = DigestUtil.md5Hex(signStr).toUpperCase();
        log.info("signStr = {}", signStr);
        log.info("sign = {}", sign);
        return Pair.of(signStr, sign);
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
        map.put("pay_memberid", getContext().getChannelEntity().getMerchantId());
    }

    @Override
    public void setWithdrawQueryMap(ZWithdrawEntity entity, TreeMap<String, Object> map) {
        map.put("mchid", getContext().getChannelEntity().getMerchantId());
        map.put("out_trade_no", entity.getId().toString());
    }


    /**
     * mchid	Merchant ID	Yes	Yes	Merchant ID assigned by the platform
     * out_trade_no	Merchant Order No.	Yes	Yes	Must be unique
     * money	Order Amount	Yes	Yes	Unit: Yuan
     * bankcode	IFSC CODE	Yes	Yes
     * bankname	Bank Name	Yes	Yes
     * accountname	Account Holder Name	Yes	Yes
     * cardnumber	Bank Card Number	Yes	Yes
     * notifyurl	Callback URL	Yes	Yes
     * pay_md5sign	MD5 Signature	Yes	No
     * @param entity
     * @param map
     */

    @Override
    public void setWithdrawMap(ZWithdrawEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        map.put("mchid", channelEntity.getMerchantId());
        map.put("out_trade_no", entity.getId().toString());
        map.put("money", entity.getAmount());
        map.put("bankcode", entity.getAccountIfsc());
        map.put("bankname", entity.getAccountBank());
        map.put("accountname", entity.getAccountUser());
        map.put("cardnumber", entity.getAccountNo());
        map.put("notifyurl", this.getWithdrawNotifyUrl(entity));
    }


    @Override
    public ChannelChargeResponse doCharge(JSONObject jsonObject) {
        throw new RenException("渠道不支持收款");
    }

    /**
     * status	        Status	Yes	Yes	success: success, error: failure (does not mean business success)
     * msg	            Status Description	Yes	Yes
     * transaction_id	Platform Transaction ID	Yes	Yes	Returned when successfu
     */

    @Override
    public ChannelWithdrawResponse doWithdraw(JSONObject jsonObject) {
        ChannelWithdrawResponse response = new ChannelWithdrawResponse();
        String status = jsonObject.getString("status");
        if (status.equals("success")) {
            response.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
            response.setChannelOrder(jsonObject.getString("transaction_id"));
            response.setError(null);
            return response;
        } else {
            if (jsonObject.getString("msg") != null) {
                throw new RenException("渠道错误:" + jsonObject.getString("msg"));
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
        String status = jsonObject.getString("status");
        if (status.equals("success")) {
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
