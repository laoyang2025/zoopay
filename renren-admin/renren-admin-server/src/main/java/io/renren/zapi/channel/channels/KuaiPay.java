package io.renren.zapi.channel.channels;


import cn.hutool.core.lang.Pair;
import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.util.concurrent.RateLimiter;
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
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.TreeMap;


// 老杨的机构:  Yang
@Slf4j
public class KuaiPay extends PostFormChannel {

    @Override
    public String request(String url, TreeMap<String, Object> map, String api) {
        return this.getForm(url, map);
    }

    /**
     * 签名值的字段名称
     */
    @Override
    public String signField() {
        return "signMsg";
    }

    /**
     * 计算签名

     * inputCharset={inputCharset}&
     * pageUrl={pageUrl}&
     * bgUrl={bgUrl}&
     * version={version}&
     * language={language}&
     * signType={signType}&
     * merchantAcctId={merchantAcctId}&
     * payerName={payerName}&
     * payerContactType={payerContactType}&
     * payerContact={payerContact}&
     * payerIdType={payerIdType}&payerId={payerId}&
     * payerIP={payerIP}&orderId={orderId}&
     * orderAmount={orderAmount}&
     * orderTime={orderTime}&
     * orderTimestamp={orderTimestamp}&
     * productName={productName}&
     * productNum={productNum}&
     * productId={productId}&
     * productDesc={productDesc}&
     * ext1={ext1}&
     * ext2={ext2}&
     * payType={payType}&
     * bankId={bankId}&
     * cardIssuer={cardIssuer}&
     * cardNum={cardNum}&
     * remitType={remitType}&
     * remitCode={remitCode}&
     * redoFlag={redoFlag}&
     * pid={pid}&
     * submitType={submitType}&
     * orderTimeOut={orderTimeOut}&
     * mobileGateway={mobileGateway}&
     * aggregatePay={aggregatePay}&
     * extDataType={extDataType}&
     * extDataContent={extDataContent}&
     * period={period}

     *
     * @return
     */
    @Override
    public Pair<String, String> getSign(TreeMap<String, Object> map, String api) {
        StringBuilder sb = new StringBuilder();
        sb .append("inputCharset=").append(map.get("inputCharset")).append("&")
                .append("pageUrl=").append(map.get("pageUrl")).append("&")
                .append("bgUrl=").append(map.get("bgUrl")).append("&")
                .append("version=").append(map.get("version")).append("&")
                .append("language=").append(map.get("language")).append("&")
                .append("signType=").append(map.get("signType")).append("&")
                .append("merchantAcctId=").append(map.get("merchantAcctId")).append("&")
                .append("payerName=").append(map.get("payerName")).append("&")
                .append("payerContactType=").append(map.get("payerContactType")).append("&")
                .append("payerContact=").append(map.get("payerContact")).append("&")
                .append("payerIdType=").append(map.get("payerIdType")).append("&")
                .append("payerId=").append(map.get("payerId")).append("&")
                .append("payerIP=").append(map.get("payerIP")).append("&")
                .append("orderId=").append(map.get("orderId")).append("&")
                .append("orderAmount=").append(map.get("orderAmount")).append("&")
                .append("orderTime=").append(map.get("orderTime")).append("&")
                .append("orderTimestamp=").append(map.get("orderTimestamp")).append("&")
                .append("productName=").append(map.get("productName")).append("&")
                .append("productNum=").append(map.get("productNum")).append("&")
                .append("productId=").append(map.get("productId")).append("&")
                .append("productDesc=").append(map.get("productDesc")).append("&")
                .append("ext1=").append("")
                .append("ext2=").append("")
                .append("payType=").append(map.get("payType")).append("&")
                .append("bankId=").append(map.get("bankId")).append("&")
                .append("cardIssuer=").append(map.get("cardIssuer")).append("&")
                .append("cardNum=").append(map.get("cardNum")).append("&")
                .append("remitType=").append(map.get("remitType")).append("&")
                .append("remitCode=").append(map.get("remitCode")).append("&")
                .append("redoFlag=").append(map.get("redoFlag")).append("&")
                .append("pid=").append(map.get("pid")).append("&")
                .append("submitType=").append(map.get("submitType")).append("&")
                .append("orderTimeOut=").append(map.get("orderTimeOut")).append("&")
                .append("mobileGateway=").append(map.get("mobileGateway")).append("&")
                .append("aggregatePay=").append(map.get("aggregatePay")).append("&")
                .append("extDataType=").append(map.get("extDataType")).append("&")
                .append("extDataContent=").append(map.get("extDataContent")).append("&")
                .append("period=").append(map.get("period")).append("&");

        String signStr = sb.toString();
        String sign = null;

        log.info("signStr = {}", signStr);
        log.info("sign = {}", sign);
        return Pair.of(signStr, sign);
    }

