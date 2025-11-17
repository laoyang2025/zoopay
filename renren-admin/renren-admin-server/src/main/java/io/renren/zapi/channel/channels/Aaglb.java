package io.renren.zapi.channel.channels;


import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Pair;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.nacos.api.naming.pojo.healthcheck.impl.Http;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.renren.commons.tools.exception.RenException;
import io.renren.zadmin.entity.ZChannelEntity;
import io.renren.zadmin.entity.ZChargeEntity;
import io.renren.zadmin.entity.ZWithdrawEntity;
import io.renren.zapi.ZooConstant;
import io.renren.zapi.channel.dto.ChannelBalanceResponse;
import io.renren.zapi.channel.dto.ChannelChargeQueryResponse;
import io.renren.zapi.channel.dto.ChannelChargeResponse;
import io.renren.zapi.channel.dto.ChannelWithdrawResponse;
import io.renren.zapi.utils.CommonUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.TreeMap;

// mark支付
// 代收密钥：4fd2211409fa496cb4b8da10af4a6079   privateKey
// 代付密钥：2OMXIYC52PR6RT6IHLHXMIQSVSG5PGWX   platformKey
// payCode: 101
//
@Slf4j
public class Aaglb extends PostFormChannel {

    /**
     * 签名值的字段名称
     */
    @Override
    public String signField() {
        return "sign";
    }

    /**
     * 计算签名
     */
    @Override
    public Pair<String, String> getSign(TreeMap<String, Object> map, String api) {
        Object signType = map.remove("sign_type");
        Object signType2 = map.remove("signType");
        Pair<String, String> pair = null;
        if (api.equals(AbstractChannel.API_WITHDRAW) ||
                api.equals(AbstractChannel.API_WITHDRAW_NOTIFY) ||
                api.equals(AbstractChannel.API_WITHDRAW_QUERY) ||
                api.equals(AbstractChannel.API_BALANCE)
        ) {
            pair = this.kvMd5Sign(map, channelEntity().getPlatformKey(), "key", false);
        } else {
            pair = this.kvMd5Sign(map, null, null, false);
        }
        if (signType != null) {
            map.put("sign_type", signType);
        }
        if (signType2 != null) {
            map.put("signType", signType2);
        }

        return Pair.of(pair.getKey(), pair.getValue().toLowerCase());
    }

    /**
     * 检查对方签名
     */
    public TreeMap<String, Object> checkSign(String body, String api) throws JsonProcessingException {
        TreeMap<String, Object> map = getTreeMapByForm(body);
        // 验证签名
        String sign = (String) map.get(signField());
        map.remove(signField());
        map.remove("signType");
        Pair<String, String> pair = getSign(map, api);
        if (!pair.getValue().equals(sign)) {
            log.error("验证签名错误: 对方签名[{}], 我方签名[{}], 我方签名串:[{}]", sign, pair.getValue(), pair.getKey());
            throw new RenException("invalid signature");
        }
        return map;
    }


