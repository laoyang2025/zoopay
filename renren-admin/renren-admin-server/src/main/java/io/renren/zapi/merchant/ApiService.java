package io.renren.zapi.merchant;

import ch.qos.logback.classic.Logger;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.renren.commons.tools.exception.RenException;
import io.renren.commons.tools.utils.ConvertUtils;
import io.renren.commons.tools.utils.Result;
import io.renren.dao.SysUserDao;
import io.renren.entity.SysDeptEntity;
import io.renren.entity.SysUserEntity;
import io.renren.zadmin.dao.*;
import io.renren.zadmin.dto.ZChargeDTO;
import io.renren.zadmin.dto.ZWithdrawDTO;
import io.renren.zadmin.entity.*;
import io.renren.zapi.AlarmService;
import io.renren.zapi.ZConfig;
import io.renren.zapi.ledger.AgentLedger;
import io.renren.zapi.ledger.AntLedger;
import io.renren.zapi.ledger.ZLedger;
import io.renren.zapi.ZooConstant;
import io.renren.zapi.agent.AgentMatchService;
import io.renren.zapi.ant.AntMatchService;
import io.renren.zapi.card.CardMatchService;
import io.renren.zapi.channel.dto.ChannelChargeQueryResponse;
import io.renren.zapi.channel.dto.ChannelChargeResponse;
import io.renren.zapi.channel.ChannelFactory;
import io.renren.zapi.channel.dto.ChannelContext;
import io.renren.zapi.channel.dto.ChannelWithdrawResponse;
import io.renren.zapi.channel.PayChannel;
import io.renren.zapi.event.ChargeSuccessEvent;
import io.renren.zapi.event.WithdrawCompleteEvent;
import io.renren.zapi.merchant.dto.*;
import io.renren.zapi.route.ChargeRouter;
import io.renren.zapi.route.RouteService;
import io.renren.zapi.route.WithdrawRouter;
import io.renren.zapi.utils.CommonUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.*;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ApiService {

    @Resource
    private AlarmService alarmService;
    @Resource
    private ZConfig config;
    @Resource
    private TaskScheduler taskScheduler;
    @Resource
    private ApplicationEventPublisher publisher;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private ChannelFactory channelFactory;
    @Resource
    private ZBalanceDao zBalanceDao;
    @Resource
    private ZLedger ledger;
    @Resource
    private AntLedger antLedger;
    @Resource
    private AgentLedger agentLedger;
    @Resource
    private ZCardDao zCardDao;
    @Resource
    private ZChannelDao zChannelDao;
    @Resource
    private SysUserDao sysUserDao;
    @Resource
    private TransactionTemplate tx;
    @Resource
    private ZChargeDao zChargeDao;
    @Resource
    private ZWithdrawDao zWithdrawDao;
    @Resource
    private RouteService routeService;
    @Resource
    private AntMatchService antMatchService;
    @Resource
    private AgentMatchService agentMatchService;
    @Resource
    private CardMatchService cardMatchService;

    /**
     * 检查渠道请求
     *
     * @param body
     * @param sign
     * @param appKey
     * @return
     */
    private void checkRequest(ApiContext context, String body, String sign, String appKey, String api) {
        Long deptId = null;
        Long merchantId = null;
        try {
            String[] split = appKey.split("-");
            deptId = Long.parseLong(split[0]);
            merchantId = Long.parseLong(split[1]);
        } catch (Exception ex) {
            log.error("invalid x-app-key: {}", appKey);
            throw new RenException("invalid x-app-key:" + appKey);
        }

        // 机构信息
        SysDeptEntity deptEntity = routeService.getDept(deptId);
        if (deptEntity == null) {
            log.error("invalid x-app-key, illegal deptId:{}", deptId);
            throw new RenException("invalid header x-app-key:" + appKey);
        }

        // 商户信息
        SysUserEntity merchant = routeService.getSysUser(merchantId);
        if (merchant == null) {
            log.error("invalid x-app-key, illegal merchantId:{}", merchantId);
            throw new RenException("invalid header x-app-key:" + appKey);
        }

        // 白名单检查
        if (!merchant.getWhiteIp().contains(CommonUtils.getIp())) {
            log.error("invalid ip: {}, merchant: {}", CommonUtils.getIp(), merchant.getUsername());
            throw new RenException("invalid ip:" + CommonUtils.getIp());
        }

        // 匹配
        String key = merchant.getDeptId().toString() + "-" + merchant.getId().toString();
        if (!key.equals(appKey)) {
            log.error("invalid header x-app-key:{}, calc key:{}" + appKey, key);
            throw new RenException("invalid header x-app-key:" + appKey);
        }

        // 设置dept, merchant, logger
        context.setDept(deptEntity);
        context.setMerchant(merchant);
        context.setLogger(CommonUtils.getLogger(deptEntity.getName() + ".merchant." + merchant.getUsername()));

        // 收付款打印日志
        if (api.equals("charge") || api.equals("withdraw")) {
            context.info("recv: {}|{}|{}", sign, appKey, body);
        }

        String signstr = body + merchant.getSecretKey();
        String calc = DigestUtil.md5Hex(signstr);
        if (!calc.equals(sign)) {
            if (merchant.getDev() == 1) {
                String msg = String.format("[only in dev], signature error, server info: sign[%s], signstr[%s], client sign[%s]", calc, signstr, sign);
                throw new RenException(msg);
            }
            throw new RenException("signature error");
        }
    }


    /**
     * 收款充值交易
     *
     * @param body
     * @param sign
     * @param appKey
     * @return
     */
    public Result<ChargeResponse> charge(String body, String sign, String appKey) {
        if (sign == null) {
            return Result.fail(5001, "header x-sign must not be empty");
        }
        if (appKey == null) {
            return Result.fail(5002, "header x-app-key must not be empty");
        }
        ApiContext context = new ApiContext();

        // 检查请求
        this.checkRequest(context, body, sign, appKey, "charge");

        // 反序列化为请求对象
        ChargeRequest chargeRequest = null;
        try {
            chargeRequest = objectMapper.readValue(body, ChargeRequest.class);
        } catch (JsonProcessingException e) {
            throw new RenException("invalid json object[" + body + "]");
        }
        if (chargeRequest.getPayCode() == null) {
            return Result.fail(6001, "payCode must not be empty");
        }
        if (chargeRequest.getCallbackUrl() == null) {
            return Result.fail(6002, "callbackUrl must not be empty");
        }
        if (chargeRequest.getAmount() == null) {
            return Result.fail(6003, "amount must not be empty");
        }
        if (chargeRequest.getNotifyUrl() == null) {
            return Result.fail(6004, "notifyUrl must not be empty");
        }
        if (chargeRequest.getOrderId() == null) {
            return Result.fail(6005, "orderId must not be empty");
        }

        SysUserEntity merchant = context.getMerchant();

        // 金额判断
        if (chargeRequest.getAmount().compareTo(merchant.getChargeMin()) < 0) {
            return Result.fail(6006, "amount too small");
        }
        if (chargeRequest.getAmount().compareTo(merchant.getChargeMax()) > 0) {
            return Result.fail(6007, "amount too big");
        }

        // 入库
        ZChargeEntity chargeEntity = ConvertUtils.sourceToTarget(chargeRequest, ZChargeEntity.class);
        chargeEntity.setDeptId(merchant.getDeptId());
        chargeEntity.setDeptName(merchant.getDeptName());
        chargeEntity.setMerchantId(merchant.getId());
        chargeEntity.setMerchantName(merchant.getUsername());
        chargeEntity.setMiddleId(merchant.getMiddleId());
        chargeEntity.setMiddleName(merchant.getMiddleName());
        chargeEntity.setRealAmount(chargeEntity.getAmount());  // 默认realAmount == amount
        zChargeDao.insert(chargeEntity);
        context.setChargeEntity(chargeEntity);

        // 处理上下文
        ApiContext.setContext(context);

        // 渠道路由, 尝试渠道收款
        ChargeRouter chargeRouter = routeService.getChargeRouter(ZooConstant.PROCESS_MODE_CHANNEL, chargeEntity.getPayCode());
        List<ZRouteEntity> routes = chargeRouter.select(chargeEntity);
        for (ZRouteEntity route : routes) {
            log.info("尝试渠道: {}", route.getObjectName());
            PayChannel payChannel = channelFactory.get(route.getObjectId());
            try {
                ChannelChargeResponse channelResponse = payChannel.charge(chargeEntity);
                if (channelResponse.getError() != null) {
                    log.error("尝试渠道[{}]失败, error:{}", payChannel.getContext().getChannelEntity().getChannelLabel(), channelResponse.getError());
                    String msg = String.format("尝试渠道[%s]收款失败, 渠道错误:{}", payChannel.getContext().getChannelEntity().getChannelLabel(), channelResponse.getError());
                    alarmService.warn(merchant.getDeptId(), "收款渠道异常", msg);
                    continue;
                }
                // 渠道处理成功了, 根据路由 更新扣率信息, 处理模式
                // 根据渠道结果， 更新 渠道单号
                if (!payChannel.isLocal()) {
                    ZChannelEntity channelEntity = payChannel.getContext().getChannelEntity();
                    zChargeDao.update(null, Wrappers.<ZChargeEntity>lambdaUpdate()
                            .eq(ZChargeEntity::getId, chargeEntity.getId())
                            .set(ZChargeEntity::getProcessStatus, ZooConstant.CHARGE_STATUS_PROCESSING)
                            .set(ZChargeEntity::getHandleMode, ZooConstant.PROCESS_MODE_CHANNEL)
                            .set(ZChargeEntity::getMerchantRate, route.getChargeRate())
                            .set(ZChargeEntity::getChannelId, channelEntity.getId())
                            .set(ZChargeEntity::getChannelLabel, channelEntity.getChannelLabel())
                            .set(ZChargeEntity::getChannelRate, channelEntity.getChargeRate())
                            .set(ZChargeEntity::getChannelCost, channelEntity.getChargeRate().multiply(chargeEntity.getAmount()).setScale(2, RoundingMode.UP))
                            .set(ZChargeEntity::getChannelId, channelEntity.getId())
                            .set(channelResponse.getChannelOrder() != null, ZChargeEntity::getChannelOrder, channelResponse.getChannelOrder())
                            .set(channelResponse.getUpi() != null, ZChargeEntity::getUpi, channelResponse.getUpi())
                    );
                }

                ChargeResponse response = new ChargeResponse();
                Result<ChargeResponse> result = new Result<>();
                response.setUpi(channelResponse.getUpi());
                response.setPayUrl(channelResponse.getPayUrl());
                response.setRaw(channelResponse.getRaw());
                response.setId(chargeEntity.getId());
                result.setData(response);
                context.info("send: {}", response);
                return result;
            } catch (Exception ex) {
                log.error("尝试渠道[{}], 异常", ex.getMessage());
                ex.printStackTrace();
                alarmService.warn(merchant.getDeptId(), "渠道异常", ex.getMessage());
                continue;
            }
        }
        throw new RenException("no serviceable channel");
    }

    public Result<ChargeQueryResponse> chargeQuery(String body, String sign, String appKey) {
        ApiContext context = new ApiContext();
        this.checkRequest(context, body, sign, appKey, "chargeQuery");
        SysUserEntity merchant = context.getMerchant();
        try {
            ChargeQueryRequest chargeQueryRequest = objectMapper.readValue(body, ChargeQueryRequest.class);

            Long id = chargeQueryRequest.getId();
            String orderId = chargeQueryRequest.getOrderId();
            ZChargeEntity zChargeEntity = null;
            if (id != null) {
                zChargeEntity = zChargeDao.selectById(id);
            } else if (orderId != null) {
                zChargeEntity = zChargeDao.selectOne(Wrappers.<ZChargeEntity>lambdaQuery()
                        .eq(ZChargeEntity::getDeptId, merchant.getDeptId())
                        .eq(ZChargeEntity::getMerchantId, merchant.getId())
                        .eq(ZChargeEntity::getOrderId, orderId)
                        .select(
                                ZChargeEntity::getProcessStatus,
                                ZChargeEntity::getId,
                                ZChargeEntity::getOrderId,
                                ZChargeEntity::getUtr,
                                ZChargeEntity::getUpi,
                                ZChargeEntity::getRealAmount
                        )
                );
            } else {
                throw new RenException("id, orderId must be provided");
            }
            if (zChargeEntity == null) {
                throw new RenException("charge record does not exist");
            }
            ChargeQueryResponse chargeQueryResponse = ConvertUtils.sourceToTarget(zChargeEntity, ChargeQueryResponse.class);
            Result<ChargeQueryResponse> result = new Result<>();
            result.setData(chargeQueryResponse);
            context.info("send: {}", result);
            return result;
        } catch (JsonProcessingException e) {
            throw new RenException("invalid json object[" + body + "]");
        }
    }

    public Result<WithdrawResponse> withdraw(String body, String sign, String appKey) {
        ApiContext context = new ApiContext();
        this.checkRequest(context, body, sign, appKey, "withdraw");
        SysUserEntity merchant = context.getMerchant();
        WithdrawRequest withdrawRequest = null;
        try {
            withdrawRequest = objectMapper.readValue(body, WithdrawRequest.class);
        } catch (JsonProcessingException e) {
            throw new RenException("invalid json object[" + body + "]");
        }

        // 金额限制
        if (withdrawRequest.getAmount().compareTo(merchant.getWithdrawMin()) < 0) {
            return Result.fail(9999, "amount too small");
        }
        if (withdrawRequest.getAmount().compareTo(merchant.getWithdrawMax()) > 0) {
            return Result.fail(9999, "amount too big");
        }

        // 入库
        ZWithdrawEntity withdrawEntity = ConvertUtils.sourceToTarget(withdrawRequest, ZWithdrawEntity.class);
        withdrawEntity.setMerchantId(merchant.getId());
        withdrawEntity.setMerchantName(merchant.getUsername());
        withdrawEntity.setMiddleId(merchant.getMiddleId());
        withdrawEntity.setMiddleName(merchant.getMiddleName());
        withdrawEntity.setMerchantRate(merchant.getWithdrawRate());
        withdrawEntity.setMerchantFix(merchant.getWithdrawFix());

        BigDecimal merchantFee = withdrawEntity.getMerchantRate().multiply(withdrawEntity.getAmount()).add(merchant.getWithdrawFix());
        withdrawEntity.setMerchantFee(merchantFee);
        withdrawEntity.setDeptId(merchant.getDeptId());
        withdrawEntity.setDeptName(merchant.getDeptName());

        String lockName = merchant.getId().toString();
        synchronized (ZooConstant.merchantLocks.intern(lockName)) {
            ZBalanceEntity balanceEntity = zBalanceDao.selectById(merchant.getId());
            if (balanceEntity.getBalance().compareTo(withdrawEntity.getAmount()) < 0) {
                throw new RenException("insufficient balance");
            }
            tx.executeWithoutResult(status -> {
                try {
                    zWithdrawDao.insert(withdrawEntity);
                } catch (DuplicateKeyException ex) {
                    throw new RenException("orderId is duplicated");
                }
                ledger.merchantWithdraw(merchant, withdrawEntity);
            });
        }
        context.setWithdrawEntity(withdrawEntity);

        Result<WithdrawResponse> result = new Result<>();
        WithdrawResponse response = new WithdrawResponse();
        result.setData(response);
        response.setId(withdrawEntity.getId());

        // 联调阶段, 自动拒绝代付
        if (1 == merchant.getDev()) {
            double random = Math.random();
            if (random > 0.5) {
                log.info("dev: reject withdraw");
                this.rejectWithdraw(withdrawEntity.getId());
                context.info("send: {}", result);
                return result;
            }
        }

        // 自动代付
        if (merchant.getAutoWithdraw().equals(1)) {
            CompletableFuture.runAsync(() -> {
                this.autoDrawAssign(withdrawEntity, context);
                // 联通测试阶段+自动代付: 如果分配了， 就直接给成功
                if (1 == context.getMerchant().getDev()) {
                    log.info("dev: success withdraw");
                    this.successWithdraw(withdrawEntity.getId(), "F4" + CommonUtils.randomDigitString(11));
                }
            });
        }

        context.info("send: {}", result);
        return result;
    }

    public void autoDrawAssign(ZWithdrawEntity withdrawEntity, ApiContext context) {
        // 启用了自动代付,
        log.debug("自动代付分配...");
        WithdrawRouter withdrawRouter = routeService.getWithdrawRouter(ZooConstant.PROCESS_MODE_CHANNEL, context.getMerchant());
        ZRouteEntity selected = withdrawRouter.select(withdrawEntity);
        PayChannel payChannel = channelFactory.get(selected.getObjectId());

        if (payChannel == null) {
            log.error("can not assign to channel: {}", selected.getObjectName());
            throw new RenException("can not find channel:" + selected.getObjectName());
        }

        // 三方渠道, 更新渠道单号， 或者渠道错误信息,  本地渠道, 会自己更新
        if (!payChannel.isLocal()) {
            log.debug("三方渠道代付...");
            this.assignToChannel(withdrawEntity, payChannel, context.getMerchant());
            return;
        }

        // 本地渠道: 会自己更新
        log.debug("本地渠道代付");
        payChannel.withdraw(withdrawEntity, context.getMerchant());
    }

    public void manualWithdrawAssign(ZWithdrawDTO dto) {
        ZWithdrawEntity entity = zWithdrawDao.selectById(dto.getId());
        entity.setHandleMode(dto.getHandleMode());

        // 分配到代理
        if (entity.getHandleMode().equals(ZooConstant.PROCESS_MODE_AGENT)) {
            entity.setAgentId(dto.getAgentId());
            this.assignToAgent(entity);
            return;
        }

        // 分配到渠道
        if (entity.getHandleMode().equals(ZooConstant.PROCESS_MODE_CHANNEL)) {
            entity.setChannelId(dto.getChannelId());
            ApiContext context = new ApiContext();
            context.setWithdrawEntity(entity);
            context.setDept(routeService.getDept(entity.getDeptId()));
            context.setMerchant(routeService.getSysUser(entity.getMerchantId()));
            String loggerName = context.getDept().getName() + ".merchant." + context.getMerchant().getUsername();
            context.setLogger(CommonUtils.getLogger(loggerName));

            SysUserEntity merchant = sysUserDao.selectById(entity.getMerchantId());
            this.assignToChannel(entity, channelFactory.get(dto.getChannelId()), merchant);
            return;
        }

        // 分配到自营卡
        if (entity.getHandleMode().equals(ZooConstant.PROCESS_MODE_CARD)) {
            entity.setCardId(dto.getCardId());
            this.assignToCard(entity, null);
            return;
        }
    }

    // 分配到卡
    public void assignToCard(ZWithdrawEntity entity, ZCardEntity zCardEntity) {
        if (zCardEntity == null) {
            zCardEntity = zCardDao.selectById(entity.getCardId());
        }
        if (zCardEntity == null) {
            log.error("invalid withdraw card: {}", entity.getCardId());
            throw new RenException("no available cards");
        }
        zWithdrawDao.update(null, Wrappers.<ZWithdrawEntity>lambdaUpdate()
                .set(ZWithdrawEntity::getHandleMode, ZooConstant.PROCESS_MODE_CARD)
                .set(ZWithdrawEntity::getCardId, zCardEntity.getId())
                .set(ZWithdrawEntity::getCardUser, zCardEntity.getAccountUser())
                .set(ZWithdrawEntity::getCardNo, zCardEntity.getAccountNo())
                .set(ZWithdrawEntity::getProcessStatus, ZooConstant.WITHDRAW_STATUS_ASSIGNED)
                .eq(ZWithdrawEntity::getId, entity.getId())
                .in(ZWithdrawEntity::getProcessStatus, List.of(ZooConstant.WITHDRAW_STATUS_NEW, ZooConstant.WITHDRAW_STATUS_ASSIGNED))
        );
    }

    // 分配到代理
    public void assignToAgent(ZWithdrawEntity entity) {
        SysUserEntity agent = sysUserDao.selectById(entity.getAgentId());
        if (agent == null) {
            log.error("invalid agent: {}", entity.getAgentId());
            throw new RenException("no available agents");
        }
        zWithdrawDao.update(null, Wrappers.<ZWithdrawEntity>lambdaUpdate()
                .set(ZWithdrawEntity::getHandleMode, ZooConstant.PROCESS_MODE_CARD)
                .set(ZWithdrawEntity::getAgentId, agent.getId())
                .set(ZWithdrawEntity::getAgentName, agent.getUsername())
                .set(ZWithdrawEntity::getProcessStatus, ZooConstant.WITHDRAW_STATUS_ASSIGNED)
                .eq(ZWithdrawEntity::getId, entity.getId())
                .in(ZWithdrawEntity::getProcessStatus, List.of(ZooConstant.WITHDRAW_STATUS_NEW, ZooConstant.WITHDRAW_STATUS_ASSIGNED))
        );
    }

    // 分配到渠道: 渠道自身会先更新为assigned
    public void assignToChannel(ZWithdrawEntity withdrawEntity, PayChannel payChannel, SysUserEntity merchant) {
        Long channelId = payChannel.getContext().getChannelEntity().getId();
        log.debug("assignToChannel: {}", payChannel.getContext().getChannelEntity());

        // 先更新分配到的渠道信息
        ChannelContext channelContext = payChannel.getContext();
        ZChannelEntity channelEntity = channelContext.getChannelEntity();
        int update = zWithdrawDao.update(null, Wrappers.<ZWithdrawEntity>lambdaUpdate()
                .eq(ZWithdrawEntity::getId, withdrawEntity.getId())
                .in(ZWithdrawEntity::getProcessStatus, List.of(ZooConstant.WITHDRAW_STATUS_NEW, ZooConstant.WITHDRAW_STATUS_ASSIGNED))
                .set(ZWithdrawEntity::getChannelId, channelId)
                .set(ZWithdrawEntity::getChannelLabel, channelEntity.getChannelLabel())
                .set(ZWithdrawEntity::getChannelCostFix, channelEntity.getWithdrawFix())
                .set(ZWithdrawEntity::getChannelCostRate, channelEntity.getWithdrawRate())
                .set(ZWithdrawEntity::getHandleMode, ZooConstant.PROCESS_MODE_CHANNEL)
                .set(ZWithdrawEntity::getProcessStatus, ZooConstant.WITHDRAW_STATUS_ASSIGNED)
        );
        if (update != 1) {
            log.error("更新渠道信息错误");
        }
        // 调用渠道
        ChannelWithdrawResponse resp = payChannel.withdraw(withdrawEntity, merchant);
        log.info("代付渠道返回: {}", resp);

        // 渠道失败
        if (resp.getError() != null) {
            String msg = String.format("尝试渠道[%s]代付失败, 渠道错误:{}", payChannel.getContext().getChannelEntity().getChannelLabel(), resp.getError());
            alarmService.warn(channelEntity.getDeptId(), "代付渠道异常", msg);
        }

        // 更新渠道返回结果
        if (resp.getChannelOrder() != null) {
            zWithdrawDao.update(null, Wrappers.<ZWithdrawEntity>lambdaUpdate()
                    .set(ZWithdrawEntity::getChannelOrder, resp.getChannelOrder())
                    .eq(ZWithdrawEntity::getId, withdrawEntity.getId())
            );
        }
    }

    public Result<WithdrawQueryResponse> withdrawQuery(String body, String sign, String appKey) {
        ApiContext context = new ApiContext();
        this.checkRequest(context, body, sign, appKey, "withdrawQuery");
        SysUserEntity merchant = context.getMerchant();

        try {
            WithdrawQueryRequest withdrawQueryRequest = objectMapper.readValue(body, WithdrawQueryRequest.class);

            Long id = withdrawQueryRequest.getId();
            String orderId = withdrawQueryRequest.getOrderId();
            ZWithdrawEntity zWithdrawEntity = null;
            if (id != null) {
                zWithdrawEntity = zWithdrawDao.selectById(id);
            } else if (orderId != null) {
                zWithdrawEntity = zWithdrawDao.selectOne(Wrappers.<ZWithdrawEntity>lambdaQuery()
                        .eq(ZWithdrawEntity::getDeptId, merchant.getDeptId())
                        .eq(ZWithdrawEntity::getMerchantId, merchant.getId())
                        .eq(ZWithdrawEntity::getOrderId, orderId)
                        .select(
                                ZWithdrawEntity::getProcessStatus,
                                ZWithdrawEntity::getAmount,
                                ZWithdrawEntity::getPictures,
                                ZWithdrawEntity::getUtr,
                                ZWithdrawEntity::getId,
                                ZWithdrawEntity::getOrderId
                        )
                );
            } else {
                throw new RenException("id, orderId must be provided");
            }
            if (zWithdrawEntity == null) {
                throw new RenException("withdraw item does not exist");
            }
            Result<WithdrawQueryResponse> result = new Result<>();
            WithdrawQueryResponse response = new WithdrawQueryResponse();
            response.setAmount(zWithdrawEntity.getAmount());
            response.setUtr(zWithdrawEntity.getUtr());
            response.setPictures(zWithdrawEntity.getPictures());
            response.setId(zWithdrawEntity.getId());
            response.setOrderId(zWithdrawEntity.getOrderId());
            response.setProcessStatus(zWithdrawEntity.getProcessStatus());
            result.setData(response);
            context.info("send: {}", result);
            return result;
        } catch (JsonProcessingException e) {
            throw new RenException("invalid json object[" + body + "]");
        }
    }

    public Result<BalanceResponse> balance(String body, String sign, String appKey) {
        ApiContext context = new ApiContext();
        this.checkRequest(context, body, sign, appKey, "balance");
        SysUserEntity merchant = context.getMerchant();
        try {
            BalanceRequest balanceRequest = objectMapper.readValue(body, BalanceRequest.class);
            ZBalanceEntity zBalanceEntity = zBalanceDao.selectOne(Wrappers.<ZBalanceEntity>lambdaQuery()
                    .eq(ZBalanceEntity::getId, merchant.getId())
                    .eq(ZBalanceEntity::getOwnerType, ZooConstant.OWNER_TYPE_MERCHANT)
                    .eq(ZBalanceEntity::getDeptId, merchant.getDeptId())
                    .select(ZBalanceEntity::getBalance)
            );
            if (zBalanceEntity == null) {
                throw new RenException("no internal account");
            }
            Result<BalanceResponse> result = new Result<>();
            BalanceResponse response = new BalanceResponse();
            response.setBalance(zBalanceEntity.getBalance());
            result.setData(response);
            return result;
        } catch (JsonProcessingException e) {
            throw new RenException("invalid json object[" + body + "]");
        }
    }

    /**
     * 付款人提供UTR
     *
     * @param id
     * @param utr
     * @return
     */
    public Result submitUtr(Long id, String utr) {
        ZChargeEntity zChargeEntity = zChargeDao.selectById(id);

        if (zChargeEntity.getUtr() != null) {
            return Result.fail(9999, "please do not duplicate");
        }

        if (zChargeEntity.getProcessStatus().equals(ZooConstant.CHARGE_STATUS_SUCCESS)) {
            return Result.fail(9999, "already success, please do not duplicate");
        }

        String ip = CommonUtils.getIp();

        // 更新流水的utr
        zChargeDao.update(null, Wrappers.<ZChargeEntity>lambdaUpdate()
                .eq(ZChargeEntity::getId, id)
                .set(ZChargeEntity::getUtr, utr)
                .set(ZChargeEntity::getIp, ip)
        );

        // 触发匹配
        SysDeptEntity dept = routeService.getDept(zChargeEntity.getDeptId());
        if (dept.getProcessMode().equals("agent")) {
            agentMatchService.utrMatch(zChargeEntity, utr);
        } else if (dept.getProcessMode().equals("ant")) {
            antMatchService.utrMatch(zChargeEntity, utr);
        } else if (dept.getProcessMode().equals("card")) {
            cardMatchService.utrMatch(zChargeEntity, utr);
        }
        return new Result();
    }

    /**
     * 通知商户代付  success | fail
     *
     * @param entity
     * @param merchant
     */
    public void notifyWithdraw(ZWithdrawEntity entity, SysUserEntity merchant) {
        WithdrawNotify withdrawNotify = ConvertUtils.sourceToTarget(entity, WithdrawNotify.class);
        String body = null;
        try {
            body = objectMapper.writeValueAsString(withdrawNotify);
        } catch (JsonProcessingException e) {
            throw new RenException("serialize notify failed");
        }
        String signstr = body + merchant.getSecretKey();
        String sign = DigestUtil.md5Hex(signstr);
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-app-key", entity.getDeptId() + "-" + entity.getMerchantId());
        headers.add("x-sign", sign);

        RequestEntity<String> request = RequestEntity.post("")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.ALL)
                .acceptCharset(StandardCharsets.UTF_8)
                .headers(headers)
                .body(body);
        ResponseEntity<String> responseEntity = null;

        try {
            responseEntity = restTemplate.postForEntity(entity.getNotifyUrl(), request, String.class);
        } catch (Exception ex) {
            log.error("代付通知失败:{}", ex.getMessage());
        }

        String loggerName = entity.getDeptName() + ".merchant." + merchant.getUsername();
        Logger logger = CommonUtils.getLogger(loggerName);
        logger.info("noti: {}", body);

        if (responseEntity != null) {
            logger.info("recv: {}|{}", sign, signstr);
        }

        // 通知失败, 增加通知失败次数
        if (responseEntity == null || !responseEntity.getStatusCode().equals(HttpStatusCode.valueOf(200))) {
            logger.error("代付通知失败");
            zWithdrawDao.update(null, Wrappers.<ZWithdrawEntity>lambdaUpdate()
                    .eq(ZWithdrawEntity::getId, entity.getId())
                    .set(ZWithdrawEntity::getNotifyCount, entity.getNotifyCount() + 1)
                    .set(ZWithdrawEntity::getNotifyStatus, ZooConstant.NOTIFY_STATUS_FAIL)
                    .set(ZWithdrawEntity::getNotifyTime, new Date())
            );

            // 延迟任务
            if (entity.getNotifyCount() < config.getMaxNotifyCount()) {
                Long nextDuration = config.getInitNotifyInterval() * (entity.getNotifyCount() + 1);
                taskScheduler.schedule(() -> {
                    publisher.publishEvent(new WithdrawCompleteEvent(this, entity.getId()));
                }, Instant.now().plusSeconds(nextDuration));
            }

            String msg = "";
            if (responseEntity == null) {
                msg = "商户:" + merchant.getUsername() + ", 通知异常, url=" + entity.getNotifyUrl() + ", body=" + body + ", 无返回内容";
            } else {
                msg = "商户:" + merchant.getUsername() + ", 通知异常, url=" + entity.getNotifyUrl() + "body=" + body + ", resp=" + responseEntity.getBody();
            }
//            alarmService.warn(entity.getDeptId(), "通知商户异常", msg);

            return;
        }

        // 更新通知状态为成功
        zWithdrawDao.update(null, Wrappers.<ZWithdrawEntity>lambdaUpdate()
                .eq(ZWithdrawEntity::getId, entity.getId())
                .set(ZWithdrawEntity::getNotifyStatus, ZooConstant.NOTIFY_STATUS_SUCCESS)
                .set(ZWithdrawEntity::getNotifyTime, new Date())
        );
    }

    /**
     * 通知商户收款  success
     *
     * @param entity
     * @param merchant
     */
    public void notifyCharge(ZChargeEntity entity, SysUserEntity merchant) {
        ChargeNotify chargeNotify = ConvertUtils.sourceToTarget(entity, ChargeNotify.class);
        String body = null;
        try {
            body = objectMapper.writeValueAsString(chargeNotify);
        } catch (JsonProcessingException e) {
            throw new RenException("serialize notify failed");
        }
        String sign = DigestUtil.md5Hex(body + merchant.getSecretKey());
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-app-key", entity.getDeptId() + "-" + entity.getMerchantId());
        headers.add("x-sign", sign);

        RequestEntity<String> request = RequestEntity.post("")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.ALL)
                .headers(headers)
                .acceptCharset(StandardCharsets.UTF_8)
                .body(body);

        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = restTemplate.postForEntity(entity.getNotifyUrl(), request, String.class);
        } catch (Exception ex) {
            // 通知失败
            log.error("收款通知失败:{}", ex.getMessage());
        }

        String loggerName = entity.getDeptName() + ".merchant." + merchant.getUsername();
        Logger logger = CommonUtils.getLogger(loggerName);
        logger.info("noti: {}", body);

        if (responseEntity != null) {
            logger.info("recv: {}", responseEntity.getBody());
        }

        // 通知失败: 增加通知失败次数
        if (responseEntity == null || !responseEntity.getStatusCode().equals(HttpStatusCode.valueOf(200))) {
            logger.error("收款通知失败, req:{}", body);
            zChargeDao.update(null, Wrappers.<ZChargeEntity>lambdaUpdate()
                    .eq(ZChargeEntity::getId, entity.getId())
                    .set(ZChargeEntity::getNotifyCount, entity.getNotifyCount() + 1)
                    .set(ZChargeEntity::getNotifyStatus, ZooConstant.NOTIFY_STATUS_FAIL)
                    .set(ZChargeEntity::getNotifyTime, new Date())
            );

            // 延迟任务再次通知
            if (entity.getNotifyCount() < config.getMaxNotifyCount()) {
                Long nextDuration = config.getInitNotifyInterval() * (entity.getNotifyCount() + 1);
                taskScheduler.schedule(() -> {
                    publisher.publishEvent(new ChargeSuccessEvent(this, entity.getId()));
                }, Instant.now().plusSeconds(nextDuration));
            }

            String msg = "";
            if (responseEntity == null) {
                msg = "商户:" + merchant.getUsername() + ", 通知异常, url=" + entity.getNotifyUrl() + ", body=" + body + ", 无返回内容";
            } else {
                msg = "商户:" + merchant.getUsername() + ", 通知异常, url=" + entity.getNotifyUrl() + "body=" + body + ", resp=" + responseEntity.getBody();
            }
//            alarmService.warn(entity.getDeptId(), "通知商户异常", msg);
            return;
        }

        // 更新通知状态为成功
        zChargeDao.update(null, Wrappers.<ZChargeEntity>lambdaUpdate()
                .eq(ZChargeEntity::getId, entity.getId())
                .set(ZChargeEntity::getNotifyStatus, ZooConstant.NOTIFY_STATUS_SUCCESS)
                .set(ZChargeEntity::getNotifyTime, new Date())
        );
    }

    /**
     * 充值成功
     *
     * @param event
     */
    @Async
    @EventListener
    public void handleChargeNotify(ChargeSuccessEvent event) {
        Long chargeId = event.getChargeId();
        ZChargeEntity zChargeEntity = zChargeDao.selectById(chargeId);
        if (zChargeEntity == null) {
            log.error("can not find charge record:{}", chargeId);
            return;
        }
        SysUserEntity merchant = sysUserDao.selectById(zChargeEntity.getMerchantId());
        this.notifyCharge(zChargeEntity, merchant);
    }

    /**
     * 代付完成: 成功或者失败都可以
     *
     * @param event
     */
    @Async
    @EventListener
    public void handleWithdrawNotify(WithdrawCompleteEvent event) {
        Long withdrawId = event.getWithdrawId();
        ZWithdrawEntity zWithdrawEntity = zWithdrawDao.selectById(withdrawId);
        if (zWithdrawEntity == null) {
            log.error("can not find withdraw record: {}", withdrawId);
            return;
        }

        SysUserEntity merchant = sysUserDao.selectById(zWithdrawEntity.getMerchantId());
        this.notifyWithdraw(zWithdrawEntity, merchant);
    }

    /**
     * @param id
     * @return
     */
    public Result chargeDev(long id, int status) {
        ZChargeEntity zChargeEntity = zChargeDao.selectById(id);
        Long merchantId = zChargeEntity.getMerchantId();
        SysUserEntity merchant = routeService.getSysUser(merchantId);
        if (1 != merchant.getDev()) {
            return Result.fail(9999, "not in dev mode");
        }
        if (status == 0) {
            return Result.ok;
        }

        // 已经是成功的
        if (zChargeEntity.getProcessStatus().equals(ZooConstant.CHARGE_STATUS_SUCCESS)) {
            publisher.publishEvent(new ChargeSuccessEvent(this, id));
            return Result.ok;
        }

        // 完成记账
        tx.executeWithoutResult(t -> {
            ledger.merchantChargeSuccess(zChargeEntity);
            zChargeDao.update(null, Wrappers.<ZChargeEntity>lambdaUpdate()
                    .eq(ZChargeEntity::getId, id)
                    .eq(ZChargeEntity::getProcessStatus, ZooConstant.CHARGE_STATUS_PROCESSING)
                    .set(ZChargeEntity::getProcessStatus, ZooConstant.CHARGE_STATUS_SUCCESS)
                    .set(ZChargeEntity::getMerchantPrincipal, zChargeEntity.getMerchantPrincipal())
                    .set(ZChargeEntity::getMerchantFee, zChargeEntity.getMerchantFee())
            );
        });

        // 触发通知
        publisher.publishEvent(new ChargeSuccessEvent(this, id));

        return Result.ok;
    }

    /**
     * @param id
     * @return
     */
    public Result withdrawDev(long id, int status) {
        ZWithdrawEntity zWithdrawEntity = zWithdrawDao.selectById(id);
        Long merchantId = zWithdrawEntity.getMerchantId();
        SysUserEntity merchant = routeService.getSysUser(merchantId);

        // 不是测试模式
        if (1 != merchant.getDev()) {
            return Result.fail(9999, "not in dev mode");
        }

        // 直接触发回调商户
        if (zWithdrawEntity.getProcessStatus().equals(ZooConstant.WITHDRAW_STATUS_FAIL) ||
                zWithdrawEntity.getProcessStatus().equals(ZooConstant.WITHDRAW_STATUS_SUCCESS)
        ) {
            publisher.publishEvent(new WithdrawCompleteEvent(null, id));
            return Result.ok;
        }

        // 失败: 退回商户余额
        if (status == 0) {
            synchronized (ZooConstant.getMerchantLock(zWithdrawEntity.getMerchantId())) {
                tx.executeWithoutResult(t -> {
                    ledger.merchantWithdrawFail(zWithdrawEntity);
                });
            }
            publisher.publishEvent(new WithdrawCompleteEvent(null, id));
        }
        // 成功:
        else if (status == 1) {
            publisher.publishEvent(new WithdrawCompleteEvent(null, id));
            return Result.ok;
        }
        return Result.fail(9999, "invalid status");
    }

    /**
     * 商户信息
     *
     * @param deptId
     * @param merchantId
     * @return
     */
    public Result<BigDecimal> merchantInfo(Long deptId, Long merchantId) {
        ZBalanceEntity balanceEntity = zBalanceDao.selectById(merchantId);
        if (!balanceEntity.getDeptId().equals(deptId)) {
            return Result.fail(9999, "invalid merchant id");
        }
        BigDecimal balance = balanceEntity.getBalance();
        Result<BigDecimal> result = new Result<>();
        result.setData(balance);
        return result;
    }

    /**
     * 收款人工回调处理
     *
     * @param dto
     */
    public void manualProcess(ZChargeDTO dto) {
        ZChargeEntity entity = zChargeDao.selectById(dto.getId());
        entity.setRealAmount(dto.getRealAmount()); // attention!!!
        synchronized (ZooConstant.getMerchantLock(entity.getMerchantId())) {
            tx.executeWithoutResult(status -> {
                ledger.merchantChargeSuccess(entity);
                zChargeDao.update(null, Wrappers.<ZChargeEntity>lambdaUpdate()
                        .eq(ZChargeEntity::getId, dto.getId())
                        .eq(ZChargeEntity::getProcessStatus, ZooConstant.CHARGE_STATUS_PROCESSING)
                        .set(ZChargeEntity::getProcessStatus, ZooConstant.CHARGE_STATUS_SUCCESS)
                        .set(ZChargeEntity::getUpi, dto.getUpi())
                        .set(ZChargeEntity::getUtr, dto.getUtr())
                        .set(ZChargeEntity::getRealAmount, dto.getRealAmount())
                        .set(ZChargeEntity::getMerchantFee, entity.getMerchantFee())
                        .set(ZChargeEntity::getMerchantPrincipal, entity.getMerchantPrincipal())
                );
            });
        }

        CompletableFuture.runAsync(() -> {
            this.notifyCharge(dto.getId());
        });
    }

    public void notifyCharge(Long id) {
        ZChargeEntity entity = zChargeDao.selectById(id);
        if (entity.getProcessStatus().equals(ZooConstant.CHARGE_STATUS_SUCCESS)) {
            SysUserEntity merchant = sysUserDao.selectById(entity.getMerchantId());
            notifyCharge(entity, merchant);
        }
    }

    public void notifyWithdraw(Long id) {
        ZWithdrawEntity entity = zWithdrawDao.selectById(id);
        if (entity.getProcessStatus().equals(ZooConstant.WITHDRAW_STATUS_SUCCESS) || entity.getProcessStatus().equals(ZooConstant.WITHDRAW_STATUS_FAIL)) {
            SysUserEntity merchant = sysUserDao.selectById(entity.getMerchantId());
            notifyWithdraw(entity, merchant);
        }
    }

    // 只有自营卡, 渠道模式下才能手动成功
    public void rejectWithdraw(Long id) {
        ZWithdrawEntity entity = zWithdrawDao.selectById(id);
        if (entity == null) {
            throw new RenException("no transaction");
        }

        synchronized (ZooConstant.getMerchantLock(entity.getMerchantId())) {
            tx.executeWithoutResult(status -> {
                // todo: 代理跑分模式
                if (ZooConstant.PROCESS_MODE_AGENT.equals(entity.getHandleMode())) {
                    if (entity.getClaimed().equals(1)) {
                    }
                }
                // todo: 码农跑分模式
                else if (ZooConstant.PROCESS_MODE_ANT.equals(entity.getHandleMode())) {
                    if (entity.getClaimed().equals(1)) {
                    }
                }
                zWithdrawDao.update(null, Wrappers.<ZWithdrawEntity>lambdaUpdate()
                        .eq(ZWithdrawEntity::getId, id)
                        .in(ZWithdrawEntity::getProcessStatus, List.of(ZooConstant.WITHDRAW_STATUS_NEW, ZooConstant.WITHDRAW_STATUS_ASSIGNED))
                        .set(ZWithdrawEntity::getProcessStatus, ZooConstant.WITHDRAW_STATUS_FAIL)
                );
                ledger.merchantWithdrawFail(entity);
            });
        }
        CompletableFuture.runAsync(() -> {
            this.notifyWithdraw(id);
        });
    }

    // 只有自营卡, 渠道模式下才能手动成功
    public void successWithdraw(Long id, String utr) {
        ZWithdrawEntity entity = zWithdrawDao.selectById(id);
        if (entity == null) {
            throw new RenException("no transaction");
        }

        synchronized (ZooConstant.getMerchantLock(entity.getMerchantId())) {
            tx.executeWithoutResult(status -> {

                // todo: 代理跑分模式
                if (entity.getHandleMode().equals(ZooConstant.PROCESS_MODE_AGENT)) {
                    if (entity.getClaimed().equals(1)) {
                        agentLedger.userMerchantWithdrawSuccess(entity);
                    }
                }
                // todo: 码农跑分模式
                else if (entity.getHandleMode().equals(ZooConstant.PROCESS_MODE_ANT)) {
                    if (entity.getClaimed().equals(1)) {
                        antLedger.antMerchantWithdrawSuccess(entity);
                    }
                }

                zWithdrawDao.update(null, Wrappers.<ZWithdrawEntity>lambdaUpdate()
                        .eq(ZWithdrawEntity::getId, id)
                        .eq(ZWithdrawEntity::getProcessStatus, ZooConstant.WITHDRAW_STATUS_ASSIGNED)
                        .set(ZWithdrawEntity::getUtr, utr)
                        .set(ZWithdrawEntity::getProcessStatus, ZooConstant.WITHDRAW_STATUS_SUCCESS)
                );
            });
        }
        CompletableFuture.runAsync(() -> {
            this.notifyWithdraw(id);
        });
    }

    // 查询渠道收款
    public void queryChannelCharge(Long id) {
        ZChargeEntity entity = zChargeDao.selectById(id);

        // 已经是终态
        if (entity.getProcessStatus().equals(ZooConstant.CHARGE_STATUS_SUCCESS)) {
            return;
        }

        Long channelId = entity.getChannelId();
        PayChannel payChannel = channelFactory.get(channelId);
        ChannelChargeQueryResponse response = payChannel.chargeQuery(entity);
        if (response.getStatus().equals(ZooConstant.CHARGE_STATUS_SUCCESS)) {
            tx.executeWithoutResult(status -> {
                ledger.merchantChargeSuccess(entity);
                zChargeDao.update(null, Wrappers.<ZChargeEntity>lambdaUpdate()
                        .eq(ZChargeEntity::getId, id)
                        .set(ZChargeEntity::getProcessStatus, ZooConstant.CHARGE_STATUS_SUCCESS)
                        .set(ZChargeEntity::getMerchantPrincipal, entity.getMerchantPrincipal())
                        .set(ZChargeEntity::getMerchantFee, entity.getMerchantFee())
                        .set(response.getUtr() != null, ZChargeEntity::getUtr, response.getUtr())
                        .set(response.getUpi() != null, ZChargeEntity::getUpi, response.getUpi())
                        .set(response.getChannelOrder() != null, ZChargeEntity::getChannelOrder, response.getChannelOrder())

                );
            });
            publisher.publishEvent(new ChargeSuccessEvent(this, id));
        }
    }

    // 查询渠道代付
    public void queryChannelWithdraw(Long id) {
        ZWithdrawEntity entity = zWithdrawDao.selectById(id);
        if (entity == null) {
            return;
        }

        // 已经是终态: 成功|失败
        if (entity.getProcessStatus().equals(ZooConstant.WITHDRAW_STATUS_SUCCESS) || entity.getProcessStatus().equals(ZooConstant.WITHDRAW_STATUS_FAIL)) {
            return;
        }

        Long channelId = entity.getChannelId();
        PayChannel payChannel = channelFactory.get(channelId);
        ChannelWithdrawResponse response = payChannel.withdrawQuery(entity);

        // 代付成功
        if (response.getStatus().equals(ZooConstant.WITHDRAW_STATUS_SUCCESS)) {
            zWithdrawDao.update(null, Wrappers.<ZWithdrawEntity>lambdaUpdate()
                    .eq(ZWithdrawEntity::getId, id)
                    .set(ZWithdrawEntity::getProcessStatus, ZooConstant.WITHDRAW_STATUS_SUCCESS)
                    .set(response.getUtr() != null, ZWithdrawEntity::getUtr, response.getUtr())
                    .set(response.getUpi() != null, ZWithdrawEntity::getUpi, response.getUpi())
                    .set(response.getChannelOrder() != null, ZWithdrawEntity::getChannelOrder, response.getChannelOrder())
            );
            publisher.publishEvent(new WithdrawCompleteEvent(this, id));
            return;
        }

        // 代付失败
        if (response.getStatus().equals(ZooConstant.WITHDRAW_STATUS_FAIL)) {
            zWithdrawDao.update(null, Wrappers.<ZWithdrawEntity>lambdaUpdate()
                    .eq(ZWithdrawEntity::getId, id)
                    .set(ZWithdrawEntity::getUtr, "渠道失败")
            );
            return;
        }

        zWithdrawDao.update(null, Wrappers.<ZWithdrawEntity>lambdaUpdate()
                .eq(ZWithdrawEntity::getId, id)
                .set(ZWithdrawEntity::getUtr, "处理中")
        );
    }

    public void batchWithdraw(List<Long> idList, Integer processStatus, Long cardId, Long channelId) {
        // 批量成功
        if (processStatus.equals(ZooConstant.WITHDRAW_STATUS_SUCCESS)) {
            for (Long aLong : idList) {
                this.successWithdraw(aLong, "no utr");
            }
            return;
        }
        // 批量成功
        if (processStatus.equals(ZooConstant.WITHDRAW_STATUS_FAIL)) {
            for (Long aLong : idList) {
                this.rejectWithdraw(aLong);
            }
            return;
        }

        Map<Long, ZWithdrawEntity> collect = zWithdrawDao.selectList(Wrappers.<ZWithdrawEntity>lambdaQuery()
                .in(ZWithdrawEntity::getId, idList)
        ).stream().collect(Collectors.toMap(ZWithdrawEntity::getId, Function.identity()));

        // 批量分配到卡
        if (processStatus.equals(100)) {

            ZCardEntity zCardEntity = zCardDao.selectById(cardId);
            for (Long aLong : idList) {
                ZWithdrawEntity entity = collect.get(aLong);
                entity.setCardId(cardId);
                this.assignToCard(collect.get(aLong), zCardEntity);
            }
            return;
        }

        // 批量分配到渠道
        if (processStatus.equals(200)) {
            PayChannel payChannel = channelFactory.get(channelId);
            for (Long aLong : idList) {
                ZWithdrawEntity entity = collect.get(aLong);
                SysUserEntity merchant = sysUserDao.selectById(entity.getMerchantId());
                this.assignToChannel(entity, payChannel, merchant);
            }
        }
    }

    public int chargeStatus(Long id) {
        ZChargeEntity zChargeEntity = zChargeDao.selectById(id);
        if (zChargeEntity.getProcessStatus().equals(ZooConstant.CHARGE_STATUS_SUCCESS)) {
            return 1;
        }
        return 0;
    }
}
