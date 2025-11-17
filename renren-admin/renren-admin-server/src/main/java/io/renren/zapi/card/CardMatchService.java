package io.renren.zapi.card;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.renren.zadmin.dao.ZCardDao;
import io.renren.zadmin.dao.ZCardLogDao;
import io.renren.zadmin.dao.ZChargeDao;
import io.renren.zadmin.entity.ZCardLogEntity;
import io.renren.zadmin.entity.ZChargeEntity;
import io.renren.zapi.AlarmService;
import io.renren.zapi.ledger.ZLedger;
import io.renren.zapi.ZooConstant;
import io.renren.zapi.event.ChargeSuccessEvent;
import io.renren.zsocket.SocketAdmin;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class CardMatchService {
    @Resource
    private AlarmService alarmService;
    @Resource
    private ZChargeDao zChargeDao;
    @Resource
    private ZCardLogDao zCardLogDao;
    @Resource
    private ZLedger ledger;
    @Resource
    private TransactionTemplate tx;
    @Resource
    private ZCardDao zCardDao;
    @Resource
    private ApplicationEventPublisher publisher;

    public void match(List<ZCardLogEntity> fresh) {
        for (ZCardLogEntity entity : fresh) {
            if (entity.getFlag().equals("plus")) {
                log.debug("match collect:{}", entity);
                matchCollect(entity);
                continue;
            }
            // 代付匹配: 暂时没有
            if (entity.getFlag().equals("minus")) {
            }
        }
    }

    /**
     * 银行流水 -->  z_charge
     */
    public void matchCollect(ZCardLogEntity entity) {
        ZChargeEntity matched = null;
        // tn匹配
        if (entity.getTn() != null) {
            matched = tnMatch(entity);
        }
        // utr匹配
        if (matched == null) {
            matched = utrMatch(entity);
        }
        // 没有匹配上
        if (matched == null) {
            int fcnt = entity.getFailCount() == null ? 0 : entity.getFailCount();
            if (fcnt == 100) {
                return;
            }
            if (fcnt > 16) {
                fcnt = 16;
            }
            zCardLogDao.update(null, Wrappers.<ZCardLogEntity>lambdaUpdate()
                    .eq(ZCardLogEntity::getId, entity.getId())
                    .set(ZCardLogEntity::getFailCount, fcnt + 1)
                    .set(ZCardLogEntity::getMatchStatus, ZooConstant.MATCH_FAIL)
            );
            return;
        }

        BigDecimal orderAmount = matched.getRealAmount();
        BigDecimal bankAmount = entity.getAmount();

        // 金额匹配
        if (bankAmount.subtract(orderAmount).compareTo(BigDecimal.ZERO) == 0) {
            log.info("匹配成功2: {}-{}-{}-{}",
                    matched.getOrderId(),
                    matched.getRealAmount(),
                    entity.getAmount(),
                    matched.getUtr()
            );
            matchSuccess(matched, entity);
            return;
        }

        String msg = String.format("金额不匹配, utr:%s, 收款金额:%s, 银行金额:%s", entity.getUtr(), matched.getAmount(), entity.getAmount());
        zCardLogDao.update(null, Wrappers.<ZCardLogEntity>lambdaUpdate()
                .eq(ZCardLogEntity::getId, entity.getId())
                .set(ZCardLogEntity::getFailCount, 16)
                .set(ZCardLogEntity::getMatchStatus, ZooConstant.MATCH_FAIL)
        );
        alarmService.warn(entity.getDeptId(), "匹配异常", msg);
        return;
    }

    private void matchSuccess(ZChargeEntity finalMatched, ZCardLogEntity cardLogEntity) {
        // 锁住商户 + 事务
        synchronized (ZooConstant.getMerchantLock(finalMatched.getMerchantId())) {
            tx.executeWithoutResult(status -> {
                // 3. 商户余额记账
                ledger.merchantChargeSuccess(finalMatched);

                // 1. 更新状态
                int update = zChargeDao.update(Wrappers.<ZChargeEntity>lambdaUpdate()
                        .eq(ZChargeEntity::getId, finalMatched.getId())
                        .eq(ZChargeEntity::getProcessStatus, ZooConstant.CHARGE_STATUS_PROCESSING)
                        .set(ZChargeEntity::getProcessStatus, ZooConstant.CHARGE_STATUS_SUCCESS)
                        .set(ZChargeEntity::getMerchantFee, finalMatched.getMerchantFee())
                        .set(ZChargeEntity::getMerchantPrincipal, finalMatched.getMerchantPrincipal())
                );
                if (update != 1) {
                    status.setRollbackOnly();
                    return;
                }

                // 2. 更新銀行流水
                update = zCardLogDao.update(null, Wrappers.<ZCardLogEntity>lambdaUpdate()
                        .eq(ZCardLogEntity::getId, cardLogEntity.getId())
                        .set(ZCardLogEntity::getMatchStatus, ZooConstant.MATCH_SUCCESS)
                        .set(ZCardLogEntity::getChargeId, finalMatched.getId())
                );
                if (update != 1) {
                    status.setRollbackOnly();
                    return;
                }
            });
        }
        // 通知商户
        publisher.publishEvent(new ChargeSuccessEvent(this, finalMatched.getId()));
    }

    /**
     * tn是我们系统提供， 会出现再银行流水里， 自动匹配
     *
     * @param entity
     * @return
     */
    public ZChargeEntity tnMatch(ZCardLogEntity entity) {
        try {
            ZChargeEntity zChargeEntity = zChargeDao.selectOne(Wrappers.<ZChargeEntity>lambdaQuery()
                    .eq(ZChargeEntity::getCardId, entity.getCardId())
                    .eq(ZChargeEntity::getTn, entity.getTn())
                    .eq(ZChargeEntity::getProcessStatus, ZooConstant.CHARGE_STATUS_PROCESSING)
            );
            return zChargeEntity;
        } catch (Exception ex) {
            log.error("TN匹配多条");
            return null;
        }
    }

    /**
     * utr是玩家提供， 然后再和银行流水匹配
     *
     * @param entity
     * @return
     */
    public ZChargeEntity utrMatch(ZCardLogEntity entity) {
        try {
            List<ZChargeEntity> zChargeEntities = zChargeDao.selectList(Wrappers.<ZChargeEntity>lambdaQuery()
                    .eq(ZChargeEntity::getCardId, entity.getCardId())
                    .eq(ZChargeEntity::getUtr, entity.getUtr())
                    .eq(ZChargeEntity::getHandleMode, ZooConstant.PROCESS_MODE_CARD)
            );

            if (zChargeEntities.size() == 0) {
                return null;
            }

            if (zChargeEntities.size() == 1) {
                ZChargeEntity found = zChargeEntities.get(0);
                if (found.getProcessStatus().equals(ZooConstant.CHARGE_STATUS_PROCESSING)) {
                    return found;
                } else if (found.getProcessStatus().equals(ZooConstant.CHARGE_STATUS_SUCCESS)) {
                    zCardLogDao.update(null, Wrappers.<ZCardLogEntity>lambdaUpdate()
                            .set(ZCardLogEntity::getMatchStatus, ZooConstant.MATCH_SUCCESS)
                            .eq(ZCardLogEntity::getId, entity.getId()));
                    entity.setFailCount(100);
                    return null;
                }
            } else {
                String msg = String.format("UTR[%s]匹配多笔收款[%d]", entity.getUtr(), zChargeEntities.size());
                log.warn(msg);
                alarmService.warn(entity.getDeptId(), "匹配异常", msg);
                entity.setFailCount(16);
            }
            return null;
        } catch (Exception ex) {
            return null;
        }
    }


    /**
     * 用户提交UTR产生的匹配
     *
     * @param zChargeEntity
     * @param utr
     */
    public void utrMatch(ZChargeEntity zChargeEntity, String utr) {
        ZCardLogEntity matchedLog = zCardLogDao.selectOne(Wrappers.<ZCardLogEntity>lambdaQuery()
                .eq(ZCardLogEntity::getUtr, utr)
                .eq(ZCardLogEntity::getCardId, zChargeEntity.getCardId())
        );

        // 找到银行流水， 且找的银行流水， 没有被匹配过.
        if (matchedLog != null && matchedLog.getChargeId() == null) {

            BigDecimal orderAmount = zChargeEntity.getRealAmount();
            BigDecimal bankAmount = matchedLog.getAmount();

            // 金额匹配
            if (bankAmount.subtract(orderAmount).compareTo(BigDecimal.ZERO) == 0) {
                log.info("匹配成功1: {}-{}-{}-{}", zChargeEntity.getOrderId(), zChargeEntity.getRealAmount(), matchedLog.getAmount(), utr);
                matchSuccess(zChargeEntity, matchedLog);
                return;
            }

            String msg = String.format("金额不匹配, utr:%s, 收款金额:%s, 银行金额:%s", zChargeEntity.getUtr(), zChargeEntity.getAmount(), matchedLog.getAmount());
            zCardLogDao.update(null, Wrappers.<ZCardLogEntity>lambdaUpdate()
                    .eq(ZCardLogEntity::getId, matchedLog.getId())
                    .set(ZCardLogEntity::getFailCount, 16)
                    .set(ZCardLogEntity::getMatchStatus, ZooConstant.MATCH_FAIL)
            );
            alarmService.warn(zChargeEntity.getDeptId(), "匹配异常", msg);
            return;

        }
    }

    /**
     * 定时任务调用
     */
    public void matchTask() {
        // 扫描3小时之前到现在的流水(收款流水 + 状态是待匹配|匹配失败 + 失败次数<16)
        Date date = DateUtils.addHours(new Date(), -3);
        List<ZCardLogEntity> zCardLogEntities = zCardLogDao.selectList(Wrappers.<ZCardLogEntity>lambdaQuery()
                .in(ZCardLogEntity::getMatchStatus, List.of(ZooConstant.MATCH_TODO, ZooConstant.MATCH_FAIL))
                .gt(ZCardLogEntity::getCreateDate, date)
                .eq(ZCardLogEntity::getFlag, "plus")
                .lt(ZCardLogEntity::getFailCount, 16)
        );
        if (zCardLogEntities.size() == 0) {
            return;
        }
        log.debug("match z_card_log: {}", zCardLogEntities.size());
        match(zCardLogEntities);
    }

}
