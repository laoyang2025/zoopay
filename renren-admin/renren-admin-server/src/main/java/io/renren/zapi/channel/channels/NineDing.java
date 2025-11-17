package io.renren.zapi.channel.channels;


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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;


// 老杨的机构:  Yang
@Slf4j
public class NineDing extends PostFormChannel {

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
        return this.kvMd5Sign(map, null, "key", false);
    }

    /**
     * 1	version	接口版本	String	是	固定值 V7(大写)
     * 2	merchant_no	商户号	String	是
     * 3	tran_flow	订单号	String	是	商户订单号(商户提供,要求唯一,建议高于20位)
     * 4	tran_datetime	13位时间戳	String	是	1696694305000(北京时间毫秒)
     * 5	amount	订单金额	String	是	单位：元,支持两位小数
     * 6	pay_type	通道编码	String	是
     * 7	notify_url	回调地址	String	是	必须包含http或https否则不生效
     * 8	redirect_url	成功跳转地址	String	否	必须包含http或https否则不生效
     * 9	product_info	订单标题/内容	String	是	订单标题，可以用订单号代替
     * 10	acc_no	付款人账号	String	否
     * 11	acc_name	付款人姓名	String	否
     * 12	pay_email	付款人邮箱	String	否
     * 13	pay_phone	付款人手机号	String	否
     * 14	bank_code	付款人银行编码	String	否
     * 15	bank_name	付款人银行名	String	否
     * 16	sign	MD5签名	String	是	详见签名规范(32位大写)
     */
    @Override
    public void setChargeMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        map.put("version", "V7");
        map.put("merchant_no", channelEntity.getMerchantId());
        map.put("tran_flow", entity.getId().toString());
        map.put("tran_datetime", new Date().getTime());
        map.put("amount", entity.getAmount());
        map.put("pay_type", channelEntity.getPayCode());
        map.put("notify_url", this.getCollectNotifyUrl(entity));
        map.put("redirect_url", this.getCollectNotifyUrl(entity));
    }

    /**
     *
     */
    @Override
    public void setChargeQueryMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();

        map.put("version", "V7");
        map.put("merchant_no", channelEntity.getMerchantId());
        map.put("tran_flow", entity.getId().toString());
        map.put("tran_datetime", new Date().getTime());

    }

    /**
     *
     */
    @Override
    public void setBalanceMap(TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        map.put("mchId", channelEntity.getMerchantId());
        map.put("reqTime", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        map.put("version", "1.0");
    }

    /**
     *
     */
    public void setWithdrawMap(ZWithdrawEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();

        map.put("version", "V7");
        map.put("merchant_no", channelEntity.getMerchantId());
        map.put("tran_flow", entity.getId().toString());
        map.put("tran_datetime", new Date().getTime());
        map.put("amount", entity.getAmount());
        map.put("pay_type", "F001");
        map.put("notify_url", this.getWithdrawNotifyUrl(entity));
        map.put("acc_name", entity.getAccountUser());
        map.put("acc_no", entity.getAccountNo());
//        map.put("bank_code", entity.getAccountBank());
        map.put("bank_name", entity.getAccountBank());
        map.put("identity_num", entity.getAccountIfsc());
        map.put("pay_method", "BANK");
    }

    /**
     *
     */
    @Override
    public void setWithdrawQueryMap(ZWithdrawEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        map.put("version", "V7");
        map.put("merchant_no", channelEntity.getMerchantId());
        map.put("tran_flow", entity.getId().toString());
        map.put("tran_datetime", new Date().getTime());
    }

    /**
     *
     */
    @Override
    public ChannelChargeResponse doCharge(JSONObject jsonObject) {
        String status = jsonObject.getString("status");
        if (!"0001".equals(status)) {
            throw new RenException("渠道失败:" + jsonObject.getString("msg"));
        }
        String payUrl = jsonObject.getString("yul1");
        ChannelChargeResponse response = new ChannelChargeResponse();
        if (StringUtils.isNotEmpty(payUrl)) {
            response.setChannelOrder(jsonObject.getString("pay_serial_no"));
            response.setPayUrl(payUrl);
            response.setUpi(null);
            response.setRaw(null);
            return response;
        } else {
            throw new RenException("渠道失败");
        }
    }

    /**
     * 0000成功,0005退回
     * 00001
     */
    @Override
    public ChannelWithdrawResponse doWithdraw(JSONObject jsonObject) {
        ChannelWithdrawResponse response = new ChannelWithdrawResponse();
        String status = jsonObject.getString("status");
        if ("0001".equals(status)) {
            response.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
            response.setChannelOrder(jsonObject.getString("pay_serial_no"));
        } else {
            response.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
            response.setError(jsonObject.getString("msg"));
        }
        return response;
    }

    /**
     *
     */
    @Override
    public ChannelChargeQueryResponse doChargeQuery(JSONObject jsonObject) {
        ChannelChargeQueryResponse response = new ChannelChargeQueryResponse();
        if ("0000".equals(jsonObject.getString("status"))) {
            response.setChannelOrder(jsonObject.getString("payOrderId"));
            response.setStatus(ZooConstant.CHARGE_STATUS_SUCCESS);
        } else {
            response.setStatus(ZooConstant.CHARGE_STATUS_PROCESSING);
        }
        return response;
    }

    /**
     *
     */
    @Override
    public ChannelWithdrawResponse doWithdrawQuery(JSONObject jsonObject) {
        ChannelWithdrawResponse response = new ChannelWithdrawResponse();
        String status = jsonObject.getString("status");
        if ("0000".equals(status)) {
            response.setStatus(ZooConstant.WITHDRAW_STATUS_SUCCESS);
            return response;
        } else if ("0005".equals(status)) {
            response.setStatus(ZooConstant.WITHDRAW_STATUS_SUCCESS);
            return response;
        } else if ("0001".equals(status)) {
            response.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
            return response;
        }
        throw new RenException("渠道异常");
    }

    /**
     * 代付余额	agentpayBalance	是	int	10000	代付余额
     * 可用代付余额	availableAgentpayBalance	是	int	10000	可用代付余额
     */
    @Override
    public ChannelBalanceResponse doBalance(JSONObject jsonObject) {
        ChannelBalanceResponse response = new ChannelBalanceResponse();
        response.setBalance(new BigDecimal(-1));
        response.setBalanceMemo("no balance");
        return response;
    }

    /**
     *
     */
    @Override
    public ChannelChargeQueryResponse chargeNotified(String contentType, Object body, Long deptId, Long id, HttpServletRequest request, HttpServletResponse response, ZChargeEntity chargeEntity) throws IOException {
        ChannelChargeQueryResponse resp = new ChannelChargeQueryResponse();

        TreeMap<String, Object> map = getTreeMap((String) body);

        // 这个渠道变态, POST里传 GET参数
        checkSign(map, API_CHARGE_NOTIFY, false);

        if ("0000".equals(map.get("status"))) {
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

        TreeMap<String, Object> map = getTreeMap((String) body);
        checkSign(map, API_WITHDRAW_NOTIFY, false);

        String state = (String) map.get("status");
        ChannelWithdrawResponse resp = new ChannelWithdrawResponse();
        if ("0000".equals(state)) {
            resp.setStatus(ZooConstant.WITHDRAW_STATUS_SUCCESS);
        } else if ("0005".equals(state)) {
            resp.setStatus(ZooConstant.WITHDRAW_STATUS_FAIL);
        } else {
            resp.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
        }
        return resp;
    }

    public String responseChargeOk() {
        return "success";
    }

    public String responseWithdrawOk() {
        return "success";
    }
}
