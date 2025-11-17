package io.renren.zapi.agent;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.renren.zadmin.entity.ZAntLogEntity;
import io.renren.zapi.AlarmService;
import io.renren.zapi.ledger.ZLedger;
import io.renren.zapi.ZooConstant;
import io.renren.zapi.event.ChargeSuccessEvent;
import io.renren.zadmin.dao.ZChargeDao;
import io.renren.zadmin.dao.ZUserLogDao;
import io.renren.zadmin.entity.ZUserLogEntity;
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
public class AgentMatchService {
    @Resource
    private AlarmService alarmService;
    @Resource
    private AgentCardStat agentCardStat;
    @Resource
    private ZChargeDao zChargeDao;
    @Resource
    private ZUserLogDao zUserLogDao;
    @Resource
    private ZLedger ledger;
    @Resource
    private TransactionTemplate tx;
    @Resource
    private ApplicationEventPublisher publisher;

    public void match(List<ZUserLogEntity> fresh) {
        for (ZUserLogEntity entity : fresh) {
            if (entity.getFlag().equals("plus")) {
                matchCollect(entity);
                continue;
            }
            if (entity.getFlag().equals("minus")) {
                matchUserCharge(entity);
            }
        }
    }


    /**
     * 收款流水匹配卡主接单
     */
    public void matchCollect(ZUserLogEntity entity) {
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
            zUserLogDao.update(null, Wrappers.<ZUserLogEntity>lambdaUpdate()
                    .set(ZUserLogEntity::getFailCount, fcnt + 1)
                    .eq(ZUserLogEntity::getId, entity.getId())
            );
            return;
        }

        if (!matched.getAmount().equals(entity.getAmount())) {
            String msg = String.format("金额不匹配, utr:%s, 收款金额:%s, 银行金额:%s", entity.getUtr(), matched.getAmount(), entity.getAmount());
            zUserLogDao.update(null, Wrappers.<ZUserLogEntity>lambdaUpdate()
                    .eq(ZUserLogEntity::getId, entity.getId())
                    .set(ZUserLogEntity::getFailCount, 16)
                    .set(ZUserLogEntity::getMatchStatus, ZooConstant.MATCH_FAIL)
            );
            alarmService.warn(entity.getDeptId(), "匹配异常", msg);
            return;
        }

        matchSuccess(matched, entity);
    }

    /**
     * 匹配成功处理
     *
     * @param finalMatched
     * @param userLogEntity
     */
    public void matchSuccess(ZChargeEntity finalMatched, ZUserLogEntity userLogEntity) {
        // 匹配成功
        synchronized (ZooConstant.getMerchantLock(finalMatched.getMerchantId())) {
            tx.executeWithoutResult(status -> {
                // 1. 商户余额记账
                ledger.merchantChargeSuccess(finalMatched);

                // 2. 更新状态
                zChargeDao.update(Wrappers.<ZChargeEntity>lambdaUpdate()
                        .eq(ZChargeEntity::getId, finalMatched.getId())
                        .eq(ZChargeEntity::getProcessStatus, ZooConstant.CHARGE_STATUS_PROCESSING)
                        .set(ZChargeEntity::getProcessStatus, ZooConstant.CHARGE_STATUS_SUCCESS)
                        .set(ZChargeEntity::getMerchantFee, finalMatched.getMerchantFee())
                        .set(ZChargeEntity::getMerchantPrincipal, finalMatched.getMerchantPrincipal())
                );
            });
        }
        // 通知商户
        publisher.publishEvent(new ChargeSuccessEvent(this, finalMatched.getId()));
        agentCardStat.increaseCardSuccess(userLogEntity.getDeptId(), userLogEntity.getCardId());
    }

    public ZChargeEntity tnMatch(ZUserLogEntity entity) {
        try {
            ZChargeEntity zChargeEntity = zChargeDao.selectOne(Wrappers.<ZChargeEntity>lambdaQuery()
                    .eq(ZChargeEntity::getUserCardId, entity.getCardId())
                    .eq(ZChargeEntity::getTn, entity.getTn())
                    .eq(ZChargeEntity::getHandleMode, ZooConstant.CHARGE_STATUS_PROCESSING)
            );
            return zChargeEntity;
        } catch (Exception ex) {
            entity.setFailCount(100);
//            String msg = String.format("UTR[%s]匹配多笔收款[%d]", entity.getUtr(), zChargeEntities.size());
            return null;
        }
    }

    public ZChargeEntity utrMatch(ZUserLogEntity entity) {
        try {
            List<ZChargeEntity> zChargeEntities = zChargeDao.selectList(Wrappers.<ZChargeEntity>lambdaQuery()
                    .eq(ZChargeEntity::getUserCardId, entity.getCardId())
                    .eq(ZChargeEntity::getUtr, entity.getUtr())
                    .eq(ZChargeEntity::getHandleMode, ZooConstant.PROCESS_MODE_AGENT)
            );

            if (zChargeEntities.size() == 0) {
                return null;
            }
            if (zChargeEntities.size() == 1) {
                ZChargeEntity found = zChargeEntities.get(0);
                if (found.getProcessStatus().equals(ZooConstant.CHARGE_STATUS_PROCESSING)) {
                    return found;
                } else if (found.getProcessStatus().equals(ZooConstant.CHARGE_STATUS_SUCCESS)) {
                    zUserLogDao.update(null, Wrappers.<ZUserLogEntity>lambdaUpdate()
                            .set(ZUserLogEntity::getMatchStatus, ZooConstant.MATCH_SUCCESS)
                            .eq(ZUserLogEntity::getId, entity.getId()));
                    entity.setFailCount(100);
                    return null;
                }
            } else {
                String msg = String.format("UTR[%s]匹配多笔收款[%d]", entity.getUtr(), zChargeEntities.size());
                alarmService.warn(entity.getDeptId(), "匹配异常", msg);
                entity.setFailCount(16);
            }
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 付款流水匹配于卡主充值: no!!!
     */
    public void matchUserCharge(ZUserLogEntity entity) {
    }

    /**
     * 用户提交UTR产生的匹配
     *
     * @param zChargeEntity
     * @param utr
     */
    public void utrMatch(ZChargeEntity zChargeEntity, String utr) {
        ZUserLogEntity matchedLog = zUserLogDao.selectOne(Wrappers.<ZUserLogEntity>lambdaQuery()
                .eq(ZUserLogEntity::getUtr, utr)
                .eq(ZUserLogEntity::getCardId, zChargeEntity.getUserCardId())
        );

        if (matchedLog == null) {
            // todo: socket
            // SocketAdminUtil.sendMessage();
            return;
        }

        // 匹配成功
        if (matchedLog.getChargeId() == null) {
            matchSuccess(zChargeEntity, matchedLog);
        }
    }

    public void matchTask() {
        // 扫描3小时之前到现在的
        Date date = DateUtils.addHours(new Date(), -3);
        List<ZUserLogEntity> zUserLogEntities = zUserLogDao.selectList(Wrappers.<ZUserLogEntity>lambdaQuery()
                .in(ZUserLogEntity::getMatchStatus, List.of(ZooConstant.MATCH_TODO, ZooConstant.MATCH_FAIL))
                .gt(ZUserLogEntity::getCreateDate, date)
                .eq(ZUserLogEntity::getFlag, "plus")
                .lt(ZUserLogEntity::getFailCount, 16)
        );
        if (zUserLogEntities.size() == 0) {
            return;
        }
        log.info("match z_user_log: {}", zUserLogEntities.size());
        match(zUserLogEntities);
    }
}
