package io.renren.zapi.ledger;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.renren.commons.tools.exception.RenException;
import io.renren.entity.SysUserEntity;
import io.renren.zadmin.dao.ZBalanceDao;
import io.renren.zadmin.dao.ZChargeDao;
import io.renren.zadmin.entity.ZAntChargeEntity;
import io.renren.zadmin.entity.ZBalanceEntity;
import io.renren.zadmin.entity.ZChargeEntity;
import io.renren.zadmin.entity.ZWithdrawEntity;
import io.renren.zapi.ZooConstant;
import io.renren.zapi.route.RouteService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Slf4j
public class AntLedger {

    @Resource
    private LedgerUtil ledgerUtil;
    @Resource
    private ZBalanceDao zBalanceDao;
    @Resource
    private RouteService routeService;
    @Resource
    private ZChargeDao zChargeDao;
    @Resource
    private TransactionTemplate tx;


    /**
     * 码农跑分模式下,收款成功:  上两级佣金记账
     * @param entity
     */
    public void antMerchantChargeSuccess(ZChargeEntity entity) {
        BigDecimal p1Rate = entity.getAntP1Rate();
        BigDecimal p2Rate = entity.getAntP2Rate();
        ZBalanceEntity accountP1 = ledgerUtil.getAccount(entity.getDeptId(), ZooConstant.OWNER_TYPE_ANT, entity.getAntP1Id());
        ZBalanceEntity accountP2 = ledgerUtil.getAccount(entity.getDeptId(), ZooConstant.OWNER_TYPE_ANT, entity.getAntP2Id());

        BigDecimal p1Fee = p1Rate.multiply(entity.getRealAmount()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal p2Fee = p2Rate.multiply(entity.getRealAmount()).setScale(2, RoundingMode.HALF_UP);

        String factMemoP1 = String.format("charge id[%d] amount[%s], level-1 commission[%s]",
                entity.getId(), entity.getRealAmount(), p1Fee
        );
        String factMemoP2 = String.format("charge id[%d] amount[%s], level-2 commission[%s]",
                entity.getId(), entity.getRealAmount(), p2Fee
        );

        int update = zBalanceDao.update(
                ledgerUtil.ledge(accountP1, LedgerConstant.FACT_ANT_P1_CHARGE_SUCCESS, entity.getId(), factMemoP1, p1Fee)
        );
        if (update != 1) {
            log.error("ledge error: merchantChargeSuccessAnt-1: {}", entity.getId());
            throw new RenException("merchantChargeSuccessAnt failed");
        }

        update = zBalanceDao.update(
                ledgerUtil.ledge(accountP2, LedgerConstant.FACT_ANT_P2_CHARGE_SUCCESS, entity.getId(), factMemoP2, p2Fee)
        );
        if (update != 1) {
            log.error("ledge error: merchantChargeSuccessAnt-2: {}", entity.getId());
            throw new RenException("merchantChargeSuccessAnt failed");
        }
    }


    /**
     * 码农接单分配成功
     * @param entity
     */
    public void antCollectAssigned(ZChargeEntity entity) {
        ZBalanceEntity account = ledgerUtil.getAccount(entity.getDeptId(), ZooConstant.OWNER_TYPE_ANT, entity.getUserId());

        String factMemo = String.format("charge id[%d] amount[%s] timeout refund", entity.getId(), entity.getRealAmount());
        BigDecimal factAmount = entity.getRealAmount().negate();
        LambdaUpdateWrapper<ZBalanceEntity> ledge = ledgerUtil.ledge(account, LedgerConstant.FACT_ANT_COLLECT_ASSIGNED, entity.getId(), factMemo, factAmount);

        int update = zBalanceDao.update(null, ledge);
        if (update != 1) {
            throw new RenException("antCollectAssigned failed");
        }
    }


    /**
     * 收款交易超时, 退回码农金额
     * @param entity
     */
    public void antCollectTimeout(ZChargeEntity entity) {
        ZBalanceEntity account = ledgerUtil.getAccount(entity.getDeptId(), ZooConstant.OWNER_TYPE_ANT, entity.getAntId());
        String factMemo = String.format("charge id[%d] amount[%s] timeout refund", entity.getId(), entity.getRealAmount());
        BigDecimal factAmount = entity.getRealAmount();
        LambdaUpdateWrapper<ZBalanceEntity> ledge = ledgerUtil.ledge(account, LedgerConstant.FACT_ANT_COLLECT_TIMEOUT, entity.getId(), factMemo, factAmount);
        int update = zBalanceDao.update(null, ledge);
        if (update != 1) {
            throw new RenException("antCollectTimeout failed");
        }
    }


    /**
     * 代付成功商户记账: 码农模式下码农充值抢单, 需要给码农上分
     */
    public void antMerchantWithdrawSuccess(ZWithdrawEntity entity) {
        // 码农账户
        ZBalanceEntity balanceEntity = ledgerUtil.getAccount(entity.getDeptId(), ZooConstant.OWNER_TYPE_ANT, entity.getAntId());

        String factMemo = String.format("merchant Withdraw ant charge[%s] success", entity.getAmount());
        BigDecimal factAmount = entity.getAmount();

        // 相当与充值
        LambdaUpdateWrapper<ZBalanceEntity> ledge = ledgerUtil.ledge(balanceEntity, LedgerConstant.FACT_ANT_ANT_CHARGE_SUCCESS, entity.getId(), factMemo, factAmount);
        int update = zBalanceDao.update(ledge);
        if (update != 1) {
            throw new RenException("antMerchantWithdrawSuccess failed");
        }

    }

    /**
     * 码农模式下: 码农充值成功
     *
     * @param entity
     */
    public void antAntChargeSuccess(ZAntChargeEntity entity) {
        ZBalanceEntity balanceEntity = ledgerUtil.getAccount(entity.getDeptId(), ZooConstant.OWNER_TYPE_ANT, entity.getId());

        String factMemo = String.format("user charge[%s] success", entity.getAmount());
        BigDecimal factAmount = entity.getAmount();

        LambdaUpdateWrapper<ZBalanceEntity> ledge = ledgerUtil.ledge(balanceEntity, LedgerConstant.FACT_ANT_ANT_CHARGE_SUCCESS, entity.getId(), factMemo, factAmount);
        int update = zBalanceDao.update(ledge);
        if (update != 1) {
            throw new RenException("antAntChargeSuccess failed");
        }
    }

}
