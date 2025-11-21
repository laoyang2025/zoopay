package io.renren.zapi.channel.channels;


import cn.hutool.core.lang.Pair;
import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.util.concurrent.RateLimiter;
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
public class GchPay extends PostFormChannel {

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

//        if ("charge".equals(api)) {
//            return null; // 发送的时候不需要签名
//        }

        ZChannelEntity channelEntity = channelEntity();
        String signStr = this.md5SignString(map, false) + "&key=" + channelEntity.getPrivateKey();
        String sign = DigestUtil.md5Hex(signStr).toUpperCase();

        log.info("signStr = {}", signStr);
        log.info("sign = {}", sign);
        return Pair.of(signStr, sign);
    }

    /**
     * {
     *     "mchNo": "M17066050245",
     *     "mchOrderNo": "mho1624005107281",
     *     "productId": "1000",
     *     "amount": 8000,
     *     "clientIp": "210.73.10.148",
     *     "notifyUrl": "http://192.168.0.29:8080/test/v3",
     *     "reqTime": 1708531905805,
     *     "sign": "C0360322DAF458EC27B515B51ACCFF311"
     * }
     */
    @Override
    public void setChargeMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        log.info("get payCode: {}", entity.getPayCode());
        map.put("mchNo", channelEntity.getMerchantId());
        map.put("mchOrderNo", entity.getId().toString());
        map.put("productId", "1000");
        map.put("amount", entity.getAmount().multiply(new BigDecimal("100")).longValue());
        map.put("clientIp", "210.73.10.148");
        map.put("notifyUrl", getCollectNotifyUrl(entity));
        map.put("reqTime", System.currentTimeMillis());
    }

    /**
     * {
     *     "payOrderId": "P202106181104177050002",
     *     "amount": 1000,
     *     "reqTime": "1622016572190",
     *     "mchNo": "M1623984572",
     *     "sign": "46940C58B2F3AE426B77A297ABF4D31E"
     * }
     * @param entity
     * @param map
     */
    @Override
    public void setChargeQueryMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();

        map.put("mchNo", channelEntity.getMerchantId());
        map.put("appId", channelEntity.getMerchantId() + "-1");
        map.put("mchOrderNo", entity.getId().toString());
        map.put("reqTime", System.currentTimeMillis());
        map.put("version", "1.0");
        map.put("apiInfo", channelEntity.getPlatformKey());
        map.put("signType", "MD5");
    }

    /**
     * {
     *     "mchNo": "M1623984572",
     *     "reqTime": "1705221893125",
     *     "sign": "D41FE9BFBBCA6CABB4A6DAEA5EBFDA14"
     * }
     */
    @Override
    public void setBalanceMap(TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        map.put("mchNo", channelEntity.getMerchantId());
        map.put("appId", channelEntity.getPlatformKey());
    }


    private static RateLimiter rateLimiterSecond = RateLimiter.create(1);
    private static RateLimiter rateLimiterMinute = RateLimiter.create(20);


    /**
     *
     */
    public void setWithdrawMap(ZWithdrawEntity entity, TreeMap<String, Object> map) {

        if (!rateLimiterSecond.tryAcquire()) {
            throw new RenException("被限流");
        }

        if (!rateLimiterMinute.tryAcquire()) {
            throw new RenException("被限流");
        }

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
     * {
     * "code":0,
     * "msg":"SUCCESS"
     * "data":{
     * "qrUrl":"https://qr.alipay.com/bax007491btwwzcqsvn900a2",
     * "wayCode":"ALI_QR",
     * "originalResponse":{
     * "mchOrderNo":"1990071740812468226",
     * "orderState":1,
     * "payData":"https://api.yunhuitxpay.com/api/scan/imgs/282e498fbb5641bb76d842302db5ff8fcaee87692a5c8e6bbffd2bd266094572444e2aa9bc02cd8c9672630029eb7f5c.png",
     * "payDataType":
     * "codeImgUrl",
     * "payOrderId": *          "P1990071739917508609"
     * }
     * },
     * }
     */
    @Override
    public ChannelChargeResponse doCharge(JSONObject jsonObject) {
        log.info("get response: {}", jsonObject);
        if (jsonObject.getIntValue("orderState") == 1) {
            JSONObject data = jsonObject.getJSONObject("data");
            ChannelChargeResponse response = new ChannelChargeResponse();
            String payUrl = data.getString("payData");
            if (StringUtils.isNotEmpty(payUrl)) {
                response.setChannelOrder(data.getString("payOrderId"));
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



/*
025-11-17T20:04:32.094+08:00  INFO 39128 --- [renren-admin-server] [nio-8083-exec-9] i.r.zapi.channel.ChannelCallbackService  : [1828626242533257218] - [1987518260730028033] channel collect notified: id = 1990390015773581313, contentType = application/x-www-form-urlencoded;charset=UTF-8, body = [ifCode=alipay&amount=100&payOrderId=P1990390011061837825&mchOrderNo=1990390015773581313&subject=H5%E6%94%AF%E4%BB%98%5BM176113407303111%E5%95%86%E6%88%B7%E4%B8%8B%E5%8D%95%5D&wayCode=ALI_QR&sign=DB411E6F2DBB7743B37F53EA42A22924&channelOrderNo=2025111722001404721453945612&reqTime=1763381027939&body=H5%E6%94%AF%E4%BB%98%5BM176113407303111%E5%95%86%E6%88%B7%E4%B8%8B%E5%8D%95%5D&createdAt=1763380932959&appId=M176113407303111-1&clientIp=180.173.123.14&successTime=1763381028000&currency=CNY&state=2&mchNo=M176113407303111]
2025-11-17T20:04:32.101+08:00 ERROR 39128 --- [renren-admin-server] [nio-8083-exec-9] i.r.zapi.channel.ChannelCallbackService  : [1828626242533257218] - [1987518260730028033] channel collect notified process error:
io.renren.commons.tools.exception.RenException: 120.26.146.75 is not in white list[120.55.72.158]
 */