package io.renren.zapi.agent;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.renren.zapi.ledger.AgentLedger;
import io.renren.zapi.event.ChargeTimeoutUserEvent;
import io.renren.zadmin.dao.ZChargeDao;
import io.renren.zadmin.dao.ZUserLogDao;
import io.renren.zadmin.entity.ZChargeEntity;
import io.renren.zadmin.entity.ZUserLogEntity;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Service
@Slf4j
public class AgentTimeoutService {
    @Resource
    private ZChargeDao zChargeDao;
    @Resource
    private AgentAppService agentAppService;
    @Resource
    private ZUserLogDao zUserLogDao;
    @Resource
    private TransactionTemplate tx;
    @Resource
    private AgentLedger agentLedger;

    /**
     *  单笔超时处理
     */
    public void timeoutProcess(ZChargeEntity entity){
        if(!checkValid(entity)) {
            return;
        }

        // 可以退回卡主资金
        tx.executeWithoutResult(status -> {
            agentLedger.userCollectTimeout(entity);
        });
    }

    /**
     * 批量超时处理
     * @param entities
     */
    public void timeoutProcess(List<ZChargeEntity> entities) {
        if(entities.size() == 0) {
            return;
        }
        ZChargeEntity first = entities.get(0);

        if (!checkValid(first)) {
            return;
        }

        for (ZChargeEntity entity : entities) {
            // 可以退回卡主资金
            tx.executeWithoutResult(status -> {
                agentLedger.userCollectTimeout(entity);
            });
        }
    }


    /**
     * 是否满足超时退款条件
     * @param first
     * @return
     */
    private boolean checkValid(ZChargeEntity first) {
        Long deptId = first.getDeptId();
        Long agentId = first.getAgentId();
        Long userId = first.getUserId();
        Long cardId = first.getUserCardId();
        if(!agentAppService.isCardOnline(deptId, agentId, userId, cardId, 40*1000L)) {
            return false;
        }

        // 是否有待匹配流水
        List<ZUserLogEntity> unmatched = zUserLogDao.selectList(Wrappers.<ZUserLogEntity>lambdaQuery()
                .isNull(ZUserLogEntity::getChargeId)
                .eq(ZUserLogEntity::getCardId, cardId)
                .select(ZUserLogEntity::getId)
        );
        if (unmatched.size() > 0) {
            return false;
        }
        return true;
    }

    /**
     * 超时处理
     * @param event
     */
    @EventListener
    public void onChargeTimeout(ChargeTimeoutUserEvent event) {
        ZChargeEntity zChargeEntity = zChargeDao.selectById(event.getChargeId());
        timeoutProcess(zChargeEntity);
    }
}
