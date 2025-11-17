package io.renren.zapi.merchant.dto;

import lombok.Data;

// 充值查询
@Data
public class ChargeQueryRequest {
    private Long id;     // 平订单号
    private String orderId;  // 商户订单号
}
