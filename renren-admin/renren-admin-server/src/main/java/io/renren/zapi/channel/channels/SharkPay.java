package io.renren.zapi.channel.channels;


import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.fastjson2.JSON;
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
import io.renren.zapi.utils.CommonUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

//
@Slf4j
public class SharkPay extends PostJsonChannel {

    @Override
    public String signField() {
        return "sign";
    }

    @Override
    public Pair<String, String> getSign(TreeMap<String, Object> map, String api) {
        String signstr = this.md5SignString(map, true) + "&key=" + channelEntity().getPrivateKey();
        String sign = DigestUtil.md5Hex(signstr).toUpperCase();
        map.put("sign", sign);
        map.put("sign_type", "MD5");
        return Pair.of(signstr, sign);
    }

    /**
     * merchant_id	商户号	是	string	商户号，系统分配
     * mer_order_num	商家订单号	是	string	保证每笔订单唯一
     * price	交易金额	是	float	以卢比为单位
     * currency_type	国家	是	int	币种，1为卢比（印度），2为菲律宾，3为尼日利亚，4为印度尼西亚，5为越南，6为巴西，7为马来西亚，8为墨西哥，9为南非
     * pay_code	通道编码	是	string	商户后台可以查询通道编码
     * attach	附带参数	是	string	商户如果没有传递的参数，可以为空，该参数参与签名最大长度不能超过200个字节，回调的时候会返回
     * notify_url	异步通知地址	是	string	不超过 200 字节,支付成功后发起,不能携带参数
     * page_url	同步跳转地址	是	string	不超过 200 字节,支付成功后跳转地址,不能携带参数
     * order_date	订单时间	是	string	时间格式yyyy-MM-dd HH:mm:ss
     * timestamp	时间戳	是	string	格式为：1646043952
     * sign_type	签名方式	是	string	固定值 MD5，不参与签名
     * sign	签名	是	string	不参与签名
     *
     * @param entity
     * @param map
     */
    @Override
    public void setChargeMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        map.put("merchant_id", channelEntity.getMerchantId());
        map.put("mer_order_num", entity.getId().toString());
        map.put("price", entity.getAmount().toString());
        map.put("currency_type", 1);
        map.put("pay_code", channelEntity.getPayCode());
        map.put("attach", entity.getId().toString());
        map.put("notify_url", this.getCollectNotifyUrl(entity));
        map.put("page_url", entity.getCallbackUrl());
        map.put("order_date", DateUtils.format(new Date(), DateUtils.DATE_TIME_PATTERN));
        map.put("timestamp", String.valueOf(new Date().getTime()));
    }

    @Override
    public void setChargeQueryMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        map.put("merchant_id", channelEntity.getMerchantId());
        map.put("mer_order_num", entity.getId().toString());
        map.put("timestamp", String.valueOf(new Date().getTime()));
        map.put("type", 1);
    }

    @Override
    public void setBalanceMap(TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        map.put("merchant_id", channelEntity.getMerchantId());
        map.put("timestamp", String.valueOf(new Date().getTime()));
    }

    @Override
    public void setWithdrawQueryMap(ZWithdrawEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        log.info("withdraw query: {}", entity);
        map.put("merchant_id", channelEntity.getMerchantId());
        map.put("mer_order_num", entity.getId().toString());
        map.put("timestamp", String.valueOf(new Date().getTime()));
        map.put("type", 2);
    }

    /**
     * account_name	收款人银行卡姓名	是	string	银行户名
     * account_num	收款银行账号	是	string	银行账号,如果是UPI代付需要直接传递UPI账户即可
     * account_bank	收款人银行名称	是	string	收款人银行名称，随便默认一个名称
     * remark	备注	是	string	1、如果是银行卡代付必填IFSC码
     * 2、如果是UPI代付，请传递固定值：UPI
     *
     * @param entity
     * @param map
     */
    @Override
    public void setWithdrawMap(ZWithdrawEntity entity, TreeMap<String, Object> map) {
        ZChannelEntity channelEntity = channelEntity();
        map.put("merchant_id", channelEntity.getMerchantId());
        map.put("mer_order_num", entity.getId().toString());
        map.put("price", entity.getAmount().toString());
        map.put("notify_url", this.getWithdrawNotifyUrl(entity));
        map.put("timestamp", String.valueOf(new Date().getTime()));
        map.put("account_name", entity.getAccountIfsc());
        map.put("account_num", entity.getAccountNo());
        map.put("account_bank", entity.getAccountBank());
        map.put("remark", entity.getAccountIfsc());
    }


    /**
     * {
     * "code":"200",
     * "msg":"success",
     * "data":{
     * "pay_url":"https://api.sharkpay.vip/gateway/pay_in_test?order_num=2025041921352550561076dd",
     * "order_num":"2025041921352550561076dd"},
     * "time":1745069725
     * }
     *
     * @param jsonObject
     * @return
     */
    @Override
    public ChannelChargeResponse doCharge(JSONObject jsonObject) {
        if (!jsonObject.getString("msg").equals("success")) {
            throw new RenException("渠道错误");
        }
        ChannelChargeResponse resp = new ChannelChargeResponse();
        JSONObject data = jsonObject.getJSONObject("data");
        resp.setChannelOrder(data.getString("order_num"));
        resp.setPayUrl(data.getString("pay_url"));
        return resp;
    }

    /**
     * {
     * "code":"200",
     * "msg":"success",
     * "data":
     * {
     * "order_num":"20250422180107141439f80a"
     * },
     * "time":1745316067
     * }
     */
    @Override
    public ChannelWithdrawResponse doWithdraw(JSONObject jsonObject) {
        ChannelWithdrawResponse response = new ChannelWithdrawResponse();
        int statusCode = jsonObject.getIntValue("code");
        if (statusCode != 200) {
            throw new RenException("渠道错误:" + jsonObject.getString("msg"));
        }
        response.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
        response.setChannelOrder(jsonObject.getJSONObject("data").getString("order_num"));
        response.setError(null);
        return response;
    }



    @Override
    public ChannelChargeQueryResponse doChargeQuery(JSONObject jsonObject) {
        throw new RenException("渠道不支持收款查询");
    }

    /**
     *  {"code":200,"msg":"success",
     *  "data":{"status":1,"order_num":"20250422180547975302d267","real_pay":"500.00","order_price":"500.00"},"time":1745316907}
     */
    @Override
    public ChannelWithdrawResponse doWithdrawQuery(JSONObject jsonObject) {
        if (jsonObject.getIntValue("code") != 200) {
            throw new RenException("查询失败");
        }
        JSONObject data = jsonObject.getJSONObject("data");
        ChannelWithdrawResponse response = new ChannelWithdrawResponse();
        int status = data.getIntValue("status");
        if (status == 2) {
            response.setStatus(ZooConstant.WITHDRAW_STATUS_SUCCESS);
        } else {
            response.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
        }
        return response;
    }

    /**
     * @param jsonObject
     * @return
     */
    @Override
    public ChannelBalanceResponse doBalance(JSONObject jsonObject) {
        JSONObject data = jsonObject.getJSONObject("data");
        ChannelBalanceResponse response = new ChannelBalanceResponse();
        BigDecimal balance = new BigDecimal(data.getString("balance"));
        BigDecimal real = new BigDecimal(data.getString("use_balance"));
        response.setBalance(balance);
        response.setBalanceMemo(real.toString());
        return response;
    }

    /**
     * {
     *     "code":200,
     *     "msg":"\u652f\u4ed8\u6210\u529f",
     *     "data":{
     *         "merchant_id":"10001000",
     *         "mer_order_num":"1913588493892366337",
     *         "price":"400.00",
     *         "real_price":"400.00",
     *         "finish_time":"2025-04-19 21:42:59",
     *         "order_num":"20250419214025551657900c",
     *         "attach":"send by sharkpay admin",
     *         "sign":"8D188111450E7E3619E75B607FCBD94B",
     *         "sign_type":"MD5"
     *     }
     * }
     */
    @Override
    public ChannelChargeQueryResponse chargeNotified(String contentType, Object body, Long deptId, Long id, HttpServletRequest request, HttpServletResponse response, ZChargeEntity chargeEntity) throws IOException {

        JSONObject jsonObject = JSON.parseObject((String)body);
        int code = jsonObject.getIntValue("code");
        if (code != 200) {
            throw new RenException("渠道通知我失败, code = " + code);
        }
        JSONObject data = jsonObject.getJSONObject("data");
        String jsonString = data.toJSONString();
        TreeMap<String, Object> map = this.getTreeMap(jsonString);

        // 验证签名
        String sign = (String)map.get(signField());
        map.remove(signField());
        map.remove("sign_type");

        Pair<String, String> pair = getSign(map, API_CHARGE_NOTIFY);
        if (!pair.getValue().equals(sign)) {
            throw new RenException("invalid signature");
        }

        ZChargeEntity zChargeEntity = getContext().getChargeDao().selectById(id);
        if (!new BigDecimal((String)map.get("price")).equals(zChargeEntity.getAmount())) {
            log.error("通知金额不匹配{} -> {}", map.get("price"), zChargeEntity.getAmount());
            throw new RenException("金额不匹配");
        }

        ChannelChargeQueryResponse resp = new ChannelChargeQueryResponse();
        resp.setStatus(ZooConstant.CHARGE_STATUS_SUCCESS);
        resp.setId(id);
        resp.setChannelOrder((String)map.get("order_num"));
        return resp;
    }


    /**
     * 返回标准的代付状态
     */
    @Override
    public ChannelWithdrawResponse drawNotified(String contentType, Object body, Long deptId, Long id, HttpServletRequest request, HttpServletResponse response, ZWithdrawEntity withdrawEntity) throws IOException {
        return this.withdrawQuery(withdrawEntity);
    }

    public String responseChargeOk() {
        return "SUCCESS";
    }

    public String responseWithdrawOk() {
        return "SUCCESS";
    }

    @Override
    public Pair<String, Object> webhook(Long deptId, Long channelId, String contentType, String body, HttpServletRequest request, HttpServletResponse response) {
        JSONObject parse = JSONObject.parse(body);
        long orderId = Long.parseLong(parse.getString("ClientOrderId"));
        ZWithdrawEntity entity = getContext().getWithdrawDao().selectById(orderId);
        BigDecimal amount = new BigDecimal(parse.getString("Amount")).setScale(2, RoundingMode.UP);
        if (amount.compareTo(entity.getAmount()) != 0) {
            throw new RenException("金额不匹配");
        }
        return Pair.of(API_WITHDRAW_NOTIFY, entity);
    }

}
