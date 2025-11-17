package io.renren.zapi.merchant.dto;

import lombok.Data;


// 代付查询请求
@Data
public class WithdrawQueryRequest {
    private Long id;             // 平台单号
    private String orderId;      // 商户单号
}
