package io.renren.zapi.ant;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.renren.commons.security.user.MyUserDetail;
import io.renren.commons.security.user.SecurityUser;
import io.renren.commons.tools.exception.RenException;
import io.renren.commons.tools.redis.RedisUtils;
import io.renren.commons.tools.utils.ConvertUtils;
import io.renren.commons.tools.utils.Result;
import io.renren.zadmin.entity.ZCardEntity;
import io.renren.zapi.ZConfig;
import io.renren.zapi.ZooConstant;
import io.renren.zapi.controller.ClaimResponse;
import io.renren.zadmin.dao.ZAntCardDao;
import io.renren.zadmin.dao.ZAntLogDao;
import io.renren.zadmin.dao.ZWithdrawDao;
import io.renren.zadmin.dto.ZAntLogDTO;
import io.renren.zadmin.entity.ZAntCardEntity;
import io.renren.zadmin.entity.ZAntLogEntity;
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
public class AntAppService {

    @Resource
    private ZWithdrawDao zWithdrawDao;

    @Resource
    private AntMatchService antMatchService;
    @Resource
    private ZAntCardDao zAntCardDao;
    @Resource
    private ZAntLogDao zAntLogDao;
    @Resource
    private TransactionTemplate tx;
    @Resource
    private ZConfig config;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private ObjectMapper objectMapper;

    /**
     * 码农app与后台心跳
     */
    public void heartbeat(MyUserDetail user, Long cardId) {
        String key = ZooConstant.antCardOnlineKey(user.getDeptId());
        String field = ZooConstant.antCardOnlineField(user.getId(), cardId);
        redisUtils.hSet(key, field, new Date().getTime(), 10 * 365 * 24 * 60 * 60);
    }

    /**
     * 所有的在线码农
     */
    public List<AntHashItem> online(Long deptId, Long timeoutMs) {
        Map<String, Object> kvMap= redisUtils.hGetAll(ZooConstant.antCardOnlineKey(deptId));
        List<AntHashItem> cards = new ArrayList<>();

        Long now = new Date().getTime();
        for (Map.Entry<String, Object> entry : kvMap.entrySet()) {
            String keystr = entry.getKey();
            Long timestamp = (Long)entry.getValue();

            if (now - timestamp > timeoutMs) {
                continue;
            }
            String[] split = keystr.split(":");
            Long userId  = Long.parseLong(split[0]);
            Long cardId  = Long.parseLong(split[1]);
            cards.add(new AntHashItem(userId, cardId, null, 0, 0));
        }
        return cards;
    }

    public boolean isCardOnline(Long deptId, Long antId, Long cardId, Long timeoutMs) {
        String key = ZooConstant.antCardOnlineKey(deptId);
        String field = ZooConstant.antCardOnlineField(antId, cardId);
        Long value = (Long)redisUtils.hGet(key, field);
        return new Date().getTime() - value  < timeoutMs;
    }

    // 上报银行流水
    public void report(MyUserDetail user, Long cardId, List<ZAntLogDTO> logs, BigDecimal balance) {
        if(balance != null) {
            ZAntCardEntity updateEntity = new ZAntCardEntity();
            updateEntity.setId(cardId);
            updateEntity.setBankBalance(balance);
            zAntCardDao.updateById(updateEntity);
        }

        List<ZAntLogEntity> entities = ConvertUtils.sourceToTarget(logs, ZAntLogEntity.class);
        List<ZAntLogEntity> fresh = new ArrayList<>();
        ZAntCardEntity card = zAntCardDao.selectById(cardId);
        tx.executeWithoutResult(status ->{
            int duplicate = 0;
            for (ZAntLogEntity entity : entities) {
                try {
                    entity.setAntId(user.getId());
                    entity.setAntName(user.getUsername());
                    entity.setDeptId(user.getDeptId());
                    entity.setDeptName(user.getDeptName());
                    entity.setCardId(cardId);
                    entity.setCardNo(card.getAccountNo());
                    entity.setCardUser(card.getAccountUser());
                    entity.setFailCount(0);
                    zAntLogDao.insert(entity);
                    fresh.add(entity);
                } catch (DuplicateKeyException ex) {
                    duplicate++;
                }
            }
            log.info("card[{}] report[{}] duplicate[{}]", cardId, entities.size(), duplicate);
        });
        CompletableFuture.runAsync(() -> {
            antMatchService.match(fresh);
        });
    }

    public Result<ClaimResponse> claimWithdraw(Long id) {
        MyUserDetail user = SecurityUser.getUser();
        ZWithdrawEntity zWithdrawEntity = zWithdrawDao.selectById(id);
        if(zWithdrawEntity.getClaimed() == 1) {
            throw new RenException("out of stock");
        }
        String lockName = zWithdrawEntity.getDeptId().toString();
        synchronized (ZooConstant.antClaimLocks.intern(lockName)) {
            int update = zWithdrawDao.update(null, Wrappers.<ZWithdrawEntity>lambdaUpdate()
                    .eq(ZWithdrawEntity::getClaimed, 0)
                    .eq(ZWithdrawEntity::getId, id)
                    .set(ZWithdrawEntity::getClaimed, 1)
                    .set(ZWithdrawEntity::getAntId, user.getId())
                    .set(ZWithdrawEntity::getAntName, user.getUsername())
            );
            if (update != 1) {
                return Result.fail(9999, "out of stock");
            }
        }

        //抢到了
        ClaimResponse claimResponse = ConvertUtils.sourceToTarget(zWithdrawEntity, ClaimResponse.class);
        Result<ClaimResponse> result = new Result<>();
        result.setData(claimResponse);
        return result;
    }

}
