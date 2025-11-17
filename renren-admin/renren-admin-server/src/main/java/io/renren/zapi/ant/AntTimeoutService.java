package io.renren.zapi.ant;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.renren.zapi.ledger.AntLedger;
import io.renren.zapi.ZooConstant;
import io.renren.zapi.event.ChargeTimeoutAntEvent;
import io.renren.zadmin.dao.ZAntLogDao;
import io.renren.zadmin.dao.ZChargeDao;
import io.renren.zadmin.entity.ZAntLogEntity;
import io.renren.zadmin.entity.ZChargeEntity;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;


import java.util.List;

@Service
@Slf4j
public class AntTimeoutService {

    @Resource
    private ZChargeDao zChargeDao;
    @Resource
    private TransactionTemplate tx;
    @Resource
    private AntLedger antLedger;
    @Resource
    private ZAntLogDao zAntLogDao;
    @Resource
    private AntAppService antAppService;

    /**
     * 超时退回资金处理
     */
    public void timeoutProcess(ZChargeEntity entity){
        if (!checkValid(entity)) return;
        // 可以退回卡主资金
        tx.executeWithoutResult(status -> {
            antLedger.antCollectTimeout(entity);
        });

    }

    /**
     * 卡在线  &&  卡的流水没有待匹配的
     * @param entity
     * @return
     */
    private boolean checkValid(ZChargeEntity entity) {
        //  交易不在处理中状态: 不合法
        if(entity.getProcessStatus() != ZooConstant.CHARGE_STATUS_PROCESSING) {
            return false;
        }

        // 卡不在线, 不合法
        Long deptId = entity.getDeptId();
        Long antId = entity.getAntId();
        Long cardId = entity.getUserCardId();
        if (!antAppService.isCardOnline(deptId, antId, cardId, 40 * 1000L)) {
            log.warn("卡不在线无法处理");
            return false;
        }

        // 有待匹配流水, 不合法
        List<ZAntLogEntity> unmatched = zAntLogDao.selectList(Wrappers.<ZAntLogEntity>lambdaQuery()
                .isNull(ZAntLogEntity::getChargeId)
                .eq(ZAntLogEntity::getCardId, cardId)
                .select(ZAntLogEntity::getId)
        );
        if (unmatched.size() > 0) {
            return false;
        }

        return true;
    }

    /**
     * 批处理: 整个流水都是一张卡的
     * @param entities
     */
    public void timeoutProcess(List<ZChargeEntity> entities) {
        if(entities.size() == 0) {
            return;
        }

        // 不合法不处理超时退款
        if(!checkValid(entities.get(0))) {
            return;
        }

        // 可以退回卡主资金
        for (ZChargeEntity entity : entities) {
            try {
                tx.executeWithoutResult(status -> {
                    antLedger.antCollectTimeout(entity);
                });
            } catch (Exception ex) {
            }
        }
    }

    @EventListener
    public void onChargeTimeout(ChargeTimeoutAntEvent event) {
        Long chargeId = event.getChargeId();
        ZChargeEntity zChargeEntity = zChargeDao.selectById(chargeId);
        timeoutProcess(zChargeEntity);
    }

}
