package io.renren.zapi.channel;

import cn.hutool.core.lang.Pair;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.renren.dao.SysUserDao;
import io.renren.entity.SysUserEntity;
import io.renren.zadmin.entity.ZChannelEntity;
import io.renren.zapi.AlarmService;
import io.renren.zapi.channel.dto.ChannelBalanceResponse;
import io.renren.zapi.channel.dto.ChannelChargeQueryResponse;
import io.renren.zapi.channel.dto.ChannelContext;
import io.renren.zapi.channel.dto.ChannelWithdrawResponse;
import io.renren.zapi.event.ChargeSuccessEvent;
import io.renren.zapi.event.WithdrawCompleteEvent;
import io.renren.zadmin.dao.ZChargeDao;
import io.renren.zadmin.dao.ZWithdrawDao;
import io.renren.zadmin.entity.ZChargeEntity;
import io.renren.zadmin.entity.ZWithdrawEntity;
import io.renren.zapi.ZooConstant;
import io.renren.zapi.ZConfig;
import io.renren.zapi.ledger.ZLedger;
import io.renren.zsocket.SocketAdmin;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static io.renren.zapi.channel.channels.AbstractChannel.*;

@Service
@Slf4j
public class ChannelCallbackService {

    @Resource
    private ZConfig config;
    @Resource
    private AlarmService alarmService;
    @Resource
    private ChannelFactory channelFactory;
    @Resource
    private ZWithdrawDao zWithdrawDao;
    @Resource
    private ZChargeDao zChargeDao;
    @Resource
    private SysUserDao sysUserDao;
    @Resource
    private ZLedger ledger;
    @Resource
    private ApplicationEventPublisher publisher;
    @Resource
    private TransactionTemplate tx;


    public void doCharge(Long deptId, Long channelId, Long id, String contentType, Object body, HttpServletRequest request, HttpServletResponse response) {
        if (config.isDebug()) {
            log.info("[{}] - [{}] channel collect notified: id = {}, contentType = {}, body = [{}]", deptId, channelId, id, contentType, body);
        }
        try {
            PayChannel payChannel = channelFactory.get(channelId);

            // 白名单检查
            payChannel.checkIp();

            ZChargeEntity zChargeEntity = null;
            if (body instanceof ZChargeEntity) {
                zChargeEntity = (ZChargeEntity) body;
            } else {
                // 查原交易 - 不存在
                zChargeEntity = zChargeDao.selectById(id);
                if (zChargeEntity == null) {
                    return;
                }
            }

            // 原交易本来就成功的
            if (zChargeEntity.getProcessStatus().equals(ZooConstant.CHARGE_STATUS_SUCCESS)) {
                payChannel.responseCharge(response, ZooConstant.CHARGE_STATUS_SUCCESS);
                return;
            }

            // 回调处理状态
            ChannelChargeQueryResponse channelChargeQueryResponse = payChannel.chargeNotified(contentType, body, deptId, id, request, response, zChargeEntity);
            int collectStatus = channelChargeQueryResponse.getStatus();

            // 渠道给的回调是成功的
            if (collectStatus == ZooConstant.CHARGE_STATUS_SUCCESS) {
                chargeSuccess(channelId, response, payChannel, zChargeEntity);
            }
        } catch (Exception e) {
            log.error("[{}] - [{}] channel collect notified process error:", deptId, channelId, e.getMessage());
            e.printStackTrace();
        }
    }

    private void chargeSuccess(Long channelId, HttpServletResponse response, PayChannel payChannel, ZChargeEntity zChargeEntity) {
        // 更新流水状态记账
        synchronized (ZooConstant.getMerchantLock(zChargeEntity.getMerchantId())) {
            tx.executeWithoutResult(status -> {
                ledger.merchantChargeSuccess(zChargeEntity);
                zChargeDao.update(null, Wrappers.<ZChargeEntity>lambdaUpdate()
                        .eq(ZChargeEntity::getProcessStatus, ZooConstant.CHARGE_STATUS_PROCESSING)
                        .eq(ZChargeEntity::getId, zChargeEntity.getId())
                        .set(ZChargeEntity::getProcessStatus, ZooConstant.CHARGE_STATUS_SUCCESS)
                        .set(ZChargeEntity::getMerchantFee, zChargeEntity.getMerchantFee())
                        .set(ZChargeEntity::getMerchantPrincipal, zChargeEntity.getMerchantPrincipal())
                );
            });
        }

        // 查询余额: 看是否到达报警金额
        CompletableFuture.runAsync(() -> {
            ChannelBalanceResponse balance = payChannel.balance();
            ChannelContext context = payChannel.getContext();
            BigDecimal warningAmount = payChannel.getContext().getChannelEntity().getWarningAmount();

            if (balance.getBalance().compareTo(warningAmount) > 0) {
                context.getChannelDao().update(null, Wrappers.<ZChannelEntity>lambdaUpdate()
                        .eq(ZChannelEntity::getId, channelId)
                        .set(ZChannelEntity::getChargeEnabled, 0)
                );
                String msg = String.format("渠道[%s]-金额已满[%s]", context.getChannelEntity().getChannelLabel(), warningAmount);
                alarmService.warn(zChargeEntity.getDeptId(), "渠道金额已满", msg);
            }
        });

        // 通知商户
        publisher.publishEvent(new ChargeSuccessEvent(this, zChargeEntity.getId()));

        // 应答商户
        payChannel.responseCharge(response, ZooConstant.CHARGE_STATUS_SUCCESS);
    }

