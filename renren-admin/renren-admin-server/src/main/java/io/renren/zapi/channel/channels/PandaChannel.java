package io.renren.zapi.channel.channels;

import cn.hutool.core.date.DateUtil;
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

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public class PandaChannel extends PostFormChannel {


    @Override
    public String signField() {
        return "pay_md5sign";
    }

    @Override
    public Pair<String, String> getSign(TreeMap<String, Object> map, String api) {
        Pair<String, String> pair = kvMd5Sign(map, null, null, false);
        map.put("data_type", "json");
        map.put("pay_attach", "NA");
        return pair;
    }

    /**
     * pay_memberid 商户号 是 是 平台分配商户号
     * pay_orderid 订单号 是 是 上送订单号唯一, 字符长度 20
     * pay_applydate 提交时间 是 是 时间格式：2016-12-26 18:18:18
     * pay_bankcode 通道编码 是 是 请联系客服人员询问
     * pay_notifyurl 服务端通知 是 是 服务端返回地址.（POST 返回数据）
     * pay_callbackurl 页面跳转通知 是 是 页面跳转返回地址（POST 返回数据）
     * pay_amount 订单金额 是 是 商品金额
     * pay_md5sign MD5 签名 是 否 请看 MD5 签名字段格式
     * pay_attach 附加字段（透传参数） 否 否 此字段在返回时按原样返回 (中文需要 url 编码)
     * data_type 下单返回方式 否 否 json返回收银台地址，html或留空则为跳转收银台
     * pay_productname 商品名称 是 否 会员标识，银行卡的见下面注释。
     *
     * @param entity
     * @param map
     */
    @Override
    public void setChargeMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        map.put("pay_memberid", channelEntity.getMerchantId());
        map.put("pay_orderid", entity.getId().toString());
        map.put("pay_applydate", DateUtil.formatDateTime(new Date()));
        map.put("pay_bankcode", channelEntity.getPayCode());
        map.put("pay_notifyurl", this.getCollectNotifyUrl(entity));
        map.put("pay_callbackurl", this.getCollectNotifyUrl(entity));
        map.put("pay_amount", entity.getAmount());
    }

    @Override
    public ChannelChargeResponse doCharge(JSONObject jsonObject) {
        ChannelChargeResponse response = new ChannelChargeResponse();
        int code = jsonObject.getIntValue("code");
        if (code == 1) {
            response.setPayUrl(jsonObject.getJSONObject("data").getString("payurl"));
        } else {
            response.setError("channel failed:" + jsonObject.getString("msg"));
        }
        return response;
    }

    /**
     * memberid 商户编号 是
     * orderid 订单号 是
     * amount 订单金额 是
     * transaction_id 交易流水号 是
     * datetime 交易时间 是
     * returncode 交易状态 是 “00” 为成功
     * attach 扩展返回（透传参数） 否 商户附加数据返回
     * sign 签名 否 请看验证签名字段格式
     */
    @Override
    public ChannelChargeQueryResponse chargeNotified(String contentType, Object body, Long deptId, Long id, HttpServletRequest request, HttpServletResponse response, ZChargeEntity chargeEntity) throws IOException {
        TreeMap<String, Object> map = this.parseForm((String) body);

        // 验证签名
        String sign = (String)map.get("sign");
        map.remove("sign");
        map.remove("attach");
        TreeMap<String, Object> tmap = new TreeMap<>(map);
        Pair<String, String> pair = getSign(tmap, API_CHARGE_NOTIFY);
        if (!pair.getValue().equals(sign)) {
            getContext().error("invalid signature: signstr[{}] sign[{}]",pair.getKey(), pair.getValue());
            throw new RenException("invalid signature");
        }

        ChannelChargeQueryResponse resp = new ChannelChargeQueryResponse();
        if (map.get("returncode").equals("00")) {
            resp.setStatus(ZooConstant.CHARGE_STATUS_SUCCESS);
            return resp;
        }
        throw new RenException("invalid notification");
    }

    /**
     * pay_memberid 商户号 * 是 * 是 平台分配商户号
     * pay_orderid 订单号 * 是 * 是 上送订单号唯一, 字符长度 20
     * pay_md5sign 签名 * 否
     */
    @Override
    public void setChargeQueryMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        map.put("pay_memberid", channelEntity().getMerchantId());
        map.put("pay_orderid", entity.getId().toString());
    }

    /**
     * memberid 商户编号 是
     * orderid 订单号 是
     * amount 订单金额 是
     * time_end 支付成功时间 是
     * transaction_id 交易流水号 是
     * returncode 交易状态 是 “00” 为成功
     * trade_state 交易状态 是 NOTPAY-未支付 SUCCESS 已支付
     * sign 签名 否 请看验证签名字段格式
     *
     * @param jsonObject
     * @return
     */
    @Override
    public ChannelChargeQueryResponse doChargeQuery(JSONObject jsonObject) {
        ChannelChargeQueryResponse response = new ChannelChargeQueryResponse();
        if (jsonObject.getString("returncode").equals("00") && jsonObject.getString("trade_state").equals("SUCCESS")) {
            response.setStatus(ZooConstant.CHARGE_STATUS_SUCCESS);
            response.setChannelOrder(jsonObject.getString("transaction_id"));
        } else {
            response.setStatus(ZooConstant.CHARGE_STATUS_PROCESSING);
        }
        return response;
    }

    @Override
    public ChannelWithdrawResponse doWithdraw(JSONObject jsonObject) {
        return null;
    }


    @Override
    public ChannelWithdrawResponse doWithdrawQuery(JSONObject jsonObject) {
        return null;
    }

    @Override
    public void setWithdrawQueryMap(ZWithdrawEntity entity, TreeMap<String, Object> map) {

    }


    /**
     * mchid 商户号 是 是 平台分配商户号
     * t 时间戳 是 是 时间戳
     * pay_md5sign 签名 否 否
     * @param map
     */
    @Override
    public void setBalanceMap(TreeMap<String, Object> map) {
        map.put("mchid", channelEntity().getMerchantId());
        map.put("t", System.currentTimeMillis());
    }

    /**
     * status 状态 成功:success 失败：error
     * msg 状态描述
     * balance 商户余额 成功时返回
     * @param jsonObject
     * @return
     */
    @Override
    public ChannelBalanceResponse doBalance(JSONObject jsonObject) {
        ChannelBalanceResponse response = new ChannelBalanceResponse();
        if (jsonObject.getString("status").equals("success")) {
            String balance = jsonObject.getString("balance");
            String blocked = jsonObject.getString("blockedbalance");
            response.setBalanceMemo(balance + "/" + blocked);
            response.setBalance(new BigDecimal(balance));
            return response;
        }
        throw new RenException("call channel failed");
    }
}

