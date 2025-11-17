package io.renren.zapi.merchant.dto;

import lombok.Data;

import java.math.BigDecimal;


// 收款通知
@Data
public class ChargeNotify {
    private Integer processStatus; //   订单状态: 0, 1: 处理中，  2: 成功,  3: 超时
    private Long id;   // 平台订单号
    private String orderId; // 商户订单号
    private String utr; // utr
    private String upi;  // upi
    private BigDecimal realAmount;  // 实际付款金额
}
