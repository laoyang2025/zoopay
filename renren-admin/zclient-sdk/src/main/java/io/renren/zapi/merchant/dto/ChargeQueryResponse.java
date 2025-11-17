package io.renren.zapi.merchant.dto;

import lombok.Data;

import java.math.BigDecimal;


// 充值收款查询
@Data
public class ChargeQueryResponse {
    private Long id;             // 平台单号
    private String orderId;        // 商户单号
    private Integer processStatus;  // 订单处理状态: 0, 1: 处理中， 2: 成功,  3: 超时
    private String utr;             // utr
    private String upi;             // upi
    private BigDecimal realAmount;  // 实际付款金额
}
