package io.renren.zapi.agent;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AgentHashItem {
    Long agentId;
    Long userId;
    Long cardId;
    BigDecimal userBalance;   // 用户余额
    BigDecimal agentBalance;  // 代理余额
    int total;          // 接单笔数
    double successRate; // 接单成功率
}
