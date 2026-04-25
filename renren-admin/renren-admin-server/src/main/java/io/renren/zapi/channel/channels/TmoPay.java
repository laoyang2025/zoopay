package io.renren.zapi.channel.channels;


import cn.hutool.core.lang.Pair;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import io.renren.commons.tools.exception.RenException;
import io.renren.commons.tools.redis.RedisUtils;
import io.renren.zadmin.entity.ZChannelEntity;
import io.renren.zadmin.entity.ZChargeEntity;
import io.renren.zadmin.entity.ZWithdrawEntity;
import io.renren.zapi.ZooConstant;
import io.renren.zapi.channel.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


// beck机构
@Slf4j
public class TmoPay extends PostJsonChannel {

    /**
     * 对应 JavaScript 的 generateHash 函数
     *
     * @param mid           初始字符串
     * @param parameters    参数Map（会排除key为hash的项）
     * @param hashingMethod 哈希算法（默认sha512）
     * @param secretKey     密钥
     * @return 小写十六进制哈希字符串，为空则返回null
     */
    public static String generateHash(String mid, Map<String, Object> parameters, String hashingMethod, String secretKey) {
        // 1. 拼接初始数据
        StringBuilder hashData = new StringBuilder(mid);

        // 2. 遍历参数，排除 key = "hash" 的项，用 | 分隔拼接
        if (parameters != null && !parameters.isEmpty()) {
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                String key = entry.getKey();
                if (!"hash".equals(key)) {
                    hashData.append("|").append(entry.getValue().toString());
                }
            }
        }

        // 3. 拼接密钥
        hashData.append("|").append(secretKey);

        // 4. 空数据判断
        String data = hashData.toString();
        if (data.isEmpty()) {
            return null;
        }

