package io.renren.zapi.ant;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.renren.zadmin.entity.ZCardLogEntity;
import io.renren.zapi.AlarmService;
import io.renren.zapi.ledger.ZLedger;
import io.renren.zapi.ZooConstant;
import io.renren.zapi.event.ChargeSuccessEvent;
import io.renren.zadmin.dao.ZAntLogDao;
import io.renren.zadmin.dao.ZChargeDao;
import io.renren.zadmin.entity.ZAntLogEntity;
import io.renren.zadmin.entity.ZChargeEntity;
import io.renren.zsocket.SocketAdmin;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;
import java.util.List;


@Service
@Slf4j
public class AntMatchService {
    @Resource
    private AlarmService alarmService;
    @Resource
    private ZChargeDao zChargeDao;
    @Resource
    private ZAntLogDao zAntLogDao;
    @Resource
    private ZLedger ledger;
    @Resource
    private TransactionTemplate tx;
    @Resource
    private ApplicationEventPublisher publisher;
    @Resource
    private AntCardStat antCardStat;

    public void match(List<ZAntLogEntity> fresh) {
        for (ZAntLogEntity entity : fresh) {
            if (entity.getFlag().equals("plus")) {
                matchCollect(entity);
                continue;
            }
            if (entity.getFlag().equals("minus")) {
                matchAntCharge(entity);
            }
        }
    }

    /**
     * 收款流水匹配码农接单
     */
    public void matchCollect(ZAntLogEntity entity) {
        //
        ZChargeEntity matched = null;
        // tn匹配
        if (entity.getTn() != null) {
            matched = tnMatch(entity);
        }
        if (matched == null) {
            matched = utrMatch(entity);
        }
        if (matched == null) {
            int fcnt = entity.getFailCount() == null ? 0 : entity.getFailCount();
            if (fcnt == 100) {
                return;
            }
            zAntLogDao.update(null, Wrappers.<ZAntLogEntity>lambdaUpdate()
                    .set(ZAntLogEntity::getFailCount, fcnt + 1)
                    .set(ZAntLogEntity::getMatchStatus, ZooConstant.MATCH_FAIL)
                    .eq(ZAntLogEntity::getId, entity.getId())
            );
            return;
        }

        if (!matched.getAmount().equals(entity.getAmount())) {
            String msg = String.format("金额不匹配, utr:%s, 收款金额:%s, 银行金额:%s", entity.getUtr(), matched.getAmount(), entity.getAmount());
            zAntLogDao.update(null, Wrappers.<ZAntLogEntity>lambdaUpdate()
                    .eq(ZAntLogEntity::getId, entity.getId())
                    .set(ZAntLogEntity::getFailCount, 16)
                    .set(ZAntLogEntity::getMatchStatus, ZooConstant.MATCH_FAIL)
            );
            alarmService.warn(entity.getDeptId(), "匹配异常", msg);
            return;

        }
        matchSuccess(matched, entity);
    }

    private void matchSuccess(ZChargeEntity matched, ZAntLogEntity entity) {
        // 匹配成功
        final ZChargeEntity finalMatched = matched;

        // 锁住商户
        synchronized (ZooConstant.getMerchantLock(finalMatched.getMerchantId())) {
            tx.executeWithoutResult(status -> {
                // 2. 商户余额记账
                ledger.merchantChargeSuccess(finalMatched);

                // 1. 更新状态
                zChargeDao.update(Wrappers.<ZChargeEntity>lambdaUpdate()
                        .eq(ZChargeEntity::getId, finalMatched.getId())
                        .set(ZChargeEntity::getProcessStatus, ZooConstant.CHARGE_STATUS_SUCCESS)
                        .eq(ZChargeEntity::getProcessStatus, ZooConstant.CHARGE_STATUS_PROCESSING)
                        .set(ZChargeEntity::getMerchantFee, finalMatched.getMerchantFee())
                        .set(ZChargeEntity::getMerchantPrincipal, finalMatched.getMerchantPrincipal())
                );
            });
        }

        // 通知商户
        publisher.publishEvent(new ChargeSuccessEvent(this, finalMatched.getId()));

        // 统计卡的成功笔数
        antCardStat.increaseCardSuccess(entity.getDeptId(), entity.getCardId());
    }

