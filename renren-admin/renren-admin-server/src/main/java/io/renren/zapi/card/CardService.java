package io.renren.zapi.card;

import cn.hutool.core.codec.Base64;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.renren.commons.tools.redis.RedisUtils;
import io.renren.dao.SysDeptDao;
import io.renren.entity.SysDeptEntity;
import io.renren.entity.SysUserEntity;
import io.renren.zadmin.dao.ZCardDao;
import io.renren.zadmin.dao.ZChargeDao;
import io.renren.zadmin.dao.ZWithdrawDao;
import io.renren.zadmin.entity.ZCardEntity;
import io.renren.zadmin.entity.ZChargeEntity;
import io.renren.zadmin.entity.ZRouteEntity;
import io.renren.zadmin.entity.ZWithdrawEntity;
import io.renren.zapi.ZConfig;
import io.renren.zapi.ZooConstant;
import io.renren.zapi.card.fill.CardLandingFill;
import io.renren.zapi.channel.dto.ChannelChargeResponse;
import io.renren.zapi.channel.dto.ChannelWithdrawResponse;
import io.renren.zapi.merchant.ApiContext;
import io.renren.zapi.utils.CommonUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class CardService {

    @Resource
    Map<String, CardLandingFill> landingFillMap;
    @Resource
    private TaskScheduler taskScheduler;
    @Resource
    private ZConfig config;
    @Resource
    private SysDeptDao sysDeptDao;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private ZCardDao zCardDao;
    @Resource
    private ZWithdrawDao zWithdrawDao;
    @Resource
    private ZChargeDao zChargeDao;
    @Resource
    private CardStat cardStat;
    @Resource
    private CardAppService cardAppService;

    public ChannelChargeResponse charge(ZChargeEntity chargeEntity, List<ZRouteEntity> selected) {

        // 选择金额 todo
        if (chargeEntity.getPayCode().equals("upimod")) {
            BigDecimal amount = chargeEntity.getAmount();
            String amtStr = amount.toString();
        }

        ZRouteEntity route = selected.get(0);
        ZCardEntity zCardEntity = zCardDao.selectById(route.getObjectId());
        zChargeDao.update(null, Wrappers.<ZChargeEntity>lambdaUpdate()
                .eq(ZChargeEntity::getId, chargeEntity.getId())
                .set(ZChargeEntity::getProcessStatus, ZooConstant.CHARGE_STATUS_PROCESSING)
                .set(ZChargeEntity::getHandleMode, ZooConstant.PROCESS_MODE_CARD)
                .set(ZChargeEntity::getCardId, zCardEntity.getId())
                .set(ZChargeEntity::getCardUser, zCardEntity.getAccountUser())
                .set(ZChargeEntity::getCardNo, zCardEntity.getAccountNo())
                .set(ZChargeEntity::getUpi, zCardEntity.getAccountUpi())
                .set(ZChargeEntity::getMerchantRate, route.getChargeRate())  // 路由上的扣率
        );

        return serveCharge(chargeEntity, zCardEntity);
    }

    @NotNull
    private ChannelChargeResponse serveCharge(ZChargeEntity chargeEntity, ZCardEntity zCardEntity) {
        ApiContext context = ApiContext.getContext();
        SysDeptEntity dept = context.getDept();
        SysUserEntity merchant = context.getMerchant();
        Map<String, Object> map = new HashMap<>();

        // 通用字段
        map.put("id", chargeEntity.getId().toString());              // 平台订单号
        map.put("deadline", new Date().getTime() + 60 * 1000 * 30);  // 20分钟超时
        map.put("domain", dept.getApiDomain());                      // 接口域名
        map.put("amount", chargeEntity.getAmount());                 // 金额
        map.put("callbackUrl", chargeEntity.getCallbackUrl());       // 成功页面

        // 不同的货币类型， 需要有不同的落地页数据
        CardLandingFill landingFill = landingFillMap.get(dept.getCurrency() + "CardLandingFill");
        landingFill.fill(map, zCardEntity, chargeEntity, dept, merchant);

        // 生成支付地址
        String json = null;
        try {
            json = objectMapper.writeValueAsString(map);
            log.debug("id ----> {}|{}", chargeEntity.getId(), json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String encode = Base64.encode(json);
        String payUrl = config.getCdnUrl() + "/#/" + dept.getCurrency() + "?&base64=" + encode;

        // 依据不同的要素生成payUrl
        ChannelChargeResponse response = new ChannelChargeResponse();
        response.setUpi(zCardEntity.getAccountUpi());
        response.setPayUrl(payUrl);

        return response;
    }


    public ChannelWithdrawResponse withdraw(ZWithdrawEntity withdrawEntity, Long select) {
        ZCardEntity card = zCardDao.selectById(select);
        log.debug("自营卡withdraw: {}", card);
        zWithdrawDao.update(null, Wrappers.<ZWithdrawEntity>lambdaUpdate()
                // 条件
                .eq(ZWithdrawEntity::getId, withdrawEntity.getId())
                .eq(ZWithdrawEntity::getProcessStatus, ZooConstant.WITHDRAW_STATUS_NEW)
                // 设置
                .set(ZWithdrawEntity::getHandleMode, ZooConstant.PROCESS_MODE_CARD)
                .set(ZWithdrawEntity::getProcessStatus, ZooConstant.WITHDRAW_STATUS_ASSIGNED)
                .set(ZWithdrawEntity::getCardId, card.getId())
                .set(ZWithdrawEntity::getCardNo, card.getAccountNo())
                .set(ZWithdrawEntity::getCardUser, card.getAccountUser())
                .set(ZWithdrawEntity::getUpi, card.getAccountUpi())
        );
        ChannelWithdrawResponse response = new ChannelWithdrawResponse();
        response.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
        response.setUpi(card.getAccountUpi());
        return response;
    }

}
