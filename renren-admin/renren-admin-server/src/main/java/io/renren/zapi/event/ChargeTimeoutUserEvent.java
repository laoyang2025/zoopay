package io.renren.zapi.event;


import org.springframework.context.ApplicationEvent;

/**
 * 代理模式: 卡主接单超时
 */
public class ChargeTimeoutUserEvent extends ApplicationEvent {
    private final Long chargeId;

    public ChargeTimeoutUserEvent(Object source, Long id) {
        super(source);
        this.chargeId = id;
    }

    public Long getChargeId() {
        return chargeId;
    }
}
