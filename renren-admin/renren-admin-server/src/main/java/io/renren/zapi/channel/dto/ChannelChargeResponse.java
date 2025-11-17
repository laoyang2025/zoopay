package io.renren.zapi.channel.dto;

import lombok.Data;

/**
 * 渠道处理的应答
 */
@Data
public class ChannelChargeResponse {
    private String payUrl;  // 支付地址
    private String channelOrder;  // 渠道订单
    private String upi;  // 收款账号信息
    private String raw;  // 原始收款连接
    private String error; // 错误信息
}
