package io.renren.zapi.event;

import org.springframework.context.ApplicationEvent;

/**
 * 代付完成事件: 可能付款成功， 也可能付款失败
 */
public class WithdrawCompleteEvent extends ApplicationEvent {
    private Long withdrawId;

    public WithdrawCompleteEvent(Object source, Long withdrawId) {
        super(source);
        this.withdrawId = withdrawId;
    }

    public Long getWithdrawId() {
        return withdrawId;
    }
}
