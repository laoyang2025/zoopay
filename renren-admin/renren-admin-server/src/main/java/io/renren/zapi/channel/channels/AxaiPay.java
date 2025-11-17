package io.renren.zapi.channel.channels;

import cn.hutool.core.lang.Pair;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.renren.commons.tools.exception.RenException;
import io.renren.entity.SysDeptEntity;
import io.renren.zadmin.entity.ZChannelEntity;
import io.renren.zadmin.entity.ZChargeEntity;
import io.renren.zadmin.entity.ZWithdrawEntity;
import io.renren.zapi.ZooConstant;
import io.renren.zapi.channel.dto.ChannelChargeQueryResponse;
import io.renren.zapi.channel.dto.ChannelChargeResponse;
import io.renren.zapi.channel.dto.ChannelWithdrawResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.math.RoundingMode;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;

/**
 * admin          : https://staging.axaipay.my:8888/login
 * Merchant ID    : UnitedAT45
 * Password       : ?20hEzh_1DN
 * API Key        : PtD6Gm0ojp8FIpE
 * Signing Key    : tL4XCm7hemR4RigEHO46r3
 */

// 美盘: Beck & Lee
@Slf4j
public class AxaiPay extends AbstractChannel {

    @Override
    public Pair<String, String> getSign(TreeMap<String, Object> map, String api) {

        // signing key
        String signKey = channelEntity().getPrivateKey();

        // sign string
        String signstr = map.get("customerEmail").toString() +
                map.get("customerName") +
                map.get("customerPhone") +
                map.get("mchtId") +
                map.get("mchtTrxnId") +
                map.get("orderDescription") +
                map.get("txnAmount");

        log.info("signstr = {}", signstr);

        byte[] hmac = new HmacUtils("HmacSHA512", signKey).hmac(signstr);
        String sign = Base64.getEncoder().encodeToString(hmac);
        return Pair.of(signstr, sign);
    }


    @Override
    public String request(String url, TreeMap<String, Object> map, String api) {
        return null;
    }


    public void setChargeQueryMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        map.put("merchantId", channelEntity.getMerchantId());
        map.put("trxnId", entity.getId());
    }

    /**
     * 查询交易结果
     *
     * @param chargeEntity
     * @return
     */
    public ChannelChargeQueryResponse chargeQuery(ZChargeEntity chargeEntity) {
        ZChannelEntity channelEntity = channelEntity();
        TreeMap<String, Object> map = new TreeMap<>();
        setChargeMap(chargeEntity, map);
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-api-key", null);
        headers.add("x-signature", null);
        String resp = this.postForm(channelEntity.getChargeQueryUrl(), map, headers);
        return null;
    }

    public void setChargeMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        map.put("mchtId", channelEntity.getMerchantId());
        map.put("mchtTrxnId", entity.getId().toString());
        map.put("txnAmount", entity.getAmount().setScale(2, RoundingMode.HALF_UP));
        map.put("customerEmail", "NA@gmail.com");
        map.put("customerName", "NA");
        map.put("customerPhone", "60123456789");
        map.put("orderDescription", "Orange Juice");

        // optional
        map.put("redirectUrl", getCollectNotifyUrl(entity));
        map.put("backendUrl", getCollectNotifyUrl(entity));

        // 1.4.3 need fields
        map.put("paymentMethod", "OB");
        map.put("acquirerCode", "B2C_TEST");
    }

    @Override
    public ChannelChargeResponse charge(ZChargeEntity chargeEntity) {
        TreeMap<String, Object> map = new TreeMap<>();
        setChargeMap(chargeEntity, map);
        Pair<String, String> signInfo = getSign(map, "charge");
        map.put("signature", signInfo.getValue());
        map.put("signstr", signInfo.getKey());
        SysDeptEntity dept = getContext().getDept();
        String pageEncoded = null;
        try {
            pageEncoded = cn.hutool.core.codec.Base64.encode(objectMapper().writeValueAsString(map));
        } catch (JsonProcessingException e) {
            throw new RenException("can not gen request");
        }
        String payUrl = dept.getApiDomain() + "/sys/zchannel/jump/" + channelEntity().getId() + "?data=" + pageEncoded;
        ChannelChargeResponse response = new ChannelChargeResponse();
        response.setPayUrl(payUrl);
        return response;
    }

    public String jumpHandle142(Map<String, String> map) {
        ZChannelEntity channelEntity = channelEntity();
//                         <body onload='document.forms[0].submit()'>
        String pageHtml142 = String.format("""
                        <html>
                         <body>
                         <form method='POST' action='%s'>
                         <input name='customerEmail' value='%s'/>
                         <input name='customerName' value='%s'/>
                         <input name='customerPhone' value='%s'/>
                         <input name='mchtId' value='%s'/>
                         <input name='mchtTrxnId' value='%s'/>
                         <input name='orderDescription' value='%s'/>
                         <input name='signature' value='%s'/>
                         <input name='txnAmount' value='%s'/>
                         <input type='submit'/>
                         <div>signstr: %s</div>
                         </form>
                         </body>
                        </html>
                        """,
                channelEntity.getChargeUrl(),
                map.get("customerEmail"),
                map.get("customerName"),
                map.get("customerPhone"),
                map.get("mchtId"),
                map.get("mchtTrxnId"),
                map.get("orderDescription"),
                map.get("signature"),
                map.get("txnAmount"),
                map.get("signstr")
        );
        return pageHtml142;
    }

    public String jumpHandle143(Map<String, String> map) {
        ZChannelEntity channelEntity = channelEntity();
        String pageHtml143 = String.format("""
                        <html>
                         <body>
                         <body onload='document.forms[0].submit()'>
                         <form method='POST' action='%s'>
                         <input type='hidden' name='customerEmail' value='%s'/>
                         <input type='hidden' name='customerName' value='%s'/>
                         <input type='hidden' name='customerPhone' value='%s'/>
                         <input type='hidden' name='mchtId' value='%s'/>
                         <input type='hidden' name='mchtTrxnId' value='%s'/>
                         <input type='hidden' name='orderDescription' value='%s'/>
                         <input type='hidden' name='signature' value='%s'/>
                         <input type='hidden' name='txnAmount' value='%s'/>
                         <input type='hidden' name='paymentMethod' value='%s'/>
                         <input type='hidden' name='acquirerCode' value='%s'/>
                         <input type='button'/>
                         </form>
                         </body>
                        </html>
                        """,
                channelEntity.getChargeUrl(),
                map.get("customerEmail"),
                map.get("customerName"),
                map.get("customerPhone"),
                map.get("mchtId"),
                map.get("mchtTrxnId"),
                map.get("orderDescription"),
                map.get("signature"),
                map.get("txnAmount"),
                map.get("paymentMethod"),
                map.get("acquirerCode")
        );
        return pageHtml143;
    }

    @Override
    public String jumpHandle(Map<String, String> map) {
        return jumpHandle142(map);

//        if (map.get("txnAmount").equals("142.00")) {
//            return jumpHandle142(map);
//        } else {
//            return jumpHandle143(map);
//        }

    }

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
