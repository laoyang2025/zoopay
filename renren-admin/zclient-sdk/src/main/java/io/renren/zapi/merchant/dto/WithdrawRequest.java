package io.renren.zapi.merchant.dto;

import lombok.Data;

import java.math.BigDecimal;


// 代付请求
@Data
public class WithdrawRequest {
    private String orderId;      // 商户单号
    private BigDecimal amount;   // 金额:  99.99
    private String accountUser;  // 账户名
    private String accountNo;    // 账户号
    private String accountBank;  // 银行:  可以填NA
    private String accountIfsc;  // IFSC
    private String notifyUrl;    // 回调url
    private String callbackUrl;  // 跳转url可以和notifyUrl一样
    private String memo;         // 可填NA
}
