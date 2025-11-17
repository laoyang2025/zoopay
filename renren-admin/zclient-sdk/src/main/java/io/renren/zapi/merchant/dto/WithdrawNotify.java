package io.renren.zapi.merchant.dto;

import lombok.Data;

import java.math.BigDecimal;


// 代付通知
@Data
public class WithdrawNotify {
    private Integer processStatus;  // 订单处理状态
    private Long id;                // 平台单号
    private String orderId;         // 商户单号
    private String utr;             // utr
    private String pictures;         // 支付凭证地址: 可能有
    private BigDecimal amount;      // 代付金额
}
