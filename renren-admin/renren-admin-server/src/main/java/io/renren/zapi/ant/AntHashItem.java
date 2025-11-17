package io.renren.zapi.ant;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AntHashItem {
    Long userId;
    Long cardId;
    BigDecimal userBalance;
    int total;
    double successRate;
}