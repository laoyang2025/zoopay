package io.renren.zapi.channel.channels;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Pair;
import com.alibaba.fastjson2.JSONObject;
import io.renren.commons.tools.exception.RenException;
import io.renren.zadmin.entity.ZChannelEntity;
import io.renren.zadmin.entity.ZChargeEntity;
import io.renren.zadmin.entity.ZWithdrawEntity;
import io.renren.zapi.channel.dto.ChannelBalanceResponse;
import io.renren.zapi.channel.dto.ChannelChargeQueryResponse;
import io.renren.zapi.channel.dto.ChannelChargeResponse;
import io.renren.zapi.channel.dto.ChannelWithdrawResponse;

import java.util.Date;
import java.util.TreeMap;

public class YxPay extends PostFormChannel {

    /**
     * status	状态	是	是	success:请求成功，error：请求失败
     * msg	状态描述	是	是
     * mchid	商户号	是	是	status=success时返回
     * balance	可提现余额	是	是	status=success时返回
     * blockedbalance	冻结余额	是	status=success时返回
     * pay_md5sign	MD5签名	是	否
     *
     * @param jsonObject
     * @return
     */
    @Override
    public ChannelBalanceResponse doBalance(JSONObject jsonObject) {
        return null;
    }

    public void setBalanceMap(TreeMap<String, Object> map) {
        map.put("mchid", channelEntity().getMerchantId());
    }

    /**
     * 参数名称	参数含义	是否必填	参与签名	参数说明
     * status	状态	是	是	success：请求成功（不代表业务成功），error：请求失败
     * msg	状态描述	是	是
     * mchid	商户号	是	是	status=success时返回
     * out_trade_no	商户订单号	是	是	status=success时返回
     * amount	金额	是	是	status=success时返回
     * transaction_id	平台流水号	是	是	status=success时返回
     * refCode	业务状态	是	是	status=success时返回
     * refMsg	业务描述	是	是	status=success时返回
     * success_time	成功时间	是	是	status=success，refCode=1时返回
     * sign	MD5签名	是	否
     * refCode返回值
     * 返回值	含义
     * 1	成功
     * 2	失败
     * 3	处理中
     * 4	待处理
     * 5	审核驳回
     * 6	待审核
     * 7	交易不存在
     * 8	未知状态
     *
     * @param jsonObject
     * @return
     */
    public ChannelWithdrawResponse doWithdrawQuery(JSONObject jsonObject) {
        throw new RenException("not implemented");
    }

    /**
     * out_trade_no	商户订单号	是	是
     * mchid	商户号	是	是
     * pay_md5sign	签名	是	否
     *
     * @param entity
     * @param map
     */
    @Override
    public void setWithdrawQueryMap(ZWithdrawEntity entity, TreeMap<String, Object> map) {
        map.put("out_trade_no", entity.getId().toString());
        map.put("mchid", channelEntity().getMerchantId());
    }


    /**
     * 返回值
     * 参数名称	参数含义	是否必填	参与签名	参数说明
     * memberid	商户编号	是	是
     * returncode	请求状态	是	是	00表示成功，其它表示失败
     * data	订单信息（数据类型：集合）	是	否
     * sign	MD5签名	是	否
     * 返回值data参数
     * 参数名称	参数含义	参数说明
     * orderid	商户订单号
     * transaction_id	平台订单号
     * amount	订单金额	单位：元
     * time_end	支付成功时间
     * trade_state	支付状态	SUCCESS：支付成功，NOTPAY：未支付
     *
     * @param jsonObject
     * @return
     */
    @Override
    public ChannelChargeQueryResponse doChargeQuery(JSONObject jsonObject) {
        throw new RenException("not implemented");
    }

    /**
     * pay_memberid	商户编号	是	是
     * pay_orderid	商户订单号	是	是
     * pay_md5sign	MD5签名	是	是
     *
     * @param entity
     * @param map
     */
    @Override
    public void setChargeQueryMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        map.put("pay_memberid", channelEntity().getMerchantId());
        map.put("pay_orderid", entity.getId().toString());
    }

    // 如何从json对象里组处标准的渠道应答
    @Override
    public ChannelWithdrawResponse doWithdraw(JSONObject jsonObject) {
        throw new RenException("not implemented");
    }

    /**
     * mchid	商户编号	是	是	平台分配商户号
     * out_trade_no	商户订单号	是	是	保证唯一值
     * money	订单金额	是	是	单位：元
     * bankname	开户行名称	是	是
     * subbranch	支行名称	是	是
     * accountname	开户名	是	是
     * cardnumber	银行卡号	是	是
     * province	省份	是	是
     * city	城市	是	是
     * extends	附加字段	否	是
     * sign	MD5签名	是	否
     *
     * @param entity
     * @param map
     */
    // 如何组代付请求字段
    @Override
    public void setWithdrawMap(ZWithdrawEntity entity, TreeMap<String, Object> map) {
        map.put("mchid", channelEntity().getMerchantId());
        map.put("out_trade_no", entity.getId().toString());
        map.put("money", entity.getAmount());
        map.put("bankname", entity.getAccountBank());
        map.put("subbranch", "todo");
        map.put("accountname", entity.getAccountUser());
        map.put("cardnumber", entity.getAccountNo());
        map.put("province", "");
        map.put("city", "");
        map.put("extends", "");
    }

    // 需要实现的: 如何从Json对象里计算处标准的渠道应答
    @Override
    public ChannelChargeResponse doCharge(JSONObject jsonObject) {
        throw new RenException("not implemented");
    }

    /**
     * pay_memberid	商户号	是	是	平台分配商户号
     * pay_orderid	订单号	是	是	上送订单号唯一, 字符长度20
     * pay_applydate	提交时间	是	是	时间格式：2016-12-26 18:18:18
     * pay_bankcode	银行编码	是	是	在商户中心查询
     * pay_notifyurl	服务端通知	是	是	服务端返回地址.（POST返回数据）
     * pay_callbackurl	页面跳转通知	是	是	页面跳转返回地址（POST返回数据）
     * pay_amount	订单金额	是	是	单位：元
     * pay_md5sign	MD5签名	是	是	请查看签名算法
     * pay_productname	商品名称	是	否
     * pay_ip	用户请求ip	否	否	商户获取用户请求ip提交给系统
     * pay_productnum	商户品数量	否	否
     * pay_productdesc	商品描述	否	否
     * pay_producturl	商户链接地址	否	否
     * pay_way	下单数据返回格式	否	否	pay_way=json则统一返回json数据格式；pay_way=html则直接返回支付结果页面，默认为post表单数据格式提交，默认为html。
     *
     * @param entity
     * @param map
     */
    // 如何组渠道需要的字段
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
        map.put("pay_productname", "vip service");
        map.put("pay_way", "json");
    }

    // 签名字段的名称
    @Override
    public String signField() {
        return "pay_md5sign";
    }

    // 获取计算签名
    public Pair<String, String> getSign(TreeMap<String, Object> map, String api) {
        return this.kvMd5Sign(map, null, null, false);
    }

    // 如何请求
    //  public String request(String url, TreeMap<String, Object> map, String api);
}

