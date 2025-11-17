package io.renren.zapi.agent;

import io.renren.commons.tools.redis.RedisUtils;
import io.renren.zapi.ZooConstant;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AgentCardStat {
    @Resource
    private RedisUtils redisUtils;

    public Map<String, Object> cardSuccess(Long deptId) {
        String key = ZooConstant.agentCardSuccessKey(deptId);
        Map<String, Object> stringObjectMap = redisUtils.hGetAll(key);
        return stringObjectMap;
    }

    public Map<String, Object> cardTotal(Long deptId) {
        String key = ZooConstant.agentCardTotalKey(deptId);
        Map<String, Object> stringObjectMap = redisUtils.hGetAll(key);
        return stringObjectMap;
    }

    public void increaseCardTotal(Long deptId, Long cardId) {
        String key = ZooConstant.agentCardTotalKey(deptId);
        redisUtils.hInc(key, cardId.toString(), 1L);
    }
    public void increaseCardSuccess(Long deptId, Long cardId) {
        String key = ZooConstant.agentCardSuccessKey(deptId);
        redisUtils.hInc(key, cardId.toString(), 1L);
    }

    public Long increaseCardTn(Long deptId, Long cardId) {
        String key = ZooConstant.userTnKey(deptId);
        return redisUtils.hInc(key, cardId.toString(), 1L);
    }
}
