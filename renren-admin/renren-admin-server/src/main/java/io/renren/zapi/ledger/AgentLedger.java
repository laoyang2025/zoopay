package io.renren.zapi.ledger;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.renren.commons.tools.exception.RenException;
import io.renren.entity.SysUserEntity;
import io.renren.zadmin.dao.ZBalanceDao;
import io.renren.zadmin.dao.ZChargeDao;
import io.renren.zadmin.entity.*;
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
public class AgentLedger {
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
     * 商户充值成功, 代理跑分模式下，代理手续费记账
     * @param entity
     */
    public void agentMerchantChargeSuccess(ZChargeEntity entity) {
        // 代理有收益
        BigDecimal fee = entity.getAgentRate().multiply(entity.getRealAmount()).setScale(2, RoundingMode.HALF_UP);
        String factMemo = String.format("charge id[%d] amount[%s], agent fee[%s], agent rate[%s]",
                entity.getId(), entity.getRealAmount(), fee, entity.getAgentRate()
        );
        ZBalanceEntity account = ledgerUtil.getAccount(entity.getDeptId(), ZooConstant.OWNER_TYPE_AGENT, entity.getAgentId());
        int update = zBalanceDao.update(
                ledgerUtil.ledge(account, LedgerConstant.FACT_AGENT_MERCHANT_CHARGE_SUCCESS, entity.getId(), factMemo, fee)
        );
        if (update != 1) {
            log.error("ledge error, merchantChargeSuccessAgent: {}", entity.getId());
            throw new RenException("merchantChargeSuccessAgent failed");
        }
    }

    /**
     *
     * @param entity
     * @param agent
     */
    public void agentCollectAssignedShared(ZChargeEntity entity, SysUserEntity agent) {
        Long shareId = agent.getShareId();
        ZBalanceEntity shareAccount = ledgerUtil.getAccount(entity.getDeptId(), ZooConstant.OWNER_TYPE_AGENT_SHARE, shareId);
        String factMemo = String.format("charge id[%d] amount[%s] assigned, shared credit[%s]", entity.getId(), entity.getRealAmount(), shareAccount.getBalance());
        BigDecimal factAmount = entity.getAgentShare();
        LambdaUpdateWrapper<ZBalanceEntity> ledge = ledgerUtil.ledge(shareAccount, LedgerConstant.FACT_AGENT_COLLECT_ASSIGNED_SHARED, entity.getId(), factMemo, factAmount);
        int update = zBalanceDao.update(null, ledge);
        if (update != 1) {
            log.error("ledge error, userCollectAssignedShared: {}, agent:{}", entity.getId(), agent.getId());
            throw new RenException("userCollectAssignedShared failed");
        }
    }

    /**
     * 卡主接单收款分配成功
     * @param entity
     */
    public void userCollectAssigned(ZChargeEntity entity) {
        // 卡主余额账户
        ZBalanceEntity account = ledgerUtil.getAccount(entity.getDeptId(), ZooConstant.OWNER_TYPE_USER, entity.getUserId());
        SysUserEntity agent = routeService.getSysUser(entity.getAgentId());
        SysUserEntity user = routeService.getSysUser(entity.getUserId());

        // 更新收款记录
        ZChargeEntity updateEntity = new ZChargeEntity();

        // 用户余额小于订单金额: 说明是共享了代理额度
        BigDecimal share = entity.getRealAmount().subtract(account.getBalance());
        if (share.compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal positiveShare = share.negate();
            entity.setAgentShare(share.negate());
            agentCollectAssignedShared(entity, agent);

            // 有借用代理额度， 需要更新
            updateEntity.setAgentShare(positiveShare);
        } else {
            updateEntity.setAgentShare(BigDecimal.ZERO);
        }

        updateEntity.setId(entity.getId());
        // 代理信息更新
        updateEntity.setAgentId(entity.getAgentId());
        updateEntity.setAgentName(agent.getUsername());
        updateEntity.setAgentRate(agent.getAgentRate());
        // 卡主信息更新
        updateEntity.setUserId(user.getId());
        updateEntity.setUsername(user.getUsername());
        // 卡片信息更新
        updateEntity.setUserCardId(entity.getUserCardId());
        updateEntity.setUserCardUser(entity.getUserCardUser());
        updateEntity.setUserCardNo(entity.getUserCardNo());
        zChargeDao.updateById(updateEntity);

        // 事实描述 + 事实金额
        String factMemo = String.format("charge id[%d] amount[%s] assigned", entity.getId(), entity.getRealAmount());
        BigDecimal factAmount = entity.getRealAmount().negate();

        // 记账
        LambdaUpdateWrapper<ZBalanceEntity> ledge = ledgerUtil.ledge(account, LedgerConstant.FACT_USER_COLLECT_ASSIGNED, entity.getId(), factMemo, factAmount);
        int update = zBalanceDao.update(null, ledge);
        if (update != 1) {
            log.error("ledge error, userCollectAssigned: {}", entity.getId());
            throw new RenException("userCollectAssigned failed");
        }

    }

