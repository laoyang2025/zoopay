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
import java.util.Date;
import java.util.TreeMap;

//
@Slf4j
public class FengPay extends PostJsonChannel {

    @Override
    public String signField() {
        return "sign";
    }

    @Override
    public Pair<String, String> getSign(TreeMap<String, Object> map, String api) {
        String signstr = this.md5SignString(map, true) + "&key=" + channelEntity().getPrivateKey();
        String sign = DigestUtil.md5Hex(signstr).toLowerCase();
        map.put("sign", sign);
        return Pair.of(signstr, sign);
    }


    @Override
    public void setChargeMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        throw new RenException("不支持");
    }

    @Override
    public void setChargeQueryMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        throw new RenException("不支持");
    }

    @Override
    public void setBalanceMap(TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        map.put("merchantNumber", channelEntity.getMerchantId());
    }

    /**
     * {
     *     "outTradeNo": "MKN2347-012123",
     *     "merchantNumber": "10086",
     *     "sign": "6512bd43d9caa6e02c990b0a82652dca"
     * }
     */
    @Override
    public void setWithdrawQueryMap(ZWithdrawEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        map.put("outTradeNo", entity.getId().toString());
        map.put("merchantNumber", channelEntity.getMerchantId());
    }

    /**
     * {
     *   "merchantNumber": "10086",
     *   "outTradeNo": "PP262024100911014795623",
     *   "amount": "300.00",
     *   "notifyUrl": "https://xxx.xx",
     *   "accName": "Vic",
     *   "accNo": "988741323333",
     *   "ifsc": "ifsc",
     *   "mobileNo": "9887415157",
     *   "sign": "6512bd43d9caa6e02c990b0a82652dca"
     * }
     */
    @Override
    public void setWithdrawMap(ZWithdrawEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
              map.put("merchantNumber", channelEntity.getMerchantId());
              map.put("outTradeNo", entity.getId().toString());
              map.put("amount", entity.getAmount().toString());
              map.put("notifyUrl", this.getWithdrawNotifyUrl(entity));
              map.put("accName", entity.getAccountUser());
              map.put("accNo", entity.getAccountNo());
              map.put("ifsc", entity.getAccountIfsc());
              map.put("mobileNo", "9887415157");
    }


    @Override
    public ChannelChargeResponse doCharge(JSONObject jsonObject) {
        if (!jsonObject.getString("msg").equals("success")) {
            throw new RenException("渠道错误");
        }
        ChannelChargeResponse resp = new ChannelChargeResponse();
        JSONObject data = jsonObject.getJSONObject("data");
        resp.setChannelOrder(data.getString("order_num"));
        resp.setPayUrl(data.getString("pay_url"));
        return resp;
    }

    /**
     *

     *
     * {
     *     "uuid": "85647968-aee6-4f9a-a79f-e0b3b9edc469",
     *     // 200 成功其余失败
     *     "code": "200",
     *     "msg": "success",
     *     "data": {
     *         // 平台单号
     *         "orderNo": "Onnafals12313231",
     *         // 0 = 提交成功 其余失败
     *         "status": "0"
     *     }
     * }
     *
     * @param jsonObject
     * @return
     */
    @Override
    public ChannelWithdrawResponse doWithdraw(JSONObject jsonObject) {
        ChannelWithdrawResponse response = new ChannelWithdrawResponse();
        String code = jsonObject.getString("code");
        if (!code.equals("200")) {
            throw new RenException("渠道错误:" + jsonObject.getString("msg"));
        }
        JSONObject data = jsonObject.getJSONObject("data");
        if (data.getString("status").equals("0"))
        response.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
        response.setChannelOrder(data.getString("orderNo"));
        response.setError(null);
        return response;
    }



    @Override
    public ChannelChargeQueryResponse doChargeQuery(JSONObject jsonObject) {
        throw new RenException("渠道不支持收款查询");
    }


    /**
     * {
     *   // 可选。如有则传
     *   "utr": "43333322222",
     *   // 商户代付单号
     *   "outTradeNo": ,
     *   // 平台代付单号
     *   "orderNo":
     *   // 代付金额
     *   "amount": "300.00"
     *   // 必填：  0 = 处理中，1 = 成功 其他失败
     *   "status": 1
     *   "sign": "加密"
     * }
     * @param jsonObject
     * @return
     */
    @Override
    public ChannelWithdrawResponse doWithdrawQuery(JSONObject jsonObject) {
        JSONObject data = jsonObject.getJSONObject("data");
        int status =  data.getIntValue("status");
        ChannelWithdrawResponse response = new ChannelWithdrawResponse();
        if (status == 1) {
            // 查询成功
            log.info("查询代付成功");
            response.setStatus(ZooConstant.WITHDRAW_STATUS_SUCCESS);
            response.setUtr(data.getString("utr"));
        }
        else if(status == 4) {
            response.setStatus(ZooConstant.WITHDRAW_STATUS_FAIL);
        }
        else {
            response.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
        }
        return response;
    }

    /**
     *
     * {
     *     "uuid": "85647968-aee6-4f9a-a79f-e0b3b9edc469",
     *     "code": "200",
     *     "msg": "success",
     *     "data": {
     *         // 商户总余额
     *         "balance": "2000",
     *         // 代付中余额 : 商户总余额-代付中余额=可用余额
     *         "forceBalance": "100",
     *         "availableBalance": "1900"
     *     }
     * }
     */
    @Override
    public ChannelBalanceResponse doBalance(JSONObject jsonObject) {
        JSONObject data = jsonObject.getJSONObject("data");
        ChannelBalanceResponse response = new ChannelBalanceResponse();
        BigDecimal balance = new BigDecimal(data.getString("balance"));
        BigDecimal real = new BigDecimal(data.getString("availableBalance"));
        response.setBalance(balance);
        response.setBalanceMemo(real.toString());
        return response;
    }


    @Override
    public ChannelChargeQueryResponse chargeNotified(String contentType, Object body, Long deptId, Long id, HttpServletRequest request, HttpServletResponse response, ZChargeEntity chargeEntity) throws IOException {
        throw new RenException("不支持");
    }


    /**
     * 返回标准的代付状态
     */
    @Override
    public ChannelWithdrawResponse drawNotified(String contentType, Object body, Long deptId, Long id, HttpServletRequest request, HttpServletResponse response, ZWithdrawEntity withdrawEntity) throws IOException {
        return this.withdrawQuery(withdrawEntity);
    }

    public String responseChargeOk() {
        return "success";
    }

    public String responseWithdrawOk() {
        return "success";
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
