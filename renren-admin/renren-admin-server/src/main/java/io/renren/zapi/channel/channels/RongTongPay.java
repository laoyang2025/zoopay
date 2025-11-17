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

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class RongTongPay extends PostFormChannel {


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
     * @param map
     * @return
     */
    @Override
    public Pair<String, String> getSign(TreeMap<String, Object> map, String api) {
        Object sign_type = map.remove("sign_type");
        String signstr = md5SignString(map, false) + channelEntity().getPrivateKey();
        String sign = DigestUtil.md5Hex(signstr);
        map.put("sign_type", sign_type);
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
        if (jsonObject.getIntValue("code") == 1) {
            ChannelChargeResponse response = new ChannelChargeResponse();
            response.setPayUrl(jsonObject.getString("payurl"));
            response.setUpi(jsonObject.getString("qrcode"));
            response.setRaw(jsonObject.getString("urlscheme"));
            response.setChannelOrder(jsonObject.getString("trade_no"));
            return response;
        } else {
            throw new RenException(channelEntity().getChannelLabel() + "错误:" + jsonObject.getString("msg"));
        }
    }


    /**
     * 组目报文
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
     */
    @Override
    public void setChargeMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        map.put("pid", Integer.parseInt(channelEntity.getMerchantId()));
        map.put("type", channelEntity.getPayCode());
        map.put("out_trade_no", entity.getId().toString());
        map.put("notify_url", getCollectNotifyUrl(entity));
        map.put("name", entity.getMemo());
        map.put("money", entity.getAmount().toString());
        map.put("clientip", "192.168.0.123");
        map.put("sign_type", "MD5");
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
        int code = jsonObject.getIntValue("code");
        int status = jsonObject.getIntValue("status");
        if (code == 1 && status == 1) {
            response.setChannelOrder(jsonObject.getString("trade_no"));
            response.setStatus(ZooConstant.CHARGE_STATUS_SUCCESS);
        } else {
            response.setStatus(ZooConstant.CHARGE_STATUS_PROCESSING);
        }
        return response;
    }

    /**
     * 操作类型	act	是	String	order	此API固定值
     * 商户ID	pid	是	Int	1001
     * 商户密钥	key	是	String	89unJUB8HZ54Hj7x4nUj56HN4nUzUJ8i
     * 系统订单号	trade_no	选择	String	20160806151343312
     * 商户订单号	out_trade_no	选择	String	20160806151343349
     * 组收款查询报文
     */
    @Override
    public void setChargeQueryMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        map.put("act", "order");
        map.put("pid", Integer.parseInt(channelEntity().getMerchantId()));
        map.put("key", channelEntity().getPrivateKey());
        map.put("trade_no", entity.getChannelOrder());
        map.put("out_trade_no", entity.getId().toString());
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
        Map<String, String> map = (Map<String, String>) body;
        // 验证签名
        String sign = map.get(signField());
        map.remove(signField());
        TreeMap<String, Object> tmap = new TreeMap<>(map);
        Pair<String, String> pair = getSign(tmap, API_CHARGE_NOTIFY);
        if (!pair.getValue().equals(sign)) {
            throw new RenException("invalid signature");
        }

        ChannelChargeQueryResponse resp = new ChannelChargeQueryResponse();

        if (map.get("trade_status").equals("TRADE_SUCCESS")) {
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


    @Override
    public String request(String url, TreeMap<String, Object> map, String api) {
        if (api.equals(API_CHARGE_QUERY) || api.equals(API_WITHDRAW_QUERY) || api.equals(API_BALANCE)) {
            map.remove("sign");
            return this.getForm(url, map);
        }
        return this.postForm(url, map, null);
    }

}
