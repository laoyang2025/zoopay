package io.renren.zapi.card;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.renren.commons.tools.utils.ConvertUtils;
import io.renren.zadmin.dao.ZCardDao;
import io.renren.zadmin.dao.ZCardLogDao;
import io.renren.zadmin.dao.ZRouteDao;
import io.renren.zadmin.dto.ZCardLogDTO;
import io.renren.zadmin.entity.ZCardEntity;
import io.renren.zadmin.entity.ZCardLogEntity;
import io.renren.zadmin.entity.ZRouteEntity;
import io.renren.zapi.AlarmService;
import io.renren.zapi.ZooConstant;
import io.renren.zsocket.SocketAdmin;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@Service
@Slf4j
public class CardAppService {
    @Resource
    private CardMatchService cardMatchService;
    @Resource
    private ZCardDao zCardDao;
    @Resource
    private ZCardLogDao zCardLogDao;
    @Resource
    private TransactionTemplate tx;
    @Resource
    private ZRouteDao zRouteDao;
    @Resource
    private AlarmService alarmService;

    // 上报银行流水
    public void report(Long deptId, String deptName, Long cardId, List<ZCardLogDTO> logs, BigDecimal balance) {
        List<ZCardLogEntity> entities = ConvertUtils.sourceToTarget(logs, ZCardLogEntity.class);
        List<ZCardLogEntity> fresh = new ArrayList<>();
        ZCardEntity card = zCardDao.selectById(cardId);

        if (card == null) {
            log.error("can not find card: {}", cardId);
            String msg = "收到不存在卡的流水上报, 请检查是否删卡后, 未停止监控:" + cardId;
            alarmService.warn(deptId, "上报异常", msg);
            return;
        }

        if (balance != null) {
            ZCardEntity updateEntity = new ZCardEntity();
            updateEntity.setId(cardId);
            updateEntity.setBankBalance(balance);
            zCardDao.updateById(updateEntity);
            log.info("update card balance:{} -> {}", cardId, balance);

            // 金额超过了告警金额
            if (balance.compareTo(card.getWarningAmount()) > 0) {
                triggerCardFull(card);
            }
        }

        // 如果新增流水
        tx.executeWithoutResult(status -> {
            int duplicate = 0;
            BigDecimal total = BigDecimal.ZERO;
            for (ZCardLogEntity entity : entities) {
                try {
                    entity.setDeptId(deptId);
                    entity.setDeptName(deptName);
                    entity.setCardId(cardId);
                    entity.setCardNo(card.getAccountNo());
                    entity.setCardUser(card.getAccountUser());
                    entity.setFailCount(0);
                    zCardLogDao.insert(entity);
                    fresh.add(entity);
                    if (entity.getFlag().equals("plus")) {
                        total = total.add(entity.getAmount());
                    }
                } catch (DuplicateKeyException ex) {
                    duplicate++;
                }
            }
            if (entities.size() > 0) {
                log.info("card[{}] report[{}] duplicate[{}], total amount[{}]", cardId, entities.size(), duplicate, total);
            }
        });

        // 新增流水匹配
        CompletableFuture.runAsync(() -> {
            cardMatchService.match(fresh);
        });
    }

    /**
     * 卡收款金额已经满了:  需要关掉那条路由
     *
     * @param card
     */
    private void triggerCardFull(ZCardEntity card) {
        CompletableFuture.runAsync(() -> {
            String msg = String.format("自营卡%s-%s已满", card.getAccountUser(), card.getAccountNo());
            SocketAdmin.sendMessage(card.getDeptId(), "warning", msg, null);
            // 关掉路由项
            zRouteDao.update(null, Wrappers.<ZRouteEntity>lambdaUpdate()
                    .eq(ZRouteEntity::getObjectId, card.getId())
                    .eq(ZRouteEntity::getDeptId, card.getDeptId())
                    .eq(ZRouteEntity::getProcessMode, ZooConstant.PROCESS_MODE_CARD)
                    .set(ZRouteEntity::getEnabled, 0)
            );
        });
    }

}
