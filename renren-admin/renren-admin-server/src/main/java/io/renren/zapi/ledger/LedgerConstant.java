package io.renren.zapi.ledger;

public class LedgerConstant {
    /**
     *
     */
    // 代理记账
    public static final int FACT_AGENT_COLLECT_ASSIGNED_SHARED = 100; // 收款分配到卡主占用代理额度
    public static final int FACT_AGENT_COLLECT_TIMEOUT_SHARED = 101;  // 收款超时, 退回代理共享额度
    public static final int FACT_AGENT_AGENT_CHARGE_SUCCESS = 102; // 代理充值成功
    public static final int FACT_AGENT_MERCHANT_CHARGE_SUCCESS = 103;  // 商户充值成功, 代理手续费
    // 码农记账
    public static final int FACT_ANT_COLLECT_ASSIGNED = 201; // 收款分配到码农
    public static final int FACT_ANT_COLLECT_TIMEOUT = 202; // 收款超时，退回码农
    public static final int FACT_ANT_P1_CHARGE_SUCCESS = 203; //收款成功，码农一级佣金
    public static final int FACT_ANT_P2_CHARGE_SUCCESS = 204; // 收款成功, 码农二级佣金
    public static final int FACT_ANT_ANT_CHARGE_SUCCESS = 205; // 码农充值成功,码农上分
    // 商户记账
    public static final int FACT_MERCHANT_CHARGE_SUCCESS = 301;  // 商戶充值成功
    public static final int FACT_MERCHANT_MERCHANT_WITHDRAW = 302; // 商户体现
    public static final int FACT_MERCHANT_WITHDRAW_FAIL = 303;  // 商户体现失败
    // 卡主记账
    public static final int FACT_USER_COLLECT_ASSIGNED = 401; // 收款分配到卡主
    public static final int FACT_USER_COLLECT_TIMEOUT = 402; // 收款超时退回卡主
    public static final int FACT_USER_USER_CHARGE_SUCCESS = 403; // 代理模式卡主充值成功, 卡主上分
    public static final int FACT_USER_MERCHANT_WITHDRAW_SUCCESS = 404;
    // 通用调账
    public static final int FACT_ADJUST = 9999;
}
