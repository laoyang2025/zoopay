package io.renren.zapi.event;


import org.springframework.context.ApplicationEvent;

/**
 *  码农模式充值超时
 */
public class ChargeTimeoutAntEvent extends ApplicationEvent {
    private final Long chargeId;

    public ChargeTimeoutAntEvent(Object source, Long id) {
        super(source);
        this.chargeId = id;
    }

    public Long getChargeId() {
        return chargeId;
    }
}
