package io.renren.zapi.channel;


import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.renren.commons.tools.exception.RenException;
import io.renren.commons.tools.redis.RedisUtils;
import io.renren.zapi.ZConfig;
import io.renren.zapi.ledger.ZLedger;
import io.renren.zapi.channel.channels.LocalChannel;
import io.renren.zapi.channel.dto.ChannelContext;
import io.renren.zapi.route.RouteService;
import io.renren.zapi.utils.CommonUtils;
import io.renren.zadmin.dao.ZChannelDao;
import io.renren.zadmin.dao.ZChargeDao;
import io.renren.zadmin.dao.ZWithdrawDao;
import io.renren.zadmin.entity.ZChannelEntity;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.InvocationTargetException;

@Service
@Slf4j
public class ChannelFactory {

    @Resource
    private LocalChannel localChannel;
    @Resource
    private ZLedger ledger;
    @Resource
    private ZConfig config;
    @Resource
    private ZChannelDao channelDao;
    @Resource
    private ZChargeDao ChargeDao;
    @Resource
    private ZWithdrawDao WithdrawDao;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private RouteService routeService;
    @Autowired
    private RedisUtils redisUtils;

    private PayChannel createChannel(ZChannelEntity channelEntity) {
        Logger logger = CommonUtils.getLogger(channelEntity.getDeptName() + ".channel." + channelEntity.getChannelLabel());

        // 准备渠道上下文
        ChannelContext context = new ChannelContext(
                channelEntity,
                new RestTemplate(),
                config,
                ledger,
                objectMapper,
                ChargeDao,
                WithdrawDao,
                channelDao,
                logger,
                routeService.getDept(channelEntity.getDeptId()),
                redisUtils
        );
        try {
            if (channelEntity.getChannelName().equals("LocalChannel")) {
                localChannel.setContext(context);
                return localChannel;
            }
            String className = "io.renren.zapi.channel.channels." + channelEntity.getChannelName();
            Class<?> aClass = Class.forName(className);
            PayChannel channel = (PayChannel)aClass.getDeclaredConstructor().newInstance();
            channel.setContext(context);
            return channel;
        } catch (InstantiationException |IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new RenException("can not create channel");
        }
    }

    /**
     * 获取支付渠道对象
     * @param channelId
     * @return
     */
    public PayChannel get(Long channelId) {
        ZChannelEntity channelEntity = channelDao.selectById(channelId);
        if (channelEntity == null) {
            throw new RenException("can not find channel:" + channelId);
        }
        return createChannel(channelEntity);
    }
}
