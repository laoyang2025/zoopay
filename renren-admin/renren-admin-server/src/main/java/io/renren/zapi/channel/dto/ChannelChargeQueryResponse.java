package io.renren.zapi.channel.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 渠道处理的应答
 */
@Data
public class ChannelChargeQueryResponse {
    private Long id;
    private String channelOrder;  // 渠道订单
    private String upi;  // 收款账号信息
    private String utr;  //
    private String error; // 错误信息
    private Integer status;
    private BigDecimal realAmount;
}
