package io.renren.zapi.card;

import io.renren.commons.tools.redis.RedisUtils;
import io.renren.zapi.ZooConstant;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CardStat {
    @Resource
    private RedisUtils redisUtils;

    public Map<String, Object> cardSuccess(Long deptId) {
        String key = ZooConstant.cardSuccessKey(deptId);
        Map<String, Object> stringObjectMap = redisUtils.hGetAll(key);
        return stringObjectMap;
    }

    public Map<String, Object> cardTotal(Long deptId) {
        String key = ZooConstant.cardTotalKey(deptId);
        Map<String, Object> stringObjectMap = redisUtils.hGetAll(key);
        return stringObjectMap;
    }

    public void increaseCardTotal(Long deptId, Long cardId) {
        String key = ZooConstant.cardTotalKey(deptId);
        redisUtils.hInc(key, cardId.toString(), 1L);
    }

    public void increaseCardSuccess(Long deptId, Long cardId) {
        String key = ZooConstant.cardSuccessKey(deptId);
        redisUtils.hInc(key, cardId.toString(), 1L);
    }

    public Long increaseCardTn(Long deptId, Long cardId) {
        String key = ZooConstant.cardTnKey(deptId);
        return redisUtils.hInc(key, cardId.toString(), 1L);
    }
}
