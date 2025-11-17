package io.renren.zapi.channel.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 渠道处理的应答
 */
@Data
public class ChannelBalanceResponse {
    private String balanceMemo;
    private BigDecimal balance;
}
