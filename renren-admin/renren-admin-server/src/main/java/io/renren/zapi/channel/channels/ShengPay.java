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
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.TreeMap;


// 老杨的机构:  Yang
@Slf4j
public class ShengPay extends PostFormChannel {

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
        ZChannelEntity channelEntity = channelEntity();
        String signStr = this.md5SignString(map, false) + channelEntity.getPrivateKey();

        String sign = DigestUtil.md5Hex(signStr);
        return Pair.of(signStr, sign);
    }

    /**
     * 商户ID	pid	是	Int	1001
     * 支付方式	type	是	String	alipay	支付方式列表
     * 商户订单号	out_trade_no	是	String	20160806151343349
     * 异步通知地址	notify_url	是	String	http://www.pay.com/notify_url.php	服务器异步通知地址
     * 跳转通知地址	return_url	否	String	http://www.pay.com/return_url.php	页面跳转通知地址
     * 商品名称	name	是	String	VIP会员	如超过127个字节会自动截取
     * 商品金额	money	是	String	1.00	单位：元，最大2位小数
     * 用户IP地址	clientip	是	String	192.168.1.100	用户发起支付的IP地址
     * 设备类型	device	否	String	pc	根据用户浏览器的UA判断，
     * 传入用户所使用的浏览器
     * 或设备类型，默认为pc
     * 设备类型列表
     * 业务扩展参数	param	否	String	没有请留空	支付后原样返回
     * 签名字符串	sign	是	String	202cb962ac59075b964b07152d234b70	签名算法点此查看
     * 签名类型	sign_type	是	String	MD5	默认为MD5
     *
     */
    @Override
    public void setChargeMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        entity.getAmount().setScale(2);
        DecimalFormat df = new DecimalFormat("0.00");
        String yuan = df.format(entity.getAmount());

        map.put("pid", channelEntity.getMerchantId());
        map.put("type", "alipay");
        map.put("out_trade_no", entity.getId().toString());
        map.put("notify_url", getCollectNotifyUrl(entity));
        map.put("name", "");
        map.put("money", yuan);
        map.put("clientip", "192.168.1.100");
        map.put("sign_type", "MD5");
    }


    @Override
    public ChannelChargeQueryResponse chargeQuery(ZChargeEntity chargeEntity) {
        ZChannelEntity channelEntity = channelEntity();
        TreeMap<String, Object> map = new TreeMap<>();
        map.put("act", "order");
        map.put("pid", channelEntity.getMerchantId());
        map.put("key", channelEntity.getPrivateKey());
        map.put("out_trade_no", chargeEntity.getId().toString());
        String resp = this.getForm("https://365.xingyaozf.top/api.php", map);
        log.info("查询订单结果: {}", resp);
        throw new RenException("todo");
    }

    @Override
    public void setChargeQueryMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
    }

    @Override
    public void setBalanceMap(TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        map.put("mchNo", channelEntity.getMerchantId());
        map.put("appId", channelEntity.getPlatformKey());
    }

    public void setWithdrawMap(ZWithdrawEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
    }


    @Override
    public void setWithdrawQueryMap(ZWithdrawEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
    }

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
