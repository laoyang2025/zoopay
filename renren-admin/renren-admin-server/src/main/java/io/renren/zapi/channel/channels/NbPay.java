package io.renren.zapi.channel.channels;


import cn.hutool.core.lang.Pair;
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
import java.util.TreeMap;


// 老杨的机构:  Yang
@Slf4j
public class NbPay extends PostFormChannel {

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
        String signStr = this.md5SignString(map, false);
        String sign = NbPayUtil.sign(signStr, channelEntity.getPrivateKey());
        log.info("signStr = {}", signStr);
        log.info("sign = {}", sign);
        return Pair.of(signStr, sign);
    }

    /**
     商户ID	pid	是	Int	1001
     接口类型	method	是	String	web	接口类型列表
     设备类型	device	否	String	pc	仅通用网页支付需要传 设备类型列表
     支付方式	type	是	String	alipay	支付方式列表
     商户订单号	out_trade_no	是	String	20160806151343349
     异步通知地址	notify_url	是	String	http://www.pay.com/notify_url.php	服务器异步通知地址
     跳转通知地址	return_url	是	String	http://www.pay.com/return_url.php	页面跳转通知地址
     商品名称	name	是	String	VIP会员	如超过127个字节会自动截取
     商品金额	money	是	String	1.00	单位：元，最大2位小数
     用户IP地址	clientip	是	String	192.168.1.100	用户发起支付的IP地址
     业务扩展参数	param	否	String	没有请留空	支付后原样返回
     分账接收方ID	huifu_id	否	String		用于指定分账接收方，非必传
     被扫支付授权码	auth_code	否	String		仅被扫支付需要传
     用户Openid	sub_openid	否	String		仅JSAPI支付需要传
     公众号AppId	sub_appid	否	String		仅JSAPI支付需要传
     自定义通道ID	channel_id	否	Int		对应进件商户列表的ID，未进件请勿传
     当前时间戳	timestamp	是	String	1721206072	10位整数，单位秒
     签名字符串	sign	是	String		参考签名规则
     签名类型	sign_type	是	String	RSA	默认为RSA
     */
    @Override
    public void setChargeMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        log.info("get payCode: {}", entity.getPayCode());
        map.put("pid", channelEntity.getMerchantId());
        map.put("method", "web");
        map.put("device", "mobile");
        map.put("type", "alipay");
        // map.put("type", entity.getPayCode());
        map.put("out_trade_no", entity.getId().toString());
        map.put("notify_url", this.getCollectNotifyUrl(entity));
        map.put("return_url", entity.getCallbackUrl());
        map.put("name", "VIP会员");
        map.put("money", entity.getAmount().setScale(2).toString());
        map.put("clientip", "192.168.1.100");
        map.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        map.put("sign_type", "RSA");
    }

    /**
     * mchId	商户号	是	string	M20248493854784	商户号
     * outTradeNo	商户订单号	是	string	P17303489458945894	返回商户传入的订单号
     * reqTime	请求时间	是	long	1622016572190	请求接口时间，13位时间戳
     * sign	签名	是	string	694da7a446ab4b1d9ceea7e5614694f4	签名值，详见 签名算法\
     */
    @Override
    public void setChargeQueryMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        map.put("mchId", channelEntity.getMerchantId());
        map.put("outTradeNo", entity.getId().toString());
        map.put("reqTime", String.valueOf(System.currentTimeMillis()));
    }

    /**
     * 余额查询组串
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
     * {"code":0,
     * "data":{
     * "mchId":"M202603031948179372",
     * "tradeNo":"P17727120655767994452",
     * "outTradeNo":"2029527609989189634",
     * "originTradeNo":"P0305200105zHQPBW1weg",
     * "amount":"10000",
     * "payUrl":"https://pay.tddnn.top/pay/S2029527618990247936",
     * "expiredTime":"1772712363697","sign":"84984586ed4215948b49498ffb923cc2"
     * },"success":true}
     */
    @Override
    public ChannelChargeResponse doCharge(JSONObject jsonObject) {
        log.info("get response: {}", jsonObject);
        if (jsonObject.getIntValue("code") == 0) {
            JSONObject data = jsonObject.getJSONObject("data");
            ChannelChargeResponse response = new ChannelChargeResponse();
            String payUrl = data.getString("payUrl");
            String sn = data.getString("tradeNo");
            if (StringUtils.isNotEmpty(payUrl)) {
                response.setChannelOrder(sn);
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
     * {
     *     "msg": "操作成功",
     *     "code": 200,
     *     "data": {
     *         "sn": "P202505191839511",
     *         "outTradeNo": "1231231",
     *         "amount": 496.00,
     *         "createTime": "2025-05-19 18:39:57",
     *         "payTime": null,
     *         "status": "cancel"
     *     }
     * }
     */
    @Override
    public ChannelChargeQueryResponse doChargeQuery(JSONObject jsonObject) {
        ChannelChargeQueryResponse response = new ChannelChargeQueryResponse();
        int code = jsonObject.getIntValue("code");
        if (code == 0) {
            JSONObject data = jsonObject.getJSONObject("data");
            int status = data.getIntValue("state");
            if (status == 1) {
                response.setChannelOrder(data.getString("tradeNo"));
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
     * mchId	商户号	是	string	M20248548834598406	商户号
     * tradeNo	支付订单号	是	string	P1739045904834948954	返回支付系统订单号
     * outTradeNo	商户订单号	是	string	20210905000702675466	返回商户传入的订单号
     * originTradeNo	通道订单号	否	String	OWN18239328912314213	示例值：OWN18239328912314213
     * amount	订单金额 (单位: 分)	是	long	10000	订单金额 (单位: 分)，例如: 10000 即为 100.00 元
     * subject	商品标题	是	string	商品标题测试	商品标题
     * body	商品描述	否	string	商品描述测试	商品描述
     * extParam	扩展参数	否	string	123321	商户扩展参数，回调时会原样返回
     * state	订单状态	是	int	1	订单状态：1支付成功 2测试冲正 0待支付 7未出码 9支付失败
     * notifyTime	通知时间	是	long	1622016572190	通知时间，13位时间戳
     * sign	签名	是	string	694da7a446ab4b1d9ceea7e5614694f4	签名值，详见 签名算法
     */
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



/*
025-11-17T20:04:32.094+08:00  INFO 39128 --- [renren-admin-server] [nio-8083-exec-9] i.r.zapi.channel.ChannelCallbackService  : [1828626242533257218] - [1987518260730028033] channel collect notified: id = 1990390015773581313, contentType = application/x-www-form-urlencoded;charset=UTF-8, body = [ifCode=alipay&amount=100&payOrderId=P1990390011061837825&mchOrderNo=1990390015773581313&subject=H5%E6%94%AF%E4%BB%98%5BM176113407303111%E5%95%86%E6%88%B7%E4%B8%8B%E5%8D%95%5D&wayCode=ALI_QR&sign=DB411E6F2DBB7743B37F53EA42A22924&channelOrderNo=2025111722001404721453945612&reqTime=1763381027939&body=H5%E6%94%AF%E4%BB%98%5BM176113407303111%E5%95%86%E6%88%B7%E4%B8%8B%E5%8D%95%5D&createdAt=1763380932959&appId=M176113407303111-1&clientIp=180.173.123.14&successTime=1763381028000&currency=CNY&state=2&mchNo=M176113407303111]
2025-11-17T20:04:32.101+08:00 ERROR 39128 --- [renren-admin-server] [nio-8083-exec-9] i.r.zapi.channel.ChannelCallbackService  : [1828626242533257218] - [1987518260730028033] channel collect notified process error:
io.renren.commons.tools.exception.RenException: 120.26.146.75 is not in white list[120.55.72.158]
 */