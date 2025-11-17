package io.renren.zapi;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.renren.commons.tools.utils.ConvertUtils;
import io.renren.zadmin.dao.*;
import io.renren.zadmin.entity.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TransferHistoryService {

    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private ZChargeDao zChargeDao;
    @Resource
    private ZChargeHisDao zChargeHisDao;
    @Resource
    private ZLogDao zLogDao;
    @Resource
    private ZLogHisDao zLogHisDao;
    @Resource
    private ZWithdrawDao zWithdrawDao;
    @Resource
    private ZWithdrawHisDao zWithdrawHisDao;
    @Resource
    private ZCardLogHisDao zCardLogHisDao;
    @Resource
    private ZCardLogDao zCardLogDao;

    private Date getTimeAgo() {
        Date currentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DAY_OF_MONTH, -35);
        Date ago = calendar.getTime();
        return ago;
    }


    public void transferCharge(Date ago) {
        // transfer collect
        try {
            transactionTemplate.executeWithoutResult(status -> {
                List<ZChargeEntity> entities = zChargeDao.selectList(Wrappers.<ZChargeEntity>lambdaQuery()
                        .le(ZChargeEntity::getCreateDate, ago)
                        .last("limit 500")
                );
                if (entities.size() == 0) {
                    return;
                }
                List<ZChargeHisEntity> hisEntities = ConvertUtils.sourceToTarget(entities, ZChargeHisEntity.class);
                zChargeDao.deleteByIds(entities.stream().map(e -> e.getId()).collect(Collectors.toList()));
                zChargeHisDao.insert(hisEntities, 500);
                log.debug("transfer {} charges", hisEntities.size());
            });
        } catch (Exception e) {
            log.warn("clean collect failed");
            e.printStackTrace();
        }
    }

    public void transferLog(Date ago) {
        try {
            transactionTemplate.executeWithoutResult(status -> {
                List<ZLogEntity> entities = zLogDao.selectList(Wrappers.<ZLogEntity>lambdaQuery()
                        .le(ZLogEntity::getCreateDate, ago)
                        .last("limit 500")
                );
                if (entities.size() == 0) {
                    return;
                }
                List<ZLogHisEntity> hisEntities = ConvertUtils.sourceToTarget(entities, ZLogHisEntity.class);
                zLogDao.deleteByIds(entities.stream().map(e -> e.getId()).collect(Collectors.toList()));
                zLogHisDao.insert(hisEntities, 500);
                log.debug("transfer {} balance log", hisEntities.size());
            });
        } catch (Exception e) {
            log.warn("clean collect failed");
            e.printStackTrace();
        }
    }

    // 转移代付流水
    public void transferWithdraw(Date ago) {
        try {
            transactionTemplate.executeWithoutResult(status -> {
                List<ZWithdrawEntity> entities = zWithdrawDao.selectList(Wrappers.<ZWithdrawEntity>lambdaQuery()
                        .le(ZWithdrawEntity::getCreateDate, ago)
                        .last("limit 500")
                );
                if (entities.size() == 0) {
                    return;
                }
                List<ZWithdrawHisEntity> hisEntities = ConvertUtils.sourceToTarget(entities, ZWithdrawHisEntity.class);
                zWithdrawDao.deleteByIds(entities.stream().map(e -> e.getId()).collect(Collectors.toList()));
                zWithdrawHisDao.insert(hisEntities, 500);
                log.debug("transfer {} withdraw", hisEntities.size());
            });
        } catch (Exception e) {
            log.warn("clean withdraw failed");
            e.printStackTrace();
        }
    }

    // 银行流水
    public void transferCardLog(Date ago) {
        try {
            transactionTemplate.executeWithoutResult(status -> {
                List<ZCardLogEntity> entities = zCardLogDao.selectList(Wrappers.<ZCardLogEntity>lambdaQuery()
                        .le(ZCardLogEntity::getCreateDate, ago)
                        .last("limit 500")
                );
                if (entities.size() == 0) {
                    return;
                }
                List<ZCardLogHisEntity> hisEntities = ConvertUtils.sourceToTarget(entities, ZCardLogHisEntity.class);
                zCardLogDao.deleteByIds(entities.stream().map(e -> e.getId()).collect(Collectors.toList()));
                zCardLogHisDao.insert(hisEntities, 500);
                log.debug("transfer {} card log", hisEntities.size());
            });
        } catch (Exception e) {
            log.warn("clean collect failed");
            e.printStackTrace();
        }
    }

    public void transfer() {
        Date timeAgo = getTimeAgo();
        long t1 = System.currentTimeMillis();

        // 转移收款流水
        transferCharge(timeAgo);
        long elapse = System.currentTimeMillis() - t1;
        if (elapse > 500) {
            return;
        }

        // 转移付款流水
        transferWithdraw(timeAgo);
        elapse = System.currentTimeMillis() - t1;
        if (elapse > 500) {
            return;
        }

        // 转移银行流水
        transferCardLog(timeAgo);
        elapse = System.currentTimeMillis() - t1;
        if (elapse > 500) {
            return;
        }

        // 转移记账流水
        transferLog(timeAgo);
        elapse = System.currentTimeMillis() - t1;
        if (elapse > 500) {
            return;
        }

    }

}

