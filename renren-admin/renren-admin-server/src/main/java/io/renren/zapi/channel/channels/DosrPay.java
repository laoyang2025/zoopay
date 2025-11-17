package io.renren.zapi.channel.channels;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Pair;
import com.alibaba.fastjson2.JSON;
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
import org.checkerframework.checker.units.qual.C;

import java.io.IOException;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.TreeMap;

public class DosrPay extends PostJsonChannel {

    /**
     * 应答里解析出标准应答
     * @param jsonObject
     * @return
     */
    @Override
    public ChannelChargeResponse doCharge(JSONObject jsonObject) {
        if (jsonObject.getIntValue("code") == 0) {
            ChannelChargeResponse response = new ChannelChargeResponse();
            JSONObject data = jsonObject.getJSONObject("data");
            response.setPayUrl(data.getString("pay_url"));
            response.setUpi(null);
            response.setRaw(null);
            response.setChannelOrder(data.getString("trade_no"));
            return response;
        } else {
            throw new RenException(jsonObject.getString("msg"));
        }
    }

    /**
     * 签名值的字段名称
     */
    @Override
    public String signField() {
        return "sign";
    }

    // 计算签名
    @Override
    public Pair<String, String> getSign(TreeMap<String, Object> map, String api) {
        return this.kvMd5Sign(map, null, "secret", false);
    }

    /**
     *  组目报文
     * @param entity
     * @param map
     */
    @Override
    public void setChargeMap(ZChargeEntity entity, TreeMap<String, Object> map) {

//        mer_id 是 string M1865180901 商户号;可在商户 后台【商户中心- 商户信息】页面 中获取。
//        order_no 是 string 22b168fb-4d15-463a-a89f-f1df7f1c0b97 商户自定义的订 单号；最大可接 收 36 位长度的 字符串
//        amount 是 long 10000 订单金额，该金 额单位分且不能 传小数点；举例 订单实际金额是 100 则该值为 10000
//        payment 是 string KJ_DS 支付通道编码;可在商户后台【商 户中心-我的通 道[通道编码字 段]】页面中获 取。
//        timestamp 是 long 2023-05-09 16:14:23 必须在当前时间 的前后 300 秒 内：格式:yyyy- MM-dd HH:mm:ss
//        callback_url 是 string 支付成功后异步回调地址, post 方式调用，发送数据 格式为 json
//        attach 是 string 支付方式的要求扩展参数，回调时会原样返回，有值 时需参与签名,使用 json_encode 一下，具体传入参 考 attach 说明章节
//        sign 是 string 914B5FD808CCD62BAC10680AFBEAE345 签名
        map.put("mer_id", null);
        map.put("order_no", entity.getId().toString());
        map.put("amount", entity.getAmount().multiply(new BigDecimal("100")).longValue());
        map.put("payment", this.channelEntity().getPayCode());
        map.put("timestamp", DateUtil.formatDateTime(new Date()));
        map.put("callback_url", getCollectNotifyUrl(entity));
        map.put("attach", "{}");
    }


    /**
     * 代付应答里 --》
     * @param jsonObject
     * @return
     */
    @Override
    public ChannelWithdrawResponse doWithdraw(JSONObject jsonObject) {
        ChannelWithdrawResponse response = new ChannelWithdrawResponse();
        response.setError(null);
        response.setUtr(null);
        response.setUpi(null);
        response.setChannelOrder(null);
        return response;
    }

    /**
     *
     */
    @Override
    public ChannelChargeQueryResponse doChargeQuery(JSONObject jsonObject) {
        ChannelChargeQueryResponse response = new ChannelChargeQueryResponse();
        // todo
        response.setChannelOrder(null);
        response.setUpi(null);
        response.setError(null);
        return response;
    }

    /**
     * 组收款查询报文
     */
    @Override
    public void setChargeQueryMap(ZChargeEntity entity, TreeMap<String, Object> map) {
    }

    /**
     *  代付查询结果 --> 标准代付应答
     */
    @Override
    public ChannelWithdrawResponse doWithdrawQuery(JSONObject jsonObject) {
        ChannelWithdrawResponse response = new ChannelWithdrawResponse();
        // todo
        response.setChannelOrder(null);
        response.setUpi(null);
        response.setUtr(null);
        response.setError(null);
        return response;
    }

    /**
     * 组代付查询请求报文
     */
    @Override
    public void setWithdrawQueryMap(ZWithdrawEntity entity, TreeMap<String, Object> map) {

    }

    /**
     *  从余额查询结果里返回标准的余额查询应答
     */
    @Override
    public ChannelBalanceResponse doBalance(JSONObject jsonObject) {
        return null;
    }

    /**
     *  余额查询组串
     */
    @Override
    public void setBalanceMap(TreeMap<String, Object> map) {
    }

    /**
     *  返回标准的支付状态
     */
    @Override
    public ChannelChargeQueryResponse chargeNotified(String contentType, Object body, Long deptId, Long id, HttpServletRequest request, HttpServletResponse response, ZChargeEntity chargeEntity) throws IOException {

        ChannelChargeQueryResponse resp = new ChannelChargeQueryResponse();
        resp.setStatus(ZooConstant.CHARGE_STATUS_PROCESSING);
        return resp;

//        JSONObject jsonObject = JSON.parseObject((String) body);
//        if (jsonObject.getIntValue("code") != 0) {
//            return ZooConstant.CHARGE_STATUS_PROCESSING;
//        }
//        JSONObject data = jsonObject.getJSONObject("data");
//        int pay_status = data.getIntValue("pay_status");
//        if (pay_status == 1) {
//            return ZooConstant.CHARGE_STATUS_SUCCESS;
//        }
//        return ZooConstant.CHARGE_STATUS_PROCESSING;

    }


    /**
     * 返回标准的代付状态
     */
    @Override
    public ChannelWithdrawResponse drawNotified(String contentType, Object body, Long deptId, Long id, HttpServletRequest request, HttpServletResponse response, ZWithdrawEntity withdrawEntity) throws IOException {
        ChannelWithdrawResponse resp = new ChannelWithdrawResponse();
        resp.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
        return resp;
    }
}
