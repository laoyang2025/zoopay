package io.renren.zapi.merchant.dto;

import lombok.Data;

import java.math.BigDecimal;


// 充值应答
@Data
public class ChargeResponse {
    private Long id;            // 平台单号
    private String payUrl;      // 支付链接
    private String upi;         // upi  可能有
    private String raw;         // 原始支付材料
}
