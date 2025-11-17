package io.renren.zapi.controller;

import lombok.Data;

// 码农卡主claim代付订单
@Data
public class ClaimResponse {
    private String accountIfsc;
    private String accountBank;
    private String accountName;
    private String accountNO;
    private Long amount;
}
