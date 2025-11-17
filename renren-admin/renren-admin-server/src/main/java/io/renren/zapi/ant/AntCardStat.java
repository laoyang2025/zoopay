package io.renren.zapi.ant;

import io.renren.commons.tools.redis.RedisUtils;
import io.renren.zapi.ZooConstant;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AntCardStat {
    @Resource
    private RedisUtils redisUtils;

    public Map<String, Object> cardSuccess(Long deptId) {
        String key = ZooConstant.antCardSuccessKey(deptId);
        Map<String, Object> stringObjectMap = redisUtils.hGetAll(key);
        return stringObjectMap;
    }

    public Map<String, Object> cardTotal(Long deptId) {
        String key = ZooConstant.antCardTotalKey(deptId);
        Map<String, Object> stringObjectMap = redisUtils.hGetAll(key);
        return stringObjectMap;
    }

    public void increaseCardTotal(Long deptId, Long cardId) {
        String key = ZooConstant.antCardTotalKey(deptId);
        redisUtils.hInc(key, cardId.toString(), 1L);
    }

    public void increaseCardSuccess(Long deptId, Long cardId) {
        String key = ZooConstant.antCardSuccessKey(deptId);
        redisUtils.hInc(key, cardId.toString(), 1L);
    }

    public Long increaseCardTn(Long deptId, Long cardId) {
        String key = ZooConstant.antTnKey(deptId);
        return redisUtils.hInc(key, cardId.toString(), 1L);
    }
}
