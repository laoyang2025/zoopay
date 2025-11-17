package io.renren.zapi.channel.channels;


import cn.hutool.core.lang.Pair;
import cn.hutool.crypto.SecureUtil;
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

//
@Slf4j
public class HaoxPay extends PostJsonChannel {

    @Override
    public String signField() {
        return "sign";
    }

    @Override
    public Pair<String, String> getSign(TreeMap<String, Object> map, String api) {
        String signstr;
        if (map.get("orderId") != null) {
            signstr = "" + map.get("orderId") + map.get("timestamp") + channelEntity().getPrivateKey();
        } else {
            signstr = "" + map.get("timestamp") + channelEntity().getPrivateKey();
        }
        String sign = SecureUtil.md5(signstr).toLowerCase();
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

    /**
     * merchantId	Integer	是	8	商户号，开户后提供，	101
     * timestamp	Long	是	32	时间戳，精确到毫秒	1647760272251
     * sign
     */
    @Override
    public void setBalanceMap(TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        map.put("merchantId", channelEntity.getMerchantId());
        map.put("timestamp", System.currentTimeMillis());
    }

    /**
     * merchantId	int	是	8	商户号，开户后提供，例：100名	V10
     * orderId	String	是	32	商户订单号，50个字符以内	test123
     * timestamp	long	是	32	时间戳，精确到毫秒	1702387023356
     * sign	String	是	64	签名，md5(orderId+timestamp+密钥)进行MD5，32位小写加密,参见加签方式
     */
    @Override
    public void setWithdrawQueryMap(ZWithdrawEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        map.put("orderId", entity.getId().toString());
        map.put("timestamp", System.currentTimeMillis());
        map.put("merchantId", channelEntity.getMerchantId());
    }

    /**
     * amount	int	是	6	订单金额，单位分	10000
     * merchantId	int	是	32	商户号，开户后提供	100000
     * orderId	String	是	16	商户订单号，50个字符以内	test12345678
     * timestamp	long	是	13	时间戳，精确到毫秒	1647760272251
     * notifyUrl	String	是	255	回调通知地址，	http://localhost/saispay
     * outType	String	是	10	代付类型，IMPS、UPI，具体支持代付类型请联系客服	UPI
     * accountHolder	String	是	50	受益人姓名 必填，只能包含英文字母或空格，长度3-50	8888
     * accountNumber	String	是	64	受益人账户 ,长度5-35位, 代付类型是UPI时,请填收款UPI地址。	0000000000 或 2193247259
     * ifsc	String	否	20	受益人账户 IFSC，IMPS代付时必填。	2341234567890
     */
    @Override
    public void setWithdrawMap(ZWithdrawEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        long timestamp = System.currentTimeMillis();
        map.put("amount", entity.getAmount().multiply(new BigDecimal(100)).intValue());
        map.put("merchantId", channelEntity.getMerchantId());
        map.put("orderId", entity.getId().toString());
        map.put("timestamp", timestamp);
        map.put("notifyUrl", this.getWithdrawNotifyUrl(entity));
        map.put("outType", "IMPS");

        map.put("accountHolder", entity.getAccountUser());
        map.put("accountNumber", entity.getAccountNo());
        map.put("ifsc", entity.getAccountIfsc());
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
     * {"msg":"Success","payOrderId":"1747926792654z1lsee","amount":10000,"statusDesc":"Initiated","code":200,"status":0}
     */
    @Override
    public ChannelWithdrawResponse doWithdraw(JSONObject jsonObject) {
        ChannelWithdrawResponse response = new ChannelWithdrawResponse();
        int code = jsonObject.getIntValue("code");
        if ( code != 200) {
            throw new RenException("渠道错误:" + jsonObject.getString("msg"));
        }
        int status  = jsonObject.getIntValue("status");
        response.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
        response.setChannelOrder(jsonObject.getString("payOrderId"));
        if ( status == 0) {
            response.setError(null);
        } else {
            response.setError(jsonObject.getString("msg"));
        }
        return response;
    }


    @Override
    public ChannelChargeQueryResponse doChargeQuery(JSONObject jsonObject) {
        throw new RenException("渠道不支持收款查询");
    }


    /**
     * 0:初始，1:代付成功，2:代付失败。
     * @param jsonObject
     * @return
     */
    @Override
    public ChannelWithdrawResponse doWithdrawQuery(JSONObject jsonObject) {
        int code = jsonObject.getIntValue("code");
        if ( code != 200) {
            throw new RenException("查询失败");
        }
        int status = jsonObject.getIntValue("status");
        ChannelWithdrawResponse response = new ChannelWithdrawResponse();
        if (status == 1) {
            // 查询成功
            log.info("查询代付成功");
            response.setStatus(ZooConstant.WITHDRAW_STATUS_SUCCESS);
            response.setUtr(jsonObject.getString("utr"));
        } else if (status == 2) {
            response.setStatus(ZooConstant.WITHDRAW_STATUS_FAIL);
        } else {
            response.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
        }
        return response;
    }

    /**
     * {
     * "code": 200,
     * "message": "success",
     * "balance": 8.96,
     * }
     */
    @Override
    public ChannelBalanceResponse doBalance(JSONObject jsonObject) {
        ChannelBalanceResponse response = new ChannelBalanceResponse();
        BigDecimal balance = new BigDecimal(jsonObject.getIntValue("balance")).divide(new BigDecimal("100"));
        BigDecimal real = balance;
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
