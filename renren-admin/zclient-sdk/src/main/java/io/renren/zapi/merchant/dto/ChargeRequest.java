package io.renren.zapi.merchant.dto;


import lombok.Data;

import java.math.BigDecimal;

// 收款充值请求
@Data
public class ChargeRequest {
    private BigDecimal amount;     // 金额:  99.99
    private String orderId;        // 商户订单号
    private String notifyUrl;      // 回调地址
    private String callbackUrl;    // 跳转地址
    private String payCode;        // 支付代码:  upi
    private String memo;           // 其他信息:  可选填
}
