package io.renren.zapi.ledger;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.renren.commons.tools.exception.RenException;
import io.renren.dao.SysUserDao;
import io.renren.entity.SysUserEntity;
import io.renren.zadmin.dao.ZBalanceDao;
import io.renren.zadmin.dao.ZChargeDao;
import io.renren.zadmin.dao.ZLogDao;
import io.renren.zadmin.entity.*;
import io.renren.zapi.TransferHistoryService;
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
public class ZLedger {

    @Resource
    private LedgerUtil ledgerUtil;
    @Resource
    private ZBalanceDao zBalanceDao;
    @Resource
    private RouteService routeService;
    @Resource
    private ZChargeDao zChargeDao;
    @Resource
    private AntLedger antLedger;
    @Resource
    private AgentLedger agentLedger;
    @Resource
    private TransactionTemplate tx;

    /**
     * 商户充值成功: 商户记账
     */
    public void merchantChargeSuccess(ZChargeEntity entity) {
        // 商户记账
        ZBalanceEntity account = ledgerUtil.getAccount(entity.getDeptId(), ZooConstant.OWNER_TYPE_MERCHANT, entity.getMerchantId());
        BigDecimal chargeRate = entity.getMerchantRate();

        BigDecimal fee = chargeRate.multiply(entity.getRealAmount()).setScale(2, RoundingMode.HALF_UP).negate();
        BigDecimal principal = entity.getRealAmount().add(fee);

        entity.setMerchantFee(fee);
        entity.setMerchantPrincipal(principal);

        String factMemo = String.format("amount[%s], fee[%s], principal[%s]", entity.getRealAmount(), fee, principal);
        LambdaUpdateWrapper<ZBalanceEntity> ledge = ledgerUtil.ledge(account, LedgerConstant.FACT_MERCHANT_CHARGE_SUCCESS, entity.getId(), factMemo, principal);
        int update = zBalanceDao.update(null, ledge);
        if (update != 1) {
            log.error("ledge error: merchantChargeSuccess: {}", entity.getId());
            throw new RenException("merchantChargeSuccess failed");
        }

        // 代理跑分模式
        if (entity.getHandleMode().equals(ZooConstant.PROCESS_MODE_AGENT)) {
            agentLedger.agentMerchantChargeSuccess(entity);
            return;
        }

        // 码农跑分模式
        if (entity.getHandleMode().equals(ZooConstant.PROCESS_MODE_ANT)) {
            antLedger.antMerchantChargeSuccess(entity);
            return;
        }

        // 渠道 | 资源卡 无需记账处理
    }


    /**
     * 商户发起代付时记账
     *
     * @param merchant
     * @param withdrawEntity
     */
    public void merchantWithdraw(SysUserEntity merchant, ZWithdrawEntity withdrawEntity) {
        ZBalanceEntity account = ledgerUtil.getAccount(merchant.getDeptId(), ZooConstant.OWNER_TYPE_MERCHANT, merchant.getId());
        BigDecimal fee = merchant.getWithdrawFix().add(merchant.getWithdrawRate().multiply(withdrawEntity.getAmount())).setScale(2, RoundingMode.HALF_UP);
        BigDecimal factAmount = withdrawEntity.getAmount().add(fee).negate();
        String factMemo = String.format("withdraw amount[%s], fee[%s], principal[%s]", withdrawEntity.getAmount(), fee, factAmount);
        LambdaUpdateWrapper<ZBalanceEntity> ledge = ledgerUtil.ledge(account, LedgerConstant.FACT_MERCHANT_MERCHANT_WITHDRAW, withdrawEntity.getId(), factMemo, factAmount);
        int update = zBalanceDao.update(ledge);
        if (update != 1) {
            throw new RenException("merchantWithdraw failed");
        }
    }


    /**
     * 代付失败商户记账: 退回商户资金
     */
    public void merchantWithdrawFail(ZWithdrawEntity entity) {
        ZBalanceEntity account = ledgerUtil.getAccount(entity.getDeptId(), ZooConstant.OWNER_TYPE_MERCHANT, entity.getMerchantId());
        if (entity.getMerchantFee() == null) {
            BigDecimal fee = entity.getMerchantRate().multiply(entity.getAmount()).add(entity.getMerchantFix());
            entity.setMerchantFee(fee);
        }
        String factMemo = String.format("withdraw[%d] amount[%s], fee[%s] failed, refund", entity.getId(), entity.getAmount(), entity.getMerchantFee());
        BigDecimal factAmount = entity.getMerchantFee().add(entity.getAmount());
        LambdaUpdateWrapper<ZBalanceEntity> ledge = ledgerUtil.ledge(account, LedgerConstant.FACT_MERCHANT_WITHDRAW_FAIL, entity.getId(), factMemo, factAmount);
        int update = zBalanceDao.update(ledge);
        if (update != 1) {
            throw new RenException("merchantWithdrawFail failed");
        }
    }


    /**
     * 通用调账
     *
     * @param id
     * @param adjust
     * @param reason
     */
    private static DefaultIdentifierGenerator defaultIdentifierGenerator = DefaultIdentifierGenerator.getInstance();

    public void adjust(Long id, BigDecimal adjust, String reason) {
        ZBalanceEntity balanceEntity = zBalanceDao.selectById(id);
        Long factId = defaultIdentifierGenerator.nextId(null);
        LambdaUpdateWrapper<ZBalanceEntity> ledge = ledgerUtil.ledge(balanceEntity, LedgerConstant.FACT_ADJUST, factId, reason, adjust);
        tx.executeWithoutResult(status -> {
            zBalanceDao.update(ledge);
        });
    }
}