    /**
     * 收款交易超时， 且卡主有借用代理额度, 需退回代理
     * @param entity
     */
    public void agentCollectTimeoutShared(ZChargeEntity entity) {
        SysUserEntity agent = routeService.getSysUser(entity.getAgentId());
        ZBalanceEntity agentAccount = ledgerUtil.getAccount(agent.getDeptId(), ZooConstant.OWNER_TYPE_AGENT_SHARE, agent.getId());

        // 事实描述 + 事实金额
        String factMemo = String.format("charge id[%d] amount[%s] timeout refund share credit[%s]", entity.getId(), entity.getRealAmount(), entity.getAgentShare());
        BigDecimal factAmount = entity.getAgentShare();

        // 记账
        LambdaUpdateWrapper<ZBalanceEntity> ledge = ledgerUtil.ledge(agentAccount, LedgerConstant.FACT_AGENT_COLLECT_TIMEOUT_SHARED, entity.getId(), factMemo, factAmount);
        int update = zBalanceDao.update(null, ledge);
        if (update != 1) {
            throw new RenException("merchantChargeSuccess failed");
        }
    }

    /**
     * @param entity
     */
    public void userCollectTimeout(ZChargeEntity entity) {
        ZBalanceEntity account = ledgerUtil.getAccount(entity.getDeptId(), ZooConstant.OWNER_TYPE_USER, entity.getUserId());

        // 当时有共享代理额度, 需要退还
        if (!entity.getAgentShare().equals(BigDecimal.ZERO)) {
            agentCollectTimeoutShared(entity);
        }

        // 事实描述 + 事实金额
        String factMemo = String.format("charge id[%d] amount[%s] timeout refund", entity.getId(), entity.getRealAmount());
        BigDecimal factAmount = entity.getRealAmount();

        // 记账
        LambdaUpdateWrapper<ZBalanceEntity> ledge = ledgerUtil.ledge(account, LedgerConstant.FACT_USER_COLLECT_TIMEOUT, entity.getId(), factMemo, factAmount);
        int update = zBalanceDao.update(null, ledge);
        if (update != 1) {
            throw new RenException("userCollectTimeout failed");
        }
    }


    /**
     * 代理充成功
     *
     * @param entity
     */
    public void agentAgentChargeSuccess(ZAgentChargeEntity entity) {
        ZBalanceEntity balanceEntity = ledgerUtil.getAccount(entity.getDeptId(), ZooConstant.OWNER_TYPE_AGENT, entity.getId());

        // 事实描述+事实金额
        String factMemo = String.format("agent charge[%s] success", entity.getAmount());
        BigDecimal factAmount = entity.getAmount();

        // 记账
        LambdaUpdateWrapper<ZBalanceEntity> ledge = ledgerUtil.ledge(balanceEntity, LedgerConstant.FACT_AGENT_AGENT_CHARGE_SUCCESS, entity.getId(), factMemo, factAmount);
        int update = zBalanceDao.update(ledge);
        if (update != 1) {
            throw new RenException("agentAgentChargeSuccess failed");
        }
    }

    /**
     * 代理模式卡主充值成功
     *
     * @param entity
     */
    public void userUserChargeSuccess(ZUserChargeEntity entity) {
        ZBalanceEntity balanceEntity = ledgerUtil.getAccount(entity.getDeptId(), ZooConstant.OWNER_TYPE_USER, entity.getId());

        String factMemo = String.format("user charge[%s] success", entity.getAmount());
        BigDecimal factAmount = entity.getAmount();

        LambdaUpdateWrapper<ZBalanceEntity> ledge = ledgerUtil.ledge(balanceEntity, LedgerConstant.FACT_USER_USER_CHARGE_SUCCESS, entity.getId(), factMemo, factAmount);
        int update = zBalanceDao.update(ledge);
        if (update != 1) {
            throw new RenException("userUserChargeSuccess failed");
        }
    }

    /**
     * 代付成功: 代理模式下卡主充值抢单
     */
    public void userMerchantWithdrawSuccess(ZWithdrawEntity entity) {

        // 卡主账户
        ZBalanceEntity balanceEntity = ledgerUtil.getAccount(entity.getDeptId(), ZooConstant.OWNER_TYPE_USER, entity.getUserId());

        String factMemo = String.format("merchant Withdraw as ant charge[%s] success", entity.getAmount());
        BigDecimal factAmount = entity.getAmount();

        // 相当与充值
        LambdaUpdateWrapper<ZBalanceEntity> ledge = ledgerUtil.ledge(balanceEntity, LedgerConstant.FACT_USER_MERCHANT_WITHDRAW_SUCCESS, entity.getId(), factMemo, factAmount);
        int update = zBalanceDao.update(ledge);
        if (update != 1) {
            throw new RenException("userMerchantWithdrawSuccess failed");
        }
    }

}