        // 5. 计算哈希（兼容指定算法，默认 SHA-512）
        try {
            String algorithm = (hashingMethod == null || hashingMethod.isEmpty()) ? "SHA-512" : hashingMethod;
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hashBytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));

            // 6. 转小写十六进制
            return bytesToHex(hashBytes).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("不支持的哈希算法: " + hashingMethod, e);
        }
    }

    /**
     * 辅助方法：字节数组转十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            // 0xFF 确保无符号转换
            hex.append(String.format("%02x", b & 0xFF));
        }
        return hex.toString();
    }


    /**
     * {
     * "merchantID": "merchantID",
     * "secretkey": "secretkey",
     * "Content-Type": "application/json"
     * }
     *
     * @return
     */

    // 请求头设置
    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        List<HttpMessageConverter<?>> messageConverters = restTemplate().getMessageConverters();
        for (HttpMessageConverter<?> messageConverter : messageConverters) {
            if (messageConverter instanceof StringHttpMessageConverter) {
                ((StringHttpMessageConverter) messageConverter).setDefaultCharset(Charset.forName("UTF8"));
            }
        }
        ChannelContext context = getContext();
        ZChannelEntity channelEntity = channelEntity();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("merchantID", channelEntity.getMerchantId());
        headers.add("secretkey", channelEntity.getPrivateKey());
        return headers;
    }

    @Override
    public String request(String url, TreeMap<String, Object> map, String api) {
        HttpHeaders httpHeaders = getHttpHeaders();
        if (api.equals("balance")) {
            return this.getJSON(url, map, httpHeaders);
        }
        return this.postJSON(url, map, httpHeaders);
    }


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
        return null;
    }


    /**
     * {
     * "name": "Test Name",
     * "mobileNumber": "8899897654",
     * "email": "testmeail@yopmail.com",
     * "amount": 10,
     * "remarks": "Payin",
     * "hash": "Generated Hash Function"
     * }
     *
     * @param entity
     * @param map
     */
    @Override
    public void setChargeMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        log.info("get payCode: {}", entity.getPayCode());
        map.put("name", "Raj Kub");
        map.put("mobileNumber", "9207281258");
        map.put("email", "hgvkknb@gmail.com");
        map.put("amount", entity.getAmount().longValue());
        map.put("remarks", "Payin");
        map.put("hash", generateHash(channelEntity.getMerchantId(), map, null, channelEntity.getPrivateKey()));
    }

    /**
     * {
     * "paymentReferenceNo": "TMO217746382840338056",
     * "hash": "Generated Hash Function"
     * }
     *
     * @param entity
     * @param map
     */
    @Override
    public void setChargeQueryMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        if (entity.getChannelOrder() == null) {
            throw new RenException("order error");
        }
        map.put("paymentReferenceNo", entity.getChannelOrder());
        map.put("hash", generateHash(channelEntity.getMerchantId(), map, null, channelEntity.getPrivateKey()));
    }

    /**
     * 余额查询组串
     */
    @Override
    public void setBalanceMap(TreeMap<String, Object> map) {
    }

    /**
     *
     */
    public void setWithdrawMap(ZWithdrawEntity entity, TreeMap<String, Object> map) {
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
     * "success":true,
     * "message":"Payment created successfully",
     * "data":{
     * "amount":"100.00",
     * "charges":"0.00",
     * "total":"100.00",
     * "clientRefNo":"TANS525520260413",
     * "paymentReferenceNo": "TMO1417760622197949769",
     * "paymentLink":"https://payment.tmonion.com/upi/TMOP0015/TMO1417760622197949769/payment",
     * "initialStatus":"Initiated",
     * "updatedStatus":"Initiated",
     * "paymentMode":"UPI",
     * "remarks":"Payin",
     * "createdAt":"2026-04-13T06:36:59.799Z",
     * "beneficiaryName":"Raj Kub",
     * "beneficiaryPhoneNo":"9207281258",
     * "beneficiaryEmail":"hgvkknb@gmail.com"
     * }
     * }
     *
     * @param jsonObject
     * @return
     */
    @Override
    public ChannelChargeResponse doCharge(JSONObject jsonObject) {
        if (jsonObject.getBoolean("success")) {

            // 请求渠道成功
            JSONObject data = jsonObject.getJSONObject("data");
            ChannelChargeResponse response = new ChannelChargeResponse();
            String payUrl = data.getString("paymentLink");
            String sn = data.getString("paymentReferenceNo");

            // 开始扣吗
            HttpHeaders httpHeaders = new HttpHeaders();
            String qrcodeResp = null;
            boolean isDev = getContext().getConfig().isDev();
            if (isDev) {
                qrcodeResp = this.getJSON("http://13.235.8.108:8000/scan?url=" + payUrl, new HashMap<String, Object>(), httpHeaders);
            } else {
                qrcodeResp = this.getJSON("http://13.235.8.108:8000/scan?url=" + payUrl, new HashMap<String, Object>(), httpHeaders);
            }
            JSONObject jj = JSON.parseObject(qrcodeResp);
            if (!jj.getString("status").equals("success")) {
                throw new RenException("error");
            }
            // upi://pay?pa=65136080@fbl&pn=MAHINSHA%20T%20S&mc=5499&mode=22&orgid=000000&mid=606810090037772&mtid=65136080&tid=FBLPG4554902DJGHQ7O9U5PAAT65136080B&tr=FBLPG4554902DJGHQ7O9U5PAAT65136080B&am=300.0
            // -> pay?pa=xxxx2xxx&
            String qrcode = jj.getString("qr_content");
            qrcode = qrcode.substring(7);

            int beg = qrcode.indexOf("?") + 1;
            int end = qrcode.indexOf("&");
            String upi = qrcode.substring(beg, end);

            response.setChannelOrder(sn);
            response.setRaw(qrcode);
            response.setUpi(upi);

            String encodedUrl = URLEncoder.encode(qrcode, Charset.forName("UTF-8"));

            // 同步产品才用得到
            String finalPayUrl = null;
            if (isDev) {
                finalPayUrl = "http://127.0.0.1:7001/sys/landing/sync.html?upi=" + encodedUrl;
            } else {
                finalPayUrl = "https://novo.txzfpay.top/sys/landing/sync.html?upi=" + encodedUrl;
            }
            response.setPayUrl(finalPayUrl);

            return response;
        } else {
            throw new RenException(channelEntity().getChannelLabel() + "错误:" + jsonObject.getString("msg"));
        }
    }

    @Override
    public boolean isAsync()  {
        return true;
    }

    @Override
    public String doChargeAsync(ChannelChargeResponse channelChargeResponse, Long id) {
        return channelChargeResponse.getRaw();
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
 * {
 * "success": true,
 * "message": "Status is already up to date.",
 * "data": [
 * {
 * "paymentReferenceNo": "TMO217746382840338056",
 * "utrId": "810950254",
 * "initialStatus": "Initiated",
 * "transactionId": "810950254",
 * "updatedStatus": "Success",
 * "reason": null,
 * "remarks": "Verification SUCCESS Transaction"
 * }
 * ]
 * }
 *
 * @param jsonObject
 * @return
 */
@Override
public ChannelChargeQueryResponse doChargeQuery(JSONObject jsonObject) {
    ChannelChargeQueryResponse response = new ChannelChargeQueryResponse();
    boolean code = jsonObject.getBoolean("success");
    if (code) {
        JSONObject data = jsonObject.getJSONArray("data").getObject(0, JSONObject.class);

        String status = data.getString("updatedStatus");
        if (status.equals("Success")) {
            response.setUtr(data.getString("utrId"));
            response.setStatus(ZooConstant.CHARGE_STATUS_SUCCESS);
        } else {
            response.setStatus(ZooConstant.CHARGE_STATUS_PROCESSING);
        }
        return response;
    } else {
        response.setStatus(ZooConstant.CHARGE_STATUS_PROCESSING);
        response.setError(jsonObject.getString("message"));
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
 * {
 * "success":true,
 * "message":"Balance Listed Successfully",
 * "data":{
 * "currentBalance":"200.00",
 * "totalBalance":"200.00",
 * "utilizedBalance":"0.00",
 * "freezBalance":"0.00",
 * "holdBalance":"0.00",
 * "lienBalance":"0.00",
 * "pendingBalance":"0.00",
 * "status":"Active"
 * }}
 */
@Override
public ChannelBalanceResponse doBalance(JSONObject jsonObject) {
    boolean success = jsonObject.getBooleanValue("success");
    if (success) {
        JSONObject data = jsonObject.getJSONObject("data");
        ChannelBalanceResponse response = new ChannelBalanceResponse();
        BigDecimal bal = new BigDecimal(data.getString("totalBalance"));
        response.setBalance(bal);
        String currbal = data.getString("currentBalance");
        response.setBalanceMemo("total:" + bal + ", current:" + currbal.toString());
        log.info("balance:{}, current:{}", bal, currbal);
        return response;
    }
    throw new RenException("查询余额失败");
}

@Override
public ChannelChargeQueryResponse chargeNotified(String contentType, Object body, Long deptId, Long id, HttpServletRequest request, HttpServletResponse response, ZChargeEntity chargeEntity) throws IOException {
    ChannelChargeQueryResponse resp = new ChannelChargeQueryResponse();
    TreeMap<String, Object> map = checkSignByJson((String) body, API_CHARGE_NOTIFY);
    if (map.get("state").equals(1)) {
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