    private void withdrawSuccess(ChannelWithdrawResponse resp, ZWithdrawEntity zWithdrawEntity, Long id, HttpServletResponse response, PayChannel chnl) {
        tx.executeWithoutResult(status -> {
            zWithdrawDao.update(null, Wrappers.<ZWithdrawEntity>lambdaUpdate()
                    .eq(ZWithdrawEntity::getId, id)
                    .eq(ZWithdrawEntity::getProcessStatus, ZooConstant.WITHDRAW_STATUS_ASSIGNED)
                    .set(ZWithdrawEntity::getProcessStatus, ZooConstant.WITHDRAW_STATUS_SUCCESS)
                    .set(resp.getUtr() != null, ZWithdrawEntity::getUtr, resp.getUtr())
            );
        });
        chnl.responseWithdraw(response, ZooConstant.CHARGE_STATUS_SUCCESS);
        publisher.publishEvent(new WithdrawCompleteEvent(this, zWithdrawEntity.getId()));
    }

    private void withdrawFail(ChannelWithdrawResponse resp, ZWithdrawEntity zWithdrawEntity, Long id, HttpServletResponse response, PayChannel chnl) {
        String msg = resp.getError();
        if (msg == null) {
            msg = "channel failed";
        }
        zWithdrawDao.update(null, Wrappers.<ZWithdrawEntity>lambdaUpdate()
                .eq(ZWithdrawEntity::getId, id)
                .set(ZWithdrawEntity::getUtr, msg)
        );
        chnl.responseWithdraw(response, ZooConstant.WITHDRAW_STATUS_FAIL);
        return;
    }

    public void doWithdraw(Long deptId, Long channelId, Long id, Object body, String contentType, HttpServletRequest request, HttpServletResponse response) {
        if (config.isDebug()) {
            log.info("[{}] - [{}] channel draw notified: id = {}, contentType = {}, body = [{}]", deptId, channelId, id, contentType, body);
        }

        // 获取渠道对象
        PayChannel chnl = channelFactory.get(channelId);
        if (chnl == null) {
            log.error("[{}] - [{}] can not find channel", deptId, channelId);
            return;
        }

        try {
            chnl.checkIp();
            ZWithdrawEntity zWithdrawEntity = null;

            if (body instanceof ZWithdrawEntity) {
                zWithdrawEntity = (ZWithdrawEntity) body;
            } else {
                zWithdrawEntity = zWithdrawDao.selectById(id);
                if (zWithdrawEntity == null) {
                    log.error("[{}] - [{}] withdraw id[{}] does not exist", deptId, channelId, id);
                    return;
                }
                log.info("selectById withdraw: id = {}, get = {}", id, zWithdrawEntity.getId());
            }

            // 当前状态不是已分配
            if (!zWithdrawEntity.getProcessStatus().equals(ZooConstant.WITHDRAW_STATUS_ASSIGNED)) {
                log.error("[{}] - [{}] withdraw id[{}] invalid status:{}", deptId, channelId, id, zWithdrawEntity.getProcessStatus());
                return;
            }

            // 处理
            ChannelWithdrawResponse resp = chnl.drawNotified(contentType, body, deptId, id, request, response, zWithdrawEntity);

            // 渠道明确失败
            if (resp.getStatus() == ZooConstant.WITHDRAW_STATUS_FAIL) {
                withdrawFail(resp, zWithdrawEntity, id, response, chnl);
                return;
            }

            // 渠道明确成功
            if (resp.getStatus() == ZooConstant.WITHDRAW_STATUS_SUCCESS) {
                withdrawSuccess(resp, zWithdrawEntity, id, response, chnl);
                return;
            }

        } catch (Exception e) {
            log.error("[{}] - [{}] channel draw notified process error:", deptId, channelId, e.getMessage());
            e.printStackTrace();
        }
    }

    // 将webhook 转换为notfiy
    public void doWebhook(Long deptId, Long channelId, String contentType, String body, HttpServletRequest request, HttpServletResponse response) {
        if (config.isDebug()) {
            log.info("[{}] - [{}] channel webhook notified: id = {}, contentType = {}, body = [{}]", deptId, channelId, contentType, body);
        }

        PayChannel chnl = channelFactory.get(channelId);
        if (chnl == null) {
            return;
        }

        // 走渠道的webhook处理
        Pair<String, Object> hookResponse = chnl.webhook(deptId, channelId, contentType, body, request, response);

        if (hookResponse == null) {
            return;
        }

        // 代付的notify
        if (hookResponse.getKey().equals(API_WITHDRAW_NOTIFY)) {
            ZWithdrawEntity withdrawEntity = (ZWithdrawEntity) hookResponse.getValue();
            doWithdraw(deptId, channelId, withdrawEntity.getId(), withdrawEntity, contentType, request, response);
            return;
        }

        // 收款的Notify
        if (hookResponse.getKey().equals(API_CHARGE_NOTIFY)) {
            ZChargeEntity entity = (ZChargeEntity) hookResponse.getValue();
            doCharge(deptId, channelId, entity.getId(), contentType, entity, request, response);
            return;
        }

    }
}