    /**
     * version	版本号	String	N	需同步返回JSON 必填，固定值 1.0
     * mch_id	商户号	String	Y	平台分配唯一
     * notify_url	异步通知地址	String	Y	不超过 200 字节,支付成功后发起,不能携带参数
     * page_url	同步跳转地址	String	N	不超过 200 字节,支付成功后跳转地址,不能携带参数
     * mch_order_no	商家订单号	String	Y	保证每笔订单唯一
     * pay_type	支付类型	String	Y	请查阅商户后台通道编码
     * trade_amount	交易金额	String	Y	当地货币 精确到元
     * order_date	订单时间	String	Y	时间格式(北京时间) yyyy-MM-dd HH:mm:ss
     * bank_code	银行代码	String	N	网银通道必填，其他类型一定不能填该参数
     * goods_name	商品名称	String	Y	不超过 50 字节
     * mch_return_msg	透传参数	String	N	不超过200字节
     * payer_phone	手机号码	String	N	付款账号,肯尼亚代收必填(0开头10位数字,例:0146889191
     * sign_type	签名方式	String	Y	固定值 MD5，不参与签名
     * sign	签名	String	Y	不参与签名
     */
    @Override
    public void setChargeMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        map.put("version", "1.0");
        map.put("mch_id", channelEntity.getMerchantId());
        map.put("mch_order_no", entity.getId().toString());
        map.put("order_date", DateUtil.formatDateTime(new Date()));
        map.put("notify_url", getCollectNotifyUrl(entity));
        map.put("page_url", getCollectNotifyUrl(entity));
        map.put("pay_type", channelEntity.getPayCode());
        map.put("trade_amount", entity.getAmount());
        map.put("goods_name", "VIP");
        map.put("mch_return_msg", "NA");
        map.put("sign_type", "MD5");
    }

    /**
     * 组收款查询报文
     */
    @Override
    public void setChargeQueryMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        map.put("merNo", channelEntity().getMerchantId());
        map.put("requestNo", CommonUtils.randomDigitString(15));
        map.put("merOrderNo", entity.getId().toString());
        map.put("orderNo", entity.getChannelOrder());
        map.put("timestamp", String.valueOf(System.currentTimeMillis()));
    }

    /**
     * 应答里解析出标准应答
     * {
     * "signType": "MD5",
     * "sign": "4ba90c98783a04d2dff2a032148d6d2b",
     * "respCode": "SUCCESS",
     * "tradeResult": "1",
     * "tradeMsg": "request success",
     * "mchId": "123456789",
     * "mchOrderNo": "2021-04-13 17:32:28",
     * "oriAmount": "100",
     * "tradeAmount": "100",
     * "orderDate": "2021-04-13 17:32:28",
     * "orderNo": "300001033",
     * "payInfo": "https://www.baidu.com"
     * }
     */
    @Override
    public ChannelChargeResponse doCharge(JSONObject jsonObject) {
        if (jsonObject.getString("respCode").equals("SUCCESS")) {
            ChannelChargeResponse response = new ChannelChargeResponse();
            String payUrl = jsonObject.getString("payInfo");
            if (StringUtils.isNotEmpty(payUrl)) {
                response.setChannelOrder(jsonObject.getString("orderNo"));
                response.setPayUrl(payUrl);
                response.setUpi(null);
                response.setRaw(null);
                return response;
            }
            throw new RenException(channelEntity().getChannelLabel() + "错误:" + jsonObject.getString("tradeMsg"));
        } else {
            throw new RenException(channelEntity().getChannelLabel() + "错误:" + jsonObject.getString("tradeMsg"));
        }
    }

    /**
     * 余额查询组串
     * sign_type	签名方式	String	Y	固定值MD5，不参与签名
     * sign	签名	String	Y	不参与签名
     * mch_id	商户代码	String	Y	平台分配唯一
     */
    @Override
    public void setBalanceMap(TreeMap<String, Object> map) {
        map.put("sign_type", "MD5");
        map.put("mch_id", channelEntity().getMerchantId());
    }

    /**
     * sign_type	签名方式	String	Y	固定值MD5，不参与签名
     * sign	签名	String	Y	不参与签名
     * mch_id	商户代码	String	Y	平台分配唯一
     * mch_transferId	商家转账订单号	String	Y	保证每笔订单唯一
     * transfer_amount	转账金额	String	Y	以当地货币 精确到元
     * apply_date	申请时间	String	Y	时间格式 （北京时间）：yyyy-MM-dd HH:mm:ss
     * bank_code	收款银行代码	String	Y	详见商户后台银行代码表
     * receive_name	收款银行户名	String	Y	银行户名
     * receive_account	收款银行账号	String	Y	银行账号(巴西PIX代付填对应的类型账号)
     * remark	备注	String	N	印度代付必填IFSC码，（哥伦比亚、厄瓜多尔、埃及、澳大利亚填写证件号码），秘鲁必填CCI码
     * back_url	异步通知地址	String	N	若填写则需参与签名,不能携带参数
     * receiver_telephone	收款人手机号码	String	N	若填写则需参与签名(加纳、埃及代付必填)
     */
    public void setWithdrawMap(ZWithdrawEntity entity, TreeMap<String, Object> map) {
        map.put("sign_type", "MD5");
        map.put("mch_id", channelEntity().getMerchantId());
        map.put("mch_transferId", entity.getId().toString());
        map.put("transfer_amount", entity.getAmount().toString());
        map.put("apply_date", DateUtil.formatDateTime(new Date()));
        map.put("bank_code", "IDPT0001");
        map.put("receive_name", entity.getAccountUser());
        map.put("receive_account", entity.getAccountNo());
        map.put("remark", entity.getAccountIfsc());
        map.put("back_url", getWithdrawNotifyUrl(entity));
    }

    /**
     * 组代付查询请求报文
     * sign_type	签名方式	String	Y	MD5不参与签名
     * sign	签名	String	Y	不参与签名
     * mch_id	商户代码	String	Y	平台分配唯一
     * mch_transferId	商家转账单号	String	Y	代付使用的转账单号
     */
    @Override
    public void setWithdrawQueryMap(ZWithdrawEntity entity, TreeMap<String, Object> map) {
        map.put("sign_type", "MD5");
        map.put("mch_id", channelEntity().getMerchantId());
        map.put("mch_transferId", entity.getId().toString());
    }


    /**
     * 代付应答里 --》
     * tradeResult
     * 0:申请成功
     * 1:转账成功
     * 2:转账失败
     * 3:转账拒绝
     * 4:处理中
     */
    @Override
    public ChannelWithdrawResponse doWithdraw(JSONObject jsonObject) {
        ChannelWithdrawResponse response = new ChannelWithdrawResponse();
        if ("SUCCESS".equals(jsonObject.getString("respCode"))) {
            String tradeResult = jsonObject.getString("tradeResult");
            // 发起代付成功
            if ("1".equals(tradeResult) || "0".equals(tradeResult)) {
                response.setChannelOrder(jsonObject.getString("tradeNo"));
                response.setError(null);
                response.setStatus(ZooConstant.WITHDRAW_STATUS_SUCCESS);
            } else if ("2".equals(tradeResult) || "3".equals(tradeResult)) {
                response.setError("渠道失败");
                response.setStatus(ZooConstant.WITHDRAW_STATUS_FAIL);
            }
            response.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
        } else {
            response.setError(jsonObject.getString("errorMsg"));
        }
        return response;
    }

    /**
     *
     */
    @Override
    public ChannelChargeQueryResponse doChargeQuery(JSONObject jsonObject) {
        throw new RenException("渠道不支持查询交易");
    }


    /**
     * 代付查询结果 --> 标准代付应答
     */
    @Override
    public ChannelWithdrawResponse doWithdrawQuery(JSONObject jsonObject) {
        return this.doWithdraw(jsonObject);
    }


    /**
     * 从余额查询结果里返回标准的余额查询应答
     * amount	总金额	String	Y	商户总金额
     * frozenAmount	冻结金额	String	Y	商户冻结金额
     * availableAmount	可用金额	String	Y	商户可用金额
     */
    @Override
    public ChannelBalanceResponse doBalance(JSONObject jsonObject) {
        ChannelBalanceResponse response = new ChannelBalanceResponse();
        if ("SUCCESS".equals(jsonObject.getString("respCode"))) {
            BigDecimal amount = new BigDecimal(jsonObject.getString("amount"));
            String frozenAmount = jsonObject.getString("frozenAmount");
            String available = jsonObject.getString("availableAmount");
            response.setBalance(amount);
            response.setBalanceMemo("冻结:" + frozenAmount + ", 可用:" + available);
            return response;
        }
        {
            throw new RenException("查询失败");
        }
    }

    /**
     * tradeResult	订单状态	String	Y	1：支付成功
     * mchId	商户号	String	Y
     * mchOrderNo	商家订单号	String	Y
     * oriAmount	原始订单金额	String	Y	商家上传的订单金额
     * amount	交易金额	String	Y	实际支付金额
     * orderDate	订单时间	String	Y
     * orderNo	平台支付订单号	String	Y
     * merRetMsg	透传参数	String	N	下单时未提交则无需参与签名
     * signType	签名方式	String	Y	不参与签名
     * sign	签名	String	Y	不参与签名
     */
    @Override
    public ChannelChargeQueryResponse chargeNotified(String contentType, Object body, Long deptId, Long id, HttpServletRequest request, HttpServletResponse response, ZChargeEntity chargeEntity) throws IOException {
        ChannelChargeQueryResponse resp = new ChannelChargeQueryResponse();
        TreeMap<String, Object> map = checkSign((String) body, API_CHARGE_NOTIFY);
        if ("1".equals(map.get("tradeResult"))) {
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
        TreeMap<String, Object> map = checkSign((String) body, API_WITHDRAW_NOTIFY);
        String tradeResult = (String) map.get("tradeResult");
        ChannelWithdrawResponse channelWithdrawResponse = new ChannelWithdrawResponse();
        if ("1".equals(tradeResult)) {
            channelWithdrawResponse.setStatus(ZooConstant.WITHDRAW_STATUS_SUCCESS);
            return channelWithdrawResponse;
        }
        if ("2".equals(tradeResult)) {
            channelWithdrawResponse.setStatus(ZooConstant.WITHDRAW_STATUS_FAIL);
            return channelWithdrawResponse;
        }
        throw new RenException("未知通知代付状态");
    }


    public String responseChargeOk() {
        return "success";
    }

    public String responseWithdrawOk() {
        return "success";
    }

}
