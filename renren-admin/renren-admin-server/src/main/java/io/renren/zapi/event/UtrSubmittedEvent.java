package io.renren.zapi.event;

import io.renren.zadmin.entity.ZChargeEntity;
import org.springframework.context.ApplicationEvent;

/**
 * 付款人提交UTR时间
 */
public class UtrSubmittedEvent extends ApplicationEvent {

    private String utr;
    private ZChargeEntity chargeEntity;

    public UtrSubmittedEvent(Object source, String utr, ZChargeEntity dept) {
        super(source);
        this.utr = utr;
        this.chargeEntity = dept;
    }

    public String getUtr() {
        return utr;
    }

    public ZChargeEntity getChargeEntity() {
        return chargeEntity;
    }
}
