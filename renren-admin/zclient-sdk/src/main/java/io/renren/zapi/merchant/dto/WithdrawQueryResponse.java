package io.renren.zapi.merchant.dto;

import lombok.Data;

import java.math.BigDecimal;


// 代付查询印答
@Data
public class WithdrawQueryResponse {
    private Long id;                  // 平台单号
    private String orderId;           // 商户单号
    private Integer processStatus;    // 订单处理状态
    private String utr;               // utr
    private String pictures;           // 付款凭证
    private BigDecimal amount;        // 付款金额
}
