package io.renren.zapi.event;

import org.springframework.context.ApplicationEvent;


/**
 * 收款成功
 */
public class ChargeSuccessEvent extends ApplicationEvent {
    private Long chargeId;

    public ChargeSuccessEvent(Object source, Long chargeId) {
        super(source);
        this.chargeId = chargeId;
    }

    public Long getChargeId() {
        return chargeId;
    }
}