    void t(TreeMap<String, Object> map, ZChargeEntity entity) {


    }

    /**
     * 字符集 	inputCharset 	2 	固定选择值：1、2、3 1代表UTF-8; 2代表GBK; 3代表GB2312 	必填 	1
     * 接受支付结果的页面地址 	pageUrl 	256 	需要是绝对地址，与bgUrl不能同时为空，当bgUrl为空时，快钱直接将支付结果GET到pageUrl，当bgUrl不为空时，按照bgUrl的方式返回 	非必填 	https://
     * 服务器接受支付结果的后台地址 	bgUrl 	256 	需要是绝对地址，与pageUrl不能同时为空，快钱将支付结果发送到bgUrl对应的地址，并且获取商户按照约定格式输出的地址，显示页面给用户 	非必填 	https://
     * 网关版本 	version 	10 	固定值：mobile1.0，注意为小写字母，移动网关：mobile1.0 	必填 	mobile1.0
     * 移动网关版本 	mobileGateway 	10 	移动网关版本，当version= mobile1.0时有效，phone代表手机版移动网关，pad代表平板移动网关，默认为phone 	非必填 	phone
     * 网关页面显示语言种类 	language 	2 	固定值：1,1代表中文显示 	必填 	1
     * 签名类型 	signType 	2 	4代表RSA签名方式 	必填 	4
     * 人民币账号 	merchantAcctId 	30 	快钱分给商户的11位商户编号。传值为商户号后面加01 	必填 	1001213884201
     * 支付人姓名 	payerName 	32 	英文或中文字符 	非必填 	张三
     * 支付人联系方式类型 	payerContactType 	2 	固定值：1或者2，1代表电子邮件方式；2代表手机联系方式 	非必填 	1
     * 支付人联系方式 	payerContact 	2 	根据payerContactType的方式填写对应字符，邮箱或者手机号码 	非必填 	1@99bill.com
     * 指定付款人 	payerIdType 	2 	数字串 类型固定值0，1，2，3 0代表不指定 1代表通过商户方ID指定付款人 2代表通过快钱账户指定付款人 3 代表付款方在商户方的会员编号(当需要支持保存信息功能的快捷支付时，,需上送此项) 4代表企业网银的交通银行直连 如果为空代表不需要指定 	非必填 	0
     * 付款人标识 	payerId 	50 	当企业网银中的交通银行直连，此值不能为空。此参数需要传入交行企业网银的付款方银行账号，当需要支持保存信息功能的快捷支付时，此值不能为空，此参数需要传入付款方在商户方的会员编号 	非必填 	1111111
     * 付款人IP 	payerIP 	256 	付款人IP，商家传递获取到的客户端IP 	非必填 	1.1.1.1
     * 终端IP 	terminalIp 	256 	商家的终端ip，支持Ipv4和Ipv6，不参与加签 	必填 	127.0.0.1
     * 网络交易平台简称 	tdpformName 	10 	网络交易平台简称，英文或中文字符串，除微信支付宝支付外其他交易方式必传，不参与加签 	必填 	ceshi
     * 商户订单号 	orderId 	30 	只允许使用字母、数字、- 、_,并以字母或数字开头，每个商户提交的订单号，必须在自身账户交易中唯一 	必填 	2019111111
     * 商户订单金额 	orderAmount 	10 	以分为单位。比方10元，提交时金额应为1000 	必填 	10
     * 商户订单提交时间 	orderTime 	14 	数字串，一共14位 格式为：年[4位]月[2位]日[2位]时[2位]分[2位]秒[2位]例如：20071117020101 	必填 	20071117020101
     * 快钱时间戳 	orderTimestamp 	14 	数字串，一共14位 格式为：年[4位]月[2位]日[2位]时[2位]分[2位]秒[2位]例如：20071117020101 	非必填 	20071117020101
     * 商品名称 	productName 	256 	英文或中文字符串 	必填 	apple
     * 商品数量 	productNum 	8 	整型数字 	非必填 	1
     * 商品代码 	productId 	20 	字母、数字或 - 、_ 的组合，如商户发布了优惠券，并只想对指定的某商品或某类商品进行优惠时，请将此参数与发布优惠券时设置的“适用商品”保持一致。只可填写一个代码。如果不使用优惠券，本参数不用填写 	非必填 	1_1
     * 商品描述 	productDesc 	400 	英文或中文字符串 	非必填 	苹果
     * 扩展字段1 	ext1 	128 	英文或中文字符串 支付完成后按照原样返回给商户 (保险代理模式请参照ext1字段说明) 	非必填 	电费
     * 扩展字段2 	ext2 	128 	英文或中文字符串，支付完成后，按照原样返回给商户 	非必填 	水费
     * 聚合支付参数 	aggregatePay 	1024 	固定格式：27-3:[limitPay=0] * limitPay：限制支付类型 ，0-不限制，1-限制信用卡，不传则默认0 * 	非必填 	27-3:[limitPay=0]
     * 支付方式 	payType 	4 	固定选择值：00、21、21-1、21-2、23-2、26-2、27-3、13；00代表显示快钱各支付方式列表，21代表快捷支付，21-1代表储蓄卡快捷，21-2代表信用卡快捷，23-2代表快捷信用卡分期支付，27-3代表支付宝WAP支付(定制版)，26-2代表微信WAP支付(定制版)，13代表快企付 	必填 	21
     * 分期期数 	period 	2 	分期期数 	非必填 	3
     * 银行代码 	bankId 	8 	银行的代码，仅在银行直连/快捷支付指定银行定制时使用。 快捷支付指定银行定制:payType=21-1,21-2 无卡支持指定银行定制:payType=15 银行代码表见下载的接口文档; 指定银行定制默认开通. 	非必填 	ABC
     * 发卡机构 	cardIssuer 	20 	字符串，固定值 	非必填 	农业银行
     * 卡号 	cardNum 	19 	整形数字，提交给快钱的支付卡号 	非必填 	622848*******
     * 同一订单禁止重复提交标志 	redoFlag 	1 	固定选择值： 1、0，1代表同一订单号只允许提交1次；0表示同一订单号在没有支付成功的前提下可重复提交多次。默认为0，建议实物购物车结算类商户采用0；虚拟产品类商户采用1； 	非必填 	1
     * 订单超时时间 	orderTimeOut 	10 	正整数，0~2592000（30天）单位为秒，默认为空，为空表示交易无超时时间，订单超时计算规则：订单支付成功时间 减去 订单提交时间（orderTime） 大于 订单超时时间（orderTimeOut） 超时成功订单，快钱会自动发起退款 	非必填 	20
     * 附加信息类型 	extDataType 	1024 	固定值为NB2（分账时使用） 	非必填 	NB2
     * 附加信息 	extDataContent 	1024 	XML与extDataType匹配使用 。二维码实名认证key为realNameCertification，平台分账Key为sharingInfo ，支付宝扫码支付是否限制信用卡key为limitPay 	非必填 	{ " realNameCertification ": { "idName": "张三", "idType": "0", "idNo": "123456" }, "sharingInfo": { "sharingFlag": "1", "feeMode": "0", "feePayer": "tester", "sharingData": "2^tester1^200^0^test" } }
     * 分账标识 	sharingFlag 	1 	分账标识，为空代表不分账；1 为分账；如快钱后台没有开通平台分账功能，此参数传 1 无效； 	非必填 	1
     * 手续费收取方式 	feeMode 	1 	当 sharingFlag 为 1，不可为空。0: 主收款方承担手续费； 	非必填 	1
     * 手续费主手款方 	feePayer 	32 	当 feeMode 为 0，不可为空。用来承担交易手续费商户的useId（可为平台商户或分账子商户或其他第三方账户）。 	非必填 	0
     * 分账明细 	sharingData 		sharingFlag=1 时此字段不能为空； 分账明细数据格式：flag^sharingContact^amount^sharingSyncFlag^memo，多条分账明细数据之间采用符号 “|”进行分隔。
     * flag：必传，固定选择值 ：2；
     * useId（平台二级商户在平台的唯一标识）；
     * sharingContact：必传，分账方（或称平台合作商户）的 useId；
     * amount：必传，对应分账方的分账金额，单位为分；
     * sharingSyncFlag，分账模式，必传。固定值为 0 或者1或者T N（1<=N<=99）。【 0：准同步分账明细，即T 1分账；1：异步分账，平台调用分账确认接口后立即分账；T N：按计划分账，代表T N分账 】
     * memo：可为空，备注字段。
     * 注：如果feePayer不为空，则sharingContact必须包含feePayer，且对应的amount需要大于交易手续费金额 amount 的总和需要等于 orderAmount 	非必填 	2^HAT_10012932671^3^0^分账方1|2^HAT_10012934672^7^0^分账方2
     * 签名字符串 	signMsg 	1024 	参数1={参数1}&参数2={参数2}&……&参数n={参数n}然后进行商户私钥证书加签形成签名后进行1024位的Base64转码。拼接顺序详见接口安全机制 	必填
     */
    @Override
    public void setChargeMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = this.channelEntity();
        map.put("inputCharset", "1");
        map.put("pageUrl", entity.getCallbackUrl());
        map.put("bgUrl", this.getCollectNotifyUrl(entity));
        map.put("version", "mobile1.0");
        map.put("language", "1");
        map.put("signType", "4");
        map.put("merchantAcctId", channelEntity.getMerchantId());
        map.put("terminalIp", "127.0.0.1");
        map.put("tdpformName", "ceshi");
        map.put("orderId", entity.getId());
        map.put("orderAmount", entity.getAmount().multiply(new BigDecimal("100")).longValue());
        map.put("orderTime", DateUtils.format(new Date(), "yyyyMMddHHmmss"));
        map.put("productName", "apple");
        map.put("payType", "27-3");
    }

    /**
     * 商户号	mchNo	是	String(30)	Y1715088123	商户号
     * 通道ID	appId	是	String(24)	Y1715088123-1	通道ID
     * 支付订单号	payOrderId	是	String(30)	P20160427210604000490	支付中心生成的订单号，与mchOrderNo二者传一即可
     * 商户订单号	mchOrderNo	是	String(30)	20160427210604000490	商户生成的订单号，与payOrderId二者传一即可
     * 请求时间	reqTime	是	long	1622016572190	请求接口时间,13位时间戳
     * 接口版本	version	是	String(3)	1.0	接口版本号，固定：1.0
     * 签名	sign	是	String(32)	C380BEC2BFD727A4B6845133519F3AD6	签名值，详见签名算法
     * 签名类型	signType	是	String(32)	MD5	签名类型，目前只支持MD5方式
     * 预留信息	apiInfo	是	String(32)	123456	商户预留字段，用于验证商户
     *
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
        if (jsonObject.getIntValue("code") == 0) {
            JSONObject data = jsonObject.getJSONObject("data");
            ChannelChargeResponse response = new ChannelChargeResponse();
            String payUrl = data.getString("qrUrl");
            if (StringUtils.isNotEmpty(payUrl)) {
                JSONObject originalResponse = data.getJSONObject("originalResponse");
                response.setChannelOrder(originalResponse.getString("payOrderId"));
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