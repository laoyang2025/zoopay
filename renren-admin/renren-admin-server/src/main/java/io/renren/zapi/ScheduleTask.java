package io.renren.zapi;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.renren.dao.SysUserDao;
import io.renren.entity.SysUserEntity;
import io.renren.zadmin.dao.ZChargeDao;
import io.renren.zadmin.dao.ZWithdrawDao;
import io.renren.zadmin.entity.ZChargeEntity;
import io.renren.zadmin.entity.ZWithdrawEntity;
import io.renren.zapi.agent.AgentMatchService;
import io.renren.zapi.agent.AgentTimeoutService;
import io.renren.zapi.ant.AntMatchService;
import io.renren.zapi.ant.AntTimeoutService;
import io.renren.zapi.card.CardMatchService;
import io.renren.zapi.channel.ChannelFactory;
import io.renren.zapi.channel.PayChannel;
import io.renren.zapi.channel.dto.ChannelWithdrawResponse;
import io.renren.zapi.event.WithdrawCompleteEvent;
import io.renren.zapi.utils.CommonUtils;
import io.renren.zsocket.SocketMessageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ScheduleTask {
    @Resource
    private AgentTimeoutService agentTimeoutService;
    @Resource
    private AntTimeoutService antTimeoutService;
    @Resource
    private ZChargeDao zChargeDao;
    @Resource
    private SocketMessageService socketMessageService;
    @Resource
    private AgentMatchService agentMatchService;
    @Resource
    private AntMatchService antMatchService;
    @Resource
    private CardMatchService cardMatchService;
    @Resource
    private TransferHistoryService transferHistoryService;
    @Resource
    private ZWithdrawDao zWithdrawDao;
    @Resource
    private ChannelFactory channelFactory;
    @Resource
    private TransactionTemplate tx;
    @Resource
    private SysUserDao sysUserDao;
    @Resource
    private ApplicationEventPublisher publisher;

    /**
     * 码农模式 | 代理模式: 交易超时处理
     */
    @Scheduled(fixedRate = 30_000)
    public void chargeTimeout() {
        Date sixMinutesAgo = DateUtils.addMinutes(new Date(), -6);
        Map<String, List<ZChargeEntity>> collect = zChargeDao.selectList(Wrappers.<ZChargeEntity>lambdaQuery()
                .eq(ZChargeEntity::getProcessStatus, ZooConstant.CHARGE_STATUS_PROCESSING)
                .in(ZChargeEntity::getHandleMode, List.of(ZooConstant.PROCESS_MODE_AGENT, ZooConstant.PROCESS_MODE_ANT))
                .lt(ZChargeEntity::getCreateDate, sixMinutesAgo)
        ).stream().collect(Collectors.groupingBy(ZChargeEntity::getHandleMode));

        // 码农跑分模式下超时处理
        List<ZChargeEntity> antModeList = collect.get(ZooConstant.PROCESS_MODE_ANT);
        if (antModeList != null) {
            Map<Long, List<ZChargeEntity>> antCardById = antModeList.stream().collect(Collectors.groupingBy(ZChargeEntity::getAntCardId));
            // 一个一个码农来处理超时
            for (List<ZChargeEntity> value : antCardById.values()) {
                antTimeoutService.timeoutProcess(value);
            }
        }

        // 代理跑分模式下超时处理
        List<ZChargeEntity> agtModeList = collect.get(ZooConstant.PROCESS_MODE_AGENT);
        if (agtModeList != null) {
            Map<Long, List<ZChargeEntity>> agtCardById = agtModeList.stream().collect(Collectors.groupingBy(ZChargeEntity::getUserCardId));
            // 一个一个卡主来处理超时
            for (List<ZChargeEntity> value : agtCardById.values()) {
                agentTimeoutService.timeoutProcess(value);
            }
        }
    }

    /**
     * 定期扫描匹配
     */
    @Scheduled(fixedRate = 33_000)
    public void chargeMatch() {
        antMatchService.matchTask();
        agentMatchService.matchTask();
        cardMatchService.matchTask();
        socketMessageService.matchTask();
    }

    /**
     * 定期数据转移到历史表
     */

    @Scheduled(fixedRate = 60_000)
    public void transferHis() {
        long t1 = System.currentTimeMillis();
        transferHistoryService.transfer();
        log.debug("transfer history consume: {}", System.currentTimeMillis() - t1);
    }

    @Scheduled(fixedRate = 60_000)
    public void withdrawQuery() {
        List<ZWithdrawEntity> zWithdrawEntities = zWithdrawDao.selectList(Wrappers.<ZWithdrawEntity>lambdaQuery()
                .eq(ZWithdrawEntity::getProcessStatus, ZooConstant.WITHDRAW_STATUS_ASSIGNED)
                .eq(ZWithdrawEntity::getHandleMode, ZooConstant.PROCESS_MODE_CHANNEL)
        );
        zWithdrawEntities.forEach(e -> {
            PayChannel payChannel = channelFactory.get(e.getChannelId());

            ChannelWithdrawResponse response;

            try {
                response = payChannel.withdrawQuery(e);
            } catch (Exception ex) {
                return;
            }
            if (!response.getStatus().equals(ZooConstant.WITHDRAW_STATUS_SUCCESS)) {
                return;
            }

            tx.executeWithoutResult(status -> {
                zWithdrawDao.update(null, Wrappers.<ZWithdrawEntity>lambdaUpdate()
                        .eq(ZWithdrawEntity::getId, e.getId())
                        .eq(ZWithdrawEntity::getProcessStatus, ZooConstant.WITHDRAW_STATUS_ASSIGNED)
                        .set(ZWithdrawEntity::getProcessStatus, ZooConstant.WITHDRAW_STATUS_SUCCESS)
                        .set(response.getUtr() != null, ZWithdrawEntity::getUtr, response.getUtr())
                );
            });
            publisher.publishEvent(new WithdrawCompleteEvent(this, e.getId()));
        });
    }
}
