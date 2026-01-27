package io.renren.zapi.channel.channels;


import cn.hutool.core.lang.Pair;
import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.fastjson2.JSONObject;
import io.renren.commons.tools.exception.RenException;
import io.renren.commons.tools.utils.DateUtils;
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
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
public class BochPay extends PostFormChannel {


    /**
     * 签名值的字段名称
     */
    @Override
    public String signField() {
        return "pay_md5sign";
    }

    /**
     * 计算签名
     *
     * @param map
     * @return
     */
    @Override
    public Pair<String, String> getSign(TreeMap<String, Object> map, String api) {

        Object payIp = map.remove("pay_ip");
        Object payProductname = map.remove("pay_productname");
        String signstr = md5SignString(map, false) + "&key=" + channelEntity().getPrivateKey();
        String sign = DigestUtil.md5Hex(signstr).toUpperCase();
        map.put("pay_ip", payIp);
        map.put("pay_productname", payProductname);
        return Pair.of(signstr, sign);
    }

    /**
     * 应答里解析出标准应答
     * 返回状态码	code	Int	1	1为成功，其它值为失败
     * 返回信息	msg	String		失败时返回原因
     * 订单号	trade_no	String	20160806151343349	支付订单号
     * 支付跳转url	payurl	String	https://pay.rttxzf.xyz/pay/wxpay/202010903/	如果返回该字段，则直接跳转到该url支付
     * 二维码链接	qrcode	String	weixin://wxpay/bizpayurl?pr=04IPMKM	如果返回该字段，则根据该url生成二维码
     * 小程序跳转url	urlscheme	String	weixin://dl/business/?ticket=xxx	如果返回该字段，则使用js跳转该url，可发起微信小程序支付
     *
     * @param jsonObject
     * @return
     */
    @Override
    public ChannelChargeResponse doCharge(JSONObject jsonObject) {
        log.info("doCharge: {}", jsonObject);
        if (jsonObject.getString("status").equals("1")) {
            ChannelChargeResponse response = new ChannelChargeResponse();
            response.setPayUrl(jsonObject.getString("h5_url"));
            response.setChannelOrder(jsonObject.getString("mch_order_id"));
            return response;
        } else {
            throw new RenException(channelEntity().getChannelLabel() + "错误:" + jsonObject.getString("msg"));
        }
    }


    /**
     * pay_memberid	商户号	✅	✅	平台分配商户号
     * pay_orderid	订单号	✅	✅	上送订单号唯一，字符长度20
     * pay_applydate	提交时间	✅	✅	时间格式：2016-12-26 18:18:18
     * pay_bankcode	银行编码	✅	✅	参考后续说明
     * pay_notifyurl	服务端通知	✅	✅	服务端返回地址（POST 返回数据）
     * pay_callbackurl	页面跳转通知	✅	✅	页面跳转返回地址（POST 返回数据）
     * pay_amount	订单金额	✅	✅	商品金额，单位元，支持2位小数，例123.45
     * pay_productname	商品名称	✅	❌	-
     * pay_ip	付款人 IP	✅	❌	付款玩家的真实IP
     * pay_md5sign	MD5 签名	✅	❌	请看 MD5 签名字段格式
     * pay_attach	附加字段	❌	❌	此字段在返回时按原样返回（中文需要url编码）
     * pay_productnum	商品数量	❌	❌	-
     * pay_productdesc	商品描述	❌	❌	-
     * pay_producturl	商品链接地址	❌	❌	-
     * pay_userid	会员 ID	❌	❌	付款用户的唯一标识，对于原生不抗诉通道，则必须要填写
     * pay_username	付款人姓名	❌	❌	若为转卡通道或银联通道，则此参数必须正确填写，否则无法拉起或到账。其他通道可留空

     */
    @Override
    public void setChargeMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        map.put("pay_memberid", channelEntity.getMerchantId());
        map.put("pay_orderid", entity.getId().toString());
        map.put("pay_applydate", DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        map.put("pay_bankcode", channelEntity.getPayCode()); // todo
        map.put("pay_notifyurl", this.getCollectNotifyUrl(entity));
        map.put("pay_callbackurl", entity.getCallbackUrl());
        map.put("pay_amount", entity.getAmount().toString());
        map.put("pay_productname", "service");
        map.put("pay_ip", "192.168.1.1");
    }


    /**
     * 代付应答里 --》
     *
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
        String tradeState = jsonObject.getString("trade_state");
        if (tradeState.equals("SUCCESS")) {
            response.setStatus(ZooConstant.CHARGE_STATUS_SUCCESS);
        } else {
            response.setStatus(ZooConstant.CHARGE_STATUS_PROCESSING);
        }
        return response;
    }

    /**
     */
    @Override
    public void setChargeQueryMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        map.put("pay_memberid", channelEntity.getMerchantId());
        map.put("pay_orderid", entity.getId().toString());
    }

    /**
     * 代付查询结果 --> 标准代付应答
     */
    @Override
    public ChannelWithdrawResponse doWithdrawQuery(JSONObject jsonObject) {
        ChannelWithdrawResponse response = new ChannelWithdrawResponse();
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
     * 从余额查询结果里返回标准的余额查询应答
     */
    @Override
    public ChannelBalanceResponse doBalance(JSONObject jsonObject) {
        ChannelBalanceResponse response = new ChannelBalanceResponse();
        response.setBalanceMemo(jsonObject.getString("money"));
        return response;
    }

    /**
     * 余额查询组串
     */
    @Override
    public void setBalanceMap(TreeMap<String, Object> map) {
        map.put("act", "query");
        map.put("pid", Integer.parseInt(channelEntity().getMerchantId()));
        map.put("key", channelEntity().getPrivateKey());
    }

    /**
     * 返回标准的支付状态
     * 商户ID	pid	是	Int	1001
     * 易支付订单号	trade_no	是	String	20160806151343349021	融通支付订单号
     * 商户订单号	out_trade_no	是	String	20160806151343349	商户系统内部的订单号
     * 支付方式	type	是	String	alipay	支付方式列表
     * 商品名称	name	是	String	VIP会员
     * 商品金额	money	是	String	1.00
     * 支付状态	trade_status	是	String	TRADE_SUCCESS	只有TRADE_SUCCESS是成功
     * 业务扩展参数	param	否	String
     * 签名字符串	sign	是	String	202cb962ac59075b964b07152d234b70	签名算法点此查看
     * 签名类型	sign_type	是	String	MD5	默认为MD5
     */
    @Override
    public ChannelChargeQueryResponse chargeNotified(String contentType, Object body, Long deptId, Long id, HttpServletRequest request, HttpServletResponse response, ZChargeEntity chargeEntity) throws IOException {
        log.info("body = {}", body);

        TreeMap<String, Object> map = this.getTreeMapByForm((String)body);

//        // 验证签名
//        String sign = map.get(signField());
//        map.remove(signField());
//        TreeMap<String, Object> tmap = new TreeMap<>(map);
//        Pair<String, String> pair = getSign(tmap, API_CHARGE_NOTIFY);
//        if (!pair.getValue().equals(sign)) {
//            throw new RenException("invalid signature");
//        }

        ChannelChargeQueryResponse resp = new ChannelChargeQueryResponse();

        if (map.get("returncode").equals("00")) {
            resp.setStatus(ZooConstant.CHARGE_STATUS_SUCCESS);
            return resp;
        }
        throw new RenException("invalid notification");
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
