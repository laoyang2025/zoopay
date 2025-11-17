/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.commons.tools.redis;

import io.lettuce.core.RedisCommandTimeoutException;
import jakarta.annotation.Resource;
import org.checkerframework.checker.units.qual.A;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0
 */
@Component
public class RedisUtils {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 改为10年
     * 默认过期时长为24小时，单位：秒
     */
    public final static long DEFAULT_EXPIRE = 10 * 365 * 60 * 60 * 24L;
    /**
     * 过期时长为1小时，单位：秒
     */
    public final static long HOUR_ONE_EXPIRE = 60 * 60 * 1L;
    /**
     * 过期时长为6小时，单位：秒
     */
    public final static long HOUR_SIX_EXPIRE = 60 * 60 * 6L;
    /**
     * 不设置过期时长
     */
    public final static long NOT_EXPIRE = -1L;

    public void set(String key, Object value, long expire) {
        redisTemplate.opsForValue().set(key, value);
        if (expire != NOT_EXPIRE) {
            expire(key, expire);
        }
    }

    public void set(String key, Object value) {
        set(key, value, DEFAULT_EXPIRE);
    }

    public Object get(String key, long expire) {
        Object value = redisTemplate.opsForValue().get(key);
        if (expire != NOT_EXPIRE) {
            expire(key, expire);
        }
        return value;
    }

    public Object get(String key) {
        return get(key, NOT_EXPIRE);
    }

    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    public void deleteByPattern(String pattern) {
        redisTemplate.delete(keys(pattern));
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public void delete(Collection<String> keys) {
        redisTemplate.delete(keys);
    }

    public Object hGet(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    public Map<String, Object> hGetAll(String key) {
        HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();
        return hashOperations.entries(key);
    }

    public List<Object> hMGet(String key, List<String> fields) {
        HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();
        List<Object> objects = hashOperations.multiGet(key, fields);
        return objects;
    }

    public Map<String, Object> hScan(String key, String pattern) {
        HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();
        Cursor<Map.Entry<String, Object>> cursor = hashOperations.scan(key, options);

        Map<String, Object> map = new HashMap<>();
        try {
            while (cursor.hasNext()) {
                Map.Entry<String, Object> entry = cursor.next();
                String field = entry.getKey(); // 获取键
                Object value = entry.getValue(); // 获取值
                map.put(field, value);
            }
            return map;
        } finally {
            cursor.close();
        }
    }

    public void hMSet(String key, Map<String, Object> map) {
        hMSet(key, map, DEFAULT_EXPIRE);
    }

    public void hMSet(String key, Map<String, Object> map, long expire) {
        redisTemplate.opsForHash().putAll(key, map);

        if (expire != NOT_EXPIRE) {
            expire(key, expire);
        }
    }

    public void hSet(String key, String field, Object value) {
        hSet(key, field, value, DEFAULT_EXPIRE);
    }

    public void hSet(String key, String field, Object value, long expire) {
        redisTemplate.opsForHash().put(key, field, value);
        if (expire != NOT_EXPIRE) {
            expire(key, expire);
        }
    }

    public Long hInc(String key, String field, Long value) {
        return redisTemplate.opsForHash().increment(key, field, value);
    }

    public void expire(String key, long expire) {
        redisTemplate.expire(key, expire, TimeUnit.SECONDS);
    }

    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    public void hDel(String key, Object... fields) {
        redisTemplate.opsForHash().delete(key, fields);
    }

    public void leftPush(String key, Object value) {
        leftPush(key, value, DEFAULT_EXPIRE);
    }

    public void leftPush(String key, Object value, long expire) {
        redisTemplate.opsForList().leftPush(key, value);

        if (expire != NOT_EXPIRE) {
            expire(key, expire);
        }
    }

    public Object rightPop(String key) {
        return redisTemplate.opsForList().rightPop(key);
    }

    public Object rightPop(String key, long timeout) {
        return redisTemplate.opsForList().rightPop(key, timeout, TimeUnit.SECONDS);
    }

    public Object rightPopNoWait(String key) {
        try {
            return redisTemplate.opsForList().rightPop(key, 1, TimeUnit.MICROSECONDS);
        } catch (RedisCommandTimeoutException ex) {
            return null;
        }
    }

    public void addToPool(String key, BigDecimal value) {
        Double hasScore = redisTemplate.opsForZSet().score(key, value.toString());
        long now = new Date().getTime();

        // 不存在, 直接将金额加入
        if (hasScore == null) {
            redisTemplate.opsForZSet().add(key, value, now);
            return;
        }

        // 这个金额存在
        long elapse = new Date().getTime()  - (long)(hasScore.doubleValue());
        if (elapse > 2 * 60 * 60 * 1000) {
            // 这个金额已经过期了， 可以重新加入
            redisTemplate.opsForZSet().add(key, value, now);
        }

        // 金额存在, 且没有过期, 需要变量所有金额, 然后找出最合适的金额
        Set<Object> range = redisTemplate.opsForZSet().range(key, 0, -1);
        List<BigDecimal> list = range.stream().map(e -> new BigDecimal((String) e)).sorted().dropWhile(e -> e.compareTo(value) > 0).toList();
        for (BigDecimal bigDecimal : list) {
        }
        // todo

    }

}