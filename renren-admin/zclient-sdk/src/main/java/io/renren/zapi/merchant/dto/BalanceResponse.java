package io.renren.zapi.merchant.dto;


import lombok.Data;

import java.math.BigDecimal;

// 余额查询应答
@Data
public class BalanceResponse {
    // 余额数字
    private BigDecimal balance;
}
