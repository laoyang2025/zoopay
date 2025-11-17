package io.renren.zapi.ledger;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.renren.commons.tools.exception.RenException;
import io.renren.dao.SysUserDao;
import io.renren.zadmin.dao.ZBalanceDao;
import io.renren.zadmin.dao.ZChargeDao;
import io.renren.zadmin.dao.ZLogDao;
import io.renren.zadmin.entity.ZBalanceEntity;
import io.renren.zadmin.entity.ZLogEntity;
import io.renren.zapi.route.RouteService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;

@Service
public class LedgerUtil {
    @Resource
    private ZLogDao zLogDao;
    @Resource
    private ZBalanceDao zBalanceDao;

    private ZLogEntity getLogEntity(ZBalanceEntity balance, int factType, Long factId, String factMemo, BigDecimal factAmount) {
        ZLogEntity entity = new ZLogEntity();
        entity.setDeptId(balance.getDeptId());
        entity.setDeptName(balance.getDeptName());
        entity.setBalanceId(balance.getId());
        entity.setFactAmount(factAmount);
        entity.setFactMemo(factMemo);
        entity.setFactId(factId);
        entity.setFactType(factType);
        entity.setOwnerId(balance.getOwnerId());
        entity.setOwnerName(balance.getOwnerName());
        entity.setOwnerType(balance.getOwnerType());
        entity.setOldBalance(balance.getBalance());
        entity.setNewBalance(balance.getBalance().add(factAmount));

        Long newVersion = balance.getVersion() + 1L;
        entity.setMutation(balance.getVersion() + " -> " + newVersion);
        return entity;
    }

    public LambdaUpdateWrapper<ZBalanceEntity> ledge(ZBalanceEntity balance, int factType, Long factId, String factMemo, BigDecimal factAmount) {
        ZLogEntity logEntity = getLogEntity(balance, factType, factId, factMemo, factAmount);
        zLogDao.insert(logEntity);
        LambdaUpdateWrapper<ZBalanceEntity> wrapper = Wrappers.<ZBalanceEntity>lambdaUpdate()
                .eq(ZBalanceEntity::getId, balance.getId())
                .eq(ZBalanceEntity::getVersion, balance.getVersion())
                .set(ZBalanceEntity::getVersion, balance.getVersion() + 1)
                .set(ZBalanceEntity::getBalance, logEntity.getNewBalance());
        return wrapper;
    }

    public void ledgeUpdate(ZBalanceEntity balance, int factType, Long factId, String factMemo, BigDecimal factAmount) {
        int update = zBalanceDao.update(null, this.ledge(balance, factType, factId, factMemo, factAmount));
        if (update != 1) {
            throw new RenException("ledge failed, factType:" + factType + ",factId:" + factId);
        }
    }

    public ZBalanceEntity getAccountByOwner(Long deptId, String ownerType, Long ownerId) {
        return zBalanceDao.selectOne(Wrappers.<ZBalanceEntity>lambdaQuery()
                .eq(ZBalanceEntity::getDeptId, deptId)
                .eq(ZBalanceEntity::getOwnerType, ownerType)
                .eq(ZBalanceEntity::getOwnerId, ownerId)
        );
    }

    public ZBalanceEntity getAccount(Long deptId, String ownerType, Long id) {
        ZBalanceEntity zBalanceEntity = zBalanceDao.selectById(id);
        if (!zBalanceEntity.getDeptId().equals(deptId)) {
            throw new RenException("deptId does not match");
        }
        if (!zBalanceEntity.getOwnerType().equals(ownerType)) {
            throw new RenException("ownerType does not match, db:" + zBalanceEntity.getOwnerType() + ", provide:" + ownerType);
        }
        return zBalanceEntity;
    }
}
