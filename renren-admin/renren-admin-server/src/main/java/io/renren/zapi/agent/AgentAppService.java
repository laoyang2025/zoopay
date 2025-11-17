package io.renren.zapi.agent;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.renren.commons.security.user.MyUserDetail;
import io.renren.commons.security.user.SecurityUser;
import io.renren.commons.tools.exception.RenException;
import io.renren.commons.tools.redis.RedisUtils;
import io.renren.commons.tools.utils.ConvertUtils;
import io.renren.commons.tools.utils.Result;
import io.renren.zadmin.entity.ZCardEntity;
import io.renren.zapi.ZooConstant;
import io.renren.zapi.controller.ClaimResponse;
import io.renren.zadmin.dao.ZUserCardDao;
import io.renren.zadmin.dao.ZUserLogDao;
import io.renren.zadmin.dao.ZWithdrawDao;
import io.renren.zadmin.dto.ZUserLogDTO;
import io.renren.zadmin.entity.ZUserCardEntity;
import io.renren.zadmin.entity.ZUserLogEntity;
import io.renren.zadmin.entity.ZWithdrawEntity;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


@Service
@Slf4j
public class AgentAppService {

    @Resource
    private RedisUtils redisUtils;
    @Resource
    private ZWithdrawDao zWithdrawDao;
    @Resource
    private ZUserLogDao zUserLogDao;
    @Resource
    private ZUserCardDao zUserCardDao;
    @Resource
    private AgentMatchService agentMatchService;
    @Resource
    private TransactionTemplate tx;


    /**
     * 设置当前卡的在线时间
     * @param user
     * @param cardId
     */
    public void heartbeat(MyUserDetail user, Long cardId) {
        Long agentId = user.getAgentId();
        Long userId = user.getId();
        Long deptId = user.getDeptId();

        String key = ZooConstant.agentCardOnlineKey(deptId);
        String field = ZooConstant.agentCardOnlineField(agentId, userId, cardId);
        redisUtils.hSet(key, field, new Date().getTime(), 10*365*24*60*60);
    }

    /**
     * 找出所有在线的卡
     * @param deptId
     * @return
     */
    public List<AgentHashItem> online(Long deptId, Long timeoutMs) {
        String key = ZooConstant.agentCardOnlineKey(deptId);
        Map<String, Object> kvMap = redisUtils.hGetAll(key);
        List<AgentHashItem> cards = new ArrayList<>();
        Long now = new Date().getTime();
        for (Map.Entry<String, Object> entry : kvMap.entrySet()) {
            Long value = (Long)entry.getValue() ;
            if (now - value > timeoutMs) {
                continue;
            }
            String keystr  = entry.getKey();
            String[] split = keystr.split(":");
            Long agentId = Long.parseLong(split[0]);
            Long userId = Long.parseLong(split[1]);
            Long cardId = Long.parseLong(split[2]);
            cards.add(new AgentHashItem(agentId, userId, cardId, null, null, 0, 0));
        }
        return cards;
    }


    /**
     * 找出某些在线的卡
     * @param deptId
     * @param agents
     * @return
     */
    public List<AgentHashItem> online(Long deptId, List<Long> agents, Long timeoutMs) {
        List<AgentHashItem> cards = new ArrayList<>();
        String hashKey = ZooConstant.agentCardOnlineKey(deptId);
        for (Long agent : agents) {
            String pattern = agent.toString() + ":*";
            Map<String, Object> map = redisUtils.hScan(hashKey, pattern);
            Long now = new Date().getTime();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Long value = (Long)entry.getValue() ;
                if (now - value > timeoutMs) {
                    continue;
                }
                String keystr = entry.getKey();
                String[] split = keystr.split(":");
                Long agentId = Long.parseLong(split[0]);
                Long userId = Long.parseLong(split[1]);
                Long cardId = Long.parseLong(split[2]);
                cards.add(new AgentHashItem(agentId, userId, cardId, null, null, 0, 0));
            }
        }
        return cards;
    }

    public boolean isCardOnline(ZUserCardEntity card, Long timeoutMs) {
       return isCardOnline(card.getDeptId(), card.getAgentId(), card.getUserId(), card.getId(), timeoutMs) ;
    }

    public boolean isCardOnline(Long deptId, Long agentId, Long userId, Long cardId, Long timeoutMs) {
        String key = ZooConstant.agentCardOnlineKey(deptId);
        String field = ZooConstant.agentCardOnlineField(agentId, userId, cardId);
        Long value = (Long)redisUtils.hGet(key, field);
        return new Date().getTime() - value  < timeoutMs;
    }

    public Result<ClaimResponse> claimWithdraw(Long id) {
        MyUserDetail user = SecurityUser.getUser();
        ZWithdrawEntity zWithdrawEntity = zWithdrawDao.selectById(id);
        if(zWithdrawEntity.getClaimed() == 1) {
            return Result.fail(9999, "out of stock");
        }

        String lockName = zWithdrawEntity.getDeptId().toString();
        synchronized (ZooConstant.userClaimLocks.intern(lockName)) {
            int update = zWithdrawDao.update(null, Wrappers.<ZWithdrawEntity>lambdaUpdate()
                    .eq(ZWithdrawEntity::getClaimed, 0)
                    .eq(ZWithdrawEntity::getId, id)
                    .set(ZWithdrawEntity::getClaimed, 1)
                    .set(ZWithdrawEntity::getAgentId, user.getAgentId())
                    .set(ZWithdrawEntity::getAgentName, user.getAgentName())
                    .set(ZWithdrawEntity::getUserId, user.getId())
                    .set(ZWithdrawEntity::getUsername, user.getUsername())
            );
            if (update != 1) {
                return Result.fail(9999, "out of stock");
            }
        }
        ClaimResponse claimResponse = ConvertUtils.sourceToTarget(zWithdrawEntity, ClaimResponse.class);
        Result<ClaimResponse> result = new Result<>();
        result.setData(claimResponse);
        return result;
    }

    /**
     * 监控app上报流水
     * @param user
     * @param cardId
     * @param logs
     */
    public void report(MyUserDetail user, Long cardId, List<ZUserLogDTO> logs, BigDecimal balance) {
        if(balance != null) {
            ZUserCardEntity updateEntity = new ZUserCardEntity();
            updateEntity.setId(cardId);
            updateEntity.setBankBalance(balance);
            zUserCardDao.updateById(updateEntity);
        }
        List<ZUserLogEntity> entities = ConvertUtils.sourceToTarget(logs, ZUserLogEntity.class);
        List<ZUserLogEntity> fresh = new ArrayList<>();
        ZUserCardEntity card = zUserCardDao.selectById(cardId);
        tx.executeWithoutResult(status ->{
            int duplicate = 0;
            for (ZUserLogEntity entity : entities) {
                try {
                    entity.setAgentId(user.getAgentId());
                    entity.setAgentName(user.getAgentName());
                    entity.setUserId(user.getId());
                    entity.setUsername(user.getUsername());
                    entity.setDeptId(user.getDeptId());
                    entity.setDeptName(user.getDeptName());
                    entity.setCardId(cardId);
                    entity.setCardNo(card.getAccountNo());
                    entity.setCardUser(card.getAccountUser());
                    entity.setFailCount(0);
                    zUserLogDao.insert(entity);
                    fresh.add(entity);
                } catch (DuplicateKeyException ex) {
                    duplicate++;
                }
            }
            log.info("card[{}] report[{}] duplicate[{}]", cardId, entities.size(), duplicate);
        });
        CompletableFuture.runAsync(() -> {
            agentMatchService.match(fresh);
        });
    }

}
