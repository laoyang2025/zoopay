package io.renren.zapi.channel.dto;


import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.renren.commons.tools.redis.RedisUtils;
import io.renren.entity.SysDeptEntity;
import io.renren.zadmin.dao.ZChannelDao;
import io.renren.zadmin.dao.ZChargeDao;
import io.renren.zadmin.dao.ZWithdrawDao;
import io.renren.zadmin.entity.ZChannelEntity;
import io.renren.zapi.ZConfig;
import io.renren.zapi.ledger.ZLedger;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.client.RestTemplate;

@Data
@AllArgsConstructor
public class ChannelContext {
    private ZChannelEntity channelEntity;
    private RestTemplate restTemplate;
    private ZConfig config;
    private ZLedger Ledger;
    private ObjectMapper objectMapper;
    private ZChargeDao chargeDao;
    private ZWithdrawDao withdrawDao;
    private ZChannelDao channelDao;
    private Logger logger;
    private SysDeptEntity dept;
    private RedisUtils redisUtils;

    public void error(String format, Object arg) {
        logger.error(format, arg);
    }
    public void error(String format, Object arg1, Object arg2) {
        logger.error(format, arg1, arg2);
    }
    public void error(String format, Object... arguments) {
        logger.error(format, arguments);
    }

    public void info(String format, Object arg) {
        logger.info(format, arg);
    }
    public void info(String format, Object arg1, Object arg2) {
        logger.info(format, arg1, arg2);
    }
    public void info(String format, Object... arguments) {
        logger.info(format, arguments);
    }

    public void warn(String format, Object arg) {
        logger.warn(format, arg);
    }
    public void warn(String format, Object arg1, Object arg2) {
        logger.warn(format, arg1, arg2);
    }
    public void warn(String format, Object... arguments) {
        logger.warn(format, arguments);
    }
}
