package io.renren.zapi.channel.dto;

import lombok.Data;

/**
 * 渠道处理的应答
 */
@Data
public class ChannelWithdrawResponse {
    private String error; // 错误信息
    private String channelOrder; // 渠道单号
    private String utr; // 唯一凭证
    private String upi; // 付款人的账号信息
    private Integer status; // 代付状态
}