    /**
     * tn方式匹配
     *
     * @param entity
     * @return
     */
    public ZChargeEntity tnMatch(ZAntLogEntity entity) {
        try {
            ZChargeEntity zChargeEntity = zChargeDao.selectOne(Wrappers.<ZChargeEntity>lambdaQuery()
                    .eq(ZChargeEntity::getAntCardId, entity.getCardId())
                    .eq(ZChargeEntity::getTn, entity.getTn())
                    .eq(ZChargeEntity::getHandleMode, ZooConstant.CHARGE_STATUS_PROCESSING)
            );
            return zChargeEntity;
        } catch (Exception ex) {
            log.error("tn匹配多条");
            entity.setFailCount(16);
            return null;
        }
    }

    /**
     * 银行上报流水触发的utr方式匹配
     *
     * @param entity
     * @return
     */
    public ZChargeEntity utrMatch(ZAntLogEntity entity) {
        try {
            List<ZChargeEntity> zChargeEntities = zChargeDao.selectList(Wrappers.<ZChargeEntity>lambdaQuery()
                    .eq(ZChargeEntity::getAntCardId, entity.getCardId())
                    .eq(ZChargeEntity::getUtr, entity.getUtr())
                    .eq(ZChargeEntity::getHandleMode, ZooConstant.PROCESS_MODE_ANT)
            );
            if (zChargeEntities.size() == 0) {
                return null;
            }
            if (zChargeEntities.size() == 1) {
                ZChargeEntity found = zChargeEntities.get(0);
                if (found.getProcessStatus().equals(ZooConstant.CHARGE_STATUS_PROCESSING)) {
                    return found;
                }
                else if (found.getProcessStatus().equals(ZooConstant.CHARGE_STATUS_SUCCESS)) {
                    zAntLogDao.update(null, Wrappers.<ZAntLogEntity>lambdaUpdate()
                            .set(ZAntLogEntity::getMatchStatus, ZooConstant.MATCH_SUCCESS)
                            .eq(ZAntLogEntity::getId, entity.getId()));
                    entity.setFailCount(100);
                    return null;
                }
                return null;
            } else {
                String msg = String.format("UTR[%s]匹配多笔收款[%d]", entity.getUtr(), zChargeEntities.size());
                log.error(msg);
                alarmService.warn(entity.getDeptId(), "匹配异常", msg);
                entity.setFailCount(16);
                return null;
            }
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 付款流水匹配于卡主充值
     */
    public void matchAntCharge(ZAntLogEntity entity) {
    }

    /**
     * 用户提交UTR触发的匹配
     *
     * @param zChargeEntity
     * @param utr
     */
    public void utrMatch(ZChargeEntity zChargeEntity, String utr) {
        ZAntLogEntity matchedLog = zAntLogDao.selectOne(Wrappers.<ZAntLogEntity>lambdaQuery()
                .eq(ZAntLogEntity::getUtr, utr)
                .eq(ZAntLogEntity::getCardId, zChargeEntity.getAntCardId())
        );

        // 匹配成功
        if (matchedLog != null && matchedLog.getChargeId() == null) {
            matchSuccess(zChargeEntity, matchedLog);
        }
    }

    public void matchTask() {
        // 扫描3小时之前到现在的
        Date date = DateUtils.addHours(new Date(), -3);
        List<ZAntLogEntity> zAntLogEntities = zAntLogDao.selectList(Wrappers.<ZAntLogEntity>lambdaQuery()
                .in(ZAntLogEntity::getMatchStatus, List.of(ZooConstant.MATCH_TODO, ZooConstant.MATCH_FAIL))
                .gt(ZAntLogEntity::getCreateDate, date)
                .eq(ZAntLogEntity::getFlag, "plus")
                .lt(ZAntLogEntity::getFailCount, 16)
        );
        if (zAntLogEntities.size() == 0) {
            return;
        }
        log.info("match z_ant_log: {}", zAntLogEntities.size());
        match(zAntLogEntities);
    }
}
