package io.renren.zapi.card.fill;


import io.renren.entity.SysDeptEntity;
import io.renren.entity.SysUserEntity;
import io.renren.zadmin.entity.ZCardEntity;
import io.renren.zadmin.entity.ZChargeEntity;

import java.util.Map;

public interface CardLandingFill {
    /**
     * @param map:   待填充的map
     * @param zCardEntity:  选中的卡
     * @param chargeEntity: 收款交易
     * @param deptEntity: 机构
     * @param merchant: 商户
     */
    void fill(Map<String, Object> map, ZCardEntity zCardEntity, ZChargeEntity chargeEntity, SysDeptEntity deptEntity, SysUserEntity merchant);
}
